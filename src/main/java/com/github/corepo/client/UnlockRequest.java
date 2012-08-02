package com.github.corepo.client;

public class UnlockRequest {
	private String key;
	private long requestedTime;
	private long timeInMillis;

	UnlockRequest(String key, long timeInMillis) {
		this.key = key;
		this.requestedTime = System.currentTimeMillis();
		this.timeInMillis = timeInMillis;
	}

	boolean isUnlockable() {
		return requestedTime + timeInMillis < System.currentTimeMillis();
	}

	String key() {
		return key;
	}
}
