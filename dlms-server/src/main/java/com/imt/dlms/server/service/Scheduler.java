package com.imt.dlms.server.service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

    Future<?> schedulePeriodically(Runnable r, long initDelay, long period, TimeUnit unit);

    void invalidate();
}
