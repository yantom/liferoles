package com.liferoles.test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jayway.restassured.response.Header;
import com.liferoles.model.Day;
import com.liferoles.model.Role;
import com.liferoles.model.RoleGoal;
import com.liferoles.model.Task;
import com.liferoles.model.User;

public class PermanentUserData {
	public static final User user = new User(new Long(999999999), "permuser@gmail.com", Day.MON, "", "permuser1",
			"uFUW8LBSpdF3tFT9nHjg1w==");
	public static final String hash = "VMERKwkwAdj9O1yx6JzDpA9cas9t378kF3petT+s5hS1AqRiDnYxGU+WcoZ1kQnWNBQiIZn/jTyvHIvCc44opg==";
	public static final Header tokenHeader = new Header("Authorization",
			"Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5OTk5OTk5OTkiLCJqdGkiOiIxMDQifQ.SiLTxh0__TADuBPOKzxNH2JoDvOnuT-kE4ynA0wfoRo");
	public static final Header tokenHeader2 = new Header("Authorization",
			"Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5OTk5OTk5OTkiLCJqdGkiOiIxMDUifQ.xwDNbFaQwPzgXNamegXH_QwWHo5fFtFyKQ5YOucJGJM");
	public static final Long tokenJIT = new Long(104);
	public static final Long token2JIT = new Long(105);
	public static Role role1 = new Role(new Long(1000000000), "role1", new ArrayList<RoleGoal>() {
		{
			add(new RoleGoal(new Long(1000000000), "goal1", false));
			add(new RoleGoal(new Long(1000000001), "goal2", false));
		}
	}, user);
	public static Role role2 = new Role(new Long(1000000001), "role2", new ArrayList<RoleGoal>(), user);
	public static List<Role> roles = Arrays.asList(role1, role2);
	// Long id, String name, boolean important, Role role, User user, LocalDate
	// date, LocalTime time, LocalDate firstDate, boolean finished, String note

	public static List<Task> tasks = Arrays.asList(
			new Task(new Long(1000000000), "task1", true, role1, user, LocalDate.of(2016, 4, 24), LocalTime.of(22, 0),
					LocalDate.of(2016, 4, 24), true, "note1"), // out of may
																// stats
			new Task(new Long(1000000001), "task2", false, role1, user, LocalDate.of(2016, 4, 25), null,
					LocalDate.of(2016, 4, 25), false, null), // week1, dayD
			new Task(new Long(1000000002), "task3", false, role1, user, null, null, LocalDate.of(2016, 5, 4), true,
					null), // week2, postponed(in backlog)
			new Task(new Long(1000000003), "task4", false, role1, user, LocalDate.of(2016, 5, 15), null,
					LocalDate.of(2016, 5, 15), false, null), // week3,
																// postponed(not
																// finished)
			new Task(new Long(1000000004), "task5", false, role2, user, LocalDate.of(2016, 5, 16), null,
					LocalDate.of(2016, 5, 14), true, null), // week3, +3days
			new Task(new Long(1000000005), "task6", false, role2, user, LocalDate.of(2016, 6, 5), null,
					LocalDate.of(2016, 5, 29), true, null), // week5,
															// postponed(finished
															// late)
			new Task(new Long(1000000006), "task7", false, role2, user, LocalDate.of(2016, 5, 30), null,
					LocalDate.of(2016, 5, 30), true, null) // out of may stats
	);
}
