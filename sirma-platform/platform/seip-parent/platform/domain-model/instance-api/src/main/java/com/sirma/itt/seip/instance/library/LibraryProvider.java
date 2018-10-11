package com.sirma.itt.seip.instance.library;

import java.util.List;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Provides method for accessing the elements of a Library
 *
 * @author kirq4e
 */
public interface LibraryProvider {

	String OBJECT_LIBRARY = "object-library";

	/**
	 * Gets classes that are part of the given Library
	 *
	 * @param forAction
	 *            For action
	 * @return List of classes part of the given Library
	 */
	List<Instance> getLibraries(String forAction);

}
