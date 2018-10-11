package com.sirma.itt.seip.instance.actions;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Helper class for invoking instance operations. The class defines helper methods to provide type safe operation
 * invocation.
 *
 * @author BBonev
 */
@Singleton
public class InstanceOperations {

	@Inject
	private OperationInvoker invoker;

	/**
	 * Execute delete operation for the given instance
	 *
	 * @param <V>
	 *            the value type
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the v
	 */
	public <V> V invokeDelete(Instance instance, Operation operation) {
		Operation local = Operation.setOperationId(operation, ActionTypeConstants.DELETE);
		return invoker.invokeOperation(instance, local, invoker.createDefaultContext(instance, local));
	}

	/**
	 * Execute attach operation for the given instance. The method creates a context and invokes
	 * {@link OperationInvoker#invokeOperation(Context)}
	 *
	 * @param instance
	 *            the target instance instance
	 * @param operation
	 *            the operation
	 * @param instances
	 *            the instances to attach
	 */
	public void invokeAttach(Instance instance, Operation operation, Instance... instances) {
		Operation local = Operation.setOperationId(operation, ActionTypeConstants.ATTACH);
		Context<String, Object> context = invoker.createDefaultContext(instance, local);
		context.put(InstanceOperationProperties.INSTANCE_ARRAY, instances);
		invoker.invokeOperation(instance, local, context);
	}

	/**
	 * Invoke detach operation for the given instance. The method creates a context and invokes
	 * {@link OperationInvoker#invokeOperation(Context)}
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param instances
	 *            the instances
	 */
	public void invokeDetach(Instance instance, Operation operation, Instance... instances) {
		Operation local = Operation.setOperationId(operation, ActionTypeConstants.DETACH);
		Context<String, Object> context = invoker.createDefaultContext(instance, local);
		context.put(InstanceOperationProperties.INSTANCE_ARRAY, instances);
		invoker.invokeOperation(instance, local, context);
	}

	/**
	 * Invoke move operation for the given instance. The method creates a context and invokes
	 * {@link OperationInvoker#invokeOperation(Context)}
	 *
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the operation
	 * @param sourceInstance
	 *            the document which will be moved
	 */
	public void invokeMove(Instance targetInstance, Operation operation, Instance sourceInstance) {
		Operation local = Operation.setOperationId(operation, ActionTypeConstants.MOVE);
		Context<String, Object> context = invoker.createDefaultContext(targetInstance, local);
		context.put(InstanceOperationProperties.INSTANCE, targetInstance);
		context.put(InstanceOperationProperties.SOURCE_INSTANCE, sourceInstance);
		invoker.invokeOperation(targetInstance, local, context);
	}

}
