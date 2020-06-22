package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Adapter service for retrieving XML definitions from underlying DMS. The adapter service is based on the extension
 * {@link DefintionAdapterServiceExtension}.
 *
 * @author BBonev
 */
public interface DefintionAdapterService {

	/**
	 * Gets the XML definitions for the given definition class.
	 *
	 * @param definitionClass
	 *            the definition class
	 * @return the definitions
	 */
	List<FileDescriptor> getDefinitions(Class<?> definitionClass);

	/**
	 * Uploads the definition to dms. The definition is considered of the type provided as class
	 *
	 * @param definitionClass
	 *            the type of uploaded definition
	 * @param descriptor
	 *            is the descriptor to fetch data from
	 * @return the dms id of uploaded
	 */
	String uploadDefinition(Class<?> definitionClass, FileAndPropertiesDescriptor descriptor);
}
