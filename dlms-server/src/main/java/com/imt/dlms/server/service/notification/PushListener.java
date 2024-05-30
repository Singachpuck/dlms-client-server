package com.imt.dlms.server.service.notification;

import gurux.dlms.objects.GXDLMSPushSetup;

public interface PushListener {

    void onBeforePush(PushListenerArgs args);

    void onAfterPush(PushListenerArgs args);
}
