package com.liferoles.rest;

import java.util.List;

import javax.ejb.EJB;
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

import com.liferoles.controller.AuthManager;
import com.liferoles.controller.RoleManager;
import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.Role;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.objects.IdResponse;

@Path("/rest/roles")
public class RestRole {
	@EJB
	private RoleManager rm;
	@EJB
	private AuthManager am;
	@GET
	@Path("/web")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Role> getAllRoles(@Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	return rm.getAllRoles(userId);
    }
	
	@GET
	@Path("/m")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Role> getAllRolesMobile(@Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		return rm.getAllRoles(userId);
    }
	
	@POST
	@Path("/web")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createRole(Role role, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		role.setUser(u);
		IdResponse id = new IdResponse();
		id.setId(rm.createRole(role));
		return id;
    }
	
	@POST
	@Path("/m")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createRoleMobile(Role role, @Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		role.setUser(u);
		IdResponse id = new IdResponse();
		id.setId(rm.createRole(role));
		return id;
    }
	
	@PUT
	@Path("/web/{roleId}")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateRole(@PathParam("roleId") Long roleId,Role role, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		role.setId(roleId);
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		role.setUser(u);
		rm.updateRole(role);
    }

	@PUT
	@Path("/m/{roleId}")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateRoleMobile(@PathParam("roleId") Long roleId, Role role, @Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException{
		role.setId(roleId);
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		role.setUser(u);
		rm.updateRole(role);
    }
	
	@DELETE
	@Path("/web/{roleId}")
    public void deleteRole(@PathParam("roleId") Long roleId,@QueryParam("newRoleId") Long newRoleId, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		Role deletedRole = new Role();
		deletedRole.setId(roleId);
		deletedRole.setUser(u);
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
	@DELETE
	@Path("/m/{roleId}")
    public void deleteRoleMobile(@PathParam("roleId") Long roleId,@QueryParam("newRoleId") Long newRoleId, @Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		Role deletedRole = new Role();
		deletedRole.setId(roleId);
		deletedRole.setUser(u);
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
}
