package com.github.corepo.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

class LRUKeyUpdateTime {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory
            .getLogger(LRUKeyUpdateTime.class);
    private final static long DEFAULT_SIZE = 10000;
    private Cache<String, UpdateTime> lastUpdated;
    private Cache<String, UpdateTime> lastWritebacked;

    LRUKeyUpdateTime(RemovalListener<String, UpdateTime> removalListener) {
        this(removalListener, DEFAULT_SIZE);
    }

    LRUKeyUpdateTime(RemovalListener<String, UpdateTime> removalListener,
                     long size) {
        this.lastUpdated = CacheBuilder.newBuilder().maximumSize(size)
                .removalListener(removalListener).build();
        this.lastWritebacked = CacheBuilder.newBuilder().maximumSize(size)
                .build();
    }

    void notifyUpdated(String key, long updatedTime) {
        UpdateTime old = lastUpdated.getIfPresent(key);

        if (old == null) {
            lastUpdated.put(key, new UpdateTime(updatedTime));
        } else {
            old.update(updatedTime);
        }

        if (lastWritebacked.getIfPresent(key) == null) {
            final long extra = 100;
            lastWritebacked.put(key, new UpdateTime(updatedTime - extra));
        }
    }

    void notifyWritebacked(String key, long writebackedTime) {
        lastWritebacked.put(key, new UpdateTime(writebackedTime));
    }

    boolean isUpdated(String key) {
        return lastUpdated.getIfPresent(key) != null;
    }

    boolean isWritebacked(String key) {
        return lastWritebacked.getIfPresent(key) != null;
    }

    boolean exists(String key) {
        return isUpdated(key);
    }

    void applyToKeysOverThan(long timeInMillis, KeyFunction function,
                             boolean logAsWritebacked) {
        for (String key : lastUpdated.asMap().keySet()) {
            long current = System.currentTimeMillis();
            UpdateTime writebackedTime = lastWritebacked.getIfPresent(key);
            UpdateTime updateedTime = lastUpdated.getIfPresent(key);

            if (writebackedTime == null) {
                continue;
            }

            if (current - timeInMillis > writebackedTime.time()
                    && updateedTime.time() > writebackedTime.time()) {
                function.execute(key);

                if (logAsWritebacked) {
                    notifyWritebacked(key, System.currentTimeMillis());
                }
            }
        }
    }

    Set<String> findAllKeys() {
        if (lastUpdated == null) {
            return new HashSet<String>();
        }

        return lastUpdated.asMap().keySet();
    }
}
