package com.liferoles.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;


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
	
	public static SaltHashPair computeHash(String input, String inputSalt) throws NoSuchAlgorithmException, InvalidKeySpecException{
		char[] passwordAsCharArray = input.toCharArray();
		byte[] salt;
		if(inputSalt == null)
			salt = getSalt();
		else
			salt = inputSalt.getBytes();
		PBEKeySpec spec = new PBEKeySpec(passwordAsCharArray, salt, 1000, 512);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		if(inputSalt == null)
			return new SaltHashPair(DatatypeConverter.printHexBinary(salt),DatatypeConverter.printHexBinary(hash));
		else
			return new SaltHashPair(inputSalt,DatatypeConverter.printHexBinary(hash));
	}
	
	public static byte[] getSalt() throws NoSuchAlgorithmException{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}
}
