package com.liferoles.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class RoleGoal implements Serializable {
	private static final long serialVersionUID = 5054086089131869755L;
	@SequenceGenerator(name = "rolegoal_id", sequenceName = "rolegoal_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rolegoal_id")
	@Id
	private Long id;
	private String name;
	private boolean finished;

	public RoleGoal() {
	};

	public RoleGoal(Long id, String name, boolean finished) {
		this.id = id;
		this.name = name;
		this.finished = finished;
	}

	public RoleGoal(String name, boolean finished) {
		this.name = name;
		this.finished = finished;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
