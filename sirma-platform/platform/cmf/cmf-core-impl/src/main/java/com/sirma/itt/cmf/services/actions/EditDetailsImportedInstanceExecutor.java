package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.ALL_OTHERS;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.DMS_ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.INHERIT;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PERMISSIONS;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.actions.EditDetailsExecutor;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Operation that does not call DMS when saving instances.
 *
 * <pre>
 * <code>
 * 		{
 * 			operation: "createSomething",
 * 			definition: "someWorkflowDefinition",
 * 			revision: definitionRevision,
 * 			id: "emf:someInstanceId",
 * 			type: "workflow",
 * 			parentId: "emf:caseId",
 * 			parentType: "case",
 * 			properties : {
 * 				property1: "some property value 1",
 * 				property2: "true",
 * 				property3: "2323"
 * 			},
 * 			permissions : {
 * 				"userName" : "Role",
 * 				....
 * 			},
 * 		"inherit" : "true"
 * 		}
 * </code>
 * </pre>
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 11)
public class EditDetailsImportedInstanceExecutor extends EditDetailsExecutor {

	@Inject
	private RoleService roleService;

	/** The Constant EDIT_DETAILS. */
	private static final Operation EDIT_DETAILS = new Operation(ActionTypeConstants.EDIT_DETAILS);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "saveImported";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = super.parseRequest(data);
		context.put(PERMISSIONS, (Serializable) BaseImportInstanceExecutor.extractPermissions(data, PERMISSIONS,
				resourceService, roleService));
		Object inherit = JsonUtil.getValueOrNull(data, INHERIT);
		if (inherit instanceof String) {
			Boolean inherited = Boolean.valueOf((String) inherit);
			context.put(INHERIT, inherited);
		}
		Object allOther = JsonUtil.getValueOrNull(data, ALL_OTHERS);
		if (allOther instanceof String) {
			context.put(ALL_OTHERS, roleService.getRoleIdentifier((String) allOther));
		}
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "boxing", "unchecked" })
	public OperationResponse execute(OperationContext context) {
		Options.DO_NOT_CALL_DMS.enable();
		Options.DISABLE_STALE_DATA_CHECKS.enable();
		Options.OVERRIDE_MODIFIER_INFO.enable();
		try {
			Instance instance = getOrCreateInstance(context);

			Instance savedInstance = getInstanceService().save(instance, createOperation(context));
			// backup the dms ID so we could delete it from DMS if needed
			if (savedInstance instanceof DMSInstance) {
				context.put(DMS_ID, ((DMSInstance) savedInstance).getDmsId());
			}

			return new OperationResponse(OperationStatus.COMPLETED, toJson(savedInstance));
		} finally {
			Options.DO_NOT_CALL_DMS.disable();
			Options.DISABLE_STALE_DATA_CHECKS.disable();
			Options.OVERRIDE_MODIFIER_INFO.disable();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Operation createOperation(OperationContext context) {
		return EDIT_DETAILS;
	}
}
