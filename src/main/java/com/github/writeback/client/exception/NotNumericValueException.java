package com.github.writeback.client.exception;

@SuppressWarnings("serial")
public class NotNumericValueException extends RuntimeException {
	public NotNumericValueException() {
		super();
	}
	
	public NotNumericValueException(String value, Exception cause) {
		super("Not numeric value " + value, cause);
	}
}
