package com.github.corepo.client;

public class CoRepositoryClient {
	private CoRepository coRepository;
	private LRUKeyUpdateTime keyUpdateTime;
	private InitialValuePuller puller;

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository, LRUKeyUpdateTime keyUpdateTime) {
		this.coRepository = coRepository;
		this.keyUpdateTime = keyUpdateTime;		
		this.puller = new InitialValuePuller(coRepository, originalRepository,
				keyUpdateTime);
	}

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository, new LRUKeyUpdateTime(new WritebackRemovalListner(
				coRepository, originalRepository)));
	}

	public Item selectAsString(String key) {
		puller.ensurePulled(key);
		return coRepository.selectAsString(key);
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
