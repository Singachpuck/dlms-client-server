package com.imt.dlms.server.service;

import java.util.Random;

public class DLMSUtil {

    public static final int PUBLIC_CLIENT_SAP = 0x10;

    public static final int LDN_SIZE = 16;

    public static String generateLDN(byte[] manufacturerId, byte[] identifier) {
        final byte[] ldn = new byte[LDN_SIZE];
        System.arraycopy(manufacturerId, 0, ldn, 0, 3);
        System.arraycopy(identifier, 0, ldn, 3, 13);
        return new String(ldn);
    }
}
