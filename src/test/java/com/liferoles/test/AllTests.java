package com.liferoles.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RestAuthTest.class, RestUserTest.class, RestRoleTest.class, RestTaskTest.class})
public class AllTests {
	
}
