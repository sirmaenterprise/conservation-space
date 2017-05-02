package com.sirma.itt.seip.instance.version.revert;

import java.io.Serializable;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Context object used to store the data required for revert operation execution.
 *
 * @author A. Kunchev
 */
public class RevertContext extends Context<String, Object> {

	private static final long serialVersionUID = -7218269743312362499L;

	private static final int INITIAL_CONTEXT_MAP_SIZE = 5;
	private static final String VERSION_ID_KEY = "versionId";
	private static final String OPERATION_KEY = "operation";
	private static final String CURRENT_ID_KEY = "currentId";
	private static final String REVERT_RESULT_INSTANCE_KEY = "result";
	private static final String CURRENT_INSTANCE_KEY = "current";

	private RevertContext(int initialSize) {
		super(initialSize);
	}

	/**
	 * Creates new {@link RevertContext} that is used to store data for the revert operation. The method will check, if
	 * the passed identifier is applicable for the revert operation and if it is not, exception will be thrown.
	 *
	 * @param versionIdentifier
	 *            the id of the version which data should be used in the revert
	 * @return {@link RevertContext} object with populated version id
	 */
	public static RevertContext create(Serializable versionIdentifier) {
		// blank identifier is handled by #isVersion
		if (!InstanceVersionService.isVersion(versionIdentifier)) {
			throw new IllegalArgumentException(
					"Invalid identifier - [" + versionIdentifier + "]. Only versions could be reverted!");
		}

		RevertContext context = new RevertContext(INITIAL_CONTEXT_MAP_SIZE);
		context.put(VERSION_ID_KEY, versionIdentifier);
		return context;
	}

	public Serializable getVersionId() {
		return getIfSameType(VERSION_ID_KEY, Serializable.class);
	}

	public Operation getOperation() {
		return getIfSameType(OPERATION_KEY, Operation.class, new Operation("revertVersion"));
	}

	public RevertContext setOperation(Operation operation) {
		put(OPERATION_KEY, operation);
		return this;
	}

	public Serializable getCurrentInstanceId() {
		return (Serializable) computeIfAbsent(CURRENT_ID_KEY,
				unused -> InstanceVersionService.getIdFromVersionId(getVersionId()));
	}

	/**
	 * Sets for the instance that will be result from the revert operation.
	 *
	 * @param instance
	 *            the reverted instance that is the result from the operation
	 * @return current object to allow method chaining
	 */
	public RevertContext setRevertResultInstance(Instance instance) {
		put(REVERT_RESULT_INSTANCE_KEY, instance);
		return this;
	}

	/**
	 * Gets the instance, result from the revert operation.
	 *
	 * @return {@link Instance} result from the revert operation.
	 */
	public Instance getRevertResultInstance() {
		return getIfSameType(REVERT_RESULT_INSTANCE_KEY, Instance.class);
	}

	/**
	 * Sets the current instance, which data will be replaced by the version.
	 *
	 * @param instance
	 *            the instance which data will be replaced
	 * @return current object to allow method chaining
	 */
	public RevertContext setCurrentInstance(Instance instance) {
		put(CURRENT_INSTANCE_KEY, instance);
		return this;
	}

	/**
	 * Gets the instance, which data will be replaced by the data from the version.
	 *
	 * @return {@link Instance} which data will be replaced
	 */
	public Instance getCurrentInstance() {
		return getIfSameType(CURRENT_INSTANCE_KEY, Instance.class);
	}

}
