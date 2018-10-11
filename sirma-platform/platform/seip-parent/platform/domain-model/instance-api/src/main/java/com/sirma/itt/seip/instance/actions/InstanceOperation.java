package com.sirma.itt.seip.instance.actions;

import java.util.Set;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines a operation that can be executed. The operations must be stateless to allow concurrent execution.
 *
 * @author BBonev
 */
public interface InstanceOperation extends Plugin {

	String TARGET_NAME = "instanceOperation";

	/**
	 * Checks if the current operation could be handled be the current instance.
	 *
	 * @param instance
	 *            the value to test
	 * @param operation
	 *            the operation
	 * @return true, if is applicable
	 */
	boolean isApplicable(Instance instance, Operation operation);

	/**
	 * Gets the supported operations by the current instance operation. The method returns set to allow single operation
	 * to have aliases. Although this way the method allows single operation to handle multiple operations in a single
	 * implementation is not recommended and is a design violation.
	 * <p>
	 * The information from this method will be used to separate implementations to minimize the calls to
	 * {@link #isApplicable(Instance, Operation)} method.
	 *
	 * @return the supported operations
	 */
	Set<String> getSupportedOperations();

	/**
	 * Execute the current operation using the given execution context
	 *
	 * @param executionContext
	 *            the execution context
	 * @return the object response of the operation if any.
	 */
	Object execute(Context<String, Object> executionContext);
}
