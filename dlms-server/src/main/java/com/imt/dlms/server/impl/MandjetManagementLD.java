package com.imt.dlms.server.impl;

import com.imt.dlms.server.core.ManagementLogicalDevice;
import com.imt.dlms.server.service.MandjetService;
import gurux.dlms.GXDateTime;
import gurux.dlms.ValueEventArgs;
import gurux.dlms.enums.AccessMode;
import gurux.dlms.enums.DataType;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSClock;
import gurux.dlms.objects.GXDLMSData;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;

public class MandjetManagementLD extends ManagementLogicalDevice {

    private final MandjetService mandjetService;

    public MandjetManagementLD(String logicalDeviceName,
                               GXDLMSAssociationLogicalName ln,
                               GXDLMSTcpUdpSetup wrapper,
                               MandjetService mandjetService) {
        super(logicalDeviceName, ln, wrapper);
        this.mandjetService = mandjetService;
    }

    @Override
    public void init() {
        this.addClock();
        this.addBatteryData();
        super.init();
    }

    private void addBatteryData() {
        final GXDLMSData d = new GXDLMSData("0.0.96.1.10.255");
        d.setValue(0);
        // Set access right. Client can't change Device name.
        d.setAccess(2, AccessMode.READ);
        d.setDataType(2, DataType.INT16);
        getItems().add(d);
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
}
