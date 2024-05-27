package com.imt.dlms.client;

import java.util.List;
import java.util.Map.Entry;

import gurux.common.GXCommon;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.dlms.GXByteBuffer;
import gurux.dlms.GXDLMSTranslator;
import gurux.dlms.GXReplyData;
import gurux.dlms.GXSimpleEntry;
import gurux.dlms.TranslatorOutputType;
import gurux.dlms.enums.Authentication;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.objects.*;
import gurux.dlms.secure.GXDLMSSecureClient;
import gurux.net.GXNet;
import gurux.net.IGXNetListener;
import gurux.net.enums.NetworkType;

public class PushListener implements IGXMediaListener, IGXNetListener, AutoCloseable {

    /**
     * Are messages traced.
     */
    private boolean trace = true;
    /**
     * TCP/IP port to listen.
     */
    private GXNet media;
    /**
     * Received data is saved to reply buffer because whole message is not
     * always received in one packet.
     */
    private GXByteBuffer reply = new GXByteBuffer();
    /**
     * Received data. This is used if GBT is used and data is received on
     * several data blocks.
     */
    private GXReplyData notify = new GXReplyData();

    /**
     * Client used to parse received data.
     */
    private GXDLMSSecureClient client;

    GXDLMSPushSetup push = new GXDLMSPushSetup();

    /**
     * Constructor.
     *
     * @param port
     *            Listener port.
     */
    public PushListener(int port, InterfaceType interfaceType)
            throws Exception {
        client = new GXDLMSSecureClient(true, 16, 1, Authentication.NONE, null,
                interfaceType);
//        media = new gurux.net.GXNet(
//                interfaceType == InterfaceType.COAP ? NetworkType.UDP
//                        : NetworkType.TCP,
//                port);
        media = new gurux.net.GXNet(
                NetworkType.UDP,
                port);
//        media.setServer(false);
        media.setTrace(TraceLevel.VERBOSE);
        media.addListener(this);
        media.open();
        // TODO; Must set communication specific settings.
        push.getPushObjectList()
                .add(new GXSimpleEntry<GXDLMSObject, GXDLMSCaptureObject>(
                        new GXDLMSClock(), new GXDLMSCaptureObject(2, 0)));
        push.getPushObjectList()
                .add(new GXSimpleEntry<GXDLMSObject, GXDLMSCaptureObject>(
                        new GXDLMSData(), new GXDLMSCaptureObject(2, 0)));
    }

    /**
     * Listener is closed.
     */
    public void close() {
        media.close();
    }

    @Override
    public void onError(Object sender, Exception ex) {
        System.out.println("Error has occurred:" + ex.getMessage());
    }

    private static void printData(final Object value) {
        if (value instanceof Object[]) {
            System.out.println("+++++++++++++++++++++++++++++++++++++++++");
            // Print received data.
            for (Object it : (Object[]) value) {
                printData(it);
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        } else if (value instanceof byte[]) {
            // Print value.
            System.out.println(GXCommon.bytesToHex((byte[]) value));
        } else if (value instanceof List<?>) {
            System.out.println("+++++++++++++++++++++++++++++++++++++++++");
            // Print received data.
            for (Object it : (List<?>) value) {
                printData(it);
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        } else {
            // Print value.
            System.out.println(String.valueOf(value));
        }
    }

    public static void printValue(Entry<GXDLMSObject, Integer> it) {
        // Print value
        System.out.println(it.getKey().getObjectType() + " "
                + it.getKey().getLogicalName() + " " + it.getValue() + ":"
                + it.getKey().getValues()[it.getValue() - 1]);
    }

    /*
     * Client has send data.
     */
    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        try {
            synchronized (this) {
                if (trace) {
                    System.out.println("<- " + gurux.common.GXCommon
                            .bytesToHex((byte[]) e.getData()));
                }
                reply.set((byte[]) e.getData());
                // Example handles only notify messages.
                client.getData(reply, notify);
                // If all data is received.
                if (notify.isComplete()) {
                    // Don't call clear here.
                    // There might be bytes from the next frame waiting.
                    reply.trim();
                    if (notify.isMoreData()) {
                        if (client.getInterfaceType() == InterfaceType.COAP) {
                            // Send ACK for the meter.
                            byte[] data = client.receiverReady(notify);
                            media.send(data, e.getSenderInfo());
                        }
                    } else {
                        // Comment this if the meter describes the content of
                        // the push message for the
                        // client in the received data.
                        // Make clone so we don't replace current values.
                        GXDLMSPushSetup clone = (GXDLMSPushSetup) push.clone();
                        clone.getPushValues(client,
                                (List<?>) notify.getValue());
                        for (Entry<GXDLMSObject, GXDLMSCaptureObject> it : clone
                                .getPushObjectList()) {
                            int index = it.getValue().getAttributeIndex() - 1;
                            System.out.println(((IGXDLMSBase) it.getKey())
                                    .getNames()[index] + ": "
                                    + it.getKey().getValues()[index]);
                        }
                        try {
                            // Show data as XML.
                            GXDLMSTranslator t = new GXDLMSTranslator(
                                    TranslatorOutputType.SIMPLE_XML);
                            String xml = t.dataToXml(notify.getData());
                            System.out.println(xml);
                            printData(notify.getValue());
                            // Un-comment this if the meter describes the
                            // content of the push message
                            // for the client in the received data.
                            // if (data.getValue() instanceof List<?>) {
                            // List<Object> tmp = (List<Object>)
                            // notify.getValue();
                            // List<Entry<GXDLMSObject, Integer>> objects =
                            // client
                            // .parsePushObjects((List<?>) tmp.get(0));
                            // // Remove first item because it's not needed
                            // // anymore.
                            // objects.remove(0);
                            // // Update clock.
                            // int valueindex = 1;
                            // for (Entry<GXDLMSObject, Integer> it : objects) {
                            // client.updateValue(it.getKey(), it.getValue(),
                            // tmp.get(valueindex));
                            // ++valueindex;
                            // printValue(it);
                            // }
                            // }
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        } finally {
                            notify.clear();
                        }
                    }
                }
            }
        } catch (

                Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {

    }

    /*
     * Client has made connection.
     */
    @Override
    public void onClientConnected(Object sender,
                                  gurux.net.ConnectionEventArgs e) {
        System.out.println("Client Connected.");
    }

    /*
     * Client has close connection.
     */
    @Override
    public void onClientDisconnected(Object sender,
                                     gurux.net.ConnectionEventArgs e) {
        // Reset server settings when connection closed.
        System.out.println("Client Disconnected.");
    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {
        // System.out.println(e.toString());
    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }
}
