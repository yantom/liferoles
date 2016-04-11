package com.liferoles.controller;

import java.time.LocalDate;
import java.util.ArrayList;
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
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	
	public Long createTask(Task task){
		Long id;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			id = (Long) session.save(task);
			task.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occured while creating " + task.toString(),e);
			throw e;
			}
		finally {
			session.close();
		}
		logger.info(task.toString() + " created");
		return id;
	}
	
	public void deleteTask(Task task){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(task);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + task.toString(),e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(task.toString() + " deleted");
	}
	
	public void updateTask(Task task){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.update(task);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating " + task.toString(),e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(task.toString() + " updated");
	}
	
	public Task getTaskById(Long id){
		Task t;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where id = :id");
			query.setLong("id", id);
			t = (Task)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving task with id " + id,e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("task with id " + id + " retrieved");
		return t;
	}
	
	public List<Task> getTasksForWeek(Long userId, LocalDate mondayOfTheWeek){
		List<Task> taskList = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where user.id = :id and date between :monday and :sunday))");
			query.setLong("id", userId);
			query.setParameter("monday", mondayOfTheWeek);
			query.setParameter("sunday", mondayOfTheWeek.plusDays(6));
			taskList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving tasks of user with id " + userId,e);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("tasks of user with id " + userId + " retrieved");
		return taskList;
	}
	
	public List<List<Task>> getInitTasks(Long userId,LocalDate dateFrom){
		List<Task> taskList;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from Task where user.id = :id and ((date is null) or (date >= :dateFrom))");
			query.setLong("id", userId);
			query.setParameter("dateFrom", dateFrom);
			taskList = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving tasks of user with id " + userId,e);
			throw e;
		}
		finally {
			session.close();
		}
		List<Task> backlog = new ArrayList<Task>();
		List<Task> future = new ArrayList<Task>();
		List<Task> nextweek = new ArrayList<Task>();
		List<Task> thisweek = new ArrayList<Task>();
		List<Task> lastweek = new ArrayList<Task>();
		List<List<Task>> tasks = new ArrayList<List<Task>>();
		for(Task t : taskList){
			if(t.getDate()==null){
				backlog.add(t);
				continue;
			}
			if(t.getDate().isBefore(dateFrom.plusDays(7))){
				lastweek.add(t);
				continue;
			}
			if(t.getDate().isBefore(dateFrom.plusDays(14))){
				thisweek.add(t);
				continue;
			}
			if(t.getDate().isBefore(dateFrom.plusDays(21))){
				nextweek.add(t);
				continue;
			}
			future.add(t);
			
		}
		tasks.add(backlog);
		tasks.add(future);
		tasks.add(nextweek);
		tasks.add(thisweek);
		tasks.add(lastweek);
		logger.info("tasks of user with id " + userId + " retrieved");
		return tasks;
	}
}