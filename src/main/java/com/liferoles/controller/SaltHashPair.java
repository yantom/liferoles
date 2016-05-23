package com.liferoles.controller;

public class SaltHashPair {
	private String salt;
	private String hash;

	public SaltHashPair() {
	};

	public SaltHashPair(String salt, String hash) {
		this.setSalt(salt);
		this.setHash(hash);
	}

	public String getHash() {
		return hash;
	}

	public String getSalt() {
		return salt;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
}
