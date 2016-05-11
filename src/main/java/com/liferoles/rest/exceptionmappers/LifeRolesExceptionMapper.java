package com.liferoles.rest.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.liferoles.exceptions.LifeRolesException;

@Provider
public class LifeRolesExceptionMapper implements ExceptionMapper<LifeRolesException> 
{
	@Override
	public Response toResponse(LifeRolesException exception) {
		exception.printStackTrace();
		return Response.status(500).build();
	}
}
