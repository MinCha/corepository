package com.github.writeback.client;

public class WriteBackClient {
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private PeriodicWriteBack periodicWriteBack;
	
	public WriteBackClient(CoRepository coRepository, OriginalRepository originalRepository) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		periodicWriteBack = new PeriodicWriteBack(coRepository, originalRepository);
		periodicWriteBack.start();
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
