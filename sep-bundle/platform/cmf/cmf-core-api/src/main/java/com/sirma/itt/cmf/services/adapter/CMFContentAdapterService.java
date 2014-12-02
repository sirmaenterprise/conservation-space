package com.sirma.itt.cmf.services.adapter;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * The Interface CMFContentAdapterService is responsible to fetch the dms content.
 */
public interface CMFContentAdapterService {

	/**
	 * Gets the content descriptor.
	 * 
	 * @param instance
	 *            the instance
	 * @return the content descriptor
	 * @throws DMSException
	 *             the dMS exception
	 */
	FileDescriptor getContentDescriptor(DMSInstance instance) throws DMSException;

	/**
	 * Gets the content descriptor.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the content descriptor
	 * @throws DMSException
	 *             the dMS exception
	 */
	FileDescriptor getContentDescriptor(String dmsId) throws DMSException;

	/**
	 * Gets the content descriptor.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @param containerId
	 *            the container id
	 * @return the content descriptor
	 * @throws DMSException
	 *             the dMS exception
	 */
	FileDescriptor getContentDescriptor(String dmsId, String containerId) throws DMSException;

}
