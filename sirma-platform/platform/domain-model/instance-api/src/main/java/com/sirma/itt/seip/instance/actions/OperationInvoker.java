package com.sirma.itt.seip.instance.actions;

import static com.sirma.itt.seip.instance.actions.InstanceOperationProperties.INSTANCE;
import static com.sirma.itt.seip.instance.actions.InstanceOperationProperties.OPERATION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Utility to help execute various operations. The implementation collects all registered {@link InstanceOperation}s and
 * provides means to execute them in a common manner.
 *
 * @author BBonev
 */
@ApplicationScoped
public class OperationInvoker {

	@Inject
	@ExtensionPoint(InstanceOperation.TARGET_NAME)
	private Iterable<InstanceOperation> operations;

	private Map<String, List<InstanceOperation>> operationCache = new HashMap<>();

	/**
	 * Initialize the invoker cache
	 */
	@PostConstruct
	protected void initialize() {
		// distribute all operations in the cache
		for (InstanceOperation instanceOperation : operations) {
			for (String operation : instanceOperation.getSupportedOperations()) {
				CollectionUtils.addValueToMap(operationCache, operation, instanceOperation);
			}
		}
	}

	/**
	 * Invoke operation using the given context. The target operation will be resolved using data from the context. The
	 * invoked operation will be resolved using data located in the properties
	 * {@link InstanceOperationProperties#INSTANCE} and {@link InstanceOperationProperties#OPERATION} so these
	 * properties are required for calling this method.
	 *
	 * @param <V>
	 *            the value type
	 * @param context
	 *            the context
	 * @return the result object if any from the operation execution.
	 */
	public <V> V invokeOperation(Context<String, Object> context) {
		if (context == null) {
			throw new EmfRuntimeException("Cannot execute operation for null context");
		}
		Instance instance = context.getIfSameType(INSTANCE, Instance.class);
		Operation operation = context.getIfSameType(OPERATION, Operation.class);
		return invokeOperation(instance, operation, context);
	}

	/**
	 * Creates the default context from the given instance and operation. The returned object is a valid instance to be
	 * passed to the method {@link #invokeOperation(Context)}. If the operation requires additional data it should be
	 * set in the context before calling the {@link #invokeOperation(Context)} method.
	 *
	 * @param instance
	 *            the target instance on which the operation will be executed
	 * @param operation
	 *            the operation object to identify the executed operation.
	 * @return the context with set default properties
	 */
	public Context<String, Object> createDefaultContext(Instance instance, Operation operation) {
		Context<String, Object> context = new Context<>(5);
		context.put(INSTANCE, instance);
		context.put(OPERATION, operation);
		return context;
	}

	/**
	 * Lookup operation using the specified instance and operation. The found operation will be invoked using the
	 * specified context. If no operation is found an exception will be thrown
	 *
	 * @param <V>
	 *            the return type
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param context
	 *            the context
	 * @return the return value
	 */
	protected <V> V invokeOperation(Instance instance, Operation operation, Context<String, Object> context) {
		if (context == null) {
			throw new EmfRuntimeException("Cannot execute operation for null context");
		}
		InstanceOperation instanceOperation = findOperation(instance, operation);
		if (instanceOperation != null) {
			return executeOperation(operation, context, instanceOperation);
		}
		throw new EmfRuntimeException("Not supported operation (" + Operation.getOperationId(operation)
				+ ") for instance of type " + (instance == null ? null : instance.getClass()));
	}

	/**
	 * Execute operation.
	 *
	 * @param <V>
	 *            the value type
	 * @param operation
	 *            the operation
	 * @param context
	 *            the context
	 * @param instanceOperation
	 *            the instance operation
	 * @return the v
	 */
	@SuppressWarnings("unchecked")
	private static <V> V executeOperation(Operation operation, Context<String, Object> context,
			InstanceOperation instanceOperation) {
		try {
			Options.CURRENT_OPERATION.set(operation);
			return (V) instanceOperation.execute(context);
		} finally {
			Options.CURRENT_OPERATION.clear();
		}
	}

	/**
	 * Find operation that is applicable for execution of the given instance and operation
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the instance operation
	 */
	protected InstanceOperation findOperation(Instance instance, Operation operation) {
		String operationId = Operation.getOperationId(operation);
		List<InstanceOperation> list = operationCache.get(operationId);
		InstanceOperation toReturn = null;
		if (!CollectionUtils.isEmpty(list)) {
			for (InstanceOperation instanceOperation : list) {
				if (instanceOperation.isApplicable(instance, operation)) {
					toReturn = instanceOperation;
					break;
				}
			}
		}
		// if not found in cache try in all operations
		if (toReturn == null) {
			for (InstanceOperation instanceOperation : operations) {
				if (instanceOperation.isApplicable(instance, operation)) {
					toReturn = instanceOperation;
					break;
				}
			}
		}
		return toReturn;
	}
}