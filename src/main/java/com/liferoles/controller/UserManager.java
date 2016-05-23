package com.liferoles.controller;

import java.util.Date;

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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.model.User;

@Stateless
@LocalBean
public class UserManager {
	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
	private javax.mail.Session gmailSession;
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;

	public UserManager() {
		try {
			gmailSession = InitialContext.doLookup("java:jboss/mail/Gmail");
		} catch (NamingException e) {
			logger.error("cannot find mail resource", e);
		}
	}

	public Long createUser(User user) throws LiferolesRuntimeException {
		SaltHashPair shp;
		shp = AuthUtils.computeHash(user.getPassword(), null);
		user.setPassword(shp.getHash());
		user.setSalt(shp.getSalt());
		Long id = null;
		try {
			em.persist(user);
			em.flush();
			id = user.getId();
		} catch (Exception e) {
			logger.error("db error occurred while creating " + user.toString());
			throw new LiferolesRuntimeException(e);
		}
		logger.info(user.toString() + " created");
		return id;
	}

	public void deleteUser(User user) throws LiferolesRuntimeException {
		try {
			User u = em.find(User.class, user.getId());
			em.remove(u);
		} catch (Exception e) {
			logger.error("db error occured while deleting " + user.toString());
			throw new LiferolesRuntimeException(e);
		}
		logger.info(user.toString() + " deleted");
	}

	public User getUserById(Long id) throws LiferolesRuntimeException {
		User u = null;
		try {
			Query query = em.createQuery("from User where id = :id");
			query.setParameter("id", id);
			u = (User) query.getSingleResult();
		} catch (NoResultException nre) {
			logger.info("user with id " + id + " not found in database");
			return null;
		} catch (Exception e) {
			logger.error("db error occured while retrieving user with id " + id);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("user with id " + id + " retrieved");
		return u;
	}

	public User getUserByMail(String mail) throws LiferolesRuntimeException {
		User u = null;
		try {
			Query query = em.createQuery("from User where email like :email");
			query.setParameter("email", mail);
			u = (User) query.getSingleResult();
		} catch (NoResultException nre) {
			logger.info("user with email " + mail + " not found in database");
			return null;
		} catch (Exception e) {
			logger.error("db error occured while retrieving user with email " + mail);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("user with email " + mail + " retrieved");
		return u;
	}

	public void sendEmail(String to, String subject, String msg) throws LiferolesRuntimeException {
		Message message = new MimeMessage(gmailSession);
		try {
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setContent(msg, "text/html");
			Transport.send(message);
		} catch (MessagingException e) {
			logger.error("error occurred while sending email to " + to, e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("Email was sent to " + to);
	}

	public void sendResetLink(String mail) throws LiferolesRuntimeException {
		User u = getUserByMail(mail);
		// 18 because if 16 padding is added and it is stripped by browser
		String token = AuthUtils.getRandomBase64Url(18);
		String tokenHash = AuthUtils.computeHash(token, mail).getHash();
		try {
			Query query = em.createNativeQuery(
					"insert into passwordreset (appuser_id, tokenhash, expirationdate) values(:userId,:tokenhash,:expiration)");
			query.setParameter("userId", u.getId());
			query.setParameter("tokenhash", tokenHash);
			query.setParameter("expiration", new Date(System.currentTimeMillis() + 60 * 60 * 1000));
			query.executeUpdate();
			// https://localhost:8443
			// https://liferoles.sde.cz
			String link = "https://liferoles.sde.cz?reset&user=" + mail + "&u=" + u.getId() + "&c=" + token;
			String subject = "Liferoles password reset link";
			String message = "Hi, you just requested for password reset. Click on <a href='" + link
					+ "'>this link</a> and reset your password. Link will expire in one hour.";
			sendEmail(mail, subject, message);
		} catch (Exception e) {
			logger.error("db error occured while creating new password reset record", e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("password reset link for user with id " + u.getId() + " sent");
	}

	public void updateUserData(User user) throws LiferolesRuntimeException {
		try {
			Query query = em.createQuery(
					"UPDATE User set firstDayOfWeek = :firstDayOfWeek, personalMission = :pm  WHERE id = :id");
			query.setParameter("firstDayOfWeek", user.getFirstDayOfWeek());
			query.setParameter("pm", user.getPersonalMission());
			query.setParameter("id", user.getId());
			query.executeUpdate();
		} catch (Exception e) {
			logger.error("db error occured while updating data of user with id " + user.getId());
			throw new LiferolesRuntimeException(e);
		}
		logger.info("data of user with id " + user.getId() + " updated");
	}

	public void updateUserEmail(User user) throws LiferolesRuntimeException {
		try {
			Query query = em.createQuery("UPDATE User set email = :email WHERE id = :id");
			query.setParameter("id", user.getId());
			query.setParameter("email", user.getEmail());
			query.executeUpdate();
		} catch (Exception e) {
			logger.error("db error occured while updating email of user with id " + user.getId());
			throw new LiferolesRuntimeException(e);
		}
		logger.info("email of user with id " + user.getId() + " updated");
	}

	public void updateUserPassword(User user) throws LiferolesRuntimeException {
		SaltHashPair shp = AuthUtils.computeHash(user.getPassword(), null);
		try {
			Query query = em.createQuery("UPDATE User set password = :password, salt = :salt  WHERE id = :id");
			query.setParameter("password", shp.getHash());
			query.setParameter("salt", shp.getSalt());
			query.setParameter("id", user.getId());
			query.executeUpdate();
		} catch (Exception e) {
			logger.error("db error occured while updating password of user with id " + user.getId());
			throw new LiferolesRuntimeException(e);
		}
		logger.info("password of user with id " + user.getId() + " updated");
	}
}
