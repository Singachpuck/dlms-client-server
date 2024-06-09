package com.dochkas.mandjetDlmsWeb.config;

import gurux.common.IGXMedia;
import gurux.common.enums.TraceLevel;
import gurux.dlms.GXDLMSClient;
import gurux.dlms.GXSimpleEntry;
import gurux.dlms.enums.Authentication;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.objects.*;
import gurux.dlms.secure.GXDLMSSecureClient;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DlmsConfig implements EnvironmentAware {

    public static final int SOCKET_NUMBER = 6;

    @Value("${dlms.notification.port}")
    private int DLMS_CLIENT_PORT;

    private Environment env;

    @Bean
    IGXMedia clientMedia() {
        final IGXMedia media = new GXNet(NetworkType.UDP, DLMS_CLIENT_PORT);
        media.setTrace(TraceLevel.VERBOSE);
        return media;
    }

    @Bean
    GXDLMSClient dlmsManagementClient() {
        final GXDLMSSecureClient client = new GXDLMSSecureClient(true,
                16, 1, Authentication.NONE, null,
                InterfaceType.WRAPPER);
        return client;
    }

    @Bean
    GXDLMSClient dlmsSupportingClient() {
        final GXDLMSSecureClient client = new GXDLMSSecureClient(true, 16,
                3013, Authentication.LOW, env.getProperty("DLMS_PASSWORD"), InterfaceType.WRAPPER);
        return client;
    }


    @Bean
    GXDLMSPushSetup managementPush() {
        final GXDLMSPushSetup push = new GXDLMSPushSetup();
        // LDN
        push.getPushObjectList()
                .add(new GXSimpleEntry<>(
                        new GXDLMSData(), new GXDLMSCaptureObject(2, 0)));
        // Clock
        push.getPushObjectList()
                .add(new GXSimpleEntry<>(
                        new GXDLMSClock(), new GXDLMSCaptureObject(2, 0)));
        // Battery
        push.getPushObjectList()
                .add(new GXSimpleEntry<>(
                        new GXDLMSData(), new GXDLMSCaptureObject(2, 0)));
        return push;
    }

    @Bean
    GXDLMSPushSetup supportingPush() {
        final GXDLMSPushSetup push = new GXDLMSPushSetup();
        // LDN
        push.getPushObjectList()
                .add(new GXSimpleEntry<>(
                        new GXDLMSData(), new GXDLMSCaptureObject(2, 0)));
        // Voltage
        push.getPushObjectList()
                .add(new GXSimpleEntry<>(
                        new GXDLMSRegister(), new GXDLMSCaptureObject(2, 0)));
        // Sockets
        for (int i = 0; i < SOCKET_NUMBER; i++) {
            push.getPushObjectList()
                    .add(new GXSimpleEntry<>(
                            new GXDLMSRegister(), new GXDLMSCaptureObject(2, 0)));
        }
        return push;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
