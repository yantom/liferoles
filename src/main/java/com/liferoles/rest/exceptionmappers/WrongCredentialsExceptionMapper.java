package com.liferoles.rest.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.liferoles.exceptions.WrongCredentialsException;


@Provider
public class WrongCredentialsExceptionMapper implements ExceptionMapper<WrongCredentialsException> {
	@Override
	public Response toResponse(WrongCredentialsException exception) {
		return Response.status(401).build();
	}
}
