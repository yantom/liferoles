package com.liferoles.controller;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferoles.exceptions.LiferolesRuntimeException;
import com.liferoles.exceptions.PossibleDataInconsistencyException;
import com.liferoles.model.Role;

@LocalBean
@Stateless
public class RoleManager {
	private static final Logger logger = LoggerFactory.getLogger(RoleManager.class);
	@PersistenceContext(unitName = "Liferoles")
	EntityManager em;

	public Long createRole(Role role) throws LiferolesRuntimeException {
		Long id;
		try {
			em.persist(role);
			em.flush();
			id = role.getId();
		} catch (Exception e) {
			logger.error("db error occured while creating " + role.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(role.toString() + " created");
		return id;
	}

	public void deleteRole(Role role) throws LiferolesRuntimeException {
		try {
			Role r = em.find(Role.class, role.getId());
			em.remove(r);
		} catch (IllegalArgumentException e) {
			logger.warn("db error occured while deleting " + role.toString(), e);
			throw new PossibleDataInconsistencyException(
					"illegal argument exception when deleting task" + role.toString(), e);
		} catch (Exception e) {
			logger.error("db error occured while deleting " + role.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(role.toString() + " deleted");
	}

	public void deleteRole(Role deletedRole, Role newRole) throws LiferolesRuntimeException {
		try {
			Query query = em.createQuery("update Task set role.id = :newRoleId where role.id = :oldRoleId)");
			query.setParameter("newRoleId", newRole.getId());
			query.setParameter("oldRoleId", deletedRole.getId());
			query.executeUpdate();
			Role r = em.find(Role.class, deletedRole.getId());
			em.remove(r);
		} catch (IllegalArgumentException e) {
			logger.warn("db error occured while deleting " + deletedRole.toString(), e);
			throw new PossibleDataInconsistencyException(
					"illegal argument exception when deleting task" + deletedRole.toString(), e);
		} catch (Exception e) {
			logger.error("db error occured while deleting " + deletedRole + " and moving tasks to " + newRole, e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(deletedRole + " deleted, tasks moved under " + newRole);
	}

	@SuppressWarnings("unchecked")
	public List<Role> getAllRoles(Long userId) throws LiferolesRuntimeException {
		List<Role> roleList = null;
		try {
			Query query = em.createQuery("from Role where user.id = :id");
			query.setParameter("id", userId);
			roleList = query.getResultList();
		} catch (Exception e) {
			logger.error("db error occured while retrieving roles of user with id " + userId, e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info("roles of user with id " + userId + " retrieved");
		return roleList;
	}

	public void updateRole(Role role) throws LiferolesRuntimeException {
		try {
			em.merge(role);
		} catch (Exception e) {
			logger.error("db error occured while updating " + role.toString(), e);
			throw new LiferolesRuntimeException(e);
		}
		logger.info(role.toString() + " updated");
	}
}
