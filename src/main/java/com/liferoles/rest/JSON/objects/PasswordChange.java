package com.liferoles.rest.JSON.objects;

public class PasswordChange {
	private String oldP;
	private String newP;

	public String getNewP() {
		return newP;
	}

	public String getOldP() {
		return oldP;
	}

	public void setNewP(String newP) {
		this.newP = newP;
	}

	public void setOldP(String oldP) {
		this.oldP = oldP;
	}
}
