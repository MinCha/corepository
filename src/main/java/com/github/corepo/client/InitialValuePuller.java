package com.github.corepo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.simplelrucache.ConcurrentLruCache;
import com.google.code.simplelrucache.LruCache;

public class InitialValuePuller {
	private final static Logger LOG = LoggerFactory.getLogger(InitialValuePuller.class);
	private LruCache<String, Object> keyCache;
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private long timeoutInMillis = 1000;

	public InitialValuePuller(CoRepository coRepository,
			OriginalRepository originalRepository,
			LruCache<String, Object> keyCache) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
		this.keyCache = keyCache;
	}

	public InitialValuePuller(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository,
				new ConcurrentLruCache<String, Object>(1000, Long.MAX_VALUE));
	}

	public void ensurePulled(String key) {
		if (keyCache.contains(key)) {
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
		
		keyCache.put(key, new Object());
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
