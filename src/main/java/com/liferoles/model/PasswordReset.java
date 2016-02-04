package com.liferoles.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class PasswordReset implements Serializable{
	@SequenceGenerator(name="passwordreset_id", sequenceName="passwordreset_id_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="passwordreset_id")
	@Id
	private Long id;
	private String tokenHash;
	private boolean used;
	private LocalDateTime expirationDate;
	@JoinColumn(name="appuser_id")
	@ManyToOne
	private User user;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User u) {
		this.user = u;
	}
	public String getTokenHash() {
		return tokenHash;
	}
	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public LocalDateTime getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(LocalDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}
}
