package com.liferoles.exceptions;

public class LifeRolesAuthException extends LifeRolesException {
	public LifeRolesAuthException(String message) {
        super(message);
    }

    public LifeRolesAuthException(Throwable cause) {
        super(cause);
    }
 
    public LifeRolesAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
