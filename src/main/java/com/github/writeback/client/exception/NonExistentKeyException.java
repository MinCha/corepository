package com.github.writeback.client.exception;

@SuppressWarnings("serial")
public class NonExistentKeyException extends RuntimeException {
	public NonExistentKeyException(String reason) {
		super("Key does not exist : " + reason);
	}
}
