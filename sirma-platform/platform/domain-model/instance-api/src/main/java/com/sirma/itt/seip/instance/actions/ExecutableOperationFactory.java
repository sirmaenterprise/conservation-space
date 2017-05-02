package com.sirma.itt.seip.instance.actions;

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

	/**
	 * Gets the immediate operation.
	 *
	 * @return the immediate operation
	 */
	ExecutableOperation getImmediateExecutor();
}
