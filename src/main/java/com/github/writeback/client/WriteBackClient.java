package com.github.writeback.client;

public class WriteBackClient {
	static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;
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
		readFromOriginalRepositoryIfNeeds(key);
		return coRepository.select(key);
	}

	public void update(WriteBackItem item) {
		readFromOriginalRepositoryIfNeeds(item.getKey());
		coRepository.update(item);
	}

	public void increase(String key) {
		readFromOriginalRepositoryIfNeeds(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		readFromOriginalRepositoryIfNeeds(key);
		coRepository.decrease(key);
	}

	private void readFromOriginalRepositoryIfNeeds(String key) {
		if (coRepository.exists(key) == false) {
			coRepository.insert(originalRepository.read(key));
		}
	}

}
