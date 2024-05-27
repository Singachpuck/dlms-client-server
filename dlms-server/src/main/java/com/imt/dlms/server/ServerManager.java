package com.imt.dlms.server;

import com.imt.dlms.server.core.ManagementLogicalDevice;
import com.imt.dlms.server.core.ServingLogicalDevice;
import com.imt.dlms.server.impl.MandjetManagementLD;
import com.imt.dlms.server.impl.MandjetSupportingLD;
import com.imt.dlms.server.service.*;
import gurux.common.*;
import gurux.common.enums.TraceLevel;
import gurux.dlms.GXDLMSNotify;
import gurux.dlms.GXServerReply;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSPushSetup;
import gurux.dlms.objects.GXDLMSScriptTable;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;
import gurux.net.ConnectionEventArgs;
import gurux.net.GXNet;
import gurux.net.IGXNetListener;
import gurux.net.enums.NetworkType;

import java.util.List;

public class ServerManager implements IGXMediaListener, IGXNetListener {

    public static final String MANAGEMENT_AA_LN = "0.0.40.0.1.255";

    // TODO: Support multiple Client SAP's
    public static final List<Integer> CLIENT_SAP_LIST = List.of(
            // Public client SAP
            0x10
    );

    // Fake
    public static final byte[] MANUFACTURER_ID = new byte[] {
            // Consists of 3 bytes
            (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
    };

    public static final long SERIAL_NUMBER = 123456;

    public static final String SUPPORTING_AA_LN = "0.0.40.0.2.255";

    public static final short SUPPORTING_LD_SAP = 3013;

    public static final int SCHEDULER_THREADS = 2;

    private final ManagementLogicalDevice managementLD;

    private final ServingLogicalDevice supportingLD;

    private final DLMSNotifyService notify;

    private IGXMedia media;

    public ServerManager() {
        final GXDLMSTcpUdpSetup wrapper = new GXDLMSTcpUdpSetup();
        final MandjetService mandjetService = new MandjetService();

        // Notify service
        this.notify = new DLMSNotifyService(
                new DefaultScheduler(SCHEDULER_THREADS),
                null
        );

        this.managementLD = new MandjetManagementLD(DLMSUtil.generateLDN(MANUFACTURER_ID),
                new GXDLMSAssociationLogicalName(MANAGEMENT_AA_LN),
                wrapper,
                mandjetService,
                this.notify);

        final String password = System.getenv("DLMS_SUPPORTING_LD_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("Dlms password is not specified. Please, set DLMS_SUPPORTING_LD_PASSWORD environmental variable.");
        }

        this.supportingLD = new MandjetSupportingLD(this.managementLD,
                DLMSUtil.generateLDN(MANUFACTURER_ID),
                new GXDLMSAssociationLogicalName(SUPPORTING_AA_LN),
                password,
                wrapper,
                SUPPORTING_LD_SAP,
                mandjetService,
                this.notify);

        this.managementLD.assignLogicalDevice(this.supportingLD);
    }

    public void init() throws Exception {
        this.managementLD.init();
        this.supportingLD.init();
        this.setupMedia();
    }

    private void setupMedia() throws Exception {
        final int port = this.managementLD.getSettings().getWrapper().getPort();
        final GXNet net = new GXNet(NetworkType.UDP, port);
//        net.setServer(false);
        net.setTrace(TraceLevel.VERBOSE);
        net.addListener(this);
        net.open();
        media = net;
        this.notify.setMedia(media);
    }

    @Override
    public void onError(Object o, Exception e) {
        System.out.println("On Error");
    }

    @Override
    public void onReceived(Object o, ReceiveEventArgs receiveEventArgs) {
        try
        {
            synchronized (this)
            {
                final GXServerReply sr = new GXServerReply((byte[]) receiveEventArgs.getData());
                do {
                    this.managementLD.handleRequest(sr);

                    if (sr.getReply() == null) {
                        this.supportingLD.handleRequest(sr);
                    }

                    // Reply is null if we do not want to send any data to the client.
                    // This is done if client try to make connection with wrong server or client address.
                    if (sr.getReply() != null) {
                        media.send(sr.getReply(), receiveEventArgs.getSenderInfo());
                        sr.setData(null);
                    }
                } while (sr.isStreaming());
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void onMediaStateChange(Object o, MediaStateEventArgs mediaStateEventArgs) {
        System.out.println(mediaStateEventArgs.getState() + " " + mediaStateEventArgs.getAccept());

    }

    @Override
    public void onTrace(Object o, TraceEventArgs traceEventArgs) {
//        System.out.println("onTrace");
    }

    @Override
    public void onPropertyChanged(Object o, PropertyChangedEventArgs propertyChangedEventArgs) {
        System.out.println("onPropertyChanged");
    }

    @Override
    public void onClientConnected(Object o, ConnectionEventArgs connectionEventArgs) {
        System.out.println("Client connected.");
    }

    @Override
    public void onClientDisconnected(Object o, ConnectionEventArgs connectionEventArgs) {
        System.out.println("Client disconnected.");
        this.managementLD.reset();
        this.supportingLD.reset();
    }

    public void close() throws Exception {
        this.managementLD.close();
        this.supportingLD.close();
        this.media.close();
    }
}
