package com.github.writeback.client.exception;

@SuppressWarnings("serial")
public class NonexistentKeyException extends RuntimeException {
	public NonexistentKeyException(String reason) {
		super(reason);
	}
}
