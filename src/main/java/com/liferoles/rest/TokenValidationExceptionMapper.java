package com.liferoles.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.liferoles.exceptions.TokenValidationException;

@Provider
public class TokenValidationExceptionMapper implements ExceptionMapper<TokenValidationException> 
{
	@Override
	public Response toResponse(TokenValidationException exception) {
		exception.printStackTrace();
		return Response.status(403).build();
	}
}