package com.liferoles.rest;

import java.time.LocalDate;

import javax.ejb.EJB;
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

import com.liferoles.controller.AuthManager;
import com.liferoles.controller.Nvd3StatsService;
import com.liferoles.controller.UserManager;
import com.liferoles.exceptions.LifeRolesAuthException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.objects.BooleanResponse;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3ChartsData;

@Path("/rest/users")
public class RestUser {
	@EJB
	private UserManager um;
	@EJB
	private AuthManager am;
	@EJB
	private Nvd3StatsService nvd3Service;
	
	@GET
	@Path("/m")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUserJWT(@Context HttpServletRequest hsr) throws TokenValidationException{
		for(int i=0;i<100;i++){
			System.out.println("what the fuck");
		}
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
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
	public Nvd3ChartsData getStats(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean currentMonth,@Context HttpServletRequest hsr){
		Nvd3ChartsData chartsData= null;
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		if(currentMonth == true)
			chartsData = nvd3Service.getJsonStatsData(year, month, true,userId);
		else
			chartsData = nvd3Service.getJsonStatsData(year, month, false,userId);
		return chartsData;
	}
	
	@GET
	@Path("/m/stats/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Nvd3ChartsData getStatsJWT(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean currentMonth,@Context HttpServletRequest hsr) throws TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		Nvd3ChartsData chartsData= null;
		if(currentMonth == true)
			chartsData = nvd3Service.getJsonStatsData(year, month, true,userId);
		else
			chartsData = nvd3Service.getJsonStatsData(year, month, false,userId);
		return chartsData;
	}
	
	
	@PUT
	@Path("/web/blocktokens")
	public void blockUsersTokens(@Context HttpServletRequest hsr){
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		am.addTokensToBlacklist(userId);
	}
	
	@GET
	@Path("/web/logout")
	public void logoutUser(@Context HttpServletRequest hsr){
		hsr.getSession().invalidate();
	}
	
	@GET
	@Path("/m/logout")
	public void logoutUserMobile(@Context HttpServletRequest hsr) throws TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		am.logoutMobileUser(token);
	}
	
	@PUT
	@Path("/m")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUser(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException, TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		u.setId(userId);
		if(u.getPassword() != null){
			um.updateUserPassword(u);
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
		um.updateUserData(u);
	}
	
	@POST
	@Path("/web/checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkPassword(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException{
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		return new BooleanResponse(am.checkPassword(u.getPassword(), userId));
	}
	
	@POST
	@Path("/m/checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkPasswordMobile(User u,@Context HttpServletRequest hsr) throws LifeRolesAuthException, TokenValidationException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		return new BooleanResponse(am.checkPassword(u.getPassword(), userId));
	}
	
	@PUT
	@Path("/m/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklogMobile(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr) throws TokenValidationException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		um.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
	
	@PUT
	@Path("/web/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklog(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr){
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		um.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
}