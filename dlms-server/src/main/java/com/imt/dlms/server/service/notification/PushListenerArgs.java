package com.imt.dlms.server.service.notification;

import gurux.dlms.objects.GXDLMSPushSetup;

public record PushListenerArgs(GXDLMSPushSetup push, int frameNumber) {
}
