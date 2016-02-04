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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liferoles.rest.JSON.UserIdDeserializer;
import com.liferoles.rest.JSON.UserIdSerializer;

/**
 *
 * @author Honzator
 */
@Entity
public class Role implements Serializable{
    
	@SequenceGenerator(name="role_id", sequenceName="role_id_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="role_id")
	@Id
	private Long id;
    private String name;
    
    private String roleGoal;
    
    
    @ManyToOne
    @JsonSerialize(using = UserIdSerializer.class)
    @JsonDeserialize(using = UserIdDeserializer.class)
    @JoinColumn(name="appuser_id")
    private User user;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public User getUser(){
		return user;
	}
	public void setUser(User user){
		this.user = user;
	}
	
	@Override
    public String toString() {
        return "Role{" + "id=" + getId() + ", name=" + getName() + '}';
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
		Role other = (Role) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getRoleGoal() {
		return roleGoal;
	}

	public void setRoleGoal(String roleGoal) {
		this.roleGoal = roleGoal;
	}
}
