package com.sirma.itt.emf.executors;

/**
 * A factory for creating ExecutableOperation objects.
 * 
 * @author BBonev
 */
public interface ExecutableOperationFactory {

	/**
	 * Gets the executor.
	 * 
	 * @param operation
	 *            the operation
	 * @return the executor
	 */
	ExecutableOperation getExecutor(String operation);
}
