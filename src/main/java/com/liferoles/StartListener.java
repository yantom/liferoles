package com.liferoles;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.liferoles.controller.RoleManager;
import com.liferoles.controller.TaskManager;
import com.liferoles.controller.UserManager;
import com.liferoles.utils.HibernateUtils;

public class StartListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent ev) {
	}

	@Override
	public void contextInitialized(ServletContextEvent ev) {
		ServletContext servletContext = ev.getServletContext();
		try {
			HibernateUtils.buildSessionFactory(true);
		} catch (LifeRolesDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
