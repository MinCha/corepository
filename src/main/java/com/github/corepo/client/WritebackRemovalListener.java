package com.github.corepo.client;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class WritebackRemovalListener implements RemovalListener<String, Long> {
	private CoRepository coRepository;
	private OriginalRepository originalRepository;

	public WritebackRemovalListener(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
	}

	public void onRemoval(RemovalNotification<String, Long> notification) {
		final String key = getKey(notification);
		if (coRepository.isInt(getKey(notification))) {
			originalRepository.writeback(coRepository.selectAsInt(key));
		} else {
			originalRepository.writeback(coRepository.selectAsObject(key));
		}
	}

	String getKey(RemovalNotification<String, Long> notification) {
		return notification.getKey();
	}
}
