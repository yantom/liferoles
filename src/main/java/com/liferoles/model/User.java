/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liferoles.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liferoles.rest.JSON.serializers.UserPartialDeserializer;
import com.liferoles.rest.JSON.serializers.UserPartialSerializer;

/**
 *
 * @author Honzator
 */
@Entity
@Table(name = "appuser")
@JsonSerialize(using = UserPartialSerializer.class)
@JsonDeserialize(using = UserPartialDeserializer.class)
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3816788789538663997L;
	@SequenceGenerator(name = "user_id", sequenceName = "appuser_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id")
	@Id
	private Long id;
	private String email;
	private Day firstDayOfWeek;
	private String personalMission;
	@JsonIgnore
	private String password;
	@JsonIgnore
	private String salt;

	public User() {
	};

	public User(Long id, String email, Day fd, String pm, String passwd, String salt) {
		this.id = id;
		this.email = email;
		this.firstDayOfWeek = fd;
		this.personalMission = pm;
		this.password = passwd;
		this.salt = salt;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getEmail() {
		return email;
	}

	public Day getFirstDayOfWeek() {
		return firstDayOfWeek;
	}

	public Long getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getPersonalMission() {
		return personalMission;
	}

	public String getSalt() {
		return salt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstDayOfWeek(Day firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPersonalMission(String personalMission) {
		this.personalMission = personalMission;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public String toString() {
		return "user with email: " + email;
	}

}
