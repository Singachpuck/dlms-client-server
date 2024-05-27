package com.imt.dlms.client;

import com.imt.dlms.client.service.DlmsClientService;
import gurux.common.GXCommon;
import gurux.common.IGXMedia;
import gurux.common.enums.TraceLevel;
import gurux.dlms.enums.Authentication;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.enums.ObjectType;
import gurux.dlms.objects.GXDLMSData;
import gurux.dlms.objects.GXDLMSObject;
import gurux.dlms.objects.GXDLMSProfileGeneric;
import gurux.dlms.secure.GXDLMSSecureClient;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientManagerTest {

    private IGXMedia media;

    void defaultTest() throws Exception {
        // 192.168.112.98
        final GXDLMSSecureClient client = new GXDLMSSecureClient(true, 16,
                3013, Authentication.LOW, "password", InterfaceType.WRAPPER);

        this.setupMedia();

        final DlmsClientService dlmsService = new DlmsClientService(client, media);

        try {
            dlmsService.connect();

            dlmsService.readAssociationView();
            System.out.println(client.getObjects());

            for (GXDLMSObject object : client.getObjects().getObjects(ObjectType.DATA)) {
                dlmsService.readObject(object, 2);
                if (object.getLogicalName().equals("0.0.42.0.0.255") && object instanceof GXDLMSData data) {
                    System.out.println(data.getValue());
                }
            }
            for (GXDLMSObject object : client.getObjects().getObjects(ObjectType.PROFILE_GENERIC)) {
                final GXDLMSProfileGeneric pg = ((GXDLMSProfileGeneric) object);
                dlmsService.readObject(pg, 3);
                long entriesInUse = ((Number) dlmsService.readObject(pg, 7)).longValue();
                long entries = ((Number) dlmsService.readObject(pg, 8)).longValue();
                final Object[] cells = dlmsService.readRowsByEntry(pg, 0, (int) entriesInUse);

                for (Object rows : cells) {
                    StringBuilder sb = new StringBuilder();
                    for (Object cell : (Object[]) rows) {
                        if (cell instanceof byte[]) {
                            sb.append(GXCommon.bytesToHex((byte[]) cell));
                            sb.append(" | ");
                        } else {
                            sb.append(String.valueOf(cell));
                            sb.append(" | ");
                        }
                    }
                    System.out.println(sb);
//                    writeTrace(sb.toString(), TraceLevel.INFO);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            dlmsService.close();
        }
    }

    private void setupMedia() throws Exception {
        final int port = 4059;
        final GXNet net = new GXNet(NetworkType.UDP, port);
        net.setHostName("localhost");
        net.setServer(false);
        net.setTrace(TraceLevel.VERBOSE);
        media = net;
        media.open();
    }
}