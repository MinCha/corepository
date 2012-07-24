package com.github.corepo.client;

public class TimeBasedWriteback implements Runnable {
	private OriginalRepository originalRepository;
	private CoRepository coRepository;
	private LRUKeyUpdateTime keyUpdateTime;
	private int writebackPeriodInMillis;
	private boolean running = true;

	public TimeBasedWriteback(LRUKeyUpdateTime keyUpdateTime,
			OriginalRepository originalRepository, CoRepository coRepository,
			int writebackPeriodInMillis) {
		this.originalRepository = originalRepository;
		this.coRepository = coRepository;
		this.keyUpdateTime = keyUpdateTime;
		this.writebackPeriodInMillis = writebackPeriodInMillis;
	}

	public void start() {
		Thread t = new Thread(new TimeBasedWriteback(keyUpdateTime,
				originalRepository, coRepository, writebackPeriodInMillis));
		t.setDaemon(true);
		t.start();
	}

	public void stop() {
		running = false;
	}

	public void run() {
		while (running) {
			for (String key : keyUpdateTime
					.findKeysOverThan(writebackPeriodInMillis)) {
				if (coRepository.isInt(key)) {
					originalRepository.writeback(coRepository.selectAsInt(key));
				} else {
					originalRepository.writeback(coRepository
							.selectAsObject(key));
				}
			}
			try {
				Thread.sleep(writebackPeriodInMillis);
			} catch (InterruptedException e) {
			}
		}
	}
}
