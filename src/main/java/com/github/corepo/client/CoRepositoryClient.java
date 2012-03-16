package com.github.corepo.client;


public class CoRepositoryClient {
	private CoRepository coRepository;
	private InitialValuePuller puller;

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this.coRepository = coRepository;
		this.puller = new InitialValuePuller(coRepository, originalRepository);
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
	}

	public void increase(String key) {
		puller.ensurePulled(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		puller.ensurePulled(key);
		coRepository.decrease(key);
	}
}
