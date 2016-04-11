package com.liferoles;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.liferoles.exceptions.LifeRolesException;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;

public class StartListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent ev) {
	}

	@Override
	public void contextInitialized(ServletContextEvent ev) {
		ServletContext servletContext = ev.getServletContext();
		
		try {
			AuthUtils.setHashKey();
			HibernateUtils.buildSessionFactory(true);
		} catch (LifeRolesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
