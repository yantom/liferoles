package com.liferoles.rest.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.liferoles.exceptions.PossibleDataInconsistencyException;

@Provider
public class PossibleDataInconsistencyExceptionMapper implements ExceptionMapper<PossibleDataInconsistencyException> {
	@Override
	public Response toResponse(PossibleDataInconsistencyException exception) {
		exception.printStackTrace();
		return Response.status(409).build();
	}
}
