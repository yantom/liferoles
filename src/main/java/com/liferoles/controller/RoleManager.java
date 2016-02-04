package com.liferoles.controller;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.*;
import com.liferoles.utils.HibernateUtils;

public class RoleManager {
	
	private Session session;
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
	
	public Long createRole(Role role) throws LifeRolesDBException{
		if(role == null){
			logger.error("role was not created due to application failure");
			throw new IllegalArgumentException("role cant be null");
		}
		Long id;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			id = (Long) session.save(role);
			role.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occured while creating " + role.toString());
			throw new LifeRolesDBException(e);
			}
		finally {
			session.close();
		}
		logger.info(role.toString() + " created");
		return id;
	}
	
	public void deleteRole(Role role)throws LifeRolesDBException{
		if(role == null){
			logger.error("role was not deleted due to application failure");
			throw new IllegalArgumentException("role cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(role);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + role.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(role.toString() + " deleted");
	}
	
	public void deleteRole(Role deletedRole, Role newRole) throws LifeRolesDBException{
		if(deletedRole == null || newRole == null){
			logger.error("role was not deleted due to application failure");
			throw new IllegalArgumentException("role cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("update Task set role.id = ? where role.id = ?)");
			query.setLong(0, newRole.getId());
			query.setLong(1, deletedRole.getId());
			query.executeUpdate();
			session.delete(deletedRole);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting role with id" + deletedRole.getId() + "and moving tasks to role with id" + newRole.getId());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(deletedRole.toString() + "role with id" + deletedRole.getId() + "deleted, tasks moved under role with id" + newRole.getId());
	}
	
	public void updateRole(Role role)throws LifeRolesDBException{
		if(role == null){
			logger.error("role was not updated due to application failure");
			throw new IllegalArgumentException("role cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.update(role);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating " + role.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(role.toString() + " updated");
	}
	
	public Role getRoleById(Long id) throws LifeRolesDBException{
		Role r;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Role where id = ?");
			query.setLong(0, id);
			r = (Role)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving role with id " + id);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("role with id " + id + " retrieved");
		return r;
	}
	
	public List<Role> getAllRoles(Long userId) throws LifeRolesDBException{
		List<Role> roleList = null;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Role where user.id = ?");
			query.setLong(0, userId);
			roleList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving roles of user with id " + userId);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("roles of user with id " + userId + " retrieved");
		return roleList;
	}
	
	
}
