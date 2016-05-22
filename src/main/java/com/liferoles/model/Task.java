/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liferoles.model;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liferoles.rest.JSON.serializers.LocalDateDeserializer;
import com.liferoles.rest.JSON.serializers.LocalDateSerializer;
import com.liferoles.rest.JSON.serializers.LocalTimeDeserializer;
import com.liferoles.rest.JSON.serializers.LocalTimeSerializer;
import com.liferoles.rest.JSON.serializers.RolePartialDeserializer;
import com.liferoles.rest.JSON.serializers.RolePartialSerializer;
import com.liferoles.rest.JSON.serializers.UserIdDeserializer;
import com.liferoles.rest.JSON.serializers.UserIdSerializer;


/**
 *
 * @author Honzator
 */
@Entity
public class Task implements Serializable{
	
	private static final long serialVersionUID = 8803629148353884150L;

	@SequenceGenerator(name="task_id", sequenceName="task_id_seq",allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="task_id")
	@Id
    private Long id;
    
    private String name;
    
    private boolean important;
    
    @ManyToOne
    @JsonSerialize(using = RolePartialSerializer.class)
    @JsonDeserialize(using = RolePartialDeserializer.class)
    @JoinColumn(name="role_id")
    private Role role;
    
    @ManyToOne
    @JsonSerialize(using = UserIdSerializer.class)
    @JsonDeserialize(using = UserIdDeserializer.class)
    @JoinColumn(name="appuser_id")
    private User user;
    
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;
    
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime time;
    
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate firstDate;
    
    private boolean finished;
    
    private String note;

    public Task(){};
    
    public Task(String name,Role r){
    	this.name=name;
    	this.role=r;
    }
	public Task(Long id, String name, boolean important, Role role, User user, LocalDate date, LocalTime time,
			LocalDate firstDate, boolean finished, String note) {
		super();
		this.id = id;
		this.name = name;
		this.important = important;
		this.role = role;
		this.user = user;
		this.date = date;
		this.time = time;
		this.firstDate = firstDate;
		this.finished = finished;
		this.note = note;
	}

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
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDate getFirstDate() {
		return firstDate;
	}

	public void setFirstDate(LocalDate firstDate) {
		this.firstDate = firstDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
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
		Task other = (Task) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
