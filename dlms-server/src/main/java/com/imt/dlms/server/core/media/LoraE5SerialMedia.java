package com.imt.dlms.server.core.media;

import com.imt.dlms.server.exception.LoraConfigurationException;
import com.imt.dlms.server.exception.SerialCommunicationException;
import gurux.common.GXCommon;
import gurux.common.ReceiveParameters;
import gurux.dlms.GXSimpleEntry;
import gurux.serial.GXSerial;

import java.util.ArrayList;
import java.util.List;

public class LoraE5SerialMedia extends SerialCopy {

    private static final int JOIN_ATTEMPTS = 5;

    private static final List<GXSimpleEntry<String, List<String>>> COMMAND_PATTERNS = new ArrayList<>(8);

    static {
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT\n", List.of("+AT: OK")));
        // 300 before
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+UART=TIMEOUT,0\n", List.of("+UART: ")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+LOG=QUIET\n", List.of("+LOG: QUIET")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+MODE=%s\n", List.of("+MODE: ")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+DR=%s\n", List.of("+DR: ", "")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+CH=NUM,%s\n", List.of("+CH: NUM")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+KEY=APPKEY,\"%s\"\n", List.of("+KEY: APPKEY")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+CLASS=%s\n", List.of("+CLASS: ")));
        COMMAND_PATTERNS.add(new GXSimpleEntry<>("AT+PORT=%s\n", List.of("+PORT: ")));
    }

    private static final GXSimpleEntry<String, List<String>> JOIN_COMMAND =
            new GXSimpleEntry<>("AT+JOIN=FORCE\n", List.of("+JOIN: Start", "+JOIN: FORCE", "+JOIN: Network joined", "+JOIN: Done"));

    private static final GXSimpleEntry<String, List<String>> CHECK_JOIN_COMMAND =
            new GXSimpleEntry<>("AT+JOIN\n", List.of("+JOIN: Joined already"));

    private static final GXSimpleEntry<String, List<String>> SEND_COMMAND =
            new GXSimpleEntry<>("AT+MSGHEX=\"%s\"\n", List.of("+MSGHEX: Start", "+MSGHEX: Done"));

    private String mode;

    private String dataRate;

    private String channel;

    private String appKey;

    private String loraClass;

    private int port;

    private boolean joined;

    /*
        send_AT_command_and_wait("AT\n\r", "OK", 1000)
    # send_AT_command_and_wait("AT+ID\r\n", "+ID: AppEui", 1000)
    # send_AT_command_and_wait("AT+MODE=LWOTAA\r\n", "+MODE: LWOTAA", 1000)
    # send_AT_command_and_wait("AT+DR=EU868\r\n", "+DR: EU868", 1000)
    # send_AT_command_and_wait("AT+CH=NUM,0-2\r\n", "+CH: NUM", 1000)
    # send_AT_command_and_wait("AT+KEY=APPKEY,\"2B7E151628AED2A6ABF7158", "+KEY: APPKEY", 1000)
    # send_AT_command_and_wait("AT+CLASS=A\r\n", "+CLASS: C", 1000)
    # send_AT_command_and_wait("AT+PORT=8\r\n", "+PORT: 8", 1000)
    # send_AT_command_and_wait("AT+JOIN\r\n", "Done", 10000)
     */

    /*
    Pattern --> Expected response [Regex, Regex]
     */

    private void receiveResponse(ReceiveParameters<byte[]> b) {
        while (this.receive(b));
    }

    public boolean joinNetwork() {
        this.joined = false;

        try {
            synchronized (this.getSynchronous()) {
                final ReceiveParameters<byte[]> p = new ReceiveParameters<>(byte[].class);

                //End of Packet.
                p.setEop('\n');
                //How long reply is waited.
                p.setWaitTime(1000);

                int i = 0;
                final List<String> params = List.of("", "", "", mode, dataRate, channel, appKey, loraClass, String.valueOf(port));
                for (GXSimpleEntry<String, List<String>> commandPattern : COMMAND_PATTERNS) {
                    this.sendCommandAndMatchResponse(p, commandPattern, params.get(i));
                    i++;
                }

                boolean joinSuccess = false;
                p.setWaitTime(8000);
                System.out.println("Trying to join Lora Network...");
                for (int j = 0; j < JOIN_ATTEMPTS; j++) {
                    try {
                        this.sendCommandAndMatchResponse(p, JOIN_COMMAND, "");
                        joinSuccess = true;
                        System.out.println("Network joined!");
                        break;
                    } catch (RuntimeException e) {
                        System.err.println("Joint attempt " + j + " failed. " + e.getMessage());
                    }
                    Thread.sleep(5000);
                }

                this.joined = joinSuccess;
                return joined;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    private void sendCommandAndMatchResponse(ReceiveParameters<byte[]> b, GXSimpleEntry<String, List<String>> command, String param) throws Exception {
        final String data = param.isEmpty() ? command.getKey()
                : String.format(command.getKey(), param);
        this.send(data, null);

        this.matchResponse(b, command.getValue());
    }

    private void matchResponse(ReceiveParameters<byte[]> b, List<String> responses) {
        this.receiveResponse(b);
        final String response = new String(b.getReply());
        b.setReply(null);
        for (int i = 0; i < responses.size(); i++) {
            final String responsePattern = responses.get(i);

            if (!response.contains(responsePattern)) {
                throw new LoraConfigurationException("Received: " + response);
            }
        }
    }

    public boolean checkJoin() {
        this.joined = false;

        try {
            synchronized (this.getSynchronous()) {
                final ReceiveParameters<byte[]> p = new ReceiveParameters<>(byte[].class);

                //End of Packet.
                p.setEop('\n');
                p.setWaitTime(1000);

                try {
                    this.sendCommandAndMatchResponse(p, CHECK_JOIN_COMMAND, "");
                    this.joined = true;
                } catch (RuntimeException e) {
                    System.out.println("Lora E5 got disconnected. Trying to join again...");
                    this.joined = false;
                }

                if (!joined) {
                    try {
                        final List<String> rejoinCommands = List.of("+JOIN: Start", "+JOIN: NORMAL", "+JOIN: Network joined", "+JOIN: Done");
                        final String reply = new String(p.getReply());
                        for (String command : rejoinCommands) {
                            if (!reply.contains(command)) {
                                throw new RuntimeException();
                            }
                        }
                        this.joined = true;
                    } catch (RuntimeException e) {
                        System.err.println("Failed to rejoin immediately.");
                    }
                }

                return joined;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void send(Object data, String target) throws Exception {
        if (joined) {
            if (data instanceof String s) {
                data = String.format(SEND_COMMAND.getKey(), GXCommon.bytesToHex(s.getBytes()));
            } else if (data instanceof byte[] b) {
                data = String.format(SEND_COMMAND.getKey(), GXCommon.bytesToHex(b));
            } else {
                throw new SerialCommunicationException("Unsupported data type.");
            }
            System.out.println(data);
        }

        if (this.getIsSynchronous()) {
            this.sendAndCheck(data, target);
        } else {
            synchronized (this.getSynchronous()) {
                this.sendAndCheck(data, target);
            }
        }
    }

    private void sendAndCheck(Object data, String target) throws Exception {
        super.send(data, target);

        if (joined) {
            final ReceiveParameters<byte[]> p = new ReceiveParameters<>(byte[].class);
            p.setEop('\n');
            p.setWaitTime(5000);
            this.matchResponse(p, SEND_COMMAND.getValue());
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDataRate() {
        return dataRate;
    }

    public void setDataRate(String dataRate) {
        this.dataRate = dataRate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getLoraClass() {
        return loraClass;
    }

    public void setLoraClass(String loraClass) {
        this.loraClass = loraClass;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
