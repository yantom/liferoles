package com.liferoles.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.model.*;

@LocalBean
@Stateless
public class TaskManager {
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;
	
	public Long createTask(Task task){
		Long id;
		try{
			em.persist(task);
			em.flush();
			id = task.getId();
		}catch (Exception e) {
			logger.error("db error occured while creating " + task.toString(),e);
			throw e;
			}
		logger.info(task.toString() + " created");
		return id;
	}
	
	public void deleteTask(Task task){
		try{
			Task t = em.find(Task.class, task.getId());
			em.remove(t);
		}catch(Exception e){
			logger.error("db error occured while deleting " + task.toString(),e);
			throw e;
		}
		logger.info(task.toString() + " deleted");
	}
	
	public void updateTask(Task task){
		try{
			em.merge(task);
		}catch(Exception e){
			logger.error("db error occured while updating " + task.toString(),e);
			throw e;
		}
		logger.info(task.toString() + " updated");
	}
	
	@SuppressWarnings("unchecked")
	public List<Task> getTasksForWeek(Long userId, LocalDate mondayOfTheWeek){
		List<Task> taskList = null;
		try{
			Query query = em.createQuery("from Task where user.id = :id and date between :monday and :sunday))");
			query.setParameter("id", userId);
			query.setParameter("monday", mondayOfTheWeek);
			query.setParameter("sunday", mondayOfTheWeek.plusDays(6));
			taskList = query.getResultList();
		}catch(Exception e){
			logger.error("db error occured while retrieving tasks of user with id " + userId,e);
			throw e;
		}
		logger.info("tasks of user with id " + userId + " retrieved");
		return taskList;
	}
	
	@SuppressWarnings("unchecked")
	public List<List<Task>> getInitTasks(Long userId,LocalDate dateFrom){
		List<Task> taskList;
		try{
			Query query = em.createQuery("from Task where user.id = :id and ((date is null) or (date >= :dateFrom))");
			query.setParameter("id", userId);
			query.setParameter("dateFrom", dateFrom);
			taskList = query.getResultList();
		}catch(Exception e){
			logger.error("db error occured while retrieving tasks of user with id " + userId,e);
			throw e;
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