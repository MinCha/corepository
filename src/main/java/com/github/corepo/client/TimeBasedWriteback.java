package com.github.corepo.client;

import java.util.concurrent.atomic.AtomicBoolean;

class TimeBasedWriteback implements Runnable {
    private Writeback writeback;
    private LRUKeyUpdateTime keyUpdateTime;
    private int writebackPeriodInMillis;
    private Object lock = new Object();
    private AtomicBoolean running = new AtomicBoolean(true);

    TimeBasedWriteback(Writeback writeback, LRUKeyUpdateTime keyUpdateTime,
	    int writebackPeriodInMillis) {
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
	    for (String key : keyUpdateTime
		    .findKeysOverThan(writebackPeriodInMillis)) {
		writeback.writeback(key);
		keyUpdateTime
			.notifyWritebacked(key, System.currentTimeMillis());
	    }
	    try {
		synchronized (lock) {
		    lock.wait(writebackPeriodInMillis);
		}
	    } catch (InterruptedException e) {
	    }
	}
    }
}
