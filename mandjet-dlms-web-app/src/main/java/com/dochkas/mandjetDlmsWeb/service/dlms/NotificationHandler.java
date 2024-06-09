package com.dochkas.mandjetDlmsWeb.service.dlms;

import com.dochkas.mandjetDlmsWeb.config.DlmsConfig;
import com.dochkas.mandjetDlmsWeb.model.dto.ManagementNotificationDto;
import com.dochkas.mandjetDlmsWeb.model.dto.SupportingNotificationDto;
import gurux.common.*;
import gurux.dlms.*;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.objects.GXDLMSPushSetup;
import gurux.net.ConnectionEventArgs;
import gurux.net.IGXNetListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationHandler implements InitializingBean, IGXMediaListener, IGXNetListener, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    @Autowired
    private IGXMedia media;

    @Autowired
    @Qualifier("dlmsManagementClient")
    private GXDLMSClient client;

    @Autowired
    @Qualifier("managementPush")
    private GXDLMSPushSetup managementPush;

    @Autowired
    @Qualifier("supportingPush")
    private GXDLMSPushSetup supportingPush;

    @Autowired
    private SimpMessagingTemplate template;

    /**
     * Received data is saved to reply buffer because whole message is not
     * always received in one packet.
     */
    private final GXByteBuffer reply = new GXByteBuffer();

    /**
     * Received data. This is used if GBT is used and data is received on
     * several data blocks.
     */
    private final GXReplyData notify = new GXReplyData();

    @Override
    public void afterPropertiesSet() throws Exception {
        media.addListener(this);

        if (!media.isOpen()) {
            media.open();
        }
    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        try {
            synchronized (this) {
                // Logging the data
                logger.trace("<- Notification: " + gurux.common.GXCommon.bytesToHex((byte[]) e.getData()));

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
                        // Make clone so we don't replace current values.
                        final GXDLMSPushSetup clone;
                        if (notify.getServerAddress() == 0 || notify.getServerAddress() == 1) {
                            clone = (GXDLMSPushSetup) managementPush.clone();
                            clone.getPushValues(client,
                                    (List<?>) notify.getValue());
                            template.convertAndSend("/mandjet/management", this.buildManagementNotification(clone));
                        } else if (notify.getServerAddress() == 3013) {
                            clone = (GXDLMSPushSetup) supportingPush.clone();
                            clone.getPushValues(client,
                                    (List<?>) notify.getValue());
                            template.convertAndSend("/mandjet/supporting", this.buildSupportingNotification(clone));
                        } else {
                            return;
                        }

                        try {
                            // Show data as XML.
                            GXDLMSTranslator t = new GXDLMSTranslator(TranslatorOutputType.SIMPLE_XML);
                            String xml = t.dataToXml(notify.getData());
                            logger.info(xml);
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        } finally {
                            notify.setServerAddress(0);
                            notify.clear();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private ManagementNotificationDto buildManagementNotification(GXDLMSPushSetup push) {
        final ManagementNotificationDto notificationDto = new ManagementNotificationDto();

        int index = push.getPushObjectList().get(0).getValue().getAttributeIndex() - 1;
        notificationDto.setLdn((byte[]) push.getPushObjectList().get(0).getKey().getValues()[index]);

        index = push.getPushObjectList().get(1).getValue().getAttributeIndex() - 1;
        notificationDto.setTimestamp(((GXDateTime) push.getPushObjectList().get(1).getKey().getValues()[index]).getLocalCalendar().getTime());

        index = push.getPushObjectList().get(2).getValue().getAttributeIndex() - 1;
        notificationDto.setBattery((short) push.getPushObjectList().get(2).getKey().getValues()[index]);

        return notificationDto;
    }

    private SupportingNotificationDto buildSupportingNotification(GXDLMSPushSetup push) {
        final SupportingNotificationDto notificationDto = new SupportingNotificationDto();

        int index = push.getPushObjectList().get(0).getValue().getAttributeIndex() - 1;
        notificationDto.setLdn((byte[]) push.getPushObjectList().get(0).getKey().getValues()[index]);

        index = push.getPushObjectList().get(1).getValue().getAttributeIndex() - 1;
        notificationDto.setVoltage((float) push.getPushObjectList().get(1).getKey().getValues()[index]);

        notificationDto.setSensorValues(new float[DlmsConfig.SOCKET_NUMBER]);
        for (int i = 0; i < DlmsConfig.SOCKET_NUMBER; i++) {
            index = push.getPushObjectList().get(2 + i).getValue().getAttributeIndex() - 1;
            notificationDto.getSensorValues()[i] = (short) push.getPushObjectList().get(2 + i).getKey().getValues()[index];
        }

        return notificationDto;
    }

    @Override
    public void onError(Object sender, Exception ex) {

    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {

    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {

    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }

    @Override
    public void onClientConnected(Object o, ConnectionEventArgs connectionEventArgs) {

    }

    @Override
    public void onClientDisconnected(Object o, ConnectionEventArgs connectionEventArgs) {

    }

    @Override
    public void close() throws Exception {
        media.close();
    }
}
