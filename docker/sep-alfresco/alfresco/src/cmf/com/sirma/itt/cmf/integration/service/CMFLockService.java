/**
 * 
 */
package com.sirma.itt.cmf.integration.service;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;

/**
 * Locking mechanism for nodes in alfresco.
 * 
 * @author borislav banchev
 */
public class CMFLockService {

	/** The lock service. */
	private LockService lockService;
	/** The lock service. */
	private ServiceRegistry serviceRegistry;
	/** The repository. */
	private Repository repository;
	/** case service. */
	private CMFService caseService;

	/**
	 * Lock node.
	 * 
	 * @param nodeID
	 *            the node id
	 */
	public void lockNode(final String nodeID) {

		NodeRef lockableNode = getCaseService().getNodeRef(nodeID);
		lockNode(lockableNode);
	}

	/**
	 * Unlock node.
	 * 
	 * @param nodeID
	 *            the node id
	 * @return the string
	 */
	public String unlockNode(final String nodeID) {

		NodeRef lockableNode = getCaseService().getNodeRef(nodeID);
		return unlockNode(lockableNode);
	}

	/**
	 * Lock node.
	 * 
	 * @param nodeID
	 *            the node id
	 */
	public void lockNode(final NodeRef nodeID) {
		lockNode(nodeID, AuthenticationUtil.getSystemUserName());
	}

	/**
	 * Unlock node.
	 * 
	 * @param nodeID
	 *            the node id
	 * @return the string
	 */
	public String unlockNode(final NodeRef nodeID) {
		Serializable lockOwner = getLockedOwner(nodeID);
		if (lockOwner != null && !"".equals(lockOwner.toString())) {
			return unlockNode(nodeID, lockOwner.toString());
		}
		return null;
	}

	/**
	 * Lock node.
	 * 
	 * @param nodeID
	 *            the node id
	 * @param username
	 *            the username
	 */
	public void lockNode(final String nodeID, final String username) {

		NodeRef lockableNode = getCaseService().getNodeRef(nodeID);
		lockNode(lockableNode, username);
	}

	/**
	 * Unlock node.
	 * 
	 * @param nodeID
	 *            the node id
	 * @param username
	 *            the username
	 */
	public void unlockNode(final String nodeID, final String username) {

		NodeRef lockableNode = getCaseService().getNodeRef(nodeID);
		unlockNode(lockableNode, username);
	}

	/**
	 * Lock node with {@link LockType#WRITE_LOCK} using {@link LockService}
	 * 
	 * @param nodeID
	 *            the node to lock
	 * @param username
	 *            the username to lock with
	 */
	public void lockNode(final NodeRef nodeID, final String username) {
		if (nodeID == null || StringUtils.isEmpty(username)) {
			return;
		}
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(username);
			getLockService().lock(nodeID, LockType.WRITE_LOCK);
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	/**
	 * Gets the locked owner.
	 * 
	 * @param nodeID
	 *            the node id
	 * @return the locked owner
	 */
	public String getLockedOwner(String nodeID) {
		NodeRef lockableNode = getCaseService().getNodeRef(nodeID);
		return getLockedOwner(lockableNode);
	}

	/**
	 * Gets the locked owner.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the locked owner
	 */
	public String getLockedOwner(NodeRef nodeRef) {
		Serializable lockOwner = serviceRegistry.getNodeService().getProperty(nodeRef,
				ContentModel.PROP_LOCK_OWNER);
		if (lockOwner == null) {
			return "";
		}
		return lockOwner.toString();
	}

	/**
	 * Checks if is locked.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the boolean
	 */
	public Boolean isLocked(NodeRef nodeRef) {
		Boolean locked = Boolean.FALSE;
		if (nodeRef != null && serviceRegistry.getNodeService().exists(nodeRef)) {
			if (serviceRegistry.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE)) {
				LockStatus lockStatus = serviceRegistry.getLockService().getLockStatus(nodeRef);
				if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER) {
					locked = true;
				}
			}

		}
		return locked;
	}

	/**
	 * Unlock node.
	 * 
	 * @param nodeRef
	 *            the {@link NodeRef}
	 * @param username
	 *            the username
	 * @return the string
	 */
	public String unlockNode(final NodeRef nodeRef, final String username) {
		if (nodeRef == null || StringUtils.isEmpty(username)) {
			return null;
		}

		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(username);
			getLockService().unlock(nodeRef);
			return username;
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	/**
	 * Gets the repository.
	 * 
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Sets the repository.
	 * 
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * Gets the lock service.
	 * 
	 * @return the lockService
	 */
	public LockService getLockService() {
		return lockService;
	}

	/**
	 * Sets the lock service.
	 * 
	 * @param lockService
	 *            the lockService to set
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * Gets the service registry.
	 * 
	 * @return the serviceRegistry
	 */
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * Gets the case service.
	 * 
	 * @return the case service
	 */
	public CMFService getCaseService() {
		return caseService;
	}

	/**
	 * Sets the case service.
	 * 
	 * @param caseService
	 *            the new case service
	 */
	public void setCaseService(CMFService caseService) {
		this.caseService = caseService;
	}
}
