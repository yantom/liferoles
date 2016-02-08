package com.liferoles.rest;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.liferoles.LifeRolesDBException;
import com.liferoles.LifeRolesException;
import com.liferoles.controller.UserManager;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.IdResponse;

@Path("/rest")
public class RestUser {
	private static final UserManager um = new UserManager();
	
	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@Context HttpServletRequest hsr){
		User u = null;
		HttpSession session = hsr.getSession();
		try{
			u = um.getUserByMail(hsr.getUserPrincipal().getName());
		}catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		session.setAttribute("userId", u.getId());
		return u;
	}
	
	@POST
    @Path("/reg")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createUser(User user) {
		IdResponse id = new IdResponse();
		try{
		id.setId(um.createUser(user));}
		catch (LifeRolesException | LifeRolesDBException e) {
			e.printStackTrace();
		}
		return id;
    }
	
	@POST
    @Path("/reset/{userMail}")
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse sendResetLink(@PathParam("userMail") String email){
		try{
			um.sendResetLink(email);
		}catch (LifeRolesException | LifeRolesDBException e) {
			e.printStackTrace();
			return new BooleanResponse(false);
		}
		return new BooleanResponse(true);
	}
	
	@GET
	@Path("/check/{userMail}")
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkIfUserExistsInDB(@PathParam("userMail") String userMail){
		try{
			if(um.getUserByMail(userMail) != null)
				return new BooleanResponse(true);
		}catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return new BooleanResponse(false);
	}
}
