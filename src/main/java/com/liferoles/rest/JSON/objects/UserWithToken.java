package com.liferoles.rest.JSON.objects;

import com.liferoles.model.User;

public class UserWithToken {

	private User user;
	private String token;

	public UserWithToken() {
	}

	public UserWithToken(User user, String token) {
		this.setUser(user);
		this.setToken(token);
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
