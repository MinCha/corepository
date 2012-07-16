package com.github.corepo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialValuePuller {
	private final static Logger LOG = LoggerFactory.getLogger(InitialValuePuller.class);
	//private LruCache<String, Object> keyCache;
	private LRUKeyUpdateTime keyUpdateTime;
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private long timeoutInMillis = 1000;

	public InitialValuePuller(CoRepository coRepository,
			OriginalRepository originalRepository,
			LRUKeyUpdateTime keyUpdateTime) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		this.keyUpdateTime = keyUpdateTime;
	}

	public void ensurePulled(String key) {
		if (keyUpdateTime.exists(key)) {
			return;
		}

		if (coRepository.exist(key)) {
			return;
		}

		if (coRepository.lock(key) == false) {
			waitUntilInitialValueIsPulled(key);
			return;
		}
		LOG.info("Lock acquired : " + key);
		Item item = originalRepository.read(key);
		if (item.isNotFound()) {
			coRepository.unlock(key);
			wakeUpAllThreadsWatingForCompletingPull(key);
			throw new NonExistentKeyException(key);
		} 
		
		keyUpdateTime.notifyUpdated(key, System.currentTimeMillis());
		coRepository.insert(item);
		coRepository.unlock(key);
		wakeUpAllThreadsWatingForCompletingPull(key);
	}

	private void wakeUpAllThreadsWatingForCompletingPull(Object key) {
		synchronized (mutex.get(key)) {
			mutex.get(key).notifyAll();
		}
	}

	private void waitUntilInitialValueIsPulled(String key) {
		int wastedTime = 0;
		synchronized (mutex.get(key)) {
			while (coRepository.exist(key) == false) {
				try {
					mutex.get(key).wait(100);
					wastedTime += 100;

					if (wastedTime > timeoutInMillis) {
						throw new TimeoutException(key);
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
