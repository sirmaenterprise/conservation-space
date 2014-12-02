package com.sirma.itt.objects.services.adapters;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * The Interface CMFObjectInstanceAdapterService is the base adapter service for.
 * {@link com.sirma.itt.objects.services.ObjectService}
 */
public interface CMFObjectInstanceAdapterService extends CMFAdapterService {

	/**
	 * Creates the object instance in dms.
	 *
	 * @param objectInstance
	 *            the object instance
	 * @return the instance dms id
	 * @throws DMSException
	 *             on any dms/http error
	 */
	public String createInstance(DMSInstance objectInstance) throws DMSException;

	/**
	 * Deletes the the object instance not forced.
	 *
	 * @param objectInstance
	 *            the object instance to delete
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on any dms/http error
	 */
	public String deleteInstance(DMSInstance objectInstance) throws DMSException;

	/**
	 * Deletes the object instance forced.
	 *
	 * @param objectInstance
	 *            the case instance to delete
	 * @param force
	 *            whether to really delete the case from dms
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on any dms/http error
	 */
	public String deleteInstance(DMSInstance objectInstance, boolean force) throws DMSException;

	/**
	 * Updates the case instance.
	 *
	 * @param objectInstance
	 *            the object instance to update
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on any dms/http error
	 */
	public String updateInstance(DMSInstance objectInstance) throws DMSException;
}
