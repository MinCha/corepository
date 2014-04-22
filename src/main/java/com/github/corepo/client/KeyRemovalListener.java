package com.github.corepo.client;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class KeyRemovalListener implements
	RemovalListener<String, UpdateTime> {
    private Writeback writeback;

    KeyRemovalListener(Writeback writeback) {
	this.writeback = writeback;
    }

    public void onRemoval(RemovalNotification<String, UpdateTime> notification) {
	final String key = getKey(notification);
	writeback.writeback(key);
    }

    String getKey(RemovalNotification<String, UpdateTime> notification) {
	return notification.getKey();
    }
}
