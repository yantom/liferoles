package com.liferoles.rest;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.liferoles.controller.TaskManager;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.Task;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.IdResponse;
import com.liferoles.utils.AuthUtils;

@Path("/rest/tasks")
public class RestTask {
	
	private static final TaskManager tm = new TaskManager();
	
	@GET
	@Path("/web/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Task>> getTasks(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getInitTasks(userId,dateFrom);
    }
	
	@GET
	@Path("/m/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Task>> getTasksMobile(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws TokenValidationException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getInitTasks(userId,dateFrom);
    }
	
	@GET
	@Path("/week/web/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasksPerWeek(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getTasksForWeek(userId,dateFrom);
    }
	
	@GET
	@Path("/week/m/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasksPerWeekMobile(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws TokenValidationException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = AuthUtils.validateToken(token);
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getTasksForWeek(userId,dateFrom);
    }
	
	@POST
	@Path("/web")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createTask(Task task, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		task.setUser(u);
		IdResponse id = new IdResponse();
		id.setId(tm.createTask(task));
		return id;
    }
	
	@POST
	@Path("/m")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createTaskMobile(Task task, @Context HttpServletRequest hsr) throws TokenValidationException {
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(AuthUtils.validateToken(token));
		task.setUser(u);
		IdResponse id = new IdResponse();
		id.setId(tm.createTask(task));
		return id;
    }
	
	@PUT
	@Path("/web")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateTask(Task task, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		task.setUser(u);
		tm.updateTask(task);
    }
	
	@PUT
	@Path("/m")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateTaskMobile(Task task, @Context HttpServletRequest hsr) throws TokenValidationException {
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(AuthUtils.validateToken(token));
		task.setUser(u);
		tm.updateTask(task);
    }
	
	@DELETE
	@Path("/web")
    public void deleteTask(@QueryParam("taskId") Long taskId,@Context HttpServletRequest hsr) {
		Task t = new Task();
		t.setId(taskId);
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		t.setUser(u);
		tm.deleteTask(t);
    }

	@DELETE
	@Path("/m")
    public void deleteTaskMobile(@QueryParam("taskId") Long taskId,@Context HttpServletRequest hsr) throws TokenValidationException {
		Task t = new Task();
		t.setId(taskId);
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(AuthUtils.validateToken(token));
		t.setUser(u);
		tm.deleteTask(t);
    }
}
