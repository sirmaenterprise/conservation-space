package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.DMS_ID;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.executors.ExecutableOperationProperties;
import com.sirma.itt.emf.executors.Operation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Operation that creates and starts a workflow.
 * 
 * <pre>
 * <code>
 * {
 * 	operation: "createWorkflow",
 * 	definition: "someWorkflowDefinition",
 * 	revision: definitionRevision,
 * 	id: "emf:someInstanceId",
 * 	type: "workflow",
 * 	parentId: "emf:caseId",
 * 	parentType: "case",
 * 	properties: {
 * 		property1: "some property value 1",
 * 		property2: "true",
 * 		property3: "2323"
 * 	},
 * 	taskId: "emf:someTaskId",
 * 	taskProperties: {
 * 		property1: "some property value 1",
 * 		property2: "true",
 * 		property3: "2323"
 * 	}
 * }
 * </code>
 * </pre>
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = StartWorkflowExecutor.TARGET_NAME, order = 130)
public class StartWorkflowExecutor extends BaseInstanceExecutor {

	private static final String NEXT_TASK_ID = "nextTaskId";
	private static final String TASK_KEY = "task";
	private static final String TASK_PROPERTIES = "taskProperties";
	@Inject
	private WorkflowService workflowService;

	@Override
	public String getOperation() {
		return ActionTypeConstants.START_WORKFLOW;
	}

	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		SchedulerContext context = super.parseRequest(data);
		// extract any specific task information
		InstanceReference reference = createTaskReference(data);
		context.put(TASK_KEY, reference);

		Map<String, String> properties = extractProperties(data, TASK_PROPERTIES);
		if (!properties.isEmpty()) {
			context.put(TASK_PROPERTIES, (Serializable) properties);
		}
		return context;
	}

	/**
	 * Creates the task reference.
	 * 
	 * @param data
	 *            the data
	 * @return the instance reference
	 */
	private InstanceReference createTaskReference(JSONObject data) {
		String taskId = JsonUtil.getStringValue(data, "taskId");
		if (taskId == null) {
			taskId = SequenceEntityGenerator.generateId(true).toString();
		}
		InstanceReference reference = typeConverter.convert(InstanceReference.class,
				TaskInstance.class.getName());
		reference.setIdentifier(taskId);
		return reference;
	}

	@Override
	public OperationResponse execute(SchedulerContext data) {
		Instance instance = getOrCreateInstance(data);
		WorkflowInstanceContext context = null;
		if (instance instanceof WorkflowInstanceContext) {
			context = (WorkflowInstanceContext) instance;
			// set the create instance in to context so that the task can use it to create itself
			data.put(ExecutableOperationProperties.CTX_TARGET_INSTANCE, context);
		} else {
			throw new EmfRuntimeException("Expected workflow data but found " + instance.getClass());
		}
		Instance task = getOrCreateInstance(data, TASK_KEY,
				ExecutableOperationProperties.CTX_TARGET_INSTANCE, TASK_PROPERTIES);

		List<TaskInstance> startedWorkflowTasks = workflowService.startWorkflow(context,
				(TaskInstance) task);
		// put the next task in the properties map for testing purposes
		if (!startedWorkflowTasks.isEmpty()) {
			TaskInstance taskInstance = startedWorkflowTasks.get(0);
			context.getProperties().put(NEXT_TASK_ID, taskInstance.getId());
		}

		// backup the dms ID so we could delete it from DMS if needed
		data.put(DMS_ID, context.getDmsId());
		// no need to keep it
		data.remove(ExecutableOperationProperties.CTX_TARGET_INSTANCE);
		return new OperationResponse(SchedulerEntryStatus.COMPLETED, JsonUtil.transformInstance(
				context, "id", "title", NEXT_TASK_ID));
	}

	@Override
	protected DefinitionModel getDefinition(SchedulerContext context, Class<?> javaClass) {
		DefinitionModel model = super.getDefinition(context, javaClass);
		if (TaskInstance.class.equals(javaClass) && (model instanceof WorkflowDefinition)) {
			return WorkflowHelper.getStartTask((WorkflowDefinition) model);
		}
		return model;
	}

	@Override
	public Map<Serializable, Operation> getDependencies(SchedulerContext data) {
		Map<Serializable, Operation> dependancies = CollectionUtils.createHashMap(4);
		dependancies.put(
				data.getIfSameType(ExecutableOperationProperties.CTX_TARGET,
						InstanceReference.class).getIdentifier(), Operation.CREATE);
		dependancies.put(data.getIfSameType(TASK_KEY, InstanceReference.class).getIdentifier(),
				Operation.CREATE);
		InstanceReference reference = data.getIfSameType(ExecutableOperationProperties.CTX_PARENT,
				InstanceReference.class);
		if (reference != null) {
			dependancies.put(reference.getIdentifier(), Operation.USE);
		}
		return dependancies;
	}

}
