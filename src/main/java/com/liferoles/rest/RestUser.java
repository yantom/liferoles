package com.liferoles.rest;

import java.time.LocalDate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.liferoles.controller.UserManager;
import com.liferoles.exceptions.LifeRolesAuthException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.ChartsData;
import com.liferoles.utils.AuthUtils;

@Path("/rest/users")
public class RestUser {
	private static final UserManager um = new UserManager();
	
	@GET
	@Path("/m")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUserJWT(@Context HttpServletRequest hsr) throws TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
		User u = um.getUserById(userId);
		return u;
	}
	
	@GET
	@Path("/web")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@Context HttpServletRequest hsr){
		User u;
		HttpSession session = hsr.getSession();
		u = um.getUserByMail(hsr.getUserPrincipal().getName());
		session.setAttribute("userId", u.getId());
		return u;
	}
	
	@GET
	@Path("/web/stats/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public ChartsData getStats(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean lastMonth,@Context HttpServletRequest hsr){
		ChartsData chartsData= null;
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		if(lastMonth == true)
			chartsData = um.getMonthStatistics(year, month, userId,true);
		else
			chartsData = um.getMonthStatistics(year, month, userId,false);
		return chartsData;
	}
	
	@GET
	@Path("/m/stats/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public ChartsData getStatsJWT(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean lastMonth,@Context HttpServletRequest hsr) throws TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
		ChartsData chartsData= null;
		if(lastMonth == true)
			chartsData = um.getMonthStatistics(year, month, userId,true);
		else
			chartsData = um.getMonthStatistics(year, month, userId,false);
		return chartsData;
	}
	
	
	@PUT
	@Path("/web/blocktokens")
	public void blockUsersTokens(@Context HttpServletRequest hsr){
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		AuthUtils.addTokensToBlacklist(userId);
	}
	
	
	@GET
	@Path("/web/logout")
	public void logoutUser(@Context HttpServletRequest hsr){
		hsr.getSession().invalidate();
	}
	
	@PUT
	@Path("/m")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUser(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException, TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
		u.setId(userId);
		if(u.getPassword() != null){
			um.updateUserPassword(u);
			return;
		}
		if(u.getEmail() != null){
			um.updateUserEmail(u);
			return;
		}
		um.updateUserData(u);
		
	}
	
	@PUT
	@Path("/web")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserMobile(User u,@Context HttpServletRequest hsr) throws  LifeRolesAuthException{
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		u.setId(userId);
		if(u.getPassword() != null){
			um.updateUserPassword(u);
			return;
		}
		if(u.getEmail() != null){
			um.updateUserEmail(u);
			return;
		}
		um.updateUserData(u);
	}
	
	@POST
	@Path("/web/checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkPassword(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException{
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		return new BooleanResponse(AuthUtils.checkPassword(u.getPassword(), userId));
	}
	
	@POST
	@Path("/m/checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkPasswordMobile(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException, TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
		return new BooleanResponse(AuthUtils.checkPassword(u.getPassword(), userId));
	}
	
	@PUT
	@Path("/m/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklogMobile(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr) throws TokenValidationException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
		um.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
	
	@PUT
	@Path("/web/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklog(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr){
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		um.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
}