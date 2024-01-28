package com.imt.dlms.server.core;

import com.imt.dlms.server.service.DLMSUtil;
import gurux.dlms.objects.GXDLMSAssociationLogicalName;
import gurux.dlms.objects.GXDLMSTcpUdpSetup;

public class ServingLogicalDevice extends LogicalDevice {

    private final ManagementLogicalDevice managementLD;

    public ServingLogicalDevice(ManagementLogicalDevice managementLD,
                                String logicalDeviceName,
                                GXDLMSAssociationLogicalName ln,
                                GXDLMSTcpUdpSetup wrapper,
                                short sap) {
        super(logicalDeviceName, ln, wrapper, sap);
        this.managementLD = managementLD;
        ln.setClientSAP(DLMSUtil.PUBLIC_CLIENT_SAP);
    }
}
