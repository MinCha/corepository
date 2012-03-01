package com.github.writeback.client;

public class WriteBackClient {
	private static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private PeriodicWriteBack periodicWriteBack;
	private KeyBasedMutexProvider mutex = new KeyBasedMutexProvider(1000);

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

			wakeUpAllThreadsWatingForPulling(key);
		} else {
			waitUnitlInitialValueIsPulled(key);
		}
	}

	private void wakeUpAllThreadsWatingForPulling(String key) {
		synchronized (mutex.get(key)) {
			mutex.get(key).notifyAll();
		}
	}

	private void waitUnitlInitialValueIsPulled(String key) {
		synchronized (mutex.get(key)) {
			while (coRepository.exists(key) == false) {
				try {
					mutex.get(key).wait(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
