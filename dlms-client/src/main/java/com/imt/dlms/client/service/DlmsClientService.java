package com.imt.dlms.client.service;

import gurux.common.IGXMedia;
import gurux.dlms.GXReplyData;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.enums.Security;
import gurux.dlms.objects.GXDLMSAssociationShortName;
import gurux.dlms.objects.GXDLMSObject;
import gurux.dlms.objects.GXDLMSProfileGeneric;
import gurux.dlms.secure.GXDLMSSecureClient;

public class DlmsClientService {

    private final GXDLMSSecureClient dlms;

    private final IGXMedia media;

    private final DlmsReader reader;

    public DlmsClientService(GXDLMSSecureClient dlms, IGXMedia media) {
        this.dlms = dlms;
        this.media = media;
        this.reader = new DlmsReader(dlms, media);
    }

    public void connect() throws Exception {
        GXReplyData reply = new GXReplyData();

        //Generate AARQ request.
        //Split requests to multiple packets if needed.
        //If password is used all data might not fit to one packet.
        for (byte[] it : dlms.aarqRequest())
        {
            reply.clear();
            reader.readDLMSPacket(it, reply);
        }

        //Parse reply.
        dlms.parseAareResponse(reply.getData());
    }

    public void readAssociationView() throws Exception {
        GXReplyData reply = new GXReplyData();
        // Get Association view from the meter.
        reader.readDataBlock(dlms.getObjectsRequest(), reply);
        dlms.parseObjects(reply.getData(), true);
        // Access rights must read differently when short Name referencing is
        // used.
        if (!dlms.getUseLogicalNameReferencing()) {
            GXDLMSAssociationShortName sn = (GXDLMSAssociationShortName) dlms
                    .getObjects().findBySN(0xFA00);
            if (sn != null && sn.getVersion() > 0) {
                this.readObject(sn, 3);
            }
        }

    }

    public Object readObject(GXDLMSObject item, int attributeIndex) throws Exception
    {
        GXReplyData reply = new GXReplyData();
        byte[] data = dlms.read(item, attributeIndex)[0];
        reader.readDataBlock(data, reply);
        return dlms.updateValue(item, attributeIndex, reply.getValue());
    }

    public Object[] readRowsByEntry(GXDLMSProfileGeneric pg, int index,
                                    int count) throws Exception {
        byte[][] data = dlms.readRowsByEntry(pg, index, count);
        GXReplyData reply = new GXReplyData();
        reader.readDataBlock(data, reply);
        return (Object[]) dlms.updateValue(pg, 2, reply.getValue());
    }

    public void writeObject(GXDLMSObject item, int attributeIndex) throws Exception
    {
        GXReplyData reply = new GXReplyData();
        byte[][] data = dlms.write(item, attributeIndex);
        reader.readDataBlock(data, reply);
    }

    public void disconnect() throws Exception {
        if (media != null && media.isOpen()
                && !dlms.isPreEstablishedConnection()) {
            GXReplyData reply = new GXReplyData();
            reader.readDLMSPacket(dlms.disconnectRequest(), reply);
        }
    }

    public void close() throws Exception {
        if (media != null && media.isOpen()) {
            GXReplyData reply = new GXReplyData();
            try {
                // Release is call only for secured connections.
                // All meters are not supporting Release and it's causing
                // problems.
                if (dlms.getInterfaceType() == InterfaceType.WRAPPER
                        || (dlms.getInterfaceType() == InterfaceType.HDLC
                        && dlms.getCiphering()
                        .getSecurity() != Security.NONE)) {
                    reader.readDataBlock(dlms.releaseRequest(), reply);
                }
            } catch (Exception e) {
                // All meters don't support release.
            }
            reply.clear();
            reader.readDLMSPacket(dlms.disconnectRequest(), reply);
            media.close();
        }
    }
}
