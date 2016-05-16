package com.liferoles;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.liferoles.controller.AuthUtils;
import com.liferoles.exceptions.LifeRolesException;

public class StartListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent ev) {
	}

	@Override
	public void contextInitialized(ServletContextEvent ev) {
		try {
			AuthUtils.setHashKey();
		} catch (LifeRolesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
