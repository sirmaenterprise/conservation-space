package com.sirma.itt.pm.services.adapter;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Interface CMFProjectInstanceAdapterService is the base adapter service
 * for {@link com.sirma.itt.pm.services.ProjectService}
 */
public interface CMFProjectInstanceAdapterService extends CMFAdapterService {

	/**
	 * Creates the project instance in dms and returns the dmsid on success
	 *
	 * @param projectInstance
	 *            the not serialized project currently active in pm
	 * @return the dms id of the project in dms system
	 * @throws DMSException
	 *             on any dms error
	 */
	String createProjectInstance(ProjectInstance projectInstance) throws DMSException;

	/**
	 * Updates project instance in DMS and returns the DMS id on success.
	 * 
	 * @param projectInstance
	 *            the project instance to update
	 * @return the DMS id if update was successful or <code>null</code> if not
	 * @throws DMSException
	 *             on any dms error
	 */
	String updateProjectInstance(ProjectInstance projectInstance) throws DMSException;

	/**
	 * Delete project instance from the DMS. The operation could be permanent if the second argument
	 * is <code>true</code>.
	 * 
	 * @param instance
	 *            the instance
	 * @param permanent
	 *            the permanent
	 * @return the DMS id if update was successful or <code>null</code> if not
	 * @throws DMSException
	 *             on any dms error
	 */
	String deleteProjectInstance(ProjectInstance instance, boolean permanent) throws DMSException;

}
