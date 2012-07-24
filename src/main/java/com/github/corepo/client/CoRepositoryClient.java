package com.github.corepo.client;

public class CoRepositoryClient {
	private CoRepository coRepository;
	private LRUKeyUpdateTime keyUpdateTime;
	private InitialValuePuller puller;
	private TimeBasedWriteback timeBasedWriteback;

	CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository,
			LRUKeyUpdateTime keyUpdateTime, int writebackPeriodInMillis) {
		this.coRepository = coRepository;
		this.keyUpdateTime = keyUpdateTime;
		this.puller = new InitialValuePuller(coRepository, originalRepository,
				keyUpdateTime);
		timeBasedWriteback = new TimeBasedWriteback(keyUpdateTime,
				originalRepository, coRepository, writebackPeriodInMillis);
		timeBasedWriteback.start();
	}

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository, int writebackPeriodInMillis) {
		this(coRepository, originalRepository,
				new LRUKeyUpdateTime(new WritebackRemovalListener(coRepository,
						originalRepository)), writebackPeriodInMillis);
	}

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository, 1000 * 60 * 5);
	}

	public Item selectAsObject(String key) {
		puller.ensurePulled(key);
		return coRepository.selectAsObject(key);
	}

	public Item selectAsInt(String key) {
		Item result = coRepository.selectAsInt(key);

		if (result.isNotFound()) {
			throw new NonExistentKeyException(key);
		}

		return result;
	}

	public void update(Item item) {
		puller.ensurePulled(item.getKey());
		coRepository.update(item);
		keyUpdateTime.notifyUpdated(item.getKey(), System.currentTimeMillis());
	}

	public void increase(String key) {
		puller.ensurePulled(key);
		coRepository.increase(key);
		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
	}

	public void decrease(String key) {
		puller.ensurePulled(key);
		coRepository.decrease(key);
		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
	}
}
