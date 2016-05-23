package com.liferoles.exceptions;

public class TokenValidationException extends Exception {
	public TokenValidationException(String message) {
		super(message);
	}

	public TokenValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenValidationException(Throwable cause) {
		super(cause);
	}
}
