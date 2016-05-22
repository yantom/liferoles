package com.liferoles.test;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.filter.session.SessionFilter;
import com.liferoles.model.Role;
import com.liferoles.model.Task;
import com.liferoles.rest.JSON.objects.nvd3stats.Nvd3ChartsData;

public class RestTaskTest {

	private static SessionFilter sessionFilter;
	private static ObjectMapper om;
	private static DataSource ds;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.insertTestUser();
		Utils.insertTestRoles();
		useRelaxedHTTPSValidation();
		baseURI = "https://localhost:8443/rest/tasks";
		sessionFilter = new SessionFilter();
		om=new ObjectMapper();
		ds = Utils.getDataSource();
		given().auth()
		.form(PermanentUserData.user.getEmail(), PermanentUserData.user.getPassword(),
				new FormAuthConfig("/j_security_check", "j_username", "j_password"))
		.filter(sessionFilter).when().get("/");
		given().filter(sessionFilter).get("https://localhost:8443/rest/users/web");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Utils.deleteTestUser();
	}

	@Before
	public void setUp() throws Exception {
		Utils.insertTestTasks();
	}

	@After
	public void tearDown() throws Exception {
		Utils.deleteTestTasks();
	}
	
	@Test
    public void getInitTasks() throws Exception{
		String jsonTasks = given().filter(sessionFilter).get("/web/2016/4/25").then().extract().asString();
		Task[][] returned = om.readValue(jsonTasks, Task[][].class);
		assertThat(returned[4][0],equalTo(PermanentUserData.tasks.get(1)));
		assertThat(returned[0][0],equalTo(PermanentUserData.tasks.get(2)));
		assertThat(returned[2][0],equalTo(PermanentUserData.tasks.get(3)));
		Task[] futureTasks = {PermanentUserData.tasks.get(4),PermanentUserData.tasks.get(5),PermanentUserData.tasks.get(6)};
		assertThat(Arrays.asList(returned[1]),containsInAnyOrder(futureTasks));
		assertThat(returned[1].length, equalTo(futureTasks.length));
		
		jsonTasks =given().header(PermanentUserData.tokenHeader).get("/m/2016/5/2").then().extract().asString();
		returned = om.readValue(jsonTasks, Task[][].class);
		assertThat(returned[0][0],equalTo(PermanentUserData.tasks.get(2)));
		assertThat(returned[3][0],equalTo(PermanentUserData.tasks.get(3)));
		assertThat(returned[2][0],equalTo(PermanentUserData.tasks.get(4)));
		Task[] futureTasks2 = {PermanentUserData.tasks.get(5),PermanentUserData.tasks.get(6)};
		assertThat(returned[1].length, equalTo(futureTasks2.length));
		assertThat(Arrays.asList(returned[1]),containsInAnyOrder(futureTasks2));
    }
	
    @Test
    public void getTasksPerWeek() throws Exception{
		String jsonTasks = given().filter(sessionFilter).get("/week/web/2016/5/14").then().extract().asString();
		Task[] returned = om.readValue(jsonTasks, Task[].class);
		assertThat(returned.length, equalTo(2));
		assertThat(Arrays.asList(returned),containsInAnyOrder(PermanentUserData.tasks.get(3),PermanentUserData.tasks.get(4)));
		jsonTasks = given().header(PermanentUserData.tokenHeader).get("/week/m/2016/5/9").then().extract().asString();
		returned = om.readValue(jsonTasks, Task[].class);
		assertThat(returned.length, equalTo(1));
		assertThat(Arrays.asList(returned),contains(PermanentUserData.tasks.get(3)));
    }
	
	@Test
    public void createTask() throws Exception {
		Task t = new Task("newtask",new Role(PermanentUserData.role1.getId()));
		given().contentType("application/json").filter(sessionFilter).body(om.writeValueAsString(t)).post("/web").then().body("id",greaterThan(0)).statusCode(200);
		t.setNote("note");
		given().contentType("application/json").header(PermanentUserData.tokenHeader).body(om.writeValueAsString(t)).post("/m").then().body("id",greaterThan(0)).statusCode(200);
    }
	
	
	@Test
    public void updateTask() throws Exception {
		//update existing task
		Task t = new Task();
		Long id = PermanentUserData.tasks.get(0).getId();
		t.setName("updated");
		t.setRole(new Role(PermanentUserData.tasks.get(0).getRole().getId()));
		given().contentType("application/json").filter(sessionFilter).body(om.writeValueAsString(t)).put("/web/"+id.toString()).then().statusCode(204);
		
		//delete it
		given().header(PermanentUserData.tokenHeader).delete("/m/"+PermanentUserData.tasks.get(0).getId()).then().statusCode(204);
		
		//test if is recreated with correct values
		t.setNote("note");
		LocalTime moment = LocalTime.now();
		int minutes = moment.getMinute();
		int hours = moment.getHour();
		t.setTime(moment);
		given().contentType("application/json").header(PermanentUserData.tokenHeader).body(om.writeValueAsString(t)).put("/m/"+id.toString()).then().statusCode(204);
		try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("select * from task where name = ?");){
			ps.setString(1, "updated");
			try(ResultSet rs = ps.executeQuery();){
				rs.next();
				assertThat(rs.getString("name"),equalTo("updated"));
				assertThat(rs.getString("note"),equalTo("note"));
				assertThat(rs.getTime("time").getMinutes(),equalTo(minutes));
				assertThat(rs.getTime("time").getHours(),equalTo(hours));
				//rest was set to default
				assertThat(rs.getBoolean("finished"),equalTo(false));
				assertThat(rs.getBoolean("important"),equalTo(false));
				assertThat(rs.getBoolean("finished"),equalTo(false));
				assertThat(rs.getDate("date"),equalTo(null));
				assertThat(rs.getDate("firstDate"),equalTo(null));
			}
		}
	}
	@Test
    public void deleteTask() throws Exception{
		given().filter(sessionFilter).delete("/web/"+PermanentUserData.tasks.get(1).getId()).then().statusCode(204);
		try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("select count(*) from task where role_id = ?");){
			ps.setLong(1, PermanentUserData.role1.getId());
			try(ResultSet rs = ps.executeQuery();){
				rs.next();
				assertTrue(rs.getLong(1) == 3);
			}
		}
		//return 409 if trying to delete already deleted
		given().header(PermanentUserData.tokenHeader).delete("/m/"+PermanentUserData.tasks.get(1).getId()).then().statusCode(409);
		given().header(PermanentUserData.tokenHeader).delete("/m/"+PermanentUserData.tasks.get(2).getId()).then().statusCode(204);
		try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("select count(*) from task where role_id = ?");){
			ps.setLong(1, PermanentUserData.role1.getId());
			try(ResultSet rs = ps.executeQuery();){
				rs.next();
				assertTrue(rs.getLong(1) == 2);
			}
		}
    }
	@Test
	 public void moveTasksToBacklog() throws Exception {
		given().header(PermanentUserData.tokenHeader).post("/m/backlog/2016/04/26").then().statusCode(204);
		try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("select count (*) from task where appuser_id = ? and date is null")){
			ps.setLong(1, PermanentUserData.user.getId());
			try(ResultSet rs = ps.executeQuery();){
				rs.next();
				assertThat(rs.getInt(1),equalTo(2));
			}
		}
		given().filter(sessionFilter).post("/web/backlog/2016/05/16").then().statusCode(204);
		try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("select count (*) from task where appuser_id = ? and date is null")){
			ps.setLong(1, PermanentUserData.user.getId());
			try(ResultSet rs = ps.executeQuery();){
				rs.next();
				assertThat(rs.getInt(1),equalTo(3));
			}
		}
	}
	
	@Test
	public void getStats() throws Exception{
		Nvd3ChartsData expected = om.readValue("{'pieChartItems':[{'key':'role1','y':3},{'key':'role2','y':2}],'barChartItemsRole':[{'key':'Earlier than planned','values':[{'x':'role1','y':0},{'x':'role2','y':0}]},{'key':'Day D','values':[{'x':'role1','y':0},{'x':'role2','y':0}]},{'key':'Within 3 days','values':[{'x':'role1','y':0},{'x':'role2','y':1}]},{'key':'Postponed','values':[{'x':'role1','y':3},{'x':'role2','y':1}]}],'barChartItemsWeek':[{'key':'Earlier than planned','values':[{'x':'Week 1','y':0},{'x':'Week 2','y':0},{'x':'Week 3','y':0},{'x':'Week 4','y':0},{'x':'Week 5','y':0}]},{'key':'Day D','values':[{'x':'Week 1','y':0},{'x':'Week 2','y':0},{'x':'Week 3','y':0},{'x':'Week 4','y':0},{'x':'Week 5','y':0}]},{'key':'Within 3 days','values':[{'x':'Week 1','y':0},{'x':'Week 2','y':0},{'x':'Week 3','y':1},{'x':'Week 4','y':0},{'x':'Week 5','y':0}]},{'key':'Postponed','values':[{'x':'Week 1','y':1},{'x':'Week 2','y':1},{'x':'Week 3','y':1},{'x':'Week 4','y':0},{'x':'Week 5','y':1}]}]}".replace('\'', '"'), Nvd3ChartsData.class);
		Nvd3ChartsData stats = om.readValue(given().header(PermanentUserData.tokenHeader).get("/m/stats/2016/5").then().extract().asString(), Nvd3ChartsData.class);
		assertThat(stats.getPieChartItems(), containsInAnyOrder(expected.getPieChartItems().get(0),expected.getPieChartItems().get(1)));
		for(int i=0;i<4;i++){
			assertThat(stats.getBarChartItemsRole().get(i).getValues(),containsInAnyOrder(expected.getBarChartItemsRole().get(i).getValues().toArray()));
			assertThat(stats.getBarChartItemsWeek().get(i).getValues(),containsInAnyOrder(expected.getBarChartItemsWeek().get(i).getValues().toArray()));
		}
		stats = om.readValue(given().filter(sessionFilter).get("/web/stats/2016/5").then().extract().asString(), Nvd3ChartsData.class);
		assertThat(stats.getPieChartItems(), containsInAnyOrder(expected.getPieChartItems().get(0),expected.getPieChartItems().get(1)));
		for(int i=0;i<4;i++){
			assertThat(stats.getBarChartItemsRole().get(i).getValues(),containsInAnyOrder(expected.getBarChartItemsRole().get(i).getValues().toArray()));
			assertThat(stats.getBarChartItemsWeek().get(i).getValues(),containsInAnyOrder(expected.getBarChartItemsWeek().get(i).getValues().toArray()));
		}
	}
}
