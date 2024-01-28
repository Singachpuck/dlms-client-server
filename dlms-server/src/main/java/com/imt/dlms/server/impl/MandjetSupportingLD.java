package com.imt.dlms.server.impl;

import com.imt.dlms.server.core.ManagementLogicalDevice;
import com.imt.dlms.server.core.ServingLogicalDevice;
import com.imt.dlms.server.service.MandjetService;
import gurux.dlms.GXUInt32;
import gurux.dlms.ValueEventArgs;
import gurux.dlms.enums.*;
import gurux.dlms.objects.*;
import gurux.dlms.objects.enums.SortMethod;

import java.util.ArrayList;
import java.util.List;

public class MandjetSupportingLD extends ServingLogicalDevice {

    private static final int REGISTER_COUNT = 6;

    private final MandjetService mandjetService;


    public MandjetSupportingLD(ManagementLogicalDevice managementLD,
                               String logicalDeviceName,
                               GXDLMSAssociationLogicalName ln,
                               String password,
                               GXDLMSTcpUdpSetup wrapper,
                               short sap,
                               MandjetService mandjetService) {
        super(managementLD, logicalDeviceName, ln, wrapper, sap);
        this.mandjetService = mandjetService;
        ln.getAuthenticationMechanismName().setMechanismId(Authentication.LOW);
        ln.setSecret(password.getBytes());
    }

    @Override
    public void init() {
        this.addVoltageRegister();
        final List<GXDLMSRegister> registers = this.populateMandjetEmonTxRegisters();
        this.addMandjetSocketProfile(registers);
        super.init();
    }

    private void addVoltageRegister() {
        final GXDLMSRegister d = new GXDLMSRegister("1.0.32.4.0.255");
        d.setValue(0.0f);
        // Set access right. Client can't change Device name.
        d.setAccess(2, AccessMode.READ);
        d.setUnit(Unit.VOLTAGE);
        d.setDataType(2, DataType.FLOAT32);
        getItems().add(d);
    }

    private List<GXDLMSRegister> populateMandjetEmonTxRegisters() {
        // 1.0.21.4.0.255
        final List<GXDLMSRegister> registers = new ArrayList<>();
        for (int i = 0; i < REGISTER_COUNT; i++) {
            final GXDLMSRegister register = new GXDLMSRegister(String.format("1.%d.21.4.0.255", i));
            register.setUnit(Unit.ACTIVE_POWER);
            register.setDataType(2, DataType.INT16);
            register.setAccess(2, AccessMode.READ);
            registers.add(register);
            getItems().add(register);
        }
        return registers;
    }

    private void addMandjetSocketProfile(List<GXDLMSRegister> registers) {
        GXDLMSProfileGeneric pg = new GXDLMSProfileGeneric("1.0.99.1.0.255");
        // Set capture period to 15 second.
        pg.setCapturePeriod(15);
        // Maximum row count.
        pg.setProfileEntries(100);
        pg.setSortMethod(SortMethod.FIFO);
        pg.setSortObject(null);
        // Add columns.
        for (GXDLMSRegister register : registers) {
            pg.addCaptureObject(register, 2, 0);
        }
        getItems().add(pg);
    }

    @Override
    public void onPreRead(ValueEventArgs[] args) throws Exception {
        super.onPreRead(args);

        for (ValueEventArgs e : args) {
            if (e.getTarget() instanceof GXDLMSClock c && e.getIndex() == 2) {
                c.setTime(c.now());
            } else if (e.getTarget() instanceof GXDLMSRegister data && e.getIndex() == 2) {
                if (data.getLogicalName().equals("1.0.32.4.0.255")) {
                    data.setValue((float) mandjetService.getVoltageFeed());
                }
            }
        }
    }

    @Override
    public void onPreGet(ValueEventArgs[] args) throws Exception {
        super.onPreGet(args);

        if (args.length == 1) {
            final ValueEventArgs arg = args[0];
            if (arg.getTarget() instanceof GXDLMSProfileGeneric pg) {
                final List<Integer> emonTxFeeds = mandjetService.getEmonTxFeeds();
                for (int i = 0; i < REGISTER_COUNT; i++) {
                    final GXDLMSRegister register = ((GXDLMSRegister) pg.getCaptureObjects().get(i).getKey());
                    register.setValue(emonTxFeeds.get(i));
                }
            }
        }
    }
}
