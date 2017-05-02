package com.sirma.itt.seip.instance.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Executor that could handle generic delete operation of any instance.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 20)
public class DeleteInstanceExecutor extends BaseInstanceExecutor {

	/** The Constant OPERATION. */
	private static final Operation OPERATION = new Operation("delete");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return OPERATION.getOperation();
	}

	/**
	 * Execute.
	 *
	 * @param context
	 *            the context
	 * @return the object
	 */
	@Override
	public OperationResponse execute(OperationContext context) {
		Instance instance = getOrCreateInstance(context);
		getInstanceService().delete(instance, OPERATION, false);
		return new OperationResponse(OperationStatus.COMPLETED, null);
	}

	/**
	 * Rollback.
	 *
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	@Override
	public boolean rollback(OperationContext data) {
		// we cannot rollback the operation because all sub elements are deleted also
		return true;
	}

}
