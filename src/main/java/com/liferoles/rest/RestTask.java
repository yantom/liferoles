package com.liferoles.rest;

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

import com.liferoles.LifeRolesDBException;
import com.liferoles.controller.TaskManager;
import com.liferoles.model.Task;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.BooleanResponse;
import com.liferoles.rest.JSON.IdResponse;

@Path("/rest/tasks")
public class RestTask {
	
	private static final TaskManager tm = new TaskManager();
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasks(@Context HttpServletRequest hsr) {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	List<Task> t = null;
		try {
			t = tm.getTasksWithoutHistory(userId);
		} catch (LifeRolesDBException e) {
			e.printStackTrace();
			return null;
		}
    	return t;
    }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createTask(Task task, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		task.setUser(u);
		IdResponse id = new IdResponse();
		try{
		id.setId(tm.createTask(task));}
		catch (LifeRolesDBException e) {
			e.printStackTrace();
		}
		return id;
    }
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public BooleanResponse updateTask(Task task, @Context HttpServletRequest hsr) {
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		task.setUser(u);
		try{
			tm.updateTask(task);}
			catch (LifeRolesDBException e) {
				e.printStackTrace();
				return new BooleanResponse(false);
			}
		return new BooleanResponse(true);
    }
	
	@DELETE
    public BooleanResponse deleteTask(@QueryParam("taskId") Long taskId,@Context HttpServletRequest hsr) {
		Task t = new Task();
		t.setId(taskId);
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		t.setUser(u);
		try{
			tm.deleteTask(t);}
			catch (LifeRolesDBException e) {
				e.printStackTrace();
				return new BooleanResponse(false);
			}
		return new BooleanResponse(true);
    }
}
