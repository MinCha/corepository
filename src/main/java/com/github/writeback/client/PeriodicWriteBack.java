package com.github.writeback.client;

public class PeriodicWriteBack extends Thread {
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private long writeBackPeriodInMills;
	private static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;

	public PeriodicWriteBack(CoRepository coRepository,
			OriginalRepository originalRepository, long writeBackPeriodInMills) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		this.writeBackPeriodInMills = writeBackPeriodInMills;
		this.setDaemon(true);
	}

	public PeriodicWriteBack(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository, DEFAULT_WRITEBACK_PERIOD_INMILLIS);
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep(writeBackPeriodInMills);
			} catch (InterruptedException e) {
			}

			for (WriteBackItem each : coRepository.selectAll()) {
				originalRepository.writeBack(each);				
			}
		}
	}
}
