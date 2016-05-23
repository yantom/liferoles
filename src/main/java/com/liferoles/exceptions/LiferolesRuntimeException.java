package com.liferoles.exceptions;

public class LiferolesRuntimeException extends RuntimeException {

	public LiferolesRuntimeException(String message) {
		super(message);
	}

	public LiferolesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LiferolesRuntimeException(Throwable cause) {
		super(cause);
	}
}
