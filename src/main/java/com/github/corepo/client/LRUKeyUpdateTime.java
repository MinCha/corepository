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
    private Cache<ItemKey, UpdateTime> lastUpdated;
    private Cache<ItemKey, UpdateTime> lastWritebacked;

    LRUKeyUpdateTime(RemovalListener<ItemKey, UpdateTime> removalListener) {
        this(removalListener, DEFAULT_SIZE);
    }

    LRUKeyUpdateTime(RemovalListener<ItemKey, UpdateTime> removalListener,
                     long size) {
        this.lastUpdated = CacheBuilder.newBuilder().maximumSize(size)
                .removalListener(removalListener).build();
        this.lastWritebacked = CacheBuilder.newBuilder().maximumSize(size)
                .build();
    }

    void notifyUpdated(ItemKey key, long updatedTime) {
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

    void notifyWritebacked(ItemKey key, long writebackedTime) {
        lastWritebacked.put(key, new UpdateTime(writebackedTime));
    }

    boolean isUpdated(ItemKey key) {
        return lastUpdated.getIfPresent(key) != null;
    }

    boolean isWritebacked(ItemKey key) {
        return lastWritebacked.getIfPresent(key) != null;
    }

    boolean exists(ItemKey key) {
        return isUpdated(key);
    }

    void applyToKeysOverThan(long timeInMillis, KeyFunction function,
                             boolean logAsWritebacked) {
        for (ItemKey key : lastUpdated.asMap().keySet()) {
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

    Set<ItemKey> findAllKeys() {
        if (lastUpdated == null) {
            return new HashSet<ItemKey>();
        }

        return lastUpdated.asMap().keySet();
    }
}
