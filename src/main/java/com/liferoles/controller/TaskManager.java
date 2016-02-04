package com.liferoles.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.*;
import com.liferoles.utils.HibernateUtils;

public class TaskManager {
	
	private Session session;
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	
	public Long createTask(Task task)throws LifeRolesDBException{
		if(task == null){
			logger.error("task was not created due to application failure");
			throw new IllegalArgumentException("task cant be null");
		}
		Long id;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			id = (Long) session.save(task);
			task.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occured while creating " + task.toString());
			throw new LifeRolesDBException(e);
			}
		finally {
			session.close();
		}
		logger.info(task.toString() + " created");
		return id;
	}
	
	public void deleteTask(Task task)throws LifeRolesDBException{
		if(task == null){
			logger.error("task was not deleted due to application failure");
			throw new IllegalArgumentException("task cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(task);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + task.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(task.toString() + " deleted");
	}
	
	public void updateTask(Task task)throws LifeRolesDBException{
		if(task == null){
			logger.error("task was not updated due to application failure");
			throw new IllegalArgumentException("task cant be null");
		}
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.update(task);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating " + task.toString());
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info(task.toString() + " updated");
	}
	
	public Task getTaskById(Long id) throws LifeRolesDBException{
		Task t;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where id = ?");
			query.setLong(0, id);
			t = (Task)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving task with id " + id);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("task with id " + id + " retrieved");
		return t;
	}
	
	public List<Task> getAllTasks(Long userId) throws LifeRolesDBException{
		List<Task> taskList = null;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where role.id in (select id from Role where user.id = ?)");
			
			taskList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving all tasks");
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("all tasks retrieved");
		return taskList;
	}
	
	public List<Task> getTasksWithoutHistory(Long userId) throws LifeRolesDBException{
		List<Task> taskList = null;
		session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where role.id in (select id from Role where user.id = ?) and ((date is null) or (date >= ?))");
			query.setLong(0, userId);
			query.setDate(1, getDateFrom());
			taskList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving tasks of user with id " + userId);
			throw new LifeRolesDBException(e);
		}
		finally {
			session.close();
		}
		logger.info("tasks of user with id " + userId + " retrieved");
		return taskList;
	}
	
	public Date getDateFrom(){
		LocalDate currDate = LocalDate.now();
		LocalDate localDateFrom = currDate.minusDays(currDate.getDayOfWeek().getValue() - 1);
		return Date.from(localDateFrom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
	/*
	public LocalDate getDateTo(){
		LocalDate currDate = LocalDate.now();
		return currDate.plusDays(14 - currDate.getDayOfWeek().getValue());
	}*/
	
	
}
