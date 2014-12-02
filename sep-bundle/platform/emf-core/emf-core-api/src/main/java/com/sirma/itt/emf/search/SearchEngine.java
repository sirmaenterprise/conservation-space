package com.sirma.itt.emf.search;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Extension for {@link com.sirma.itt.emf.search.SearchService} that realize a concrete search
 * algorithms.
 * 
 * @author BBonev
 */
public interface SearchEngine extends Plugin {

	/** The target name. */
	String TARGET_NAME = "searchEngine";

	/**
	 * Checks if is supported.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param target
	 *            the target
	 * @param arguments
	 *            the arguments
	 * @return true, if is supported
	 */
	<E extends Instance, S extends SearchArguments<E>> boolean isSupported(Class<?> target,
			S arguments);

	/**
	 * Perform the search for the given target and the given arguments.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param target
	 *            the target
	 * @param arguments
	 *            the arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments);
}
