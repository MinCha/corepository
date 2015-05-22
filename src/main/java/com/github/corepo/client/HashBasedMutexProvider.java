package com.github.corepo.client;

import java.util.ArrayList;
import java.util.List;

class HashBasedMutexProvider {
    private static final int DEFAULT_DISPERSION = 1000;
    private List<Object> mutexes = new ArrayList<Object>();
    private int dispersion;

    HashBasedMutexProvider() {
        this(DEFAULT_DISPERSION);
    }

    HashBasedMutexProvider(int dispersion) {
        this.dispersion = dispersion;
        for (int i = 0; i < dispersion; i++) {
            mutexes.add(new Object());
        }
    }

    Object get(Object key) {
        return mutexes.get(Math.abs(key.hashCode() % dispersion));
    }
}
