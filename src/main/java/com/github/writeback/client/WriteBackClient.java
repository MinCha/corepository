package com.github.writeback.client;

public class WriteBackClient {
	static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;
	static final Object mutex = new Object();
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private PeriodicWriteBack periodicWriteBack;

	public WriteBackClient(CoRepository coRepository,
			OriginalRepository originalRepository, long writeBackPeriodInMillis) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		periodicWriteBack = new PeriodicWriteBack(coRepository,
				originalRepository, writeBackPeriodInMillis);
		periodicWriteBack.start();
	}

	public WriteBackClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository,
				DEFAULT_WRITEBACK_PERIOD_INMILLIS);
	}

	public WriteBackItem select(String key) {
		pullInitialValueIfThereIsNo(key);
		return coRepository.select(key);
	}

	public void update(WriteBackItem item) {
		pullInitialValueIfThereIsNo(item.getKey());
		coRepository.update(item);
	}

	public void increase(String key) {
		pullInitialValueIfThereIsNo(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		pullInitialValueIfThereIsNo(key);
		coRepository.decrease(key);
	}

	private void pullInitialValueIfThereIsNo(String key) {
		if (coRepository.exists(key)) {
			return;
		}

		if (coRepository.lock(key)) {
			coRepository.insert(originalRepository.read(key));
			coRepository.unlock(key);

			wakeUpAllThreadsWatingForPulling();
		} else {
			waitUnitlInitialValueIsPulled(key);
		}
	}

	private void wakeUpAllThreadsWatingForPulling() {
		synchronized (mutex) {
			mutex.notifyAll();
		}
	}

	private void waitUnitlInitialValueIsPulled(String key) {
		synchronized (mutex) {
			while (coRepository.exists(key) == false) {
				try {
					mutex.wait(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
