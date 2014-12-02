package com.sirma.itt.cmf.search;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.SupportablePlugin;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Extension for the {@link com.sirma.itt.cmf.search.DmsSearchEngine} that provides specific search
 * post loading for particular class.
 * 
 * @author BBonev
 */
public interface DmsSearchEngineExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "dmsSearchEngineExtension";

	/**
	 * Perform the actual search
	 * 
	 * @param <E>
	 *            the expected return search type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void search(S arguments);
}
