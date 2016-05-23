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

import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.PossibleDataInconsistencyException;
import com.liferoles.model.Task;

@LocalBean
@Stateless
public class TaskManager {
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;

	public Long createTask(Task task) throws LiferolesRuntimeException {
		Long id;
		try {
			em.persist(task);
			em.flush();
			id = task.getId();
		} catch (Exception e) {
			logger.error("db error occured while creating " + task.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(task.toString() + " created");
		return id;
	}

	public void deleteTask(Task task) throws LiferolesRuntimeException {
		try {
			Task t = em.find(Task.class, task.getId());
			em.remove(t);
		} catch (IllegalArgumentException e) {
			logger.warn("db error occured while deleting " + task.toString(), e);
			throw new PossibleDataInconsistencyException(
					"illegal argument exception when deleting task" + task.toString());
		} catch (Exception e) {
			logger.error("db error occured while deleting " + task.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(task.toString() + " deleted");
	}

	/**
	 * Returns list tasks older than specified date, organized into five lists
	 * according to the week to which particular tasks belongs.
	 * 
	 * @param userId
	 * @param dateFrom
	 *            should be first day of last week
	 * @return list of five lists of tasks, 1:backloged, 2:future ()
	 * @throws LiferolesRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public List<List<Task>> getInitTasks(Long userId, LocalDate dateFrom) throws LiferolesRuntimeException {
		List<Task> taskList;
		try {
			Query query = em.createQuery("from Task where user.id = :id and ((date is null) or (date >= :dateFrom))");
			query.setParameter("id", userId);
			query.setParameter("dateFrom", dateFrom);
			taskList = query.getResultList();
		} catch (Exception e) {
			logger.error("db error occured while retrieving tasks of user with id " + userId, e);
			throw new LiferolesRuntimeException(e);
		}
		List<Task> backlog = new ArrayList<Task>();
		List<Task> future = new ArrayList<Task>();
		List<Task> nextweek = new ArrayList<Task>();
		List<Task> thisweek = new ArrayList<Task>();
		List<Task> lastweek = new ArrayList<Task>();
		List<List<Task>> tasks = new ArrayList<List<Task>>();
		for (Task t : taskList) {
			if (t.getDate() == null) {
				backlog.add(t);
				continue;
			}
			if (t.getDate().isBefore(dateFrom.plusDays(7))) {
				lastweek.add(t);
				continue;
			}
			if (t.getDate().isBefore(dateFrom.plusDays(14))) {
				thisweek.add(t);
				continue;
			}
			if (t.getDate().isBefore(dateFrom.plusDays(21))) {
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

	@SuppressWarnings("unchecked")
	public List<Task> getTasksForWeek(Long userId, LocalDate mondayOfTheWeek) throws LiferolesRuntimeException {
		List<Task> taskList = null;
		try {
			Query query = em.createQuery("from Task where user.id = :id and date between :monday and :sunday))");
			query.setParameter("id", userId);
			query.setParameter("monday", mondayOfTheWeek);
			query.setParameter("sunday", mondayOfTheWeek.plusDays(6));
			taskList = query.getResultList();
		} catch (Exception e) {
			logger.error("db error occured while retrieving tasks of user with id " + userId, e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("tasks of user with id " + userId + " retrieved");
		return taskList;
	}

	public void moveOldTasksToBacklog(Long userId, LocalDate fromDay) throws LiferolesRuntimeException {
		try {
			Query q = em.createQuery(
					"update Task set date = null, time = null where user.id = :id and finished = false and date < :firstDay");
			q.setParameter("id", userId);
			q.setParameter("firstDay", fromDay);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("db error occured while moving tasks of user " + userId + " to backlog", e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("tasks of user " + userId + " moved to backlog");
	}

	public void updateTask(Task task) throws LiferolesRuntimeException {
		try {
			em.merge(task);
		} catch (Exception e) {
			logger.error("db error occured while updating " + task.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(task.toString() + " updated");
	}
}