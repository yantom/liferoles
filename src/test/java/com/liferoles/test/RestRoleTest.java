package com.liferoles.test;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.filter.session.SessionFilter;
import com.liferoles.model.Role;
import com.liferoles.model.RoleGoal;

public class RestRoleTest {
	private static SessionFilter sessionFilter;
	private static ObjectMapper om;
	private static DataSource ds;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.insertTestUser();
		useRelaxedHTTPSValidation();
		baseURI = "https://localhost:8443/rest/roles";
		sessionFilter = new SessionFilter();
		om = new ObjectMapper();
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

	@After
	public void afterMethod() throws Exception {
		Utils.deleteTestRoles();
	}

	@Before
	public void beforeMethod() throws Exception {
		Utils.insertTestRoles();
	}

	@Test
	public void createRole() throws Exception {
		Role r = new Role("roul", new ArrayList<RoleGoal>() {
			{
				add(new RoleGoal("goul", false));
			}
		});
		given().contentType("application/json").filter(sessionFilter).body(om.writeValueAsString(r)).post("/web").then()
				.body("id", greaterThan(0)).statusCode(200);
		given().contentType("application/json").header(PermanentUserData.tokenHeader).body(om.writeValueAsString(r))
				.post("/m").then().body("id", greaterThan(0)).statusCode(200);
	}

	@Test
	public void deleteRole() throws Exception {
		Utils.insertTestTasks();
		// del and move from r1 to r2, web
		given().param("newRoleId", PermanentUserData.role2.getId()).filter(sessionFilter)
				.delete("/web/" + PermanentUserData.role1.getId()).then().statusCode(204);
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select count(*) from task where role_id = ?");) {
			ps.setLong(1, PermanentUserData.role2.getId());
			try (ResultSet rs = ps.executeQuery();) {
				rs.next();
				assertTrue(rs.getLong(1) == 7);
			}
		}
		// del mob
		given().header(PermanentUserData.tokenHeader).delete("/m/" + PermanentUserData.role2.getId()).then()
				.statusCode(204);
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select * from task where role_id=?");) {
			ps.setLong(1, PermanentUserData.role2.getId());
			try (ResultSet rs = ps.executeQuery();) {
				assertTrue(!rs.next());
			}
		}
	}

	@Test
	public void getAllRoles() throws Exception {
		String jsonRoles = given().filter(sessionFilter).get("/web").then().extract().asString();
		assertThat(Arrays.asList((om.readValue(jsonRoles, Role[].class))),
				containsInAnyOrder(PermanentUserData.role1, PermanentUserData.role2));
		jsonRoles = given().header(PermanentUserData.tokenHeader).get("/m").then().extract().asString();
		assertThat(Arrays.asList((om.readValue(jsonRoles, Role[].class))),
				containsInAnyOrder(PermanentUserData.role1, PermanentUserData.role2));
	}

	@Test
	public void updateRole() throws Exception {
		Role r = SerializationUtils.clone(PermanentUserData.role1);
		Long id = r.getId();
		r.setUser(null);
		r.setId(null);
		r.setName("updatedName");
		given().contentType("application/json").filter(sessionFilter).body(om.writeValueAsString(r))
				.put("/web/" + id.toString()).then().statusCode(204);
		r.getGoals().get(1).setFinished(true);
		given().contentType("application/json").header(PermanentUserData.tokenHeader).body(om.writeValueAsString(r))
				.put("/m/" + id.toString()).then().statusCode(204);
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select * from role where id = ?");
				PreparedStatement ps2 = conn.prepareStatement("select finished from rolegoal where id = ?");) {
			ps.setLong(1, PermanentUserData.role1.getId());
			ps2.setLong(1, r.getGoals().get(1).getId());
			try (ResultSet rs = ps.executeQuery(); ResultSet rs2 = ps2.executeQuery();) {
				rs.next();
				assertThat(rs.getString("name"), equalTo("updatedName"));
				rs2.next();
				assertThat(rs2.getBoolean("finished"), equalTo(true));
			}
		}
	}
}
