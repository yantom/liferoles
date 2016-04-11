package com.liferoles.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtils {
	
	private static SessionFactory sessionFactory;
	private static final Logger logger = LoggerFactory.getLogger(HibernateUtils.class);
	public static void buildSessionFactory(boolean appdb){
		try {
			if(appdb)
			sessionFactory = new Configuration().configure().buildSessionFactory();
			else
			sessionFactory = new Configuration().configure("hibernate-test.cfg.xml").buildSessionFactory();

		}catch(Throwable e){
			logger.error("wtf",e);
			throw e;
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;}
	
	public static void closeSessionFactory() {
        sessionFactory.close();
    }
}
