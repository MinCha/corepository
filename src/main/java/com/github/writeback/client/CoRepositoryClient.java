package com.github.writeback.client;

import com.google.code.simplelrucache.ConcurrentLruCache;
import com.google.code.simplelrucache.LruCache;


public class CoRepositoryClient {
	private static final long DEFAULT_WRITEBACK_PERIOD_INMILLIS = 1000 * 60 * 5;
	private static final Object dummyValue = new Object();
	private CoRepository coRepository;
	private OriginalRepository originalRepository;
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
	private LruCache<String, Object> keys = new ConcurrentLruCache<String, Object>(1000, Long.MAX_VALUE);

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository, long writeBackPeriodInMillis) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
	}

	public CoRepositoryClient(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this(coRepository, originalRepository,
				DEFAULT_WRITEBACK_PERIOD_INMILLIS);
	}

	public Item selectAsString(String key) {
		pullInitialValueIfThereIsNo(key);
		return coRepository.selectAsString(key);
	}

	public Item selectAsInt(String key) {
		pullInitialValueIfThereIsNo(key);
		return coRepository.selectAsInt(key);
	}
	
	public void update(Item item) {
		pullInitialValueIfThereIsNo(item.getKey());
		coRepository.update(item);
	}

	public void increase(String key) {
		pullInitialValueIfThereIsNo(key);
		coRepository.increase(key);
	}

	public void decrease(String key) {
		pullInitialValueIfThereIsNo(key);
		coRepository.decrease(key);
	}

	private void pullInitialValueIfThereIsNo(String key) {
		if (keys.contains(key)) {
			return;
		} 
		
		if (coRepository.exists(key)) {
			return;
		}
		
		if (coRepository.lock(key)) {
			coRepository.insert(originalRepository.read(key));
			keys.put(key, dummyValue);
			coRepository.unlock(key);
			wakeUpAllThreadsWatingForCompletingPull(key);
		} else {
			waitUnitlInitialValueIsPulled(key);
		}
	}

	private void wakeUpAllThreadsWatingForCompletingPull(Object key) {
		synchronized (mutex.get(key)) {
			mutex.get(key).notifyAll();
		}
	}

	private void waitUnitlInitialValueIsPulled(String key) {
		synchronized (mutex.get(key)) {
			while (coRepository.exists(key) == false) {
				try {
					mutex.get(key).wait(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
