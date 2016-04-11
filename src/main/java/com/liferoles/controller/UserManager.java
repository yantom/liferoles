package com.liferoles.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LifeRolesAuthException;
import com.liferoles.exceptions.LifeRolesException;
import com.liferoles.model.*;
import com.liferoles.rest.JSON.BarChartDataItem;
import com.liferoles.rest.JSON.BarChartDataValue;
import com.liferoles.rest.JSON.ChartsData;
import com.liferoles.rest.JSON.PieChartDataItem;
import com.liferoles.utils.AuthUtils;
import com.liferoles.utils.HibernateUtils;
import com.liferoles.utils.SaltHashPair;

@Stateless
public class UserManager {
	private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
	private javax.mail.Session gmailSession;

	public UserManager(){
		try {
			gmailSession = InitialContext.doLookup("java:jboss/mail/Gmail");
		} catch (NamingException e) {
			logger.error("cannot find mail resource",e);
		}
	}
	public Long createUser(User user) throws LifeRolesAuthException{
		SaltHashPair shp;
		shp = AuthUtils.computeHash(user.getPassword(), null);
		user.setPassword(shp.getHash());
		user.setSalt(shp.getSalt());
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		Long id = null;
		try{
			tx = session.beginTransaction();
			id = (Long)session.save(user);
			user.setId(id);
			tx.commit();
		}catch (HibernateException e) {
			if (tx != null) tx.rollback();
			logger.error("db error occurred while creating " + user.toString());
			throw e;
			} 
		finally {
			session.close();
		}
		logger.info(user.toString() + " created");
		return id;
	}
	
	public void deleteUser(User user){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			session.delete(user);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while deleting " + user.toString());
			throw e;
		}
		finally {
			session.close();
		}
		logger.info(user.toString() + " deleted");
	}
	
	public void updateUserPassword(User user) throws LifeRolesAuthException{
		SaltHashPair shp = AuthUtils.computeHash(user.getPassword(), null);
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("UPDATE User set password = :password, salt = :salt  WHERE id = :id");
			query.setString("password", shp.getHash());
			query.setString("salt", shp.getSalt());
			query.setLong("id", user.getId());
			query.executeUpdate();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating password of user with id " + user.getId());
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("password of user with id " + user.getId() + " updated");
	}
	
	public void updateUserEmail(User user){
		Transaction tx = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("UPDATE User set email = :email WHERE id = :id");
			query.setString("email", user.getEmail());
			query.setLong("id", user.getId());
			query.executeUpdate();
			tx.commit();
		}
		catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating email of user with id " + user.getId());
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("email of user with id " + user.getId() + " updated");
	}
	
	public void updateUserData(User user){
		Transaction tx = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("UPDATE User set firstDayOfWeek = :firstDayOfWeek WHERE id = :id");
			query.setParameter("firstDayOfWeek", user.getFirstDayOfWeek());
			query.setLong("id", user.getId());
			query.executeUpdate();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while updating data of user with id " + user.getId());
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("data of user with id " + user.getId() + " updated");
	}
	
	public User getUserById(Long id){
		User u = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where id = :id");
			query.setLong("id", id);
			u = (User)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving user with id " + id);
			throw e;
		}
		finally {
			session.close();
		}
		if(u == null)
			logger.info("user with id " +id + " not found in database");
		else
			logger.info("user with id " + id + " retrieved");
		return u;
	}
	
	public User getUserByMail(String mail){
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		User u = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("from User where email = :email");
			query.setString("email", mail);
			u = (User)query.uniqueResult();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving user with email " + mail);
			throw e;
		}
		finally {
			session.close();
		}
		if(u == null)
			logger.info("user with email " + mail + " not found in database");
		else
			logger.info("user with email " + mail + " retrieved");
		return u;
	}
	
	private List<Object[]> getMonthDataForStatistics(LocalDate fromDate,LocalDate toDate,Long userId){
		List<Object[]> rows = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = null;
		try{
			tx=session.beginTransaction();
			Query query = session.createQuery("select firstDate, date, role.name from Task where user.id = :userId and firstDate between :fromDate and :toDate");
			query.setLong("userId", userId);
			query.setParameter("fromDate",fromDate);
			query.setParameter("toDate",toDate);
			rows = query.list();
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while retrieving tasks for statistic from date " + fromDate + " to date " + toDate + " userId: " + userId);
			throw e;
		}
		finally {
			session.close();
		}
		logger.info("tasks for statistics of user with id " + userId +" retrieved");
		return rows;
	}
	
	public ChartsData getMonthStatistics(int year, int month, Long userId,boolean lastMonth){
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int nextMonth;
		int nextMonthYear;
		if(month == 12){
			nextMonth=1;
			nextMonthYear = year+1;
		}
		else{
			nextMonth=month+1;
			nextMonthYear=year;
		}	
		LocalDate firstDayOfNextMonth = LocalDate.of(nextMonthYear, nextMonth, 1);
		LocalDate dateFrom = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue()-1);
		LocalDate possibleDateTo = firstDayOfNextMonth.minusDays(firstDayOfNextMonth.getDayOfWeek().getValue());
		LocalDate dateTo;
		if(lastMonth){
			dateTo = LocalDate.now().minusDays(5);
		}
		else{
			dateTo = possibleDateTo;
		}
		long weeksInMonth = ChronoUnit.WEEKS.between(dateFrom, possibleDateTo.plusDays(1));
		
		List<Object[]> rows = getMonthDataForStatistics(dateFrom, dateTo, userId);
		if(rows.size()==0)
			return null;
		Integer count;
		String roleName;
		String weekOfMonthCode;
		int effeciencyIndex;
		LocalDate date;
		LocalDate firstDate;
		Set<String> tmpSet = new HashSet<>();
		Map<String,Integer> pieChartData = new HashMap<>();
		Map<String, Integer>[] roleBarChartData = (Map<String, Integer>[]) new Map[4];
		Map<String, Integer>[] weekBarChartData = (Map<String, Integer>[]) new Map[4];
		roleBarChartData[0] = new HashMap<String,Integer>();
		roleBarChartData[1] = new HashMap<String,Integer>();
		roleBarChartData[2] = new HashMap<String,Integer>();
		roleBarChartData[3] = new HashMap<String,Integer>();
		weekBarChartData[0] = new HashMap<String,Integer>();
		weekBarChartData[1] = new HashMap<String,Integer>();
		weekBarChartData[2] = new HashMap<String,Integer>();
		weekBarChartData[3] = new HashMap<String,Integer>();
		for(Object[] row : rows){
			tmpSet.add((String)row[2]);
		}
		for(String role : tmpSet){
			roleBarChartData[0].put(role, 0);
			roleBarChartData[1].put(role, 0);
			roleBarChartData[2].put(role, 0);
			roleBarChartData[3].put(role, 0);
			pieChartData.put(role, 0);
		}
		for(int i=1; i<=weeksInMonth;i++){
			weekBarChartData[0].put(String.format("Week %d",i), 0);
			weekBarChartData[1].put(String.format("Week %d",i), 0);
			weekBarChartData[2].put(String.format("Week %d",i), 0);
			weekBarChartData[3].put(String.format("Week %d",i), 0);
		}
		
		
		for (Object[] row : rows) {
			roleName = (String) row[2];
			firstDate = (LocalDate) row[0];
			date = (LocalDate) row[1];
			weekOfMonthCode = getweekOfMonthCode(firstDate);
			effeciencyIndex = getEffeciencyIndex(firstDate, date);

			count=pieChartData.get(roleName);
			pieChartData.put(roleName, count + 1);
			count=roleBarChartData[effeciencyIndex].get(roleName);
			roleBarChartData[effeciencyIndex].put(roleName, count + 1);
			count=weekBarChartData[effeciencyIndex].get(weekOfMonthCode);
			weekBarChartData[effeciencyIndex].put(weekOfMonthCode, count + 1);
		}
		ChartsData chartsData = new ChartsData();

		List<PieChartDataItem> pieChartDataList = new ArrayList<>();
		List<BarChartDataItem> roleBarCharDataList = new ArrayList<>();
		List<BarChartDataItem> weekBarCharDataList = new ArrayList<>();
		
		BarChartDataItem roleBarCharDataItemList0 = new BarChartDataItem();
		BarChartDataItem weekBarCharDataItemList0 = new BarChartDataItem();
		BarChartDataItem roleBarCharDataItemList1 = new BarChartDataItem();
		BarChartDataItem weekBarCharDataItemList1 = new BarChartDataItem();
		BarChartDataItem roleBarCharDataItemList2 = new BarChartDataItem();
		BarChartDataItem weekBarCharDataItemList2 = new BarChartDataItem();
		BarChartDataItem roleBarCharDataItemList3 = new BarChartDataItem();
		BarChartDataItem weekBarCharDataItemList3 = new BarChartDataItem();
		
		List<BarChartDataValue> roleBarCharDataValueList0 = new ArrayList<>();
		List<BarChartDataValue> weekBarCharDataValueList0 = new ArrayList<>();
		List<BarChartDataValue> roleBarCharDataValueList1 = new ArrayList<>();
		List<BarChartDataValue> weekBarCharDataValueList1 = new ArrayList<>();
		List<BarChartDataValue> roleBarCharDataValueList2 = new ArrayList<>();
		List<BarChartDataValue> weekBarCharDataValueList2 = new ArrayList<>();
		List<BarChartDataValue> roleBarCharDataValueList3 = new ArrayList<>();
		List<BarChartDataValue> weekBarCharDataValueList3 = new ArrayList<>();
		
		for (String key : pieChartData.keySet()){
			pieChartDataList.add(new PieChartDataItem(key,pieChartData.get(key)));
		}
		chartsData.setPieChartItems(pieChartDataList);
		
		for(String key:roleBarChartData[0].keySet()){
			roleBarCharDataValueList0.add(new BarChartDataValue(key, roleBarChartData[0].get(key)));
		}
		roleBarCharDataItemList0.setKey("Earlier than planned");
		roleBarCharDataItemList0.setValues(roleBarCharDataValueList0);
		roleBarCharDataList.add(roleBarCharDataItemList0);
		
		
		for(String key:roleBarChartData[1].keySet()){
			roleBarCharDataValueList1.add(new BarChartDataValue(key, roleBarChartData[1].get(key)));
		}
		roleBarCharDataItemList1.setKey("Day D");
		roleBarCharDataItemList1.setValues(roleBarCharDataValueList1);
		roleBarCharDataList.add(roleBarCharDataItemList1);
		
		for(String key:roleBarChartData[2].keySet()){
			roleBarCharDataValueList2.add(new BarChartDataValue(key, roleBarChartData[2].get(key)));
		}
		roleBarCharDataItemList2.setKey("Within 3 days");
		roleBarCharDataItemList2.setValues(roleBarCharDataValueList2);
		roleBarCharDataList.add(roleBarCharDataItemList2);
		
		
		for(String key:roleBarChartData[3].keySet()){
			roleBarCharDataValueList3.add(new BarChartDataValue(key, roleBarChartData[3].get(key)));
		}
		roleBarCharDataItemList3.setKey("Postponed");
		roleBarCharDataItemList3.setValues(roleBarCharDataValueList3);
		roleBarCharDataList.add(roleBarCharDataItemList3);
		
		chartsData.setBarChartItemsRole(roleBarCharDataList);
		
		
		for(String key:weekBarChartData[0].keySet()){
			weekBarCharDataValueList0.add(new BarChartDataValue(key, weekBarChartData[0].get(key)));
		}
		weekBarCharDataItemList0.setKey("Earlier than planned");
		weekBarCharDataItemList0.setValues(weekBarCharDataValueList0);
		weekBarCharDataList.add(weekBarCharDataItemList0);
		
		
		for(String key:weekBarChartData[1].keySet()){
			weekBarCharDataValueList1.add(new BarChartDataValue(key, weekBarChartData[1].get(key)));
		}
		weekBarCharDataItemList1.setKey("Day D");
		weekBarCharDataItemList1.setValues(weekBarCharDataValueList1);
		weekBarCharDataList.add(weekBarCharDataItemList1);
		
		for(String key:weekBarChartData[2].keySet()){
			weekBarCharDataValueList2.add(new BarChartDataValue(key, weekBarChartData[2].get(key)));
		}
		weekBarCharDataItemList2.setKey("Within 3 days");
		weekBarCharDataItemList2.setValues(weekBarCharDataValueList2);
		weekBarCharDataList.add(weekBarCharDataItemList2);
		
		for(String key:weekBarChartData[3].keySet()){
			weekBarCharDataValueList3.add(new BarChartDataValue(key, weekBarChartData[3].get(key)));
		}
		weekBarCharDataItemList3.setKey("Postponed");
		weekBarCharDataItemList3.setValues(weekBarCharDataValueList3);
		weekBarCharDataList.add(weekBarCharDataItemList3);
		
		chartsData.setBarChartItemsWeek(weekBarCharDataList);
		return chartsData;
	}
	
	private String getweekOfMonthCode(LocalDate date){
		int dayOfWeek = date.getDayOfWeek().getValue();
		int lastDayOfWeek = date.plusDays(7-dayOfWeek).getDayOfMonth();
		if(lastDayOfWeek < 8)
			return "Week 1";
		if(lastDayOfWeek < 15)
			return "Week 2";
		if(lastDayOfWeek < 22)
			return "Week 3";
		if(lastDayOfWeek < 29)
			return "Week 4";
		return "Week 5";
	}
	
	private int getEffeciencyIndex(LocalDate firstDate, LocalDate date){
		if(date == null)
			return 3;
		if(date.isBefore(firstDate))
			return 0;
		if(date.equals(firstDate))
			return 1;
		if(date.isBefore(firstDate.plusDays(4)))
			return 2;
		return 3;
	}

	public void sendEmail(String to,String subject,String msg) throws LifeRolesException{
			   Message message = new MimeMessage(gmailSession);
			   try{
				   message.setRecipients(Message.RecipientType.TO,
				    InternetAddress.parse(to));
				   message.setSubject(subject);
				   message.setContent(msg,"text/html");
				   Transport.send(message);
			   }catch(MessagingException ex){
				   logger.error("error occurred while sending email to " + to,ex);
				   throw new LifeRolesException(ex);
			   }
			   logger.info("Email was sent to " + to);
	}
	
	public void sendResetLink(String mail) throws LifeRolesException{
		Transaction tx = null;
		User u = getUserByMail(mail);
		Session session = HibernateUtils.getSessionFactory().openSession();
		//18 because if 16 padding is added and it is stripped by browser
		String token = AuthUtils.getRandomBase64Url(18);
		String tokenHash = AuthUtils.computeHash(token, mail).getHash();
		try{
			tx=session.beginTransaction();
			SQLQuery query = session.createSQLQuery("insert into passwordreset (appuser_id, tokenhash, expirationdate) values(:userId,:tokenhash,:expiration)");
			query.setLong("userId", u.getId());
			query.setString("tokenhash",tokenHash);
			query.setParameter("expiration", LocalDateTime.now().plusMinutes(60));
			query.executeUpdate();
			//https://localhost:8443
			//https://liferoles.sde.cz
			String link = "https://liferoles.sde.cz?reset&user="+ mail +"&u=" + u.getId() + "&c=" + token;
			String subject = "Liferoles password reset link";
			String message = "Hi, you just requested for password reset. Click on <a href='"+link+"'>this link</a> and reset your password. Link will expire in one hour.";
			sendEmail(mail,subject,message);
			tx.commit();
		}catch(HibernateException e){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while creating new password reset record",e);
			throw e;
		} catch (LifeRolesException e) {
			if(tx!=null) tx.rollback();
			throw e;
		}
		finally { 
			session.close();
		}
		logger.info("password reset link for user with id " + u.getId() + " sent");
	}
	
	public void moveOldTasksToBacklog(Long userId,LocalDate firstDayOfCurrentWeek){
		Transaction tx = null;
		Session session = HibernateUtils.getSessionFactory().openSession();
		try{
			tx=session.beginTransaction();
			Query q = session.createQuery("update Task set date = null, time = null where user.id = :id and finished = false and date < :firstDay");
			q.setLong("id", userId);
			q.setParameter("firstDay", firstDayOfCurrentWeek);
			q.executeUpdate();
			tx.commit();
		}catch(HibernateException ex){
			if(tx!=null) tx.rollback();
			logger.error("db error occured while moving tasks of user " + userId + " to backlog",ex);
			throw ex;
		}
		finally { 
			session.close();
		}
		logger.info("tasks of user " + userId + " moved to backlog");
	}
}
