package com.github.writeback.client.exception;

@SuppressWarnings("serial")
public class IllegalKeyException extends RuntimeException {
	public IllegalKeyException(String reason) {
		super(reason);
	}
}
