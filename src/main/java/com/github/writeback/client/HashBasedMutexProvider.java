package com.github.writeback.client;

import java.util.ArrayList;
import java.util.List;

class HashBasedMutexProvider {
	private List<Object> mutexes = new ArrayList<Object>();
	private int dispersion;

	HashBasedMutexProvider(int dispersion) {
		this.dispersion = dispersion;
		for (int i = 0; i < dispersion; i++) {
			mutexes.add(new Object());
		}
	}

	Object get(Object key) {
		return mutexes.get(Math.abs(key.hashCode()) % dispersion);
	}
}
