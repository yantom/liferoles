package com.liferoles.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.HibernateException;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;

//used for exceptions which are thrown mainly because of data inconsistency
@Provider
public class HibernateExceptionMapper implements ExceptionMapper<HibernateException> 
{
	@Override
	public Response toResponse(HibernateException exception) {
		exception.printStackTrace();
		if (exception instanceof StaleStateException) {
			return Response.status(409).build();
        }
		return Response.status(500).build();
	}
}