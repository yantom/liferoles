package com.liferoles.utils;

import java.io.BufferedInputStream;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LifeRolesAuthException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.List;


public class AuthUtils {
	
	private static byte[] hashKey = new byte[64];
	//private static Cipher encryptCipher;
	//private static Cipher decryptCipher;
	private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
	
	public static void setHashKey() throws LifeRolesAuthException{
		try (BufferedInputStream bis = new BufferedInputStream(AuthUtils.class.getClassLoader().getResourceAsStream("hashkey"))){
			bis.read(hashKey);
		} catch (IOException e) {
			logger.error("wtf",e);
			throw new LifeRolesAuthException("unable to create hashkey",e);
		}
	}
	/*
	public static void setCiphersKeys() throws LifeRolesAuthException{
		byte[] dataKey = new byte[16];
		try (BufferedInputStream bis = new BufferedInputStream(AuthUtils.class.getClassLoader().getResourceAsStream("datakey"))){
			bis.read(dataKey);
		} catch (IOException e) {
			throw new LifeRolesAuthException("unable to read datakey",e);
		}
		Key key = new SecretKeySpec(dataKey, "AES");
		try{
		decryptCipher = Cipher.getInstance("AES");
		decryptCipher.init(Cipher.DECRYPT_MODE, key);
		encryptCipher = Cipher.getInstance("AES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new LifeRolesAuthException(e);
		}
	}*/
	
	public static String issueNewToken(Long userId) throws LifeRolesAuthException{
		Transaction tx = null;
		Long jit = null;
		Session session= HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			SQLQuery query = session.createSQLQuery("insert into tokens (appuser_id) values(:userId) returning jit");
			query.setLong("userId", userId);
			jit = ((java.math.BigInteger) query.uniqueResult()).longValue();
			tx.commit();
		}catch(HibernateException ex){
			if(tx!=null) tx.rollback();
			logger.error("error while insetring token for user with id" +userId,ex);
			throw ex;
		}finally {
			session.close();
		}
		return Jwts.builder().setSubject(userId.toString()).setId(jit.toString()).signWith(SignatureAlgorithm.HS256,hashKey).compact();
	}
	/**
	 * 
	 * @param token
	 * @return	user's id
	 * @throws LifeRolesDBException
	 * @throws TokenValidationException	if token is invalid
	 */
	public static Long validateToken(String token) throws TokenValidationException{
		Long userId;
		Long jit;
		try{
			Claims body = Jwts.parser().setSigningKey(hashKey).parseClaimsJws(token).getBody();
			jit = Long.parseLong(body.getId());
			userId = Long.parseLong(body.getSubject());
			}
		catch(Exception ex){
			logger.info("token validation failed - token: " + token);
			throw new TokenValidationException("token validation failed",ex);
		}
		finally{
			
		}
		Transaction tx = null;
		Session session= HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			SQLQuery query = session.createSQLQuery("select blacklist from tokens where jit = :jit");
			query.setLong("jit", jit);
			Object obj  = query.uniqueResult();
			if(obj == null){
				throw new TokenValidationException("JIT was not found in DB");
			}
			if((Boolean)obj == true){
				tx.commit();
				logger.info("blocked access from blacklisted JIT - " + jit);
				throw new TokenValidationException("JIT is in blacklist");
				}
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("problem occurred when checking JIT blacklist table",e);
			throw e;
		}finally {
			session.close();
		}
		return userId;
	}
	
	public static void addTokensToBlacklist(Long userId){
		Transaction tx = null;
		Session session= HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			SQLQuery query = session.createSQLQuery("update tokens set blacklist=true where appuser_id = :userId");
			query.setLong("userId", userId);
			int i = query.executeUpdate();
			tx.commit();
			logger.info(i + " tokens of user with id " + userId + " blacklisted");
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("problem occurred when trying to add tokens of user " + userId + " to blacklist",e);
			throw e;
		}finally {
			session.close();
		}
	}
	
	public static byte[] getHashKey(){
		return hashKey;
	}
	
	public static String getRandomBase64(int bytes) throws LifeRolesAuthException{
		return Base64.getEncoder().encodeToString(getRandomBytes(bytes));
	}
	
	public static String getRandomBase64Url(int bytes) throws LifeRolesAuthException{
		return Base64.getUrlEncoder().encodeToString(getRandomBytes(bytes));
	}
	
	/**
	 * 
	 * @param input	password or whatever input
	 * @param inputSalt	if null new salt will be generated
	 * @return	object with hash of input and salt
	 * @throws LifeRolesAuthException
	 */
	public static SaltHashPair computeHash(String input, String inputSalt) throws LifeRolesAuthException{
		char[] inputAsCharArray = input.toCharArray();
		byte[] salt;
		if(inputSalt == null)
			salt = getRandomBytes(16);
		else if(inputSalt.contains("@"))
			salt = inputSalt.getBytes();
		else
			salt = Base64.getDecoder().decode(inputSalt);
		PBEKeySpec spec = new PBEKeySpec(inputAsCharArray, salt, 1000, 512);
		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			logger.error("error occurred when computing hash",e);
			throw new LifeRolesAuthException(e);
			
		}
		byte[] hash;
		try {
			hash = skf.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			logger.error("error occurred when computing hash",e);
			throw new LifeRolesAuthException(e);
		}
		if(inputSalt == null)
			return new SaltHashPair(Base64.getEncoder().encodeToString(salt),Base64.getEncoder().encodeToString(hash));
		else
			return new SaltHashPair(inputSalt,Base64.getEncoder().encodeToString(hash));
	}
	
	private static byte[] getRandomBytes(int bytes) throws LifeRolesAuthException {
		SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			logger.error("wtf",e);
			throw new LifeRolesAuthException(e);
		}
		byte[] salt = new byte[bytes];
		sr.nextBytes(salt);
		return salt;
	}
	
	/**
	 * 
	 * @param accessingUser	user object populated with password and email
	 * @return	null if password/email is wrong, user otherwise
	 * @throws LifeRolesDBException
	 * @throws LifeRolesAuthException
	 */
	public static User authenticateMobileUser(User accessingUser) throws LifeRolesAuthException{
		Session session= HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		User dbUser = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where email = :email");
			query.setString("email", accessingUser.getEmail());
			dbUser = (User) query.uniqueResult();
			tx.commit();
		}catch(HibernateException ex){
			if(tx!=null) tx.rollback();
			logger.error("error occurred when retrieving user with email " + accessingUser.getEmail(),ex);
			throw ex;
		}finally{
			session.close();
			session=null;
		}
		if(dbUser == null){
			logger.info("authentication failed - no user with email " + accessingUser.getEmail() + " found in db");
			return null;
		}
		String accessingUserHash = computeHash(accessingUser.getPassword(), dbUser.getSalt()).getHash();
		if (!accessingUserHash.equals(dbUser.getPassword())){
			logger.info("authentication failed - wrong password for " + accessingUser);
			return null;
		}
		logger.info("authentication succes - " + accessingUser);
		return dbUser;
	}
	
	public static void useResetToken(String resetToken,User u) throws LifeRolesAuthException, TokenValidationException{
		String tokenHash = AuthUtils.computeHash(resetToken, u.getEmail()).getHash();
		Transaction tx = null;
		Session session= HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			SQLQuery query = session.createSQLQuery("update passwordreset set used = true where tokenhash = :tokenHash and appuser_id = :userId and used = false and expirationdate > :dateNow");
			query.setLong("userId", u.getId());
			query.setParameter("dateNow", LocalDateTime.now());
			query.setString("tokenHash", tokenHash);
			int i = query.executeUpdate();
			if( i!= 1){
				if(tx!=null) tx.rollback();
				throw new TokenValidationException("token expired, used or don't exists for this user");
			}
			tx.commit();
			logger.info(i + "token " + tokenHash + " used");
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("problem occurred when trying to use token " + tokenHash + " of user " + u.getId(),e);
			throw e;
		}finally {
			session.close();
		}
	}
	
	public static boolean checkPassword(String password, Long userId) throws LifeRolesAuthException{
		Session session= HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		String dbHash;
		String salt;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("select password, salt from User where id = :id");
			query.setLong("id", userId);
			List<Object[]> l = query.list();
			dbHash = (String)l.get(0)[0];
			salt = (String)l.get(0)[1];
			tx.commit();
		}catch(HibernateException ex){
			if(tx!=null) tx.rollback();
			logger.error("error occurred when retrieving user with id " + userId,ex);
			throw ex;
		}finally{
			session.close();
			session=null;
		}
		String accessingUserHash = computeHash(password, salt).getHash();
		if (!accessingUserHash.equals(dbHash)){
			logger.info("authentication failed - wrong password for user with id " + userId);
			return false;
		}
		logger.info("authentication succes - user with id " + userId);
		return true;
	}
	/*
	public static byte[] encryptString(String input) throws LifeRolesAuthException{
		byte[] output;
		try {
			output = encryptCipher.doFinal(input.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new LifeRolesAuthException(e);
		}
		return output;
	}
	
	public static String decryptString(byte[] input) throws LifeRolesAuthException{
		String output;
		try {
			output = new String(decryptCipher.doFinal(input));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new LifeRolesAuthException(e);
		}
		return output;
	}*/
	
}
