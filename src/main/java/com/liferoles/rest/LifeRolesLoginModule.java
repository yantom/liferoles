package com.liferoles.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.security.auth.spi.DatabaseServerLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.controller.AuthUtils;
import com.liferoles.exceptions.LiferolesRuntimeException;

public class LifeRolesLoginModule extends DatabaseServerLoginModule {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseServerLoginModule.class);

	@Override
	public String createPasswordHash(String username, String password, String digestOption) {
		String salt = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			ps = conn.prepareStatement("select salt from appuser where email = ?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next()) {
				salt = rs.getString("salt");
			}
			;
		} catch (Exception e) {
			logger.error("wtf", e);
			throw new SecurityException("unable to authenticate user - can not retrieve salt", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		String hash;
		try {
			hash = AuthUtils.computeHash(password, salt).getHash();
		} catch (LiferolesRuntimeException e) {
			throw new SecurityException("unable to authenticate user - can not compute password hash", e);
		}
		return hash;
	}
}
