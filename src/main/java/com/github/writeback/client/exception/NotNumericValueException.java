package com.github.writeback.client.exception;

@SuppressWarnings("serial")
public class NotNumericValueException extends RuntimeException {
	public NotNumericValueException(String reason) {
		super(reason);
	}
}
