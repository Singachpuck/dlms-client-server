package com.imt.dlms.server.core;

import com.imt.dlms.server.service.DLMSNotifyService;
import com.imt.dlms.server.service.DLMSUtil;
import com.imt.dlms.server.service.Scheduler;
import gurux.dlms.GXSimpleEntry;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSSapAssignment;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;

public abstract class ManagementLogicalDevice extends LogicalDevice {

    public static final short MANAGEMENT_LD_SAP = 0x01;

    private final GXDLMSSapAssignment sapAssignment = new GXDLMSSapAssignment();

    public ManagementLogicalDevice(String logicalDeviceName,
                                   GXDLMSAssociationLogicalName ln,
                                   GXDLMSTcpUdpSetup wrapper,
                                   DLMSNotifyService notify) {
        super(logicalDeviceName, ln, wrapper, MANAGEMENT_LD_SAP, notify);
        ln.setClientSAP(DLMSUtil.PUBLIC_CLIENT_SAP);
    }

    @Override
    public void init() {
        this.assignLogicalDevice(this);
        getItems().add(sapAssignment);
        super.init();
    }

    public void assignLogicalDevice(LogicalDevice ld) {
        sapAssignment
                .getSapAssignmentList()
                .add(new GXSimpleEntry<>(ld.getLogicalDeviceSAP(), ld.getLogicalDeviceName()));
    }
}
