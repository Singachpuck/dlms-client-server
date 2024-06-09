package com.imt.dlms.notificationforwarder.service;

import org.springframework.stereotype.Service;

@Service
public class Util {

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
