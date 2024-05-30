package com.imt.dlms.server.service.notification;

import com.imt.dlms.server.core.media.LoraE5SerialMedia;
import com.imt.dlms.server.exception.LoraConfigurationException;
import gurux.common.IGXMedia;
import gurux.dlms.GXDLMSNotify;
import gurux.dlms.objects.GXDLMSPushSetup;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DLMSNotifyService {

    private static final long DEFAULT_DELAY_SEC = 5;

    private static long TASK_SEQ = 0;

    private final Scheduler scheduler;

    private IGXMedia media;

    private final HashMap<Long, Future<?>> tasks = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public DLMSNotifyService(Scheduler scheduler, IGXMedia media) {
        this.scheduler = scheduler;
        this.media = media;
    }

    /**
     * Schedules a push message.
     * @return id of the task. Used by cancel().
     */
    public long schedulePushMessage(GXDLMSPushSetup p, GXDLMSNotify notify, List<PushListener> pushListenerList, long period) {
        final Future<?> task = scheduler.schedulePeriodically(() -> {
            for (PushListener pushListener : pushListenerList) {
                pushListener.onBeforePush(new PushListenerArgs(p, 0));
            }
            try {
                lock.lock();
                if (!media.isOpen()) {
                    media.open();
                }
                if (media instanceof LoraE5SerialMedia lora && !lora.checkJoin()) {
                    if (!lora.joinNetwork()) {
                        throw new LoraConfigurationException("Not possible to join Lora Network.");
                    }
                }
                int i = 0;
                for (byte[] it : notify.generatePushSetupMessages(null, p)) {
                    media.send(it, p.getDestination());
                    i++;
                }

                for (PushListener pushListener : pushListenerList) {
                    pushListener.onAfterPush(new PushListenerArgs(p, i));
                }
            } catch (Exception e) {
                System.err.println("Failed to send a push message: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        }, DEFAULT_DELAY_SEC, period, TimeUnit.SECONDS);

        tasks.put(TASK_SEQ, task);
        TASK_SEQ++;
        return TASK_SEQ - 1;
    }

    public void clear(long id) {
        final Future<?> task = tasks.get(id);
        if (task != null) {
            task.cancel(true);
        }
    }

    public void clearAll() {
        this.scheduler.invalidate();
    }

    public void setMedia(IGXMedia media) {
        this.media = media;
    }
}
