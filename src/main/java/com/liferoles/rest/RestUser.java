package com.liferoles.rest;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.liferoles.controller.AuthManager;
import com.liferoles.controller.UserManager;
import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.exceptions.WrongCredentialsException;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.objects.PasswordChange;

@Path("/rest/users")
public class RestUser {
	@EJB
	private UserManager um;
	@EJB
	private AuthManager am;

	@POST
	@Path("/web/tokensBlacklist")
	public void blockUsersTokens(@Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		am.addTokensToBlacklist(userId);
	}

	@GET
	@Path("/web")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		User u;
		HttpSession session = hsr.getSession();
		u = um.getUserByMail(hsr.getUserPrincipal().getName());
		session.setAttribute("userId", u.getId());
		return u;
	}

	@GET
	@Path("/m")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUserJWT(@Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		User u = um.getUserById(userId);
		return u;
	}

	@POST
	@Path("/web/logout")
	public void logoutUser(@Context HttpServletRequest hsr) throws ServletException {
		hsr.logout();
	}

	@POST
	@Path("/m/logout")
	public void logoutUserMobile(@Context HttpServletRequest hsr)
			throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		am.logoutMobileUser(token);
	}

	@POST
	@Path("/m/data")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserDataM(User u, @Context HttpServletRequest hsr)
			throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		u.setId(userId);
		um.updateUserData(u);
	}

	@POST
	@Path("/web/data")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserDataW(User u, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		u.setId(userId);
		um.updateUserData(u);
	}

	@POST
	@Path("/m/mail")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserEmailM(User u, @Context HttpServletRequest hsr)
			throws TokenValidationException, LiferolesRuntimeException, WrongCredentialsException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		u.setId(userId);
		if (am.checkPassword(u.getPassword(), u.getId()))
			um.updateUserEmail(u);
		else
			throw new WrongCredentialsException("wrong credentials");
	}

	@POST
	@Path("/web/mail")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserEmailW(User u, @Context HttpServletRequest hsr)
			throws LiferolesRuntimeException, WrongCredentialsException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		u.setId(userId);
		if (am.checkPassword(u.getPassword(), u.getId()))
			um.updateUserEmail(u);
		else
			throw new WrongCredentialsException("wrong credentials");
	}

	@POST
	@Path("/m/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserPasswordM(PasswordChange pc, @Context HttpServletRequest hsr)
			throws TokenValidationException, LiferolesRuntimeException, WrongCredentialsException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		User u = new User();
		u.setId(userId);
		u.setPassword(pc.getOldP());
		if (am.checkPassword(u.getPassword(), u.getId())) {
			u.setPassword(pc.getNewP());
			um.updateUserPassword(u);
		} else
			throw new WrongCredentialsException("wrong credentials");
	}

	@POST
	@Path("/web/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUserPasswordW(PasswordChange pc, @Context HttpServletRequest hsr)
			throws LiferolesRuntimeException, WrongCredentialsException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		User u = new User();
		u.setId(userId);
		u.setPassword(pc.getOldP());
		if (am.checkPassword(u.getPassword(), u.getId())) {
			u.setPassword(pc.getNewP());
			um.updateUserPassword(u);
		} else
			throw new WrongCredentialsException("wrong credentials");
	}
}