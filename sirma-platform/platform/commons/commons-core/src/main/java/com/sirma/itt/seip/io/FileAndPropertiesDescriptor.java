package com.sirma.itt.seip.io;

import java.io.Serializable;
import java.util.Map;

/**
 * Descriptor for files associated as well with properties
 *
 * @author borislav banchev
 */
public interface FileAndPropertiesDescriptor extends FileDescriptor {
	/**
	 * Get the properties associated with this descriptor
	 *
	 * @return the properties
	 */
	Map<String, Serializable> getProperties();

}
