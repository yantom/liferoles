package com.liferoles.rest;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.security.auth.spi.DatabaseServerLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LifeRolesException;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;

public class LifeRolesLoginModule extends DatabaseServerLoginModule {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseServerLoginModule.class);
	@Override
	public String createPasswordHash(String username, String password, String digestOption)
	   {
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		String salt = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("select salt from User where email = :email");
			query.setString("email", username);
			salt = (String)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("wtf", e);
			throw new SecurityException("unable to authenticate user - can not retrieve salt",e);
		}
		finally {
			session.close();
		}
		String hash;
		try {
			hash = AuthUtils.computeHash(password, salt).getHash();
		} catch (LifeRolesException e) {
			throw new SecurityException("unable to authenticate user - can not compute password hash",e);
		}
		return hash;
	   }
}
