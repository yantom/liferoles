package com.liferoles.controller;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.*;
import com.liferoles.utils.HibernateUtils;

public class RoleManager {
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
	
	public Long createRole(Role role){
		Long id;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			id = (Long) session.save(role);
			role.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occured while creating " + role.toString(),e);
			throw e;
			}
		finally {
			session.close();
		}
		logger.info(role.toString() + " created");
		return id;
	}
	
	public void deleteRole(Role role){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(role);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + role.toString(),e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(role.toString() + " deleted");
	}
	
	public void deleteRole(Role deletedRole, Role newRole){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("update Task set role.id = :newRoleId where role.id = :oldRoleId)");
			query.setLong("newRoleId", newRole.getId());
			query.setLong("oldRoleId", deletedRole.getId());
			query.executeUpdate();
			session.delete(deletedRole);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + deletedRole + " and moving tasks to " + newRole,e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(deletedRole + " deleted, tasks moved under " + newRole);
	}
	
	public void updateRole(Role role){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.update(role);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating " + role.toString(),e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(role.toString() + " updated");
	}
	
	public Role getRoleById(Long id){
		Role r;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Role where id = :id");
			query.setLong("id", id);
			r = (Role)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving role with id " + id,e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("role with id " + id + " retrieved");
		return r;
	}
	
	public List<Role> getAllRoles(Long userId){
		List<Role> roleList = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Role where user.id = :id");
			query.setLong("id", userId);
			roleList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving roles of user with id " + userId,e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("roles of user with id " + userId + " retrieved");
		return roleList;
	}
}
