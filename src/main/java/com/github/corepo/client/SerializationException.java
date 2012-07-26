package com.github.corepo.client;

@SuppressWarnings("serial")
public class SerializationException extends RuntimeException {
	public SerializationException(String value, Exception cause) {
		super(value, cause);
	}
}
