package com.sirma.itt.seip.instance.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperationProperties;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default executor for all immediate operations.
 *
 * @author svelikov
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 514)
public class ImmediateOperationExecutor extends EditDetailsExecutor {

	@Override
	public String getOperation() {
		return "immediateOperation";
	}

	@Override
	protected Operation createOperation(OperationContext context) {
		// Build operation using the original actionId.
		String operationId = (String) context.get(ExecutableOperationProperties.OPERATION);
		return new Operation(operationId);
	}

}
