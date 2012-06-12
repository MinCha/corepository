package com.github.corepo.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

public class LRUKeyUpdateTime {
	private final static long DEFAULT_SIZE = 100000;
	private Cache<String, Long> lastUpdated;
	private Cache<String, Long> lastWritebacked;

	public LRUKeyUpdateTime(RemovalListener<String, Long> removalListener) {
		this(removalListener, DEFAULT_SIZE);
	}

	public LRUKeyUpdateTime(RemovalListener<String, Long> removalListener, long size) {
		this.lastUpdated = CacheBuilder.newBuilder().maximumSize(size)
				.removalListener(removalListener).build();
		this.lastWritebacked = CacheBuilder.newBuilder().maximumSize(size).build();
	}

	public void notifyUpdated(String key, long updatedTime) {
		lastUpdated.put(key, updatedTime);
		
		if (lastWritebacked.getIfPresent(key) == null) {
			lastWritebacked.put(key, updatedTime);
		}
	}

	public void notifyWritebacked(String key, long writebackedTime) {
		lastWritebacked.put(key, writebackedTime);
	}

	public boolean isUpdated(String key) {
		return lastUpdated.getIfPresent(key) != null;
	}

	public boolean isWritebacked(String key) {
		return lastWritebacked.getIfPresent(key) != null;
	}
	
	public boolean exists(String key) {
		return isUpdated(key);
	}

	public List<String> findKeysOverThan(long timeInMillis) {
		List<String> result = new ArrayList<String>();
		for (String key : lastUpdated.asMap().keySet()) {
			long current = System.currentTimeMillis();
			Long time = lastWritebacked.getIfPresent(key);
			if (current - timeInMillis > time) {
				result.add(key);
			}
		}
		return result;
	}
}
