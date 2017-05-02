package com.sirma.itt.seip;

import java.util.Collection;

/**
 * Supportable extension that provides access to possible supported definitions.
 *
 * @author BBonev
 * @param <T>
 *            the definition id type
 */
public interface DefinitionSupportable<T> {

	/**
	 * Gets the supported definitions.
	 *
	 * @return the supported definitions
	 */
	Collection<T> getSupportedDefinitions();

}