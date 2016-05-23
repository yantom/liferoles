package com.liferoles.rest.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.liferoles.exceptions.LiferolesRuntimeException;

@Provider
public class LiferolesRuntimeExceptionMapper implements ExceptionMapper<LiferolesRuntimeException> {
	@Override
	public Response toResponse(LiferolesRuntimeException exception) {
		exception.printStackTrace();
		return Response.status(500).build();
	}
}
