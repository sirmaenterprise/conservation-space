package com.sirma.itt.emf.adapter;

import java.util.List;

/**
 * Adapter service for retrieving XML definitions from underlying DMS.
 *
 * @author BBonev
 */
public interface DMSDefintionAdapterService extends CMFAdapterService {

	/**
	 * Gets the XML definitions for the given definition class.
	 *
	 * @param definitionClass
	 *            the definition class
	 * @return the definitions
	 * @throws DMSException
	 *             exception if error occurred
	 */
	List<FileDescriptor> getDefinitions(Class<?> definitionClass) throws DMSException;

	/**
	 * Uploads the definition to dms. The definition is considered of the type provided as class
	 *
	 * @param definitionClass
	 *            the type of uploaded definition
	 * @param descriptor
	 *            is the descriptor to fetch data from
	 * @return the dms id of uploaded
	 * @throws DMSException
	 *             on any error
	 */
	String uploadDefinition(Class<?> definitionClass, FileAndPropertiesDescriptor descriptor)
			throws DMSException;
}
