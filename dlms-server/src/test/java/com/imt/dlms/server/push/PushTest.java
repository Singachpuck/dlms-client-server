package com.imt.dlms.server.push;

import gurux.dlms.GXDateTime;
import gurux.dlms.GXSimpleEntry;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.enums.Security;
import gurux.dlms.objects.GXDLMSCaptureObject;
import gurux.dlms.objects.GXDLMSClock;
import gurux.dlms.objects.GXDLMSObject;
import gurux.dlms.objects.GXDLMSPushSetup;
import gurux.dlms.secure.GXDLMSSecureNotify;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;

import org.junit.jupiter.api.Test;

import java.util.Calendar;

public class PushTest {
    public static void main(String[] args) {
        Settings settings = new Settings();
        int ret = Settings.getParameters(args, settings);
        if (ret != 0) {
            System.exit(1);
            return;
        }

        settings.port = 4059;

        GXNet media = new GXNet(
                NetworkType.UDP,
                "localhost", settings.port);
        GXDLMSSecureNotify cl =
                new GXDLMSSecureNotify(true, 16, 1, InterfaceType.WRAPPER);
        // Un-comment this if you want to send encrypted push messages.
         cl.getCiphering().setSecurity(Security.AUTHENTICATION_ENCRYPTION);
        GXDLMSPushSetup p = new GXDLMSPushSetup();
        GXDLMSClock clock = new GXDLMSClock();
        // Un-comment this if you want to describe the content of the push
        // message for
        // the client.
        // p.getPushObjectList()
        // .add(new GXSimpleEntry<GXDLMSObject, GXDLMSCaptureObject>(p,
        // new GXDLMSCaptureObject(2, 0)));
        p.getPushObjectList()
                .add(new GXSimpleEntry<GXDLMSObject, GXDLMSCaptureObject>(clock,
                        new GXDLMSCaptureObject(2, 0)));
//        try (PushListener SNServer = new PushListener(settings.port, settings.interfaceType)) {
//            System.out.println("Starting to listen Push messages in port "
//                    + settings.port);
//            System.out.println(
//                    "Press X to close and Enter to send a Push message.");
        try {
            System.out.println(
                    "Press X to close and Enter to send a Push message.");
            ret = 10;
            while ((ret = System.in.read()) != -1) {
                // Send push.
                if (ret == 10) {
                    System.out.println("Sending Push message.");
                    media.open();
                    clock.setTime(
                            new GXDateTime(Calendar.getInstance().getTime()));
                    for (byte[] it : cl.generatePushSetupMessages(null, p)) {
                        media.send(it, null);
                    }
                    media.close();
                }
                // Close app.
                if (ret == 'x' || ret == 'X') {
                    break;
                }
            }
            media.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    void ex1() {

    }
}
