package com.sirma.itt.seip.instance.library;

import java.util.List;

import com.sirma.itt.seip.domain.instance.ClassInstance;
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

	/**
	 * Gets classes that are part of the given Library
	 *
	 * @param libraryId
	 *            The id of the Library
	 * @param forAction
	 *            For action
	 * @return List of classes part of the given Library
	 */
	List<ClassInstance> getAllowedLibraries(String libraryId, String forAction);

	/**
	 * Retrieves an element from the given library or returns null if such element doesn't exists
	 *
	 * @param libraryId
	 *            Id of the library
	 * @param elementId
	 *            Id of the element from the library
	 * @return An element from the library or null if the element doesn't exists
	 */
	ClassInstance getLibraryElement(String libraryId, String elementId);

}
