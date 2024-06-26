package com.imt.dlms.server.impl;

import com.imt.dlms.server.ServerManager;
import com.imt.dlms.server.config.DlmsConfig;
import com.imt.dlms.server.core.ManagementLogicalDevice;
import com.imt.dlms.server.service.notification.DLMSNotifyService;
import com.imt.dlms.server.service.MandjetService;
import com.imt.dlms.server.service.notification.PushListenerArgs;
import gurux.dlms.GXDateTime;
import gurux.dlms.GXSimpleEntry;
import gurux.dlms.ValueEventArgs;
import gurux.dlms.enums.AccessMode;
import gurux.dlms.enums.DataType;
import gurux.dlms.enums.ObjectType;
import gurux.dlms.objects.*;

import java.util.Collections;
import java.util.Map;

public class MandjetManagementLD extends ManagementLogicalDevice {

    // Seconds
    private static final int PUSH_SEND_INTERVAL = 60;

    private final MandjetService mandjetService;

    private Long pushScheduleId;

    public MandjetManagementLD(String logicalDeviceName,
                               GXDLMSAssociationLogicalName ln,
                               GXDLMSTcpUdpSetup wrapper,
                               MandjetService mandjetService,
                               DLMSNotifyService notify) {
        super(logicalDeviceName, ln, wrapper, notify);
        this.mandjetService = mandjetService;
    }

    @Override
    public void init() {
        final GXDLMSClock clock = this.addClock();
        final GXDLMSData battery = this.addBatteryData();
        final GXDLMSPushSetup pushSetup = this.addPushSetup(clock, battery);
        // Order is important
        super.init();
        final GXDLMSData ldn = (GXDLMSData) getItems().findByLN(ObjectType.DATA, "0.0.42.0.0.255");
        pushSetup.getPushObjectList()
                .add(0, new GXSimpleEntry<>(ldn,
                        new GXDLMSCaptureObject(2, 0)));
        this.pushScheduleId = this.getNotifyService()
                .schedulePushMessage(pushSetup, this.getNotify(), Collections.singletonList(this), PUSH_SEND_INTERVAL);
    }

    private GXDLMSData addBatteryData() {
        final GXDLMSData d = new GXDLMSData("0.0.96.1.10.255");
        d.setValue(0);
        // Set access right. Client can't change Device name.
        d.setAccess(2, AccessMode.READ);
        d.setDataType(2, DataType.INT16);
        getItems().add(d);
        return d;
    }

    private GXDLMSClock addClock() {
        final GXDLMSClock clock = new GXDLMSClock();
        clock.setTimeZone(60);
        clock.setBegin(new GXDateTime(-1, 3, 31, -1, -1, -1, -1));
        clock.setEnd(new GXDateTime(-1, 10, 27, -1, -1, -1, -1));
        clock.setDeviation(60);
        clock.setEnabled(true);
        getItems().add(clock);
        return clock;
    }

    private GXDLMSPushSetup addPushSetup(GXDLMSClock clock, GXDLMSData battery) {
        GXDLMSPushSetup p = new GXDLMSPushSetup();
        p.setDestination(DlmsConfig.CLIENT_ENDPOINT);
        p.getPushObjectList()
                .add(new GXSimpleEntry<>(clock,
                        new GXDLMSCaptureObject(2, 0)));
        p.getPushObjectList()
                .add(new GXSimpleEntry<>(battery,
                        new GXDLMSCaptureObject(2, 0)));
        return p;
    }

    @Override
    public void onBeforePush(PushListenerArgs args) {
        System.out.println("Sending Push message.");

        for (Map.Entry<GXDLMSObject, GXDLMSCaptureObject> entry : args.push().getPushObjectList()) {
            if (entry.getKey() instanceof GXDLMSClock c && entry.getValue().getAttributeIndex() == 2) {
                c.setTime(c.now());
            } else if (entry.getKey() instanceof GXDLMSData data && entry.getValue().getAttributeIndex() == 2) {
                if (data.getLogicalName().equals("0.0.96.1.10.255")) {
                    data.setValue((short) mandjetService.getBatteryPercentage());
                }
            }
        }
    }

    @Override
    public void onAfterPush(PushListenerArgs args) {
        System.out.println("Push sent in " + args.frameNumber() + " frames.");
    }

    @Override
    public void onPreRead(ValueEventArgs[] args) throws Exception {
        for (ValueEventArgs e : args) {
            if (e.getTarget() instanceof GXDLMSClock c && e.getIndex() == 2) {
                c.setTime(c.now());
            } else if (e.getTarget() instanceof GXDLMSData data && e.getIndex() == 2) {
                if (data.getLogicalName().equals("0.0.96.1.10.255")) {
                    data.setValue((short) mandjetService.getBatteryPercentage());
                }
            }
        }
    }

    public void close() throws Exception {
        if (pushScheduleId != null) {
            this.getNotifyService().clear(pushScheduleId);
        }
        super.close();
    }
}
