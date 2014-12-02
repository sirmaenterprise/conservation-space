package com.sirma.itt.cmf.services;

import java.util.List;

import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.User;

/**
 * The DraftService is a service responsible for the draft instances operation. Could find, create,
 * delete drafts
 *
 * @author bbanchev
 */
public interface DraftService {

	/**
	 * Gets the drafts for given instance for all users.
	 *
	 * @param instance
	 *            the instance
	 * @return the list of drafts
	 */
	public List<DraftInstance> getDrafts(Instance instance);

	/**
	 * Gets the draft for given user and instance.
	 *
	 * @param instance
	 *            the instance
	 * @param user
	 *            the creator
	 * @return the draft or null if not found
	 */
	public DraftInstance getDraft(Instance instance, User user);

	/**
	 * Creates a new draft for given instance and user. Only one such entry is possible, so multiple
	 * invocation would store only one entry.
	 *
	 * @param instance
	 *            the instance to find draft for
	 * @param user
	 *            the creator of the draft. If missing and exception is thrown
	 * @return the instance created or null on any fail
	 */
	public DraftInstance create(Instance instance, User user);

	/**
	 * Removes a draft for a instance and specific user
	 *
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the instance deleted or null if notning is deleted
	 */
	public DraftInstance delete(Instance instance, User user);

	/**
	 * Delete all drafts for given instance.
	 *
	 * @param instance
	 *            is the instance to find drafts for
	 * @return the list of deleted instances
	 */
	public List<DraftInstance> delete(Instance instance);

}
