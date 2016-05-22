package com.liferoles.rest;

import java.time.LocalDate;
import java.util.List;

import javax.ejb.EJB;
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

import com.liferoles.controller.AuthManager;
import com.liferoles.controller.Nvd3StatsService;
import com.liferoles.controller.TaskManager;
import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.TokenValidationException;
import com.liferoles.model.Task;
import com.liferoles.model.User;
import com.liferoles.rest.JSON.objects.IdResponse;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3ChartsData;

@Path("/rest/tasks")
public class RestTask {
	@EJB
	private TaskManager tm;
	@EJB
	private AuthManager am;
	@EJB
	private Nvd3StatsService nvd3Service;
	
	@GET
	@Path("/web/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Task>> getTasks(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws LiferolesRuntimeException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getInitTasks(userId,dateFrom);
    }
	
	@GET
	@Path("/m/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Task>> getTasksMobile(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getInitTasks(userId,dateFrom);
    }
	
	@GET
	@Path("/week/web/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasksPerWeek(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws LiferolesRuntimeException {
		Long userId = (Long) hsr.getSession().getAttribute("userId");
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getTasksForWeek(userId,dateFrom);
    }
	
	@GET
	@Path("/week/m/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasksPerWeekMobile(@Context HttpServletRequest hsr, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
    	LocalDate dateFrom = LocalDate.of(year, month, day);
		return tm.getTasksForWeek(userId,dateFrom);
    }
	
	@POST
	@Path("/web")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public IdResponse createTask(Task task, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
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
    public IdResponse createTaskMobile(Task task, @Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		task.setUser(u);
		IdResponse id = new IdResponse();
		id.setId(tm.createTask(task));
		return id;
    }
	
	@PUT
	@Path("/web/{taskId}")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateTask(@PathParam("taskId") Long taskId, Task task, @Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		User u = new User();
		task.setId(taskId);
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		task.setUser(u);
		tm.updateTask(task);
    }
	
	@PUT
	@Path("/m/{taskId}")
	@Consumes(MediaType.APPLICATION_JSON)
    public void updateTaskMobile(@PathParam("taskId") Long taskId, Task task, @Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		User u = new User();
		task.setId(taskId);
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		task.setUser(u);
		tm.updateTask(task);
    }
	
	@DELETE
	@Path("/web/{taskId}")
    public void deleteTask(@PathParam("taskId") Long taskId,@Context HttpServletRequest hsr) throws LiferolesRuntimeException {
		Task t = new Task();
		t.setId(taskId);
		User u = new User();
		u.setId((Long) hsr.getSession().getAttribute("userId"));
		t.setUser(u);
		tm.deleteTask(t);
    }

	@DELETE
	@Path("/m/{taskId}")
    public void deleteTaskMobile(@PathParam("taskId") Long taskId,@Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		Task t = new Task();
		t.setId(taskId);
		User u = new User();
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		u.setId(am.validateToken(token));
		t.setUser(u);
		tm.deleteTask(t);
    }
	
	@GET
	@Path("/web/stats/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Nvd3ChartsData getStats(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean currentMonth,@Context HttpServletRequest hsr){
		Nvd3ChartsData chartsData= null;
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		if(currentMonth == true)
			chartsData = nvd3Service.getJsonStatsData(year, month, true,userId);
		else
			chartsData = nvd3Service.getJsonStatsData(year, month, false,userId);
		return chartsData;
	}
	
	@GET
	@Path("/m/stats/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Nvd3ChartsData getStatsJWT(@PathParam("year") int year,@PathParam("month") int month, @QueryParam("last") boolean currentMonth,@Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException{
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		Nvd3ChartsData chartsData= null;
		if(currentMonth == true)
			chartsData = nvd3Service.getJsonStatsData(year, month, true,userId);
		else
			chartsData = nvd3Service.getJsonStatsData(year, month, false,userId);
		return chartsData;
	}
	
	@POST
	@Path("/m/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklogMobile(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr) throws TokenValidationException, LiferolesRuntimeException {
		String token = (hsr.getHeader("Authorization")).split(" ")[1];
		Long userId = am.validateToken(token);
		tm.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
	
	@POST
	@Path("/web/backlog/{year}/{month}/{day}")
	public void moveOldTasksToBacklog(@PathParam("year") int year,@PathParam("month") int month,@PathParam("day") int day,@Context HttpServletRequest hsr) throws LiferolesRuntimeException{
		Long userId = (Long) hsr.getSession().getAttribute("userId");
		tm.moveOldTasksToBacklog(userId,LocalDate.of(year, month, day));
	}
}
