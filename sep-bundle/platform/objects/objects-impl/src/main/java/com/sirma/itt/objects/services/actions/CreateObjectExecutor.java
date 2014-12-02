package com.sirma.itt.objects.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.ExecutableOperationProperties;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;
import com.sirma.itt.objects.services.ObjectService;

/**
 * Executor for domain object creation. The revert operation here is physical and permanent delete
 * of the object.
 * 
 * @author Adrian Mitev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 510)
public class CreateObjectExecutor extends BaseInstanceExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateObjectExecutor.class);

	@Inject
	private ObjectService objectService;

	@Inject
	private SequenceEntityGenerator idGenerator;

	@Override
	public String getOperation() {
		return ObjectActionTypeConstants.CREATE_OBJECT;
	}
	
	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		SchedulerContext context = super.parseRequest(data);
		// enrich with the context property
		String content = JsonUtil.getStringValue(data, "content");
		context.put("content", content);

		return context;
	}

	@Override
	protected Instance getOrCreateInstance(SchedulerContext context) {
		Instance instance = super.getOrCreateInstance(context);
		String id = instance.getId().toString();
		idGenerator.registerId(id);

		// Creating a RestInstance is performed because there is already a specific type converter
		// that is able to handle RestInstance and convert it to an ObjectInstance with a view that
		// has a content (DocumentInstance). Don't copy the instance id because the type converter
		// expects only instances that don't have id set
		RestInstance restInstance = new RestInstance();
		restInstance.setId(id);
		restInstance.setType(ObjectInstance.class.getSimpleName().toLowerCase());
		restInstance.setDefinitionId(instance.getIdentifier());
		restInstance.setContent((String) context.get("content"));
		restInstance.getProperties().putAll(instance.getProperties());

		ObjectInstance result = typeConverter.convert(ObjectInstance.class, restInstance);

		// set parent (owning) instance if available
		if (instance instanceof OwnedModel) {
			InstanceReference reference = (InstanceReference) context
					.get(ExecutableOperationProperties.CTX_PARENT);
			if (reference != null) {
				Instance parent = reference.toInstance();
				result.setOwningInstance(parent);
			}
		}

		return result;
	}

	@Override
	public boolean rollback(SchedulerContext data) {
		Instance instance = getOrCreateInstance(data);
		if (instance instanceof ObjectInstance) {
			ObjectInstance projectInstance = (ObjectInstance) instance;
			if (projectInstance.getDmsId() != null) {
				// delete all content from DMS about the case
				try {
					objectService.delete(projectInstance,
							new Operation(ActionTypeConstants.DELETE), true);
				} catch (Exception e) {
					LOGGER.warn("Failed to rollback domain object instance creation by deleting"
							+ " due to {}", e.getMessage(), e);
					return false;
				}
			}
			// probably we didn't manage to create it so nothing to worry about
		} else {
			if (instance != null) {
				LOGGER.warn("Invalid instance type passed for rollback. Expected {} but was {}.",
						ObjectInstance.class.getSimpleName(), instance.getClass().getSimpleName());
			} else {
				LOGGER.debug("Nothing to rollback.");
			}
		}

		return true;
	}

}