package com.github.corepo.client;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class WritebackRemovalListener implements
		RemovalListener<String, UpdateTime> {
	private CoRepository coRepository;
	private OriginalRepository originalRepository;

	public WritebackRemovalListener(CoRepository coRepository,
			OriginalRepository originalRepository) {
		this.coRepository = coRepository;
		this.originalRepository = originalRepository;
	}

	public void onRemoval(RemovalNotification<String, UpdateTime> notification) {
		final String key = getKey(notification);

		if (coRepository.exists(key) == false) {
			return;
		}

		if (coRepository.isInt(getKey(notification))) {
			originalRepository.writeback(coRepository.selectAsInt(key));
		} else {
			originalRepository.writeback(coRepository.selectAsObject(key));
		}
	}

	String getKey(RemovalNotification<String, UpdateTime> notification) {
		return notification.getKey();
	}
}
