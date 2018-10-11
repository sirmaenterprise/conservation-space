package com.sirma.sep.instance.operation.executor;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_TARGET;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.DMS_ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PERMISSIONS;

import java.io.Serializable;

import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.role.RoleService;

/**
 * Base class to implement the import operations.
 *
 * @author BBonev
 */
public abstract class BaseImportInstanceExecutor extends BaseInstanceExecutor {

	@Inject
	private RoleService roleService;

	@Override
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = super.parseRequest(data);
		context.put(DMS_ID, JsonUtil.getStringValue(data, DMS_ID));
		// mark the class as non persisted - we are just importing it
		InstanceReference reference = context.getIfSameType(CTX_TARGET, InstanceReference.class);
		idManager.registerId(reference.getId());
		InstanceExecutorUtils.extractAdditionalPermissions(data, context, roleService);
		context.put(PERMISSIONS, (Serializable) InstanceExecutorUtils.extractPermissions(data, PERMISSIONS,
				resourceService, roleService));
		return context;
	}

	@Override
	public OperationResponse execute(OperationContext context) {
		Instance instance = getOrCreateInstance(context);
		if (instance instanceof DMSInstance) {
			String dmsId = context.getIfSameType(DMS_ID, String.class);
			((DMSInstance) instance).setDmsId(dmsId);
		}
		instance.getProperties().put(DefaultProperties.IS_IMPORTED, Boolean.TRUE);
		beforeSave(instance, context);

		Options.DO_NOT_CALL_DMS.enable();
		try {
			Instance savedInstance = getInstanceService().save(instance, new Operation(getOperation()));
			return new OperationResponse(OperationStatus.COMPLETED, toJson(savedInstance));
		} finally {
			Options.DO_NOT_CALL_DMS.disable();
		}
	}

	/**
	 * Method called before save of the instance after has been created.
	 *
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 */
	protected void beforeSave(Instance instance, OperationContext context) {
		// nothing to do here
	}

	@Override
	public boolean rollback(OperationContext data) {
		// nothing to rollback
		return true;
	}
}