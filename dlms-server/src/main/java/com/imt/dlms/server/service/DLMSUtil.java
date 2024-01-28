package com.imt.dlms.server.service;

import java.util.Random;

public class DLMSUtil {

    public static final int PUBLIC_CLIENT_SAP = 0x10;

    public static final int LDN_SIZE = 16;

    public static String generateLDN(byte[] manufacturerId) {
        final Random random = new Random();
        final byte[] ldn = new byte[LDN_SIZE];
        random.nextBytes(ldn);
        System.arraycopy(manufacturerId, 0, ldn, 0, 3);
        return new String(ldn);
    }
}
