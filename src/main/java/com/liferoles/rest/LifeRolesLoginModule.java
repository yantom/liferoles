package com.liferoles.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.security.auth.spi.DatabaseServerLoginModule;

import com.liferoles.LifeRolesDBException;
import com.liferoles.LifeRolesException;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;

public class LifeRolesLoginModule extends DatabaseServerLoginModule {
	
	@Override
	public String createPasswordHash(String username, String password, String digestOption)
	   {
		if(password.length()==128)
			return password;
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
