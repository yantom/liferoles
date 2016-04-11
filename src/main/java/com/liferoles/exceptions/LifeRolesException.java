package com.liferoles.exceptions;

public class LifeRolesException extends Exception {
	
	public LifeRolesException(String message) {
        super(message);
    }

    public LifeRolesException(Throwable cause) {
        super(cause);
    }
 
    public LifeRolesException(String message, Throwable cause) {
        super(message, cause);
    }
}
