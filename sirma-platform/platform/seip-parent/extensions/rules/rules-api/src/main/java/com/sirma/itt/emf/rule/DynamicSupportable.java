package com.sirma.itt.emf.rule;

import java.util.Collection;

/**
 * Provides means to configure a supportable object via common interface.
 *
 * @author BBonev
 */
public interface DynamicSupportable {

	/**
	 * Sets the supported operations.
	 *
	 * @param operations
	 *            the new supported operations
	 */
	void setSupportedOperations(Collection<String> operations);

	/**
	 * Sets the supported types.
	 *
	 * @param supportedTypes
	 *            the new supported types
	 */
	void setSupportedTypes(Collection<String> supportedTypes);

	/**
	 * Sets the supported definitions.
	 *
	 * @param definitions
	 *            the new supported definitions
	 */
	void setSupportedDefinitions(Collection<String> definitions);

	/**
	 * Sets the checks if is async supported.
	 *
	 * @param mode
	 *            the new checks if is async supported
	 */
	void setIsAsyncSupported(boolean mode);
}
