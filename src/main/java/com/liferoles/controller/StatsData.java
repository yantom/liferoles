package com.liferoles.controller;

import java.util.HashMap;
import java.util.Map;


/**
 * General class for holding the stats data.
 * 
 * Class has 3 properties:
 * 
 * <p>
 * Map <b>countOfTasksPerRole</b> is a Map which Key is role name and Value is
 * count of tasks finished
 * 
 * <p>
 * Map[] <b>countOfTasksPerRoleAndEffeciency</b> is an Array with four Maps,
 * where each Maps index represents the time elapsed between the day to which
 * the task was initially scheduled and the day of completion. Index 0 - tasks
 * finished before the planned day, 1 - tasks finished in the planned day, 2 -
 * tasks finished within 3 days, 3 - postponed tasks. In each Map, Key is a role
 * name and Value is the count of tasks. Eg.
 * countOfTasksPerRoleAndEffeciency[3].get('Student') will return count of
 * postponed tasks of role 'Student'.
 * 
 * <p>
 * Map[] <b>countOfTasksPerWeekAndEffeciency</b> is similar to the previous,
 * just the Map Key is not a role name but a string representation of the number
 * of the week in month. Eg.
 * countOfTasksPerWeekAndEffeciency[2].get('Week 2') will return count of
 * tasks finished within 3 days after plan, in the second week of the month.
 * 
 * @author Honzator
 *
 */
public class StatsData {
	private Map<String, Integer> countOfTasksPerRole;
	private Map<String, Integer>[] countOfTasksPerRoleAndEffeciency;
	private Map<String, Integer>[] countOfTasksPerWeekAndEffeciency;

	@SuppressWarnings("unchecked")
	public StatsData() {
		setCountOfTasksPerRole(new HashMap<>());
		setCountOfTasksPerRoleAndEffeciency((Map<String, Integer>[]) new Map[4]);
		setCountOfTasksPerWeekAndEffeciency((Map<String, Integer>[]) new Map[4]);
	}

	public Map<String, Integer> getCountOfTasksPerRoleData() {
		return countOfTasksPerRole;
	}

	public void setCountOfTasksPerRole(Map<String, Integer> countOfTasksPerRole) {
		this.countOfTasksPerRole = countOfTasksPerRole;
	}

	public Map<String, Integer>[] getCountOfTasksPerRoleAndEffeciencyData() {
		return countOfTasksPerRoleAndEffeciency;
	}

	public void setCountOfTasksPerRoleAndEffeciency(Map<String, Integer>[] countOfTasksPerRoleAndEffeciency) {
		this.countOfTasksPerRoleAndEffeciency = countOfTasksPerRoleAndEffeciency;
	}

	public Map<String, Integer>[] getCountOfTasksPerWeekAndEffeciencyData() {
		return countOfTasksPerWeekAndEffeciency;
	}

	public void setCountOfTasksPerWeekAndEffeciency(Map<String, Integer>[] countOfTasksPerWeekAndEffeciency) {
		this.countOfTasksPerWeekAndEffeciency = countOfTasksPerWeekAndEffeciency;
	}
}
