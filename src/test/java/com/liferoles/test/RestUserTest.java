package com.liferoles.test;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.authentication.FormAuthConfig;
import com.jayway.restassured.filter.session.SessionFilter;

public class RestUserTest {
	private static SessionFilter sessionFilter;
	private static DataSource ds;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		useRelaxedHTTPSValidation();
		baseURI = "https://localhost:8443/rest/users";
		sessionFilter = new SessionFilter();
		ds = Utils.getDataSource();
	}
	@After
	public void afterMethod() throws Exception {
		Utils.deleteTestUser();
	}
	@Before
	public void beforeMethod() throws Exception {
		Utils.insertTestUser();
		//obtain session before each test
		given().auth()
		.form(PermanentUserData.user.getEmail(), PermanentUserData.user.getPassword(),
				new FormAuthConfig("/j_security_check", "j_username", "j_password"))
		.filter(sessionFilter).when().get("/");
		//needed to fill session with id of the user (web)
		given().filter(sessionFilter).get("/web");
	}
	@Test
	public void getUser() {
		// valid token
		given().header(PermanentUserData.tokenHeader).get("/m").then().statusCode(200).body("email", equalTo(PermanentUserData.user.getEmail()));
		// session
		given().filter(sessionFilter).get("/web").then().statusCode(200).body("email", equalTo(PermanentUserData.user.getEmail()));
	}
	@Test
	public void invalidTokenFailure(){
		given().header("Authorization", "Bearer Some-nonsense.which-does-not.match-any-token").get("/m").then()
		.statusCode(403);
	}
	@Test
	public void updateUserData() throws Exception {
		given().header(PermanentUserData.tokenHeader).contentType("application/json")
				.body("{\"personalMission\":\"fish\"}").post("/m/data").then().statusCode(204);
		try(Connection conn = ds.getConnection();
			PreparedStatement ps = conn.prepareStatement("select personalMission, firstDayOfWeek, email from appuser where id = ?");){
			ps.setLong(1, PermanentUserData.user.getId());
			try(ResultSet rs = ps.executeQuery()){
				rs.next();
				assertThat(rs.getString("email"),equalTo(PermanentUserData.user.getEmail()));
				assertThat(rs.getInt("firstDayOfWeek"),equalTo(0));
				assertThat(rs.getString("personalMission"),equalTo("fish"));
			}
		}
		given().filter(sessionFilter).contentType("application/json")
				.body("{\"firstDayOfWeek\":3}").post("/web/data").then()
				.statusCode(204);
		try(Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select personalMission, firstDayOfWeek, email from appuser where id = ?");){
				ps.setLong(1, PermanentUserData.user.getId());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					assertThat(rs.getString("email"),equalTo(PermanentUserData.user.getEmail()));
					assertThat(rs.getInt("firstDayOfWeek"),equalTo(3));
					assertThat(rs.getString("personalMission"),equalTo(""));
				}
			}
	}

	@Test
	public void updateUserEmail() throws Exception{
		// valid credentials
		given().header(PermanentUserData.tokenHeader).contentType("application/json")
				.body(("{'email':'mail1','password':'" + PermanentUserData.user.getPassword() + "'}").replace('\'', '"')).post("/m/mail")
				.then().statusCode(204);
		try(Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select email from appuser where id = ?");){
				ps.setLong(1, PermanentUserData.user.getId());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					assertThat(rs.getString("email"),equalTo("mail1"));
				}
		}
		given().filter(sessionFilter).contentType("application/json")
				.body(("{'email':'mail2','password':'" + PermanentUserData.user.getPassword() + "'}").replace('\'', '"')).post("/web/mail")
				.then().statusCode(204);
		try(Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select email from appuser where id = ?");){
				ps.setLong(1, PermanentUserData.user.getId());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					assertThat(rs.getString("email"),equalTo("mail2"));
				}
		}
		// invalid credentials
		given().header(PermanentUserData.tokenHeader).contentType("application/json")
				.body(("{'email':'wrong','password':'wrong'}").replace('\'', '"')).post("/m/mail").then().statusCode(401);
		given().filter(sessionFilter).contentType("application/json")
				.body(("{'email':'wrong','password':'wrong'}").replace('\'', '"')).post("/web/mail").then()
				.statusCode(401);
	}

	@Test
	public void updateUserPassword() throws Exception{
		// valid credentials
		String afterFirst;
		given().header(PermanentUserData.tokenHeader).contentType("application/json")
				.body(("{'newP':'new1','oldP':'" + PermanentUserData.user.getPassword() + "'}").replace('\'', '"'))
				.post("/m/password").then().statusCode(204);
		try(Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select password from appuser where id = ?");){
				ps.setLong(1, PermanentUserData.user.getId());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					afterFirst = rs.getString("password");
					assertThat(afterFirst,not(equalTo(PermanentUserData.hash)));
				}
		}
		given().filter(sessionFilter).contentType("application/json")
				.body(("{'newP':'new2','oldP':'new1'}").replace('\'', '"'))
				.post("/web/password").then().statusCode(204);
		try(Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("select password from appuser where id = ?");){
				ps.setLong(1, PermanentUserData.user.getId());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					assertThat(rs.getString("password"),not(equalTo(afterFirst)));
				}
		}
		// invalid credentials
		given().header(PermanentUserData.tokenHeader).contentType("application/json")
				.body(("{'newP':'newpassword','oldP':'wrong'}").replace('\'', '"')).post("/m/password").then()
				.statusCode(401);
		given().filter(sessionFilter).contentType("application/json")
				.body(("{'newP':'newpassword','oldP':'wrong'}").replace('\'', '"')).post("/web/password")
				.then().statusCode(401);
	}

	@Test
	public void logoutUser() {
		given().header(PermanentUserData.tokenHeader).post("/m/logout");
		given().header(PermanentUserData.tokenHeader).get("/m").then().statusCode(403);
		given().filter(sessionFilter).post("/web/logout").then().statusCode(204);
		String response = given().filter(sessionFilter).get("/web").then().statusCode(200).extract().asString();
		assertTrue(response.contains("ng-app=\"liferolesAuth\""));
	}

	@Test
	public void addTokensToBlacklist() {
		given().header(PermanentUserData.tokenHeader).get("/m").then().statusCode(200);
		given().header(PermanentUserData.tokenHeader2).get("/m").then().statusCode(200);
		given().filter(sessionFilter).post("/web/tokensBlacklist").then().statusCode(204);
		given().header(PermanentUserData.tokenHeader2).get("/m").then().statusCode(403);
	}
}
