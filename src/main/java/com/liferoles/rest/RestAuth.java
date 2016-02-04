package com.liferoles.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liferoles.controller.PasswordResetManager;
import com.liferoles.controller.UserManager;
import com.liferoles.model.LifeRolesDBException;
import com.liferoles.rest.JSON.BooleanResponse;

@Path("/rest/auth")
public class RestAuth {
	private static final PasswordResetManager pm = new PasswordResetManager();
	
	@POST
    @Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse sendResetLink(@QueryParam("userMail") String email, @PathParam("userId") Long userId){
		try{
			pm.sendResetLink(email,userId);
		}catch (LifeRolesDBException e) {
			e.printStackTrace();
			return new BooleanResponse(false);
		}
		return new BooleanResponse(true);
	}
}
