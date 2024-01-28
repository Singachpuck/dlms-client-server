package com.imt.dlms.client.service;

import gurux.common.IGXMedia;
import gurux.common.ReceiveParameters;
import gurux.dlms.*;
import gurux.dlms.enums.ErrorCode;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.secure.GXDLMSSecureClient;

public class DlmsReader {

    private final GXDLMSSecureClient dlms;

    private final IGXMedia media;

    private final int waitTime = 60000;

    public DlmsReader(GXDLMSSecureClient dlms, IGXMedia media) {
        this.dlms = dlms;
        this.media = media;
    }

    public void readDLMSPacket(byte[] data, GXReplyData reply)
            throws Exception {
        if (!reply.getStreaming() && (data == null || data.length == 0)) {
            return;
        }
        GXReplyData notify = new GXReplyData();
        reply.setError((short) 0);
        Object eop = (byte) 0x7E;
        // In network connection terminator is not used.
        if (dlms.getInterfaceType() != InterfaceType.HDLC
                && dlms.getInterfaceType() != InterfaceType.HDLC_WITH_MODE_E) {
            eop = null;
        }
        GXByteBuffer rd = new GXByteBuffer();
        int pos = 0;
        boolean succeeded = false;
        ReceiveParameters<byte[]> p = new ReceiveParameters<>(byte[].class);
        p.setEop(eop);
        p.setAllData(true);
        p.setCount(dlms.getFrameSize(rd));
        p.setWaitTime(waitTime);
        synchronized (media.getSynchronous()) {
            while (!succeeded) {
                if (!reply.isStreaming()) {
//                    writeTrace(
//                            "TX: " + now() + "\t" + GXCommon.bytesToHex(data),
//                            TraceLevel.VERBOSE);
                    media.send(data, null);
                }
                succeeded = media.receive(p);
                if (!succeeded) {
                    if (p.getEop() == null) {
                        p.setCount(dlms.getFrameSize(rd));
                    }
                    // Try to read again...
                    if (pos++ == 3) {
                        throw new RuntimeException(
                                "Failed to receive reply from the device in given time.");
                    }
                    System.out.println("Data send failed. Try to resend "
                            + String.valueOf(pos) + "/3");
                }
            }
            rd = new GXByteBuffer(p.getReply());
            int msgPos = 0;
            // Loop until whole DLMS packet is received.
            try {
                while (!dlms.getData(rd, reply, notify)) {
                    p.setReply(null);
                    if (notify.getData().getData() != null) {
                        // Handle notify.
                        if (!notify.isMoreData()) {
                            // Show received push message as XML.
                            GXDLMSTranslator t = new GXDLMSTranslator(
                                    TranslatorOutputType.SIMPLE_XML);
                            String xml = t.dataToXml(notify.getData());
                            System.out.println(xml);
                            notify.clear();
                            msgPos = rd.position();
                        }
                        continue;
                    }

                    if (p.getEop() == null) {
                        p.setCount(dlms.getFrameSize(rd));
                    }
                    while (!media.receive(p)) {
                        // If echo.
                        if (reply.isEcho()) {
                            media.send(data, null);
                        }
                        // Try to read again...
                        if (++pos == 3) {
                            throw new Exception(
                                    "Failed to receive reply from the device in given time.");
                        }
                        System.out.println("Data send failed. Try to resend "
                                + String.valueOf(pos) + "/3");
                    }
                    rd.position(msgPos);
                    rd.set(p.getReply());
                }
            } catch (Exception e) {
//                writeTrace("RX: " + now() + "\t" + rd.toString(),
//                        TraceLevel.ERROR);
                throw e;
            }
        }
//        writeTrace("RX: " + now() + "\t" + rd.toString(), TraceLevel.VERBOSE);
        if (reply.getError() != 0) {
            if (reply.getError() == ErrorCode.REJECTED.getValue()) {
                Thread.sleep(1000);
                readDLMSPacket(data, reply);
            } else {
                throw new GXDLMSException(reply.getError());
            }
        }
    }

    public void readDataBlock(byte[] data, GXReplyData reply) throws Exception {
        if (data.length != 0) {
            readDLMSPacket(data, reply);
            while (reply.isMoreData()) {
                data = dlms.receiverReady(reply.getMoreData());
                readDLMSPacket(data, reply);
            }
        }
    }

    public boolean readDataBlock(byte[][] data, GXReplyData reply) throws Exception {
        if (data != null) {
            for (byte[] it : data) {
                reply.clear();
                readDataBlock(it, reply);
            }
        }
        return reply.getError() == 0;
    }
}
