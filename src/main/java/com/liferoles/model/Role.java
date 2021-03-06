/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liferoles.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liferoles.rest.JSON.serializers.UserIdDeserializer;
import com.liferoles.rest.JSON.serializers.UserIdSerializer;

/**
 *
 * @author Honzator
 */
@Entity
public class Role implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2065523585834141786L;
	/**
	 * 
	 */
	@SequenceGenerator(name = "role_id", sequenceName = "role_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_id")
	@Id
	private Long id;
	private String name;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "role_id")
	private List<RoleGoal> goals;

	@ManyToOne
	@JsonSerialize(using = UserIdSerializer.class)
	@JsonDeserialize(using = UserIdDeserializer.class)
	@JoinColumn(name = "appuser_id")
	private User user;

	public Role() {
	};

	public Role(Long id) {
		this.id = id;
	}

	public Role(Long id, String name, List<RoleGoal> goals, User u) {
		this.id = id;
		this.name = name;
		this.goals = goals;
	}

	public Role(String name, List<RoleGoal> goals) {
		this.name = name;
		this.goals = goals;
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

	public List<RoleGoal> getGoals() {
		return goals;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public User getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setGoals(List<RoleGoal> goals) {
		this.goals = goals;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Role with id" + id;
	}

}
