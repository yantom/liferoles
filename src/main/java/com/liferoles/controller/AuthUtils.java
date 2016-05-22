package com.liferoles.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.SaltHashPair;
import com.liferoles.exceptions.LiferolesRuntimeException;

public class AuthUtils {
	private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
	private static byte[] hashKey = new byte[64];
	
	public static void setHashKey(){
		try (BufferedInputStream bis = new BufferedInputStream(AuthManager.class.getClassLoader().getResourceAsStream("hashkey"))){
			bis.read(hashKey);
		} catch (IOException e) {
			logger.error("unable to create hashkey",e);
			throw new LiferolesRuntimeException("unable to create hashkey",e);
		}
	}
	public static byte[] getHashKey(){
		return hashKey;
	}
	public static String getRandomBase64(int bytes) throws LiferolesRuntimeException{
		return Base64.getEncoder().encodeToString(getRandomBytes(bytes));
	}
	
	public static String getRandomBase64Url(int bytes) throws LiferolesRuntimeException{
		return Base64.getUrlEncoder().encodeToString(getRandomBytes(bytes));
	}
	/**
	 * 
	 * @param input	password or whatever input
	 * @param inputSalt	if null new salt will be generated
	 * @return	object with hash of input and salt
	 * @throws LiferolesRuntimeException
	 */
	public static SaltHashPair computeHash(String input, String inputSalt) throws LiferolesRuntimeException{
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
			throw new LiferolesRuntimeException(e);
			
		}
		byte[] hash;
		try {
			hash = skf.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			logger.error("error occurred when computing hash",e);
			throw new LiferolesRuntimeException(e);
		}
		if(inputSalt == null)
			return new SaltHashPair(Base64.getEncoder().encodeToString(salt),Base64.getEncoder().encodeToString(hash));
		else
			return new SaltHashPair(inputSalt,Base64.getEncoder().encodeToString(hash));
	}
	private static byte[] getRandomBytes(int bytes) throws LiferolesRuntimeException {
		SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.toString(),e);
			throw new LiferolesRuntimeException(e);
		}
		byte[] salt = new byte[bytes];
		sr.nextBytes(salt);
		return salt;
	}
}
