package com.sirma.itt.seip;

import java.util.Collection;

/**
 * Interface to add means an operation to define the supported operation the implementation can execute.
 *
 * @author BBonev
 * @param <T>
 *            the operation identifier type
 */
public interface OperationSupportable<T> {

	/**
	 * Gets the primary operation the current implementation can execute or represent. The operation returned from this
	 * method may not be included in the set return by the method {@link #getSupportedOperations()}
	 *
	 * @return the primary operation executed by the current implementation and should never be <code>null</code>.
	 */
	T getPrimaryOperation();

	/**
	 * Gets the supported operations that can be handled by the implementation. The result could be empty set.
	 *
	 * @return the supported operations
	 */
	Collection<T> getSupportedOperations();
}
