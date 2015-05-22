package com.github.corepo.client;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class KeyRemovalListener implements
        RemovalListener<ItemKey, UpdateTime> {
    private Writeback writeback;

    KeyRemovalListener(Writeback writeback) {
        this.writeback = writeback;
    }

    public void onRemoval(RemovalNotification<ItemKey, UpdateTime> notification) {
        final ItemKey key = getKey(notification);
        writeback.execute(key);
    }

    ItemKey getKey(RemovalNotification<ItemKey, UpdateTime> notification) {
        return notification.getKey();
    }
}
