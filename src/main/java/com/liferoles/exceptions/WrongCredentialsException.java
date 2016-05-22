package com.liferoles.exceptions;

public class WrongCredentialsException extends Exception {
	public WrongCredentialsException(String message) {
        super(message);
    }

    public WrongCredentialsException(Throwable cause) {
        super(cause);
    }
 
    public WrongCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

