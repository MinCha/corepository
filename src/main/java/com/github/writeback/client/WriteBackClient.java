package com.github.writeback.client;


public class WriteBackClient {
	static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;
	static final Object mutex = new Object();
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private PeriodicWriteBack periodicWriteBack;

	public WriteBackClient(CoRepository coRepository, OriginalRepository originalRepository, long writeBackPeriodInMillis) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		periodicWriteBack = new PeriodicWriteBack(coRepository, originalRepository, writeBackPeriodInMillis);
		periodicWriteBack.start();		
	}

	public WriteBackClient(CoRepository coRepository, OriginalRepository originalRepository) {
		this(coRepository, originalRepository, DEFAULT_WRITEBACK_PERIOD_INMILLIS);
	}

	public WriteBackItem select(String key) {
		saveInitialValueFromOriginalRepositoryIfThereIsNo(key);
		return coRepository.select(key);
	}

	public void update(WriteBackItem item) {
		saveInitialValueFromOriginalRepositoryIfThereIsNo(item.getKey());
		coRepository.update(item);
	}

	public void increase(String key) {
		saveInitialValueFromOriginalRepositoryIfThereIsNo(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		saveInitialValueFromOriginalRepositoryIfThereIsNo(key);
		coRepository.decrease(key);
	}

	private void saveInitialValueFromOriginalRepositoryIfThereIsNo(String key) {
		if (coRepository.exists(key)) {
			return;
		}

		if (coRepository.lock(key) == false) {
			try {
				synchronized (mutex) {
					mutex.wait();					
				}
			} catch (InterruptedException e) {
			}
			return;
		}

		coRepository.insert(originalRepository.read(key));
		coRepository.unlock(key);

		synchronized (mutex) {
			mutex.notifyAll();			
		}
	}
}
