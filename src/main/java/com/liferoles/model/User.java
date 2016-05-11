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
public class User implements Serializable{
    
	@SequenceGenerator(name="user_id", sequenceName="appuser_id_seq",allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="user_id")
	@Id
	private Long id;
    private String email;
    private Day firstDayOfWeek;
    private String personalMission;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
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
	
	@Override
	public String toString(){
		return "user with email: " + email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
	public Day getFirstDayOfWeek() {
		return firstDayOfWeek;
	}
	public void setFirstDayOfWeek(Day firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}
	public String getPersonalMission() {
		return personalMission;
	}
	public void setPersonalMission(String personalMission) {
		this.personalMission = personalMission;
	}
	
}
