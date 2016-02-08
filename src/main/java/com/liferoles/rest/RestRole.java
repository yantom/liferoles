package com.liferoles.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.liferoles.LifeRolesDBException;
import com.liferoles.controller.RoleManager;
import com.liferoles.model.Role;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.IdResponse;

@Path("/rest/roles")
public class RestRole {
	private static final RoleManager rm = new RoleManager();
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Role> getAllRoles(@Context HttpServletRequest hsr) {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	List<Role> r = null;
		try {
			r = rm.getAllRoles(userId);
		} catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
    	return r;
    }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createRole(Role role, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		role.setUser(u);
		IdResponse id = new IdResponse();
		try{
		id.setId(rm.createRole(role));}
		catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return id;
    }
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
    public BooleanResponse updateRole(Role role, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		role.setUser(u);
		try{
			rm.updateRole(role);}
			catch (LifeRolesDBException e) {
				e.printStackTrace();
				return new BooleanResponse(false);
			}
		return new BooleanResponse(true);
    }
	
	@DELETE
    public BooleanResponse deleteRole(@QueryParam("roleId") Long roleId,@QueryParam("newRoleId") Long newRoleId, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		Role deletedRole = new Role();
		deletedRole.setId(roleId);
		deletedRole.setUser(u);
		try{

			if(newRoleId == null){
				rm.deleteRole(deletedRole);
			}
			else{
				Role newRole = new Role();
				newRole.setId(newRoleId);
				newRole.setUser(u);
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
