package com.imt.dlms.server.service;

import com.imt.dlms.server.core.media.LoraE5SerialMedia;
import com.imt.dlms.server.exception.UnknownCommunicationModeException;
import gurux.common.IGXMedia;
import gurux.common.IGXMediaListener;
import gurux.common.enums.TraceLevel;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;

import java.util.ArrayList;
import java.util.List;

public class MediaFactory {

    private MediaConfig config;

    public IGXMedia getMedia() {
        if (config.mode == MediaType.TCP_IP) {
            final GXNet net = new GXNet(config.networkType, config.port);
//        net.setServer(false);
            net.setTrace(config.trace);
            config.listeners.forEach(net::addListener);
            return net;
        } else if (config.mode == MediaType.E5_LORA) {
            final LoraE5SerialMedia lora = new LoraE5SerialMedia();
            // Serial
            lora.setPortName(config.portName);
            lora.setBaudRate(config.baudRate);
            lora.setDataBits(config.dataBits);
            lora.setParity(config.parity);
            lora.setStopBits(config.stopBits);
            lora.setTrace(config.trace);
            // Lora E5
            lora.setMode(config.loraMode);
            lora.setDataRate(config.dataRate);
            lora.setChannel(config.channel);
            lora.setAppKey(config.appKey);
            lora.setLoraClass(config.loraClass);
            lora.setPort(config.port);
            config.listeners.forEach(lora::addListener);
            return lora;
        } else {
            throw new UnknownCommunicationModeException(config.mode.name());
        }
    }

    public void setConfig(MediaConfig config) {
        this.config = config;
    }

    public static class MediaConfig {

        // Common
        private MediaType mode;

        private final  List<IGXMediaListener> listeners = new ArrayList<>();

        private int port;

        private TraceLevel trace = TraceLevel.VERBOSE;

        // TCP/IP
        private NetworkType networkType = NetworkType.TCP;

        // Serial
        private String portName;

        private BaudRate baudRate;

        private int dataBits;

        private Parity parity = Parity.NONE;

        private StopBits stopBits = StopBits.ONE;

        // Lora E5
        private String loraMode = "LWOTAA";

        private String dataRate;

        private String channel;

        private String appKey;

        private String loraClass;

        public MediaConfig withPort(int port) {
            this.port = port;
            return this;
        }

        public MediaConfig withTrace(TraceLevel trace) {
            this.trace = trace;
            return this;
        }

        public MediaConfig withListener(IGXMediaListener listener) {
            this.listeners.add(listener);
            return this;
        }

        public MediaConfig withNetworkType(NetworkType networkType) {
            this.networkType = networkType;
            return this;
        }

        public MediaConfig withMode(MediaType mode) {
            this.mode = mode;
            return this;
        }

        public MediaConfig withPortName(String portName) {
            this.portName = portName;
            return this;
        }

        public MediaConfig withBaudRate(BaudRate baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public MediaConfig withDataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public MediaConfig withParity(Parity parity) {
            this.parity = parity;
            return this;
        }

        public MediaConfig withStopBits(StopBits stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public MediaConfig withLoraMode(String loraMode) {
            this.loraMode = loraMode;
            return this;
        }

        public MediaConfig withDataRate(String dataRate) {
            this.dataRate = dataRate;
            return this;
        }

        public MediaConfig withChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public MediaConfig withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public MediaConfig withLoraClass(String loraClass) {
            this.loraClass = loraClass;
            return this;
        }
    }
}
