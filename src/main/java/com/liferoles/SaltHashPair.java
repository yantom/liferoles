package com.liferoles;

public class SaltHashPair {
	private String salt;
	private String hash;
	
	public SaltHashPair(){};
	
	public SaltHashPair(String salt, String hash){
		this.setSalt(salt);
		this.setHash(hash);
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
