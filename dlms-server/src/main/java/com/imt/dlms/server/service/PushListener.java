package com.imt.dlms.server.service;

import gurux.dlms.objects.GXDLMSPushSetup;

public interface PushListener {

    void onBeforePush(GXDLMSPushSetup p);

    void onAfterPush(GXDLMSPushSetup p);
}
