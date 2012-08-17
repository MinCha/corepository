package com.github.corepo.client;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TimeBasedWriteback implements Runnable {
	private static final Logger LOG = LoggerFactory
			.getLogger(TimeBasedWriteback.class);
	private OriginalRepository originalRepository;
	private CoRepository coRepository;
	private LRUKeyUpdateTime keyUpdateTime;
	private int writebackPeriodInMillis;
	private Object lock = new Object();
	private AtomicBoolean running = new AtomicBoolean(true);

	TimeBasedWriteback(LRUKeyUpdateTime keyUpdateTime,
			OriginalRepository originalRepository, CoRepository coRepository,
			int writebackPeriodInMillis) {
		this.originalRepository = originalRepository;
		this.coRepository = coRepository;
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
				writeback(key);
			}
			try {
				synchronized (lock) {
					lock.wait(writebackPeriodInMillis);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	void writebackAll() {
		if (coRepository.isConnected() == false) {
			return;
		}

		LOG.info("All keys will be writebacked.");
		Set<String> keys = keyUpdateTime.findAllKeys();
		for (String key : keyUpdateTime.findAllKeys()) {
			writeback(key);
		}
		LOG.info(keys.size() + " keys has just been writebacked.");
	}

	void writeback(String key) {
		if (coRepository.exists(key) == false) {
			return;
		}

		if (coRepository.isInt(key)) {
			originalRepository.writeback(Arrays.asList(coRepository
					.selectAsInt(key)));
		} else {
			originalRepository.writeback(Arrays.asList(coRepository
					.selectAsObject(key)));
		}
		keyUpdateTime.notifyWritebacked(key, System.currentTimeMillis());
	}
}
