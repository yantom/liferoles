package com.liferoles.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.liferoles.controller.RoleManager;
import com.liferoles.model.LifeRolesDBException;
import com.liferoles.model.Role;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.IdResponse;

@Path("/rest/roles")
public class RestRole {
	private static final RoleManager rm = new RoleManager();
	
	@GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Role> getAllRoles(@PathParam("userId") Long userId) {
    	List<Role> r = null;
		try {
			r = rm.getAllRoles(userId);
		} catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
    	return r;
    }
	
	@POST
    @Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createRole(Role role) {
		IdResponse id = new IdResponse();
		try{
		id.setId(rm.createRole(role));}
		catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return id;
    }
	
	@PUT
    @Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
    public BooleanResponse updateRole(Role role) {
		try{
			rm.updateRole(role);}
			catch (LifeRolesDBException e) {
				e.printStackTrace();
				return new BooleanResponse(false);
			}
		return new BooleanResponse(true);
    }
	
	@DELETE
    @Path("/")
    public BooleanResponse deleteRole(@QueryParam("roleId") Long roleId,@QueryParam("newRoleId") Long newRoleId) {
		Role deletedRole = new Role();
		deletedRole.setId(roleId);
		try{

			if(newRoleId == null){
				rm.deleteRole(deletedRole);
			}
			else{
				Role newRole = new Role();
				newRole.setId(newRoleId);
				rm.deleteRole(deletedRole, newRole);
			}
			}
			catch (LifeRolesDBException e) {
				e.printStackTrace();
				return new BooleanResponse(false);
			}
		return new BooleanResponse(true);
    }
}
