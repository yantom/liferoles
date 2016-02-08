package com.liferoles.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.LifeRolesDBException;
import com.liferoles.LifeRolesException;
import com.liferoles.model.*;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;
import com.liferoles.utils.SaltHashPair;

public class UserManager {
	
	private Session session;
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
	
	public Long createUser(User user)throws LifeRolesDBException, LifeRolesException{
		if(user == null){
			logger.error("user was not created due to application failure");
			throw new IllegalArgumentException("user cant be null");
		}
		SaltHashPair shp;
		shp = AuthUtils.computeHash(user.getPassword(), null);
		user.setPassword(shp.getHash());
		user.setSalt(shp.getSalt());
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		Long id = null;
		try{
			tx = session.beginTransaction();
			id = (Long)session.save(user);
			user.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occured while creating user with email: " + user.getEmail());
			throw new LifeRolesDBException(e);
			} 
		finally {
			session.close();
		}
		logger.info(user.toString() + " created");
		return id;
	}
	
	public void deleteUser(User user)throws LifeRolesDBException{
		if(user == null){
			logger.error("user was not deleted due to application failure");
			throw new IllegalArgumentException("user cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(user);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + user.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(user.toString() + " deleted");
	}
	
	public void UpdateUser(User user)throws LifeRolesDBException, LifeRolesException{
		if(user == null){
			logger.error("user was not updated due to application failure");
			throw new IllegalArgumentException("user cant be null");
		}
		if(user.getPassword() != null){
				SaltHashPair shp = AuthUtils.computeHash(user.getPassword(), null);
				user.setPassword(shp.getHash());
				user.setSalt(shp.getSalt());
		}
		
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			if(user.getPassword() == null){
				Query query = session.createQuery("UPDATE User set personalMission = :personalMission, email = :email, language = :language,  WHERE id = :id");
				query.setString("personalMission", user.getPersonalMission());
				query.setString("email", user.getEmail());
				query.setParameter("language", user.getLanguage());
				query.setLong("id", user.getId());
				query.executeUpdate();
			}
			else{
				session.update(user);
			}
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating " + user.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(user.toString() + " updated");
	}
	
	public User getUserById(Long id) throws LifeRolesDBException{
		User u = null;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where id = :id");
			query.setLong("id", id);
			u = (User)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving user with id " + id);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("user with id " + id + " retrieved");
		return u;
	}
	
	//returns null if usermail is not in database
	public User getUserByMail(String mail) throws LifeRolesDBException{
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		User u = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where email = :email");
			query.setString("email", mail);
			u = (User)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving user with email " + mail);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		if(u == null)
			logger.info("user with email " + mail + " not found in database");
		else
			logger.info("user with email " + mail + " retrieved");
		return u;
	}
	
	public List<User> getAllUsers() throws LifeRolesDBException{
		List<User> userList = null;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User");
			userList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving all users");
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("all users retrieved");
		return userList;
	}
	
	public void sendEmail(String to,String subject,String msg) throws LifeRolesException{
		try {
			Properties properties = System.getProperties();
			String user = "emailaccount";
			String passwd = "password";
			properties.setProperty("mail.smtp.auth", "true");
			properties.setProperty("mail.smtp.starttls.enable", "true");
			properties.setProperty("mail.smtp.host", "smtp.gmail.com");
			properties.setProperty("mail.smtp.port", "587");
			javax.mail.Session gmailSession = javax.mail.Session.getInstance(properties,
					  new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, passwd);
						}
					  });
			
			   Message message = new MimeMessage(gmailSession);
			   message.setFrom(new InternetAddress("emailaccount"));
			   message.setRecipients(Message.RecipientType.TO,
			    InternetAddress.parse(to));
			   message.setSubject(subject);
			   message.setContent(msg,"text/html");
			   Transport.send(message);
			 
			   logger.info("Email was sent");
			 
			  } catch (MessagingException e) {
			   logger.error("Error while sending email");
			   throw new LifeRolesException ("Error while sending email",e);
			  }
	}
	
	public void sendResetLink(String mail) throws LifeRolesException, LifeRolesDBException{
		Transaction tx = null;
		User u = getUserByMail(mail);
		session = HibernateUtils.getSessionFactory().openSession();
		PasswordReset pr = new PasswordReset();
		String token = AuthUtils.generateToken();
		String tokenHash;
		tokenHash = AuthUtils.computeHash(token, mail).getHash();
		pr.setUser(u);
		pr.setTokenHash(tokenHash);
		pr.setUsed(false);
		pr.setExpirationDate(LocalDateTime.now().plusMinutes(60));
		try{
			tx=session.beginTransaction();
			session.save(pr);
			String subject = "Liferoles reset password link";
			String link="www.liferoles.com/" + u.getId() + "/" + token;
			String message = "Hi, you just requested for password reset. Click on <a href='" + link +"'>this link</a> and create new password. Link will expire in one hour.";
			sendEmail(mail,subject,message);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while creating new password reset record");
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("password reset link for user with id " + u.getId() + " sent");
	}
}
