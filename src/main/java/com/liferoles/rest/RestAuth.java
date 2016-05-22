package com.liferoles.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.controller.AuthManager;
import com.liferoles.controller.UserManager;
import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.objects.BooleanResponse;
import com.liferoles.rest.JSON.objects.IdResponse;
import com.liferoles.rest.JSON.objects.UserWithToken;

@Path("/rest/auth")
public class RestAuth {
	@EJB
	private UserManager um;
	@EJB
	private AuthManager am;
	private static final Logger logger = LoggerFactory.getLogger(AuthManager.class);
	
	@POST
    @Path("/m/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public UserWithToken loginJWT(User user) throws LiferolesRuntimeException {
		User dbUser;
		dbUser = am.authenticateMobileUser(user);
		if(dbUser == null)
			return null;
		String token = am.issueNewToken(dbUser.getId());
		return new UserWithToken(dbUser,token);
    }
	
	@POST
    @Path("/web/reg")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse registerUserWeb(User user, @QueryParam("captcha") String captcha) throws LiferolesRuntimeException {
		if(captcha == null || captcha.isEmpty()){
			logger.error("captcha token missing");
			throw new LiferolesRuntimeException("captcha token missing");
		}
		boolean captchaOK;
		try {
			captchaOK = resolveCaptcha(captcha);
		} catch (IOException e) {
			logger.error("captcha IO exception",e);
			throw new LiferolesRuntimeException(e);
		}
		if(captchaOK){
			IdResponse id = new IdResponse();
			id.setId(um.createUser(user));
			return id;
		}
		logger.error("bot trying to register");
		throw new LiferolesRuntimeException("BOT");
    }
	
	@POST
    @Path("/m/reg")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse registerUserMobile(User user) throws LiferolesRuntimeException {
		IdResponse id = new IdResponse();
		id.setId(um.createUser(user));
		return id;
    }
	
	@POST
    @Path("/getResetCode/{userMail}")
	public void sendResetLink(@PathParam("userMail") String email) throws LiferolesRuntimeException{
		um.sendResetLink(email);
	}
	
	@GET
	@Path("/check/{userMail}")
	@Produces(MediaType.APPLICATION_JSON)
	public BooleanResponse checkIfUserExistsInDB(@PathParam("userMail") String userMail) throws LiferolesRuntimeException{
		if(um.getUserByMail(userMail) != null)
			return new BooleanResponse(true);
		return new BooleanResponse(false);
	}
	
	@POST
	@Path("/reset")
	@Consumes(MediaType.APPLICATION_JSON)
	public void resetUserPassword(UserWithToken u) throws TokenValidationException, LiferolesRuntimeException{
		am.useResetToken(u.getToken(), u.getUser());
		um.updateUserPassword(u.getUser());
	}
	
	private boolean resolveCaptcha(String captcha) throws IOException{
		String url = "https://www.google.com/recaptcha/api/siteverify";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		//server - 6LflgxsTAAAAAEn24PDzG9ntLPhzkgAC7GuHpE9O
		//localhost - 6LefrxkTAAAAAN_qGCsykWxPIaAVoNWcmrfhKw7O
		String urlParameters = "secret=6LflgxsTAAAAAEn24PDzG9ntLPhzkgAC7GuHpE9O&response="+captcha;
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response);
		return Pattern.matches(".+\\: true.+", response);
	}
}
