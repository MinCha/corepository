package com.github.corepo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialValuePuller {
	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory
			.getLogger(InitialValuePuller.class);
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
	private LRUKeyUpdateTime keyUpdateTime;
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private long timeoutInMillis = 1000;
	private Unlocker unlocker;

	public InitialValuePuller(CoRepository coRepository,
			OriginalRepository originalRepository,
			LRUKeyUpdateTime keyUpdateTime) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		this.keyUpdateTime = keyUpdateTime;
		this.unlocker = new Unlocker(coRepository);
		unlocker.active();
	}

	public void ensurePulled(String key) {
		if (keyUpdateTime.exists(key)) {
			return;
		}

		if (coRepository.exists(key)) {
			return;
		}

		if (coRepository.lock(key) == false) {
			waitUntilInitialValueIsPulled(key);
			return;
		}

		Item item = originalRepository.read(key);
		if (item.isNotFound()) {
			unlocker.requestUnlock(key);
			wakeUpAllThreadsWatingForCompletingPull(key);
			throw new NonExistentKeyException(key);
		}

		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
		coRepository.insert(item);
		unlocker.requestUnlock(key);
		wakeUpAllThreadsWatingForCompletingPull(key);
	}

	private void wakeUpAllThreadsWatingForCompletingPull(Object key) {
		synchronized (mutex.get(key)) {
			mutex.get(key).notifyAll();
		}
	}

	private void waitUntilInitialValueIsPulled(String key) {
		int waitingTime = 0;
		synchronized (mutex.get(key)) {
			while (coRepository.exists(key) == false) {
				try {
					waitingTime += 100;

					if (waitingTime > timeoutInMillis) {
						throw new TimeoutException(
								"Waited so long or key does`t exist on OriginalRepository. Key is "
										+ key);
					}

					mutex.get(key).wait(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
