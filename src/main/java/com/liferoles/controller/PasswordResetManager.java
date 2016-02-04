package com.liferoles.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.LifeRolesDBException;
import com.liferoles.model.PasswordReset;
import com.liferoles.model.User;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;

public class PasswordResetManager {
	private Session session;
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);

	public void sendEmail(String to,String subject,String msg) throws MessagingException{
		try {
			Properties properties = System.getProperties();
			String user = "email";
			String passwd = "passwd";
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
			   message.setFrom(new InternetAddress("email"));
			   message.setRecipients(Message.RecipientType.TO,
			    InternetAddress.parse(to));
			   message.setSubject(subject);
			   message.setContent(msg,"text/html");
			   Transport.send(message);
			 
			   logger.info("Email was sent");
			 
			  } catch (MessagingException e) {
			   logger.error("Error while sending email");
			   throw e;
			  }
	}
	
	public void sendResetLink(String mail, Long userId) throws LifeRolesDBException{
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		User u = new User();
		u.setId(userId);
		PasswordReset pr = new PasswordReset();
		String token = AuthUtils.generateToken();
		String tokenHash;
		try {
			tokenHash = AuthUtils.computeHash(token, mail).getHash();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			logger.error("error occured while computing token hash");
			throw new LifeRolesDBException(e1);
		}
		pr.setUser(u);
		pr.setTokenHash(tokenHash);
		pr.setUsed(false);
		pr.setExpirationDate(LocalDateTime.now().plusMinutes(30));
		try{
			tx=session.beginTransaction();
			session.save(pr);
			String subject = "Liferoles reset password link";
			String link="www.liferoles.com/" + userId + "/" + token;
			String message = "Hi, you just requested for password reset. Click on <a href='" + link +"'>this link</a> and create new password. Link will expire in 30 minutes.";
			sendEmail(mail,subject,message);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while creating new password reset record");
			throw new LifeRolesDBException(e);
		}catch(MessagingException mex){
			if(tx!=null) tx.rollback();
			throw new LifeRolesDBException("error occured while sending email",mex);
		}
		finally {
			session.close();
		}
		logger.info("password reset link for user with id " + userId + " sent");
	}
}
