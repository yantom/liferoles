package com.liferoles.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.liferoles.LifeRolesException;


public class AuthUtils {
	
	public static String generateToken()
	{
		Random rng = new Random();
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    char[] text = new char[32];
	    for (int i = 0; i < 32; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}
	
	
	public static SaltHashPair computeHash(String input, String inputSalt) throws LifeRolesException{
		char[] passwordAsCharArray = input.toCharArray();
		byte[] salt;
		//used with registration
		if(inputSalt == null)
			salt = getSalt();
		//used for password resetlink token (salt = email)
		else if(inputSalt.contains("@"))
			salt = inputSalt.getBytes();
		//used for authentication (salt in hex representation)
		else
			salt = DatatypeConverter.parseHexBinary(inputSalt);
		PBEKeySpec spec = new PBEKeySpec(passwordAsCharArray, salt, 1000, 512);
		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new LifeRolesException(e);
		}
		byte[] hash;
		try {
			hash = skf.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			throw new LifeRolesException(e);
		}
		if(inputSalt == null)
			return new SaltHashPair(DatatypeConverter.printHexBinary(salt),DatatypeConverter.printHexBinary(hash));
		else
			return new SaltHashPair(inputSalt,DatatypeConverter.printHexBinary(hash));
	}
	
	public static byte[] getSalt() throws LifeRolesException {
		SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new LifeRolesException(e);
		}
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}
}
