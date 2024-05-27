package com.imt.dlms.server.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultScheduler implements Scheduler {

    private final ScheduledExecutorService scheduler;

    public DefaultScheduler(int threadN) {
        this.scheduler = Executors.newScheduledThreadPool(threadN);
    }

    public ScheduledFuture<?> schedulePeriodically(Runnable r, long initDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(r, initDelay, period, unit);
    }

    public void invalidate() {
        scheduler.shutdownNow();
    }
}
