package com.sirma.itt.emf.adapter;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Read only class for describing data access in different formats to remote systems.
 * 
 * @author borislav banchev
 * @author BBonev
 */
public interface FileDescriptor extends Serializable {

	/**
	 * The id if the file.
	 *
	 * @return the id
	 */
	String getId();

	/**
	 * Gets the container id.
	 *
	 * @return the container id
	 */
	String getContainerId();

	/**
	 * Provides read access to the file identified by the current descriptor.
	 * 
	 * @return the stream with the content
	 */
	InputStream getInputStream();

}
