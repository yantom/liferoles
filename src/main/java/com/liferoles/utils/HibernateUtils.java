package com.liferoles.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;

import com.liferoles.model.LifeRolesDBException;

public class HibernateUtils {
	
	private static SessionFactory sessionFactory;
	
	public static void buildSessionFactory(boolean appdb)throws LifeRolesDBException{
		try {
			if(appdb)
			sessionFactory = new Configuration().configure().buildSessionFactory();
			else
			sessionFactory = new Configuration().configure("hibernate-test.cfg.xml").buildSessionFactory();

		}catch(Throwable e){
			throw new LifeRolesDBException("problem occured while creating sessionfactory",e);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;}
	
	public static void closeSessionFactory() {
        sessionFactory.close();
    }
	
	public static void createTables() throws LifeRolesDBException {
		Session session = sessionFactory.openSession();

		try (FileReader fr = new FileReader(new File("src/main/resources/initdb.sql"))) {
				Transaction tx = null;
			try{
						
					tx= session.beginTransaction();
					session.doWork(new Work() {

						public void execute(Connection conn) throws SQLException {
							ScriptRunner sr = new ScriptRunner(conn, false, true);
							try {
								sr.runScript(fr);
							} catch (IOException e) {
								throw new SQLException("error occured while reading init sql file", e);
							}
						}});
					tx.commit();
			}catch(HibernateException e){
				if(tx != null){
					tx.rollback();
				}
				throw new LifeRolesDBException("error occured while creating tables",e);
			}
			finally{
				session.close();
			}
		} catch (IOException e) {
			throw new LifeRolesDBException("error occured while reading init sql file", e);
		}
	}
	
	public static void dropTables() throws LifeRolesDBException{
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.createSQLQuery("DROP TABLE task").executeUpdate();
			session.createSQLQuery("DROP TABLE role").executeUpdate();
			session.createSQLQuery("DROP TABLE appuser").executeUpdate();
			tx.commit();
		}catch(HibernateException e){
			if(tx != null){
				tx.rollback();
			}
			throw new LifeRolesDBException("error occured while deleting tables",e);
		}finally{
			session.close();
		}
	}
}
