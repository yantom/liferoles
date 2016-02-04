package com.liferoles.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.liferoles.controller.PasswordResetManager;
import com.liferoles.controller.UserManager;
import com.liferoles.model.LifeRolesDBException;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.IdResponse;

@Path("/rest/users")
public class RestUser {
	private static final UserManager um = new UserManager();

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@QueryParam("userId") Long userId, @QueryParam("userMail") String userMail){
		User u = null;
		try{
			if(userId != null)
				u = um.getUserById(userId);
			else if(userMail != null)
				u = um.getUserByMail(userMail);
		}catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return u;
	}
	
	@POST
    @Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createUser(User user) {
		IdResponse id = new IdResponse();
		try{
		id.setId(um.createUser(user));}
		catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return id;
    }
}
