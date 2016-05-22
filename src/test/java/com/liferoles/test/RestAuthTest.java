package com.liferoles.test;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.hamcrest.core.IsNot;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.authentication.FormAuthConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestAuthTest {
	
	/**
	 * missing tests for registerUserWeb (can not automatically send captcha)
	 * missing tests for sendResetLink, resetUserPassword (requires email)
	 */
	
	public RestAuthTest(){
		baseURI = "https://localhost:8443";
		useRelaxedHTTPSValidation();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DataSource ds = Utils.getDataSource();
        try(Connection conn = ds.getConnection();PreparedStatement ps = conn.prepareStatement("delete from appuser where email like 'testuser1@gmail.com'");){
        	ps.execute();
        }
	}

	@Test
    public void a_registerUserMobile() {
		//valid registration
		given().contentType("application/json").body("{'email':'testuser1@gmail.com','password':'testuser1'}".replace('\'', '"')).post("/rest/auth/m/reg").then().body("id",greaterThan(0)).statusCode(200);
		//registration with already used email
		given().contentType("application/json").body("{'email':'testuser1@gmail.com','password':'testuser1'}".replace('\'', '"')).post("/rest/auth/m/reg").then().statusCode(greaterThanOrEqualTo(300));
	}
	
	@Test
    public void b_loginJWT(){
		//valid credentials
		given().contentType("application/json").body("{'email':'testuser1@gmail.com','password':'testuser1'}".replace('\'', '"')).post("/rest/auth/m/login").then().body("token",IsNot.not(isEmptyOrNullString())).statusCode(200);
		//invalid credentials
		given().contentType("application/json").body("{'email':'testuser1@gmail.com','password':'testuser2'}".replace('\'', '"')).post("/rest/auth/m/login").then().statusCode(204);
	}
	
	@Test
	public void c_loginSession(){
		//valid credentials
		String responseFromGoodPasswd = given().auth().form("testuser1@gmail.com", "testuser1", new FormAuthConfig("/j_security_check", "j_username", "j_password")).when().get("/").then().extract().asString();
		assertTrue(responseFromGoodPasswd.contains("ng-app=\"liferolesApp\""));
		//invalid credentials
		String responseFromBadPasswd = given().auth().form("testuser1@gmail.com", "testuser2", new FormAuthConfig("/j_security_check", "j_username", "j_password")).when().get("/").then().extract().asString();
		assertTrue(responseFromBadPasswd.contains("ng-app=\"liferolesAuth\""));
	}
	
	@Test
	public void d_checkIfUserExistsInDB(){
		//user which exists
		given().get("/rest/auth/check/testuser1@gmail.com").then().body("response", equalTo(true));
		//user which does not exist
		given().get("/rest/auth/check/testuser2@gmail.com").then().body("response", equalTo(false));
	}
	
}
