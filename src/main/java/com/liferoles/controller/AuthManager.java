package com.liferoles.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Stateless
@LocalBean
public class AuthManager {
	
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;
	
	@Resource
	protected SessionContext ctx; 
	
	private static final Logger logger = LoggerFactory.getLogger(AuthManager.class);
	
	public String issueNewToken(Long userId) throws LiferolesRuntimeException{
		Long jit = null;
		try{
			Query query = em.createNativeQuery("insert into tokens (appuser_id) values(:userId) returning jit");
			query.setParameter("userId", userId);
			jit = ((java.math.BigInteger) query.getSingleResult()).longValue();
		}
		catch(Exception e){
			logger.error("error while insetring token for user with id" +userId,e);
			throw new LiferolesRuntimeException(e);
		}
		return Jwts.builder().setSubject(userId.toString()).setId(jit.toString()).signWith(SignatureAlgorithm.HS256,AuthUtils.getHashKey()).compact();
	}
	/**
	 * 
	 * @param token
	 * @return	user's id
	 * @throws LifeRolesDBException
	 * @throws TokenValidationException	if token is invalid
	 * @throws LiferolesRuntimeException 
	 */
	public Long validateToken(String token) throws TokenValidationException, LiferolesRuntimeException{
		Long userId;
		Long jit;
		Object obj;
		try{
			Claims body = Jwts.parser().setSigningKey(AuthUtils.getHashKey()).parseClaimsJws(token).getBody();
			jit = Long.parseLong(body.getId());
			userId = Long.parseLong(body.getSubject());
			}
		catch(Exception e){
			logger.info("token validation failed - token: " + token);
			throw new TokenValidationException("token validation failed",e);
		}
		try{
			Query query = em.createNativeQuery("select blacklist from tokens where jit = :jit");
			query.setParameter("jit", jit);
			obj  = query.getSingleResult();
		}
		catch (NoResultException nre){
			throw new TokenValidationException("JIT was not found in DB");
		}
		catch(Exception e){
			logger.error("problem occurred when checking JIT blacklist table",e);
			throw new LiferolesRuntimeException(e);
		}
		if((Boolean)obj == true){
			logger.info("blocked access from blacklisted JIT - " + jit);
			throw new TokenValidationException("JIT is in blacklist");
		}
		return userId;
	}
	
	public void logoutMobileUser(String token) throws TokenValidationException, LiferolesRuntimeException{
		Long jit;
		try{
			Claims body = Jwts.parser().setSigningKey(AuthUtils.getHashKey()).parseClaimsJws(token).getBody();
			jit = Long.parseLong(body.getId());
			}
		catch(Exception e){
			logger.info("logout of mobile user failed - token: " + token);
			throw new TokenValidationException("logout of mobile user failed",e);
		}
		
		try{
			Query query = em.createNativeQuery("update tokens set blacklist=true where jit = :jit");
			query.setParameter("jit", jit);
			query.executeUpdate();
		}catch(Exception e){
			logger.error("problem occurred when updating blacklist of the token during user logout, JIT: " + jit,e);
			throw new LiferolesRuntimeException(e);
		}
	}
	
	public void addTokensToBlacklist(Long userId) throws LiferolesRuntimeException{
		try{
			Query query = em.createNativeQuery("update tokens set blacklist = true where appuser_id = :userId");
			query.setParameter("userId", userId);
			int i = query.executeUpdate();
			logger.info(i + " tokens of user with id " + userId + " blacklisted");
		}catch(Exception e){
			logger.error("problem occurred when trying to add tokens of user " + userId + " to blacklist",e);
			throw new LiferolesRuntimeException(e);
		}
	}
	/**
	 * 
	 * @param accessingUser	user object populated with password and email
	 * @return	null if password/email is wrong, user otherwise
	 * @throws LiferolesRuntimeException 
	 * @throws LifeRolesDBException
	 */
	public User authenticateMobileUser(User accessingUser) throws LiferolesRuntimeException{
		User dbUser = null;
		try{
			Query query = em.createQuery("from User where email = :email");
			query.setParameter("email", accessingUser.getEmail());
			dbUser = (User) query.getSingleResult();
		}
		catch (NoResultException nre){
			logger.info("authentication failed - no user with email " + accessingUser.getEmail() + " found in db");
			return null;
		}
		catch(Exception e){
			logger.error("error occurred when retrieving user with email " + accessingUser.getEmail(),e);
			throw new LiferolesRuntimeException(e);
		}
		String accessingUserHash = AuthUtils.computeHash(accessingUser.getPassword(), dbUser.getSalt()).getHash();
		if (!accessingUserHash.equals(dbUser.getPassword())){
			logger.info("authentication failed - wrong password for " + accessingUser);
			return null;
		}
		logger.info("authentication succes - " + accessingUser);
		return dbUser;
	}
	
	public void useResetToken(String resetToken,User u) throws TokenValidationException, LiferolesRuntimeException{
		String tokenHash = AuthUtils.computeHash(resetToken, u.getEmail()).getHash();
		try{
			Query query = em.createNativeQuery("update passwordreset set used = true where tokenhash = :tokenHash and appuser_id = :userId and used = false and expirationdate > :dateNow");
			query.setParameter("userId", u.getId());
			query.setParameter("dateNow", new Date());
			query.setParameter("tokenHash", tokenHash);
			int i = query.executeUpdate();
			if( i!= 1){
				ctx.setRollbackOnly();
				throw new TokenValidationException("token expired, used or don't exists for this user");
			}
			logger.info(i + "token " + tokenHash + " used");
			}
		catch(TokenValidationException e){
			throw e;
		}
		catch(Exception e){
			logger.error("problem occurred when trying to use token " + tokenHash + " of user " + u.getId(),e);
			throw new LiferolesRuntimeException(e);
		}
	}
	
	public boolean checkPassword(String password, Long userId) throws LiferolesRuntimeException{
		String dbHash;
		String salt;
		try{
			Query query = em.createQuery("select password, salt from User where id = :id");
			query.setParameter("id", userId);
			@SuppressWarnings("unchecked")
			List<Object[]> l = query.getResultList();
			dbHash = (String)l.get(0)[0];
			salt = (String)l.get(0)[1];
		}catch(Exception e){
			logger.error("error occurred when retrieving user with id " + userId,e);
			throw new LiferolesRuntimeException(e);
		}
		String accessingUserHash = AuthUtils.computeHash(password, salt).getHash();
		if (!accessingUserHash.equals(dbHash)){
			logger.info("authentication failed - wrong password for user with id " + userId);
			return false;
		}
		logger.info("authentication succes - user with id " + userId);
		return true;
	}
}
