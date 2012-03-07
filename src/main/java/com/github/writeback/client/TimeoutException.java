package com.github.writeback.client;

@SuppressWarnings("serial")
public class TimeoutException extends RuntimeException {
	public TimeoutException(String key) {
		super("Timeout : " + key);
	}
}
