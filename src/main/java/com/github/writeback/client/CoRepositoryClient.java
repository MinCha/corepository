package com.github.writeback.client;


public class CoRepositoryClient {
	private CoRepository coRepository;
	private InitialValuePuller puller;

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this.coRepository = coRepository;
		this.puller = new InitialValuePuller(coRepository, originalRepository);
	}

	public Item selectAsString(String key) {
		puller.encurePulled(key);
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
		puller.encurePulled(item.getKey());
		coRepository.update(item);
	}

	public void increase(String key) {
		puller.encurePulled(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		puller.encurePulled(key);
		coRepository.decrease(key);
	}
}
