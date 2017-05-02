package com.sirma.itt.emf.adapter;

import java.util.List;

import com.sirma.itt.seip.adapters.AdapterService;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Adapter service for retrieving notification templates from underlying DMS.
 *
 * @author bbanchev
 */
public interface CMFMailNotificaionAdapterService extends AdapterService {

	/**
	 * Gets the template definitions for the system
	 *
	 * @return the definitions as file descriptors
	 * @throws DMSException
	 *             exception if error occurred
	 */
	List<FileDescriptor> getTemplates() throws DMSException;
}
