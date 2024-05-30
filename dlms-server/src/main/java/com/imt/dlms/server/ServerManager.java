package com.imt.dlms.server;

import com.imt.dlms.server.core.media.LoraE5SerialMedia;
import com.imt.dlms.server.core.ManagementLogicalDevice;
import com.imt.dlms.server.core.ServingLogicalDevice;
import com.imt.dlms.server.exception.LoraConfigurationException;
import com.imt.dlms.server.impl.MandjetSupportingLD;
import com.imt.dlms.server.impl.MandjetManagementLD;
import com.imt.dlms.server.service.*;
import com.imt.dlms.server.service.MediaFactory.MediaConfig;
import com.imt.dlms.server.service.notification.DLMSNotifyService;
import com.imt.dlms.server.service.notification.DefaultScheduler;
import gurux.common.*;
import gurux.dlms.GXServerReply;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;
import gurux.io.BaudRate;
import gurux.net.ConnectionEventArgs;
import gurux.net.IGXNetListener;
import gurux.net.enums.NetworkType;

import static com.imt.dlms.server.config.DlmsConfig.*;

public class ServerManager implements IGXMediaListener, IGXNetListener {

    public static final int SCHEDULER_THREADS = 2;

    private final ManagementLogicalDevice managementLD;

    private final ServingLogicalDevice supportingLD;

    private final DLMSNotifyService notify;

    private final MediaFactory mediaFactory;

    private IGXMedia media;

    public ServerManager() {
        final GXDLMSTcpUdpSetup wrapper = new GXDLMSTcpUdpSetup();
        final MandjetService mandjetService = new MandjetService();

        // Notify service
        this.notify = new DLMSNotifyService(
                new DefaultScheduler(SCHEDULER_THREADS),
                null
        );

        this.managementLD = new MandjetManagementLD(MANAGEMENT_LDN,
                new GXDLMSAssociationLogicalName(MANAGEMENT_AA_LN),
                wrapper,
                mandjetService,
                this.notify);

        this.managementLD.getNotify().setMaxReceivePDUSize(MAX_PDU_SIZE);

        final String password = System.getenv("DLMS_SUPPORTING_LD_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("Dlms password is not specified. Please, set DLMS_SUPPORTING_LD_PASSWORD environmental variable.");
        }

        this.supportingLD = new MandjetSupportingLD(this.managementLD,
                SUPPORTING_LDN,
                new GXDLMSAssociationLogicalName(SUPPORTING_AA_LN),
                password,
                wrapper,
                SUPPORTING_LD_SAP,
                mandjetService,
                this.notify);

        this.supportingLD.getNotify().setMaxReceivePDUSize(MAX_PDU_SIZE);

        this.managementLD.assignLogicalDevice(this.supportingLD);

        this.mediaFactory = new MediaFactory();
    }

    public void init() throws Exception {
        this.setupMedia();
        this.managementLD.init();
        this.supportingLD.init();
    }

    private void setupMedia() throws Exception {
        final MediaConfig mediaConfig = new MediaConfig();
        final MediaType mode = MediaType.valueOf(System.getenv("COMMUNICATION_MEDIA"));
        mediaConfig
                .withMode(mode)
                .withListener(this);

        if (mode == MediaType.TCP_IP) {
            mediaConfig
                    .withPort(this.managementLD.getSettings().getWrapper().getPort())
                    .withNetworkType(NetworkType.UDP);
        } else if (mode == MediaType.E5_LORA) {
            mediaConfig
                    .withPortName(System.getenv("LORA_SERIAL_PORT"))
                    .withBaudRate(BaudRate.BAUD_RATE_9600)
                    .withDataBits(8)
                    .withDataRate("DR0")
                    .withChannel("0-2")
                    .withAppKey(System.getenv("LORA_API_KEY"))
                    .withLoraClass("A")
                    .withPort(8);
        }

        mediaFactory.setConfig(mediaConfig);

        this.media = mediaFactory.getMedia();
        media.open();
        if (mode == MediaType.E5_LORA && media instanceof LoraE5SerialMedia l) {
            if (!l.joinNetwork()) {
                this.close();
                throw new LoraConfigurationException("Failed to join Lora Network. Check your configuration or tyr again later.");
            }
        }
        notify.setMedia(media);
    }

    @Override
    public void onError(Object o, Exception e) {
        System.out.println(o + " " + e.getMessage());
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
