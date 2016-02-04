package com.liferoles.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.*;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;
import com.liferoles.utils.SaltHashPair;

public class UserManager {
	
	private Session session;
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
	
	public Long createUser(User user)throws LifeRolesDBException{
		if(user == null){
			logger.error("user was not created due to application failure");
			throw new IllegalArgumentException("user cant be null");
		}
		SaltHashPair shp;
		try{
			  shp = AuthUtils.computeHash(user.getPassword(), null);
		}catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("error occured while computing password hash");
			throw new LifeRolesDBException(e);
		}
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
	
	public void UpdateUser(User user)throws LifeRolesDBException{
		if(user == null){
			logger.error("user was not updated due to application failure");
			throw new IllegalArgumentException("user cant be null");
		}
		if(user.getPassword() != null){
			try{
				SaltHashPair shp = AuthUtils.computeHash(user.getPassword(), null);
				user.setPassword(shp.getHash());
				user.setSalt(shp.getSalt());
			}
			catch(NoSuchAlgorithmException | InvalidKeySpecException e){
				logger.error("error occured while computing password hash");
				throw new LifeRolesDBException(e);
			}
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
			Query query = session.createQuery("from User where id = ?");
			query.setLong(0, id);
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
	
	public User getUserByMail(String mail) throws LifeRolesDBException{
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		User u = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where email = ?");
			query.setString(0, mail);
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
}
