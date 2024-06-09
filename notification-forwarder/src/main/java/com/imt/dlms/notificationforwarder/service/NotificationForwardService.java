package com.imt.dlms.notificationforwarder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Base64;

@Service
public class NotificationForwardService implements EnvironmentAware {

    private final Logger logger = LoggerFactory.getLogger(NotificationForwardService.class);

    private Environment env;

    @Autowired
    private Util util;

    public void forwardDlmsNotification(String payload) {
        final int port = env.getProperty("DLMS_CLIENT_PORT", Integer.class);
        final String ipAddress = env.getProperty("DLMS_CLIENT_HOST");

        try {
            final DatagramSocket socket = new DatagramSocket();

            final byte[] data = Base64.getDecoder().decode(payload);

            logger.trace(util.bytesToHex(data));

            final InetAddress address = InetAddress.getByName(ipAddress);
            final DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

            logger.info("Forwarding DLMS Notification to destination: " + ipAddress + ":" + port);
            socket.send(packet);
            socket.close();
            logger.info("UDP Datagram sent successfully.");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
