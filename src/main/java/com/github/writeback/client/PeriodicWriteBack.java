package com.github.writeback.client;

public class PeriodicWriteBack extends Thread {
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private long writeBackPeriodInMills;

	public PeriodicWriteBack(CoRepository coRepository,
			OriginalRepository originalRepository, long writeBackPeriodInMills) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		this.writeBackPeriodInMills = writeBackPeriodInMills;
		this.setDaemon(true);
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
