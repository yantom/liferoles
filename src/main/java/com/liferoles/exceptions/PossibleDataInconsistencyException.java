package com.liferoles.exceptions;

public class PossibleDataInconsistencyException extends RuntimeException {
	public PossibleDataInconsistencyException(String message) {
        super(message);
    }

    public PossibleDataInconsistencyException(Throwable cause) {
        super(cause);
    }
 
    public PossibleDataInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
