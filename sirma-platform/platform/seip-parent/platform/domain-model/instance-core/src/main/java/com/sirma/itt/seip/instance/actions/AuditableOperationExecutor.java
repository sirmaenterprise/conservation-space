package com.sirma.itt.seip.instance.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperationProperties;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationInvoker;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default executor for editable operations. This operations only log the user action in the audit log. The executor
 * calls the proper {@link com.sirma.itt.seip.instance.actions.InstanceOperation} using the {@link OperationInvoker}.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 550)
public class AuditableOperationExecutor extends BaseInstanceExecutor {

	private static final Operation AUDITABLE = new Operation("auditable");
	@Inject
	private OperationInvoker operationInvoker;

	@Override
	public String getOperation() {
		return AUDITABLE.getOperation();
	}

	@Override
	public OperationResponse execute(OperationContext context) {

		Instance instance = getOrCreateInstance(context);

		Context<String, Object> invokerContext = operationInvoker.createDefaultContext(instance,
				createOperation(context));
		operationInvoker.invokeOperation(invokerContext);

		return new OperationResponse(OperationStatus.COMPLETED, toJson(instance));
	}

	@Override
	protected Operation createOperation(OperationContext context) {
		// Build operation using the original actionId.
		String operationId = (String) context.get(ExecutableOperationProperties.USER_OPERATION_ID);
		boolean isUserOperation = (boolean) context.get(ExecutableOperationProperties.IS_USER_OPERATION);
		return new Operation(AUDITABLE.getOperation(), operationId, isUserOperation);
	}

	/**
	 * Adds to context the user operation.
	 *
	 * @param data
	 *            the JSON data from the request
	 */
	@Override
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = super.parseRequest(data);
		context.put(ExecutableOperationProperties.USER_OPERATION_ID,
				JsonUtil.getStringValue(data, ExecutableOperationProperties.USER_OPERATION_ID));
		return context;
	}
}
