package com.github.writeback.client;

import java.util.ArrayList;
import java.util.List;

class KeyBasedMutexProvider {
	private List<Object> mutexes = new ArrayList<Object>();
	private int lockCount;

	KeyBasedMutexProvider(int lockCount) {
		this.lockCount = lockCount;
		for (int i = 0; i < lockCount; i++) {
			mutexes.add(new Object());
		}
	}

	Object get(Object key) {
		return mutexes.get(key.hashCode() % lockCount);
	}
}
