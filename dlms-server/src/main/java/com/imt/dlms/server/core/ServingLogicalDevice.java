package com.imt.dlms.server.core;

import com.imt.dlms.server.service.DLMSNotifyService;
import com.imt.dlms.server.service.DLMSUtil;
import com.imt.dlms.server.service.Scheduler;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;

public abstract class ServingLogicalDevice extends LogicalDevice {

    private final ManagementLogicalDevice managementLD;

    public ServingLogicalDevice(ManagementLogicalDevice managementLD,
                                String logicalDeviceName,
                                GXDLMSAssociationLogicalName ln,
                                GXDLMSTcpUdpSetup wrapper,
                                short sap,
                                DLMSNotifyService notify) {
        super(logicalDeviceName, ln, wrapper, sap, notify);
        this.managementLD = managementLD;
        ln.setClientSAP(DLMSUtil.PUBLIC_CLIENT_SAP);
    }
}
