package com.sirma.itt.cmf.services;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.beans.model.WorkLogEntry;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Service for common access to both workflow and standalone tasks.
 *
 * @author BBonev
 */
public interface TaskService {

	/**
	 * Log work for the given task reference for the given user.
	 *
	 * @param task
	 *            the task reference
	 * @param userId
	 *            the user id to log the work to
	 * @param loggedData
	 *            the logged data
	 * @return the generated id for the logged entry.
	 */
	Serializable logWork(InstanceReference task, String userId, Map<String, Serializable> loggedData);

	/**
	 * Update logged work data for the given id.
	 *
	 * @param id
	 *            the id
	 * @param loggedData
	 *            the logged data
	 * @return true, if successful
	 */
	boolean updateLoggedWork(Serializable id, Map<String, Serializable> loggedData);

	/**
	 * Delete logged work.
	 *
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	boolean deleteLoggedWork(Serializable id);

	/**
	 * Gets the logged data for the given task instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the logged data
	 */
	List<WorkLogEntry> getLoggedData(Instance instance);
}
