package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_TARGET;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.DMS_ID;

import org.json.JSONObject;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Base class to implement the import operations.
 * 
 * @author BBonev
 */
public abstract class BaseImportInstanceExecutor extends BaseInstanceExecutor {

	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		SchedulerContext context = super.parseRequest(data);
		context.put(DMS_ID, JsonUtil.getStringValue(data, DMS_ID));
		// mark the class as non persisted - we are just importing it
		InstanceReference reference = context.getIfSameType(CTX_TARGET, InstanceReference.class);
		SequenceEntityGenerator.registerId(reference.getIdentifier());
		return context;
	}

	@Override
	public OperationResponse execute(SchedulerContext context) {
		Instance instance = getOrCreateInstance(context);
		if (instance instanceof DMSInstance) {
			String dmsId = context.getIfSameType(DMS_ID, String.class);
			((DMSInstance) instance).setDmsId(dmsId);
		}
		instance.getProperties().put(DefaultProperties.IS_IMPORTED, Boolean.TRUE);
		beforeSave(instance, context);

		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		try {
			Instance savedInstance = instanceService.save(instance, new Operation(getOperation()));
			return new OperationResponse(SchedulerEntryStatus.COMPLETED,toJson(savedInstance));
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
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
	protected void beforeSave(Instance instance, SchedulerContext context) {
		// nothing to do here
	}

	@Override
	public boolean rollback(SchedulerContext data) {
		// nothing to rollback
		return true;
	}

}
