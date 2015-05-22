package com.github.corepo.client;

import java.util.concurrent.atomic.AtomicBoolean;

class TimeBasedWriteback implements Runnable {
    private Writeback writeback;
    private LRUKeyUpdateTime keyUpdateTime;
    private long writebackPeriodInMillis;
    private Object lock = new Object();
    private AtomicBoolean running = new AtomicBoolean(true);

    TimeBasedWriteback(Writeback writeback, LRUKeyUpdateTime keyUpdateTime,
                       long writebackPeriodInMillis) {
        this.writeback = writeback;
        this.keyUpdateTime = keyUpdateTime;
        this.writebackPeriodInMillis = writebackPeriodInMillis;
    }

    void start() {
        Thread t = new Thread(this);
        t.setName("CoRepository TimeBasedWriteback");
        t.setDaemon(true);
        t.start();
    }

    void stop() {
        running.set(false);
        synchronized (lock) {
            lock.notify();
        }

    }

    public void run() {
        while (running.get()) {
            keyUpdateTime.applyToKeysOverThan(writebackPeriodInMillis,
                    writeback, true);
            try {
                synchronized (lock) {
                    lock.wait(writebackPeriodInMillis);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
