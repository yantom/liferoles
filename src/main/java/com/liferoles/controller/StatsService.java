package com.liferoles.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class computes month statistics for user. The purpose is to provide
 * general {@link com.liferoles.controller.StatsData} object, which is then
 * transformed into object specific for frontend charts library in abstract
 * getJsonStatsData method.
 * 
 * @author Jan Tomášek
 *
 * @param <T>
 *            Type of class which object is retrieved in abstract method
 *            getJsonStatsData.
 */
public abstract class StatsService<T> {
	private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;

	public abstract T getJsonStatsData(int year, int month, boolean currentMonth, Long userId);

	/**
	 * Retrieves StatsData object filled with statistics for the given month and
	 * user.
	 * 
	 * @param year
	 *            year for which statistics should be computed
	 * @param month
	 *            month for which statistics should be computed
	 * @param currentMonth
	 *            true if the month for which statistics should be computed is a
	 *            current month
	 * @param userId
	 * @return StatsData object
	 */
	@SuppressWarnings("unchecked")
	public StatsData getStatsData(int year, int month, boolean currentMonth, Long userId) {
		int countOfWeeksInMonth = computeCountOfWeeksInMonth(year, month);
		List<Object[]> rowsFromDB = getDataForMonthStatsFromDB(year, month, currentMonth, userId);

		if (rowsFromDB.size() == 0)
			return null;
		// initialize maps
		Map<String, Integer> countOfTasksPerRole = new HashMap<>();
		Map<String, Integer>[] countOfTasksPerRoleAndEffeciency = (Map<String, Integer>[]) new Map[4];
		Map<String, Integer>[] countOfTasksPerWeekAndEffeciency = (Map<String, Integer>[]) new Map[4];
		countOfTasksPerRoleAndEffeciency[0] = new HashMap<String,Integer>();
		countOfTasksPerRoleAndEffeciency[1] = new HashMap<String,Integer>();
		countOfTasksPerRoleAndEffeciency[2] = new HashMap<String,Integer>();
		countOfTasksPerRoleAndEffeciency[3] = new HashMap<String,Integer>();
		countOfTasksPerWeekAndEffeciency[0] = new HashMap<String,Integer>();
		countOfTasksPerWeekAndEffeciency[1] = new HashMap<String,Integer>();
		countOfTasksPerWeekAndEffeciency[2] = new HashMap<String,Integer>();
		countOfTasksPerWeekAndEffeciency[3] = new HashMap<String,Integer>();
		Set<String> namesOfRoles = new HashSet<>();
		for (Object[] row : rowsFromDB) {
			namesOfRoles.add((String) row[2]);
		}
		for (String roleName : namesOfRoles) {
			countOfTasksPerRole.put(roleName, 0);
			countOfTasksPerRoleAndEffeciency[0].put(roleName, 0);
			countOfTasksPerRoleAndEffeciency[1].put(roleName, 0);
			countOfTasksPerRoleAndEffeciency[2].put(roleName, 0);
			countOfTasksPerRoleAndEffeciency[3].put(roleName, 0);
		}
		for (int i = 1; i <= countOfWeeksInMonth; i++) {
			countOfTasksPerWeekAndEffeciency[0].put(String.format("Week %d", i), 0);
			countOfTasksPerWeekAndEffeciency[1].put(String.format("Week %d", i), 0);
			countOfTasksPerWeekAndEffeciency[2].put(String.format("Week %d", i), 0);
			countOfTasksPerWeekAndEffeciency[3].put(String.format("Week %d", i), 0);
		}

		// fill maps with counts
		Integer count;
		String roleName;
		String weekOfMonthString;
		int effeciencyIndex;
		LocalDate date;
		LocalDate firstDate;
		for (Object[] row : rowsFromDB) {
			roleName = (String) row[2];
			firstDate = (LocalDate) row[0];
			date = (LocalDate) row[1];
			weekOfMonthString = getWeekOfMonthString(firstDate);
			effeciencyIndex = getEffeciencyIndex(firstDate, date);
			count = countOfTasksPerRole.get(roleName);
			countOfTasksPerRole.put(roleName, count + 1);
			count = countOfTasksPerRoleAndEffeciency[effeciencyIndex].get(roleName);
			countOfTasksPerRoleAndEffeciency[effeciencyIndex].put(roleName, count + 1);
			count = countOfTasksPerWeekAndEffeciency[effeciencyIndex].get(weekOfMonthString);
			countOfTasksPerWeekAndEffeciency[effeciencyIndex].put(weekOfMonthString, count + 1);
		}

		// fill and retrieve StatsData object
		StatsData sd = new StatsData();
		sd.setCountOfTasksPerRole(countOfTasksPerRole);
		sd.setCountOfTasksPerRoleAndEffeciency(countOfTasksPerRoleAndEffeciency);
		sd.setCountOfTasksPerWeekAndEffeciency(countOfTasksPerWeekAndEffeciency);
		return sd;
	}

	/**
	 * Retrieves data for month statistics.
	 * 
	 * @param year
	 *            year for which statistics should be computed
	 * @param month
	 *            month for which statistics should be computed
	 * @param currentMonth
	 *            true if the month for which statistics should be computed is a
	 *            current month
	 * @param userId
	 * @return list with rows from database
	 */
	@SuppressWarnings("unchecked")
	private List<Object[]> getDataForMonthStatsFromDB(int year, int month, boolean currentMonth, Long userId) {
		Pair<LocalDate, LocalDate> timeInterval = computeTimeInterval(year, month, currentMonth);
		List<Object[]> rows = null;
		try {
			Query query = em.createQuery(
					"select firstDate, date, role.name from Task where user.id = :userId and firstDate between :fromDate and :toDate");
			query.setParameter("userId", userId);
			query.setParameter("fromDate", timeInterval.getElement0());
			query.setParameter("toDate", timeInterval.getElement1());
			rows = query.getResultList();
		} catch (Exception e) {
			logger.error("db error occured while retrieving tasks for statistic from date " + timeInterval.getElement0()
					+ " to date " + timeInterval.getElement1() + " userId: " + userId);
			throw e;
		}
		logger.info("tasks for statistics of user with id " + userId + " retrieved");
		return rows;
	}

	/**
	 * Computes time interval for which statistics should be computed.
	 * 
	 * <p>
	 * Time interval begins with Monday of first week of the given month, where
	 * first week is the week which contains the first Sunday of the month.
	 * <p>
	 * Time interval ends with Sunday of the last week of month, where last week
	 * is the week which contains the last Sunday of the month. If the month for
	 * which statistics should be computed is a current month, time interval
	 * ends with four days before current day.
	 * 
	 * @param year
	 *            year for which statistics should be computed
	 * @param month
	 *            month for which statistics should be computed
	 * @param currentMonth
	 *            true if the month for which statistics should be computed is a
	 *            current month
	 * @return date from/to which look for tasks in the database
	 */
	private Pair<LocalDate, LocalDate> computeTimeInterval(int year, int month, boolean currentMonth) {
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int nextMonth;
		int nextMonthYear;
		if (month == 12) {
			nextMonth = 1;
			nextMonthYear = year + 1;
		} else {
			nextMonth = month + 1;
			nextMonthYear = year;
		}
		LocalDate firstDayOfNextMonth = LocalDate.of(nextMonthYear, nextMonth, 1);
		LocalDate dateFrom = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() - 1);
		LocalDate dateTo;
		if (currentMonth) {
			dateTo = LocalDate.now().minusDays(4);
		} else {
			dateTo = firstDayOfNextMonth.minusDays(firstDayOfNextMonth.getDayOfWeek().getValue());
		}
		return Pair.createPair(dateFrom, dateTo);
	}

	private int computeCountOfWeeksInMonth(int year, int month) {
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int nextMonth;
		int nextMonthYear;
		if (month == 12) {
			nextMonth = 1;
			nextMonthYear = year + 1;
		} else {
			nextMonth = month + 1;
			nextMonthYear = year;
		}
		LocalDate firstDayOfNextMonth = LocalDate.of(nextMonthYear, nextMonth, 1);
		LocalDate dateFrom = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() - 1);
		LocalDate dateTo = firstDayOfNextMonth.minusDays(firstDayOfNextMonth.getDayOfWeek().getValue());
		return (int) ChronoUnit.WEEKS.between(dateFrom, dateTo.plusDays(1));
	}

	/**
	 * @param date
	 * @return string representation of number of week of given date.
	 */
	private String getWeekOfMonthString(LocalDate date) {
		int numberOfSundayInMonth = date.plusDays(7 - date.getDayOfWeek().getValue()).getDayOfMonth();
		if (numberOfSundayInMonth < 8)
			return "Week 1";
		if (numberOfSundayInMonth < 15)
			return "Week 2";
		if (numberOfSundayInMonth < 22)
			return "Week 3";
		if (numberOfSundayInMonth < 29)
			return "Week 4";
		return "Week 5";
	}

	/**
	 * 
	 * @param firstDate
	 *            date on which the task was initially planed
	 * @param date
	 *            date of task's completion
	 * @return 0 if task was completed before firstDate, 1 if task was completed
	 *         in firstDate, 2 if task was completed within 3 days after
	 *         firstDate, 3 otherwise
	 */
	private int getEffeciencyIndex(LocalDate firstDate, LocalDate date) {
		if (date == null)
			return 3;
		if (date.isBefore(firstDate))
			return 0;
		if (date.equals(firstDate))
			return 1;
		if (date.isBefore(firstDate.plusDays(4)))
			return 2;
		return 3;
	}
}
