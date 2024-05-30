package com.imt.dlms.server.config;

import com.imt.dlms.server.service.DLMSUtil;

import java.util.List;

public class DlmsConfig {

    public static final String MANAGEMENT_AA_LN = "0.0.40.0.1.255";

    // TODO: Support multiple Client SAP's
    public static final List<Integer> CLIENT_SAP_LIST = List.of(
            // Public client SAP
            0x10
    );

    public static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");

    // Fake
    public static final byte[] MANUFACTURER_ID = new byte[] {
            // Consists of 3 bytes
            0x01, 0x02, 0x7F
    };

    public static final String MANAGEMENT_LDN = DLMSUtil.generateLDN(MANUFACTURER_ID, new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D
    });

    public static final String SUPPORTING_LDN = DLMSUtil.generateLDN(MANUFACTURER_ID, new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0E
    });

    public static final long SERIAL_NUMBER = 123456;

    public static final String SUPPORTING_AA_LN = "0.0.40.0.2.255";

    public static final short SUPPORTING_LD_SAP = 3013;

    // From 0x0000 to 0xFFFF
    public static final int MAX_PDU_SIZE = 0x0032;
}
