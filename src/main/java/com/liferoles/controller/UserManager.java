package com.liferoles.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.SaltHashPair;
import com.liferoles.exceptions.LifeRolesAuthException;
import com.liferoles.exceptions.LifeRolesException;
import com.liferoles.model.*;

@Stateless
@LocalBean
public class UserManager {
	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
	private javax.mail.Session gmailSession;
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;
	
	public UserManager(){
		try {
			gmailSession = InitialContext.doLookup("java:jboss/mail/Gmail");
		} catch (NamingException e) {
			logger.error("cannot find mail resource",e);
		}
	}
	public Long createUser(User user) throws LifeRolesAuthException{
		SaltHashPair shp;
		shp = AuthManager.computeHash(user.getPassword(), null);
		user.setPassword(shp.getHash());
		user.setSalt(shp.getSalt());
		Long id = null;
		try{
			em.persist(user);
			em.flush();
			id = user.getId();
		}catch (Exception e) {
			logger.error("db error occurred while creating " + user.toString());
			throw e;
			} 
		logger.info(user.toString() + " created");
		return id;
	}
	
	public void deleteUser(User user){	
		try{
			User u = em.find(User.class, user.getId());
			em.remove(u);
		}catch(Exception e){
			logger.error("db error occured while deleting " + user.toString());
			throw e;
		}
		logger.info(user.toString() + " deleted");
	}
	
	public void updateUserPassword(User user) throws LifeRolesAuthException{
		SaltHashPair shp = AuthManager.computeHash(user.getPassword(), null);
		try{
			Query query = em.createQuery("UPDATE User set password = :password, salt = :salt  WHERE id = :id");
			query.setParameter("password", shp.getHash());
			query.setParameter("salt", shp.getSalt());
			query.setParameter("id", user.getId());
			query.executeUpdate();
		}catch(Exception e){
			logger.error("db error occured while updating password of user with id " + user.getId());
			throw e;
		}
		logger.info("password of user with id " + user.getId() + " updated");
	}
	
	public void updateUserData(User user){
		try{
			Query query = em.createQuery("UPDATE User set firstDayOfWeek = :firstDayOfWeek, email = :email, personalMission = :pm  WHERE id = :id");
			query.setParameter("firstDayOfWeek", user.getFirstDayOfWeek());
			query.setParameter("pm", user.getPersonalMission());
			query.setParameter("email", user.getEmail());
			query.setParameter("id", user.getId());
			query.executeUpdate();
		}catch(Exception e){
			logger.error("db error occured while updating data of user with id " + user.getId());
			throw e;
		}
		logger.info("data of user with id " + user.getId() + " updated");
	}
	
	public User getUserById(Long id){
		User u = null;
		try{
			Query query = em.createQuery("from User where id = :id");
			query.setParameter("id", id);
			u = (User)query.getSingleResult();
		}catch(Exception e){
			logger.error("db error occured while retrieving user with id " + id);
			throw e;
		}
		if(u == null)
			logger.info("user with id " +id + " not found in database");
		else
			logger.info("user with id " + id + " retrieved");
		return u;
	}
	
	public User getUserByMail(String mail){
		User u = null;
		try{
			Query query = em.createQuery("from User where email = :email");
			query.setParameter("email", mail);
			u = (User)query.getSingleResult();
		}catch(Exception e){
			logger.error("db error occured while retrieving user with email " + mail);
			throw e;
		}
		if(u == null)
			logger.info("user with email " + mail + " not found in database");
		else
			logger.info("user with email " + mail + " retrieved");
		return u;
	}

	public void sendEmail(String to,String subject,String msg) throws LifeRolesException{
			   Message message = new MimeMessage(gmailSession);
			   try{
				   message.setRecipients(Message.RecipientType.TO,
				    InternetAddress.parse(to));
				   message.setSubject(subject);
				   message.setContent(msg,"text/html");
				   Transport.send(message);
			   }catch(MessagingException ex){
				   logger.error("error occurred while sending email to " + to,ex);
				   throw new LifeRolesException(ex);
			   }
			   logger.info("Email was sent to " + to);
	}
	
	public void sendResetLink(String mail) throws LifeRolesException{
		User u = getUserByMail(mail);
		//18 because if 16 padding is added and it is stripped by browser
		String token = AuthManager.getRandomBase64Url(18);
		String tokenHash = AuthManager.computeHash(token, mail).getHash();
		try{
			Query query = em.createNativeQuery("insert into passwordreset (appuser_id, tokenhash, expirationdate) values(:userId,:tokenhash,:expiration)");
			query.setParameter("userId", u.getId());
			query.setParameter("tokenhash",tokenHash);
			query.setParameter("expiration", LocalDateTime.now().plusMinutes(60));
			query.executeUpdate();
			//https://localhost:8443
			//https://liferoles.sde.cz
			String link = "https://liferoles.sde.cz?reset&user="+ mail +"&u=" + u.getId() + "&c=" + token;
			String subject = "Liferoles password reset link";
			String message = "Hi, you just requested for password reset. Click on <a href='"+link+"'>this link</a> and reset your password. Link will expire in one hour.";
			sendEmail(mail,subject,message);
		}
		catch (LifeRolesException e) {
			throw e;
		}
		catch(Exception e){
			logger.error("db error occured while creating new password reset record",e);
			throw e;
		} 
		logger.info("password reset link for user with id " + u.getId() + " sent");
	}
	
	public void moveOldTasksToBacklog(Long userId,LocalDate firstDayOfCurrentWeek){
		try{
			Query q = em.createQuery("update Task set date = null, time = null where user.id = :id and finished = false and date < :firstDay");
			q.setParameter("id", userId);
			q.setParameter("firstDay", firstDayOfCurrentWeek);
			q.executeUpdate();
		}catch(Exception ex){
			logger.error("db error occured while moving tasks of user " + userId + " to backlog",ex);
			throw ex;
		}
		logger.info("tasks of user " + userId + " moved to backlog");
	}
}
