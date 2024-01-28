package com.imt.dlms.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MandjetServiceTest {

    // TODO: Make MOCK
    @Test
    void getEmonTxFeeds() {
        final MandjetService mandjetService = new MandjetService();
        final List<Integer> emonTxFeeds = mandjetService.getEmonTxFeeds();

        System.out.println(emonTxFeeds);
    }

    @Test
    void getBatteryFeed() {
        final MandjetService mandjetService = new MandjetService();
        final int battery = mandjetService.getBatteryPercentage();

        System.out.println(battery);
    }

    @Test
    void getVoltageFeed() {
        final MandjetService mandjetService = new MandjetService();
        final double voltage = mandjetService.getVoltageFeed();

        System.out.println(voltage);
    }
}