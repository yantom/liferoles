package com.liferoles.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class Utils {
	public static DataSource getDataSource() throws IOException {
        Properties config = new Properties();
        config.load(Utils.class.getResourceAsStream("/datasource.properties"));
        PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setServerName(config.getProperty("server"));
		ds.setDatabaseName(config.getProperty("db"));
		ds.setPortNumber(Integer.parseInt(config.getProperty("port")));
		ds.setUser(config.getProperty("user"));
		ds.setPassword(config.getProperty("password"));
        return ds;
    }
	
	public static void insertTestUser() throws IOException, SQLException{
		DataSource ds = getDataSource();
        try(
        		Connection conn = ds.getConnection();
        		PreparedStatement ps = conn.prepareStatement("insert into appuser (id, email, password, salt) values (?,?,?,?)");
        		PreparedStatement ps2 = conn.prepareStatement("insert into tokens (jit,appuser_id,blacklist) values (?,?,false)");
        		){
        	ps.setLong(1, PermanentUserData.user.getId());
        	ps.setString(2, PermanentUserData.user.getEmail());
        	ps.setString(3, PermanentUserData.hash);
        	ps.setString(4, PermanentUserData.user.getSalt());
        	ps.execute();
        	ps2.setLong(1, PermanentUserData.tokenJIT);
        	ps2.setLong(2, PermanentUserData.user.getId());
        	ps2.execute();
        	ps2.setLong(1, PermanentUserData.token2JIT);
        	ps2.setLong(2, PermanentUserData.user.getId());
        	ps2.execute();
        }
	}
	
	public static void insertTestRoles() throws IOException, SQLException{
		DataSource ds = getDataSource();
        try(
        		Connection conn = ds.getConnection();
        		PreparedStatement ps = conn.prepareStatement("insert into role (id,appuser_id,name) values (?,?,?)");
        		PreparedStatement ps2 = conn.prepareStatement("insert into rolegoal (id,role_id,name,finished) values (?,?,?,?)");){
        	ps.setLong(2,PermanentUserData.user.getId());
        	for(int i = 0;i<PermanentUserData.roles.size();i++){
        		ps.setLong(1,PermanentUserData.roles.get(i).getId());
            	ps.setString(3,PermanentUserData.roles.get(i).getName());
            	ps.execute();
        	}
        	ps2.setLong(2, PermanentUserData.role1.getId());
        	for(int i=0;i<PermanentUserData.role1.getGoals().size();i++){
        		ps2.setLong(1, PermanentUserData.role1.getGoals().get(i).getId());
            	ps2.setString(3, PermanentUserData.role1.getGoals().get(i).getName());
            	ps2.setBoolean(4, PermanentUserData.role1.getGoals().get(i).isFinished());
            	ps2.execute();
        	}
        }
	}
	
	public static void insertTestTasks() throws IOException, SQLException{
		DataSource ds = getDataSource();
        try(
        		Connection conn = ds.getConnection();
        		PreparedStatement ps = conn.prepareStatement("insert into task (id, name, important, role_id, appuser_id, date, time, firstDate, finished, note) values (?,?,?,?,?,?,?,?,?,?)");){
        	ps.setLong(5, PermanentUserData.user.getId());
        	LocalTime t;
        	LocalDate d;
        	for(int i=0;i<PermanentUserData.tasks.size();i++){
        		ps.setLong(1, PermanentUserData.tasks.get(i).getId());
        		ps.setString(2, PermanentUserData.tasks.get(i).getName());
        		ps.setBoolean(3, PermanentUserData.tasks.get(i).isImportant());
        		ps.setLong(4, PermanentUserData.tasks.get(i).getRole().getId());
        		d = PermanentUserData.tasks.get(i).getDate();
        		if(d == null)
        			ps.setNull(6, java.sql.Types.DATE);
        		else
        			ps.setDate(6, Date.valueOf(d));
        		t = PermanentUserData.tasks.get(i).getTime();
        		if(t == null)
        			ps.setNull(7, java.sql.Types.TIME);
        		else
        			ps.setTime(7, Time.valueOf(t));
        		ps.setDate(8, Date.valueOf(PermanentUserData.tasks.get(i).getFirstDate()));
        		ps.setBoolean(9, PermanentUserData.tasks.get(i).isFinished());
        		ps.setString(10, PermanentUserData.tasks.get(i).getNote());
        		ps.execute();
        	}
        }
	}
	
	public static void deleteTestUser() throws IOException, SQLException{
		DataSource ds = getDataSource();
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("delete from appuser where id = ?");)
				{
			ps.setLong(1, PermanentUserData.user.getId());
			ps.execute();
		}
	}
	
	public static void deleteTestRoles() throws IOException, SQLException{
		DataSource ds = getDataSource();
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("delete from role where appuser_id = ?");)
				{
			ps.setLong(1, PermanentUserData.user.getId());
			ps.execute();
		}
	}
	
	public static void deleteTestTasks() throws IOException, SQLException{
		DataSource ds = getDataSource();
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement("delete from task where appuser_id = ?");)
				{
			ps.setLong(1, PermanentUserData.user.getId());
			ps.execute();
		}
	}
}
