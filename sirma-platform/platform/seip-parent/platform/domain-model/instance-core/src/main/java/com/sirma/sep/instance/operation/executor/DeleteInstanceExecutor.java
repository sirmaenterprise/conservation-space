package com.sirma.sep.instance.operation.executor;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.BaseInstanceExecutor;
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
@Extension(target = ExecutableOperation.TARGET_NAME, order = 20)
public class DeleteInstanceExecutor extends BaseInstanceExecutor {

	private static final Operation OPERATION = new Operation("delete");

	@Override
	public String getOperation() {
		return OPERATION.getOperation();
	}

	@Override
	public OperationResponse execute(OperationContext context) {
		Instance instance = getOrCreateInstance(context);
		getInstanceService().delete(instance, OPERATION, false);
		return new OperationResponse(OperationStatus.COMPLETED, null);
	}

	@Override
	public boolean rollback(OperationContext data) {
		// we cannot rollback the operation because all sub elements are deleted also
		return true;
	}
}