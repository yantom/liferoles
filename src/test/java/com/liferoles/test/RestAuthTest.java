package com.liferoles.test;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;


public class RestAuthTest {
	
	public RestAuthTest(){
		/*
		baseURI = "https://localhost:8443/rest/auth";
		RestAssured.useRelaxedHTTPSValidation();
		//port = 8443;
		authentication = form("permanentuser@gmail.com", "permuser01", new FormAuthConfig("/j_security_check", "j_username", "j_password"));
	*/
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	
	
	
	@Test
	public void checkIfUserExistsInDB() {
		System.out.println("what the hdsfsdfsdf");
		//given().get("/check/novymail");
	}
	
	/*
	@Test
	public void loginUserMobile() {
		given()
		.auth()
		.form("testuser1", "r", new FormAuthConfig("/j_spring_security_check", "j_username", "j_password"))
		.when()
		.get("/formAuth")
		.then()
		.statusCode(200);
		
		
		User user = new User();
		user.setEmail("permanentuser@gmail.com");
		user.setPassword("permpass00");
		try{
			ra.registerUserMobile(user);
		}
		catch(Exception ex){
			ex.printStackTrace();
			fail("Creating user with right data failed.");
		}
	}*/
	
}
