package com.github.corepo.client;

public class CoRepositoryClient implements Runnable {
	private CoRepository coRepository;
	private LRUKeyUpdateTime keyUpdateTime;
	private InitialValuePuller puller;
	private TimeBasedWriteback timeBasedWriteback;
	private Unlocker unlocker;

	CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository,
			LRUKeyUpdateTime keyUpdateTime, int writebackPeriodInMillis) {
		this.coRepository = coRepository;
		this.keyUpdateTime = keyUpdateTime;
		this.unlocker = new Unlocker(coRepository);
		this.puller = new InitialValuePuller(coRepository, originalRepository,
				keyUpdateTime, unlocker);
		timeBasedWriteback = new TimeBasedWriteback(keyUpdateTime,
				originalRepository, coRepository, writebackPeriodInMillis);

		Runtime.getRuntime().addShutdownHook(new Thread(this));
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
		puller.ensurePulled(key);
		Item result = coRepository.selectAsInt(key);
		return result;
	}

	public void update(Item item) {
		puller.ensurePulled(item.getKey());
		coRepository.update(item);
		keyUpdateTime.notifyUpdated(item.getKey(), System.currentTimeMillis());
	}

	public int increase(String key) {
		puller.ensurePulled(key);
		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
		return coRepository.increase(key);
	}

	public int decrease(String key) {
		puller.ensurePulled(key);
		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
		return coRepository.decrease(key);
	}

	public void close() {
		unlocker.stop();
		timeBasedWriteback.stop();
		keyUpdateTime.clear();
		coRepository.close();
	}

	public void run() {
		close();
	}
}
