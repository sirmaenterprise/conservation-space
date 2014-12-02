package com.sirma.itt.cmf.alfresco4.services;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils.QueryMode;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessor;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.codelist.CodelistPropertiesConstants;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * The Class WorkflowAlfresco4Service is the implementation for the adapter to process workflows and
 * standalone tasks.
 */
@ApplicationScoped
public class WorkflowAlfresco4Service implements CMFWorkflowAdapterService,
		AlfrescoCommunicationConstants {

	/** The Constant WORKFLOW_INSTANCE_ID_TEMPLATE. */
	private static final String WORKFLOW_INSTANCE_ID_TEMPLATE = "{workflow_instance_id}";

	/** The Constant KEY_WORKFLOW_INSTANCE. */
	private static final String KEY_WORKFLOW_INSTANCE = "workflowInstance";
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(WorkflowAlfresco4Service.class);
	/** The debug enabled. */
	private boolean debugEnabled;

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The workflow instance convert. */
	@Converter(name = ConverterConstants.WORKFLOW)
	@Inject
	private DMSTypeConverter wfConvertor;

	/** The task instance convert. */
	@Converter(name = ConverterConstants.TASK)
	@Inject
	private DMSTypeConverter taskConvertor;

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;

	/** The task definitions cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_TASK_DEFINITION, defaultValue = "227")
	private Integer taskDefinitionsCL;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ExpressionsManager manager;

	/**
	 * Default constructor.
	 */
	public WorkflowAlfresco4Service() {
		debugEnabled = LOGGER.isDebugEnabled();
	}

	/**
	 * Visitor implementation for Workflows and task.
	 *
	 * @author Borislav Banchev
	 */
	private class WorkflowVistor extends DefaultQueryVisitor {

		/** The builder. */
		private Map<String, String> taskProps = new HashMap<String, String>(4, 10);

		/** The wf props. */
		private Map<String, String> wfProps = new HashMap<String, String>(2, 5);

		/** The mode. */
		private QueryMode mode;

		/**
		 * New instance for visitor.
		 *
		 * @param mode
		 *            is the query mode
		 */
		public WorkflowVistor(QueryMode mode) {
			super();
			this.mode = mode;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.itt.cmf.search.Query.Visitor#visit(com.sirma.itt.cmf.search .Query)
		 */
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(Query query) throws Exception {
			appendByValueType(taskProps, wfProps, builder, query, mode);
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.itt.cmf.search.AbstractQueryVistor#end()
		 */
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void end() {
			if (!taskProps.isEmpty()) {
				if (builder.length() > 0) {
					builder.append("&");
				}
				builder.append(KEY_PROPERTIES).append("=").append(new JSONObject(taskProps));
			}
			if (!wfProps.isEmpty()) {
				if (builder.length() > 0) {
					builder.append("&");
				}
				builder.append("workflowProperties").append("=").append(new JSONObject(wfProps));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#transition
	 * (java.lang.String, com.sirma.itt.cmf.beans.model.TaskInstance)
	 */
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractTaskInstance> List<T> transition(String transition, T task)
			throws DMSException {
		if ((task == null) || (task.getTaskInstanceId() == null) || (task.getProperties() == null)) {
			throw new DMSException("Invalid data for task is provided for current transition! "
					+ task);
		}
		JSONObject request = new JSONObject();
		String restResult = null;
		try {
			request.put(KEY_TASK_ID, task.getTaskInstanceId());
			request.put(KEY_TRANSITION_ID, transition);
			task.getProperties().put(TaskProperties.TRANSITION_OUTCOME, transition);
			// add the properties
			Map<String, Serializable> convertProperties = taskConvertor.convertCMFtoDMSProperties(
					task.getProperties(), task, DMSTypeConverter.WORKFLOW_TASK_LEVEL);

			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));

			request.put(TaskProperties.NEXT_STATE_PROP_MAP,
					getNextStateProps(task, DMSTypeConverter.WORKFLOW_TASK_LEVEL));
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			restResult = restClient.request(ServiceURIRegistry.CMF_WORKFLOW_TRANSITION,
					createMethod);
			if (debugEnabled) {
				debug("Transition:", transition, " with id:", task.getTaskInstanceId(),
						" result: ", restResult);
			}
			if (restResult != null) {
				WorkflowInstanceContext context = null;
				Long revision = null;
				Class<? extends AbstractTaskInstance> cls = TaskInstance.class;
				if (task instanceof TaskInstance) {
					context = ((TaskInstance) task).getContext();
					revision = context.getRevision();
				} else if (task instanceof StandaloneTaskInstance) {
					cls = StandaloneTaskInstance.class;
				}
				List<T> fromResponse = (List<T>) updateTaskModelFromResponse(new JSONObject(
						restResult), revision, context, cls);
				debug("Result ", fromResponse);
				return fromResponse;
			}
		} catch (DMSClientException e) {
			throw new DMSException("Tranisition '" + transition + "' for task '"
					+ task.getTaskInstanceId() + "' is not processed due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Tranisition '" + transition + "' for task '"
					+ task.getTaskInstanceId() + "' is not processed!", e);
		}
		throw new DMSException("Tranisition '" + transition + "' for task '"
				+ task.getTaskInstanceId() + "' is not processed !");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#updateTask
	 * (com.sirma.itt.cmf.beans.model.TaskInstance, java.util.Map)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractTaskInstance> List<T> updateTask(T task,
			Map<String, Serializable> toRemove) throws DMSException {
		if ((task == null) || (task.getTaskInstanceId() == null) || (task.getProperties() == null)) {
			throw new DMSException("Invalid data for task is provided for current update!");
		}
		JSONObject request = new JSONObject();
		String restResult = null;
		try {
			if (task instanceof StandaloneTaskInstance) {
				StandaloneTaskInstance standaloneTask = (StandaloneTaskInstance) task;
				request.put(KEY_TYPE, standaloneTask.getDmsTaskType());
				request.put(KEY_REFERENCE_ID, standaloneTask.getParentContextId());
			}
			request.put(KEY_TASK_ID, task.getTaskInstanceId());
			// add the properties
			Map<String, Serializable> convertProperties = taskConvertor.convertCMFtoDMSProperties(
					task.getProperties(), task, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			List<String> nullable = (List<String>) task.getProperties().get(
					TaskProperties.NULLABLE_PROPS);
			if (nullable != null) {
				for (String string : nullable) {
					Pair<String, Serializable> convertCMFtoDMSProperty = taskConvertor
							.convertCMFtoDMSProperty(string, "", task,
									DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
					if (convertCMFtoDMSProperty.getFirst() != null) {
						convertProperties.put(convertCMFtoDMSProperty.getFirst(), null);
					}
				}
			}
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			restResult = restClient.request(ServiceURIRegistry.CMF_WORKFLOW_TASKUPDATE,
					createMethod);
			if (debugEnabled) {
				debug("Update for id:", task.getTaskInstanceId(), " result: ", restResult);
			}
			if (restResult != null) {
				return new ArrayList<>(Arrays.asList(task));
			}
		} catch (DMSClientException e) {
			throw new DMSException("Task " + task.getTaskInstanceId()
					+ " is not updated due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Task " + task.getTaskInstanceId() + " is not updated!", e);
		}
		throw new DMSException("Task " + task.getTaskInstanceId() + " is not updated!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#startWorkflow
	 * (java.lang.String, com.sirma.itt.cmf.beans.model.WorkflowInstanceContext)
	 */
	@SuppressWarnings("unchecked")
	/**
	 * Start workflow in dms using the bpmn engine and return the after start tasks.
	 *
	 * @param startTask
	 *            the starting task
	 * @param workflowContext
	 *            the workflow context
	 * @return the workflow instance context
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public List<TaskInstance> startWorkflow(TaskInstance startTask,
			WorkflowInstanceContext workflowContext) throws DMSException {
		String workflowId = null;
		if ((workflowContext == null) || ((workflowId = workflowContext.getIdentifier()) == null)) {
			throw new DMSException("Invalid data is provided for starting workflow"
					+ (workflowContext == null ? " null context!" : " null context identifier!"));
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_WORKFLOW_ID, workflowId);
			request.put(KEY_REFERENCE_ID,
					((DMSInstance) workflowContext.getOwningInstance()).getDmsId());

			Map<String, Serializable> contextProperties = wfConvertor.convertCMFtoDMSProperties(
					workflowContext.getProperties(), workflowContext,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			Map<String, Serializable> startTaskProperties = taskConvertor
					.convertCMFtoDMSProperties(startTask.getProperties(), startTask,
							DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			if (debugEnabled) {
				debug("Starting workflow with context data: ", contextProperties,
						" and start task data: ", startTaskProperties);
			}
			contextProperties.putAll(startTaskProperties);
			// add the revision just in case
			if (!contextProperties.containsKey(CMF_WF_MODEL_PREFIX + WorkflowProperties.REVISION)) {
				contextProperties.put(CMF_WF_MODEL_PREFIX + WorkflowProperties.REVISION,
						workflowContext.getRevision());
			}

			request.put(
					TaskProperties.NEXT_STATE_PROP_MAP,
					getNextStateProps(workflowContext,
							DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL));
			Serializable currentStateProps = workflowContext.getProperties().get(
					TaskProperties.CURRENT_STATE_PROP_MAP);
			if (currentStateProps != null) {
				// get the basic definition for next task
				request.put(TaskProperties.CURRENT_STATE_PROP_MAP, taskConvertor
						.filterCMFProperties((Map<String, Serializable>) currentStateProps,
								DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL));
			}

			JSONObject jsonObject = new JSONObject(contextProperties);
			request.put(KEY_PROPERTIES, jsonObject);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient.request(ServiceURIRegistry.CMF_WORKFLOW_START,
					createMethod);
			if (debugEnabled) {
				debug("Workflow with definition:", workflowId, " result: ", restResult);
			}
			if (restResult != null) {
				JSONObject responce = new JSONObject(restResult);
				JSONObject workflowInfo;
				if (responce.has("workflow")) {
					workflowInfo = responce.getJSONObject("workflow");
				} else {
					workflowInfo = responce;
				}
				// get the required info
				String workflowInstanceId = workflowInfo.getString(KEY_ID);
				String dmsPackage = workflowInfo.getString("package");
				// String context = workflowInfo.getString("context");
				// finally set
				workflowContext.setWorkflowInstanceId(workflowInstanceId);
				workflowContext.setDmsId(dmsPackage);

				// check if we have tasks to process
				List<TaskInstance> fromResponse;
				if (responce.has("tasks")) {
					fromResponse = updateTaskModelFromResponse(responce.getJSONObject("tasks"),
							workflowContext.getRevision(), workflowContext, TaskInstance.class);
				} else {
					fromResponse = new LinkedList<TaskInstance>();
				}
				return fromResponse;
			}
		} catch (DMSClientException e) {
			throw new DMSException("Workflow " + workflowId + " is not started due to dms error!",
					e);
		} catch (Exception e) {
			throw new DMSException("Workflow " + workflowId + " is not started!", e);
		}
		throw new DMSException("Workflow " + workflowId + " is not started!");
	}

	/**
	 * Retrieves next state props and enrich them with current container
	 *
	 * @param instance
	 *            the instance to process
	 * @param level
	 *            the level the use for converting
	 * @return the map with updated and retrieved data
	 * @throws JSONException
	 *             on any json error
	 */
	private Map<String, Serializable> getNextStateProps(Instance instance, FieldProcessor level)
			throws JSONException {
		@SuppressWarnings("unchecked")
		Map<String, Serializable> nextStateProps = (Map<String, Serializable>) instance
				.getProperties().get(TaskProperties.NEXT_STATE_PROP_MAP);
		if (nextStateProps == null) {
			nextStateProps = new HashMap<String, Serializable>(1);
		}
		Pair<String, Serializable> contextId = taskConvertor.convertCMFtoDMSProperty("containerId",
				((TenantAware) instance).getContainer(), DMSTypeConverter.PROPERTIES_MAPPING);
		// get the basic definition for next task
		Map<String, Serializable> nextStatePropsConverted = taskConvertor.filterCMFProperties(
				nextStateProps, level);
		nextStatePropsConverted.put(contextId.getFirst(), contextId.getSecond());

		return nextStatePropsConverted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StandaloneTaskInstance startTask(StandaloneTaskInstance task) throws DMSException {
		if (task == null) {
			throw new DMSException("Invalid data is provided 'null' for starting task!");
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_TYPE, task.getDmsTaskType());
			request.put(KEY_REFERENCE_ID, task.getParentContextId());
			Map<String, Serializable> convertedProperties = taskConvertor
					.convertCMFtoDMSProperties(task.getProperties(), task,
							DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			// add the revision just in case
			// if (!convertedProperties.containsKey(CMF_WF_MODEL_PREFIX +
			// WorkflowProperties.REVISION)) {
			// convertedProperties.put(CMF_WF_MODEL_PREFIX + WorkflowProperties.REVISION,
			// workflowContext.getRevision());
			// }
			Map<String, Serializable> updateNextStateProps = getNextStateProps(task,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			convertedProperties.putAll(updateNextStateProps);
			request.put(KEY_PROPERTIES, convertedProperties);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient.request(ServiceURIRegistry.CMF_TASK_START, createMethod);
			if (debugEnabled) {
				debug("Task with definition:", task.getDmsTaskType(), " result: ", restResult);
			}
			if (restResult != null) {
				JSONObject responce = new JSONObject(restResult);

				// check if we have tasks to process
				if (responce.has("tasks")) {
					List<StandaloneTaskInstance> updateTaskModelFromResponse = updateTaskModelFromResponse(
							responce.getJSONObject("tasks"), task.getRevision(), task,
							StandaloneTaskInstance.class);
					if (updateTaskModelFromResponse.size() == 1) {
						StandaloneTaskInstance standaloneTaskInstance = updateTaskModelFromResponse
								.get(0);
						return standaloneTaskInstance;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Task " + task.getTaskInstanceId()
					+ " is not started due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Task " + task.getTaskInstanceId() + " is not started!", e);
		}
		throw new DMSException("Task " + task.getTaskInstanceId() + " is not started!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#cancelWorkflow
	 * (com.sirma.itt.cmf.beans.model.WorkflowInstanceContext)
	 */
	/**
	 * Cancel workflow.
	 *
	 * @param workflowContext
	 *            the workflow context
	 * @throws DMSException
	 *             the dMS exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void cancelWorkflow(WorkflowInstanceContext workflowContext) throws DMSException {
		if ((workflowContext == null) || (workflowContext.getWorkflowInstanceId() == null)) {
			throw new DMSException("Invalid data is provided for cancelling workflow!");
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_WORKFLOW_ID, workflowContext.getWorkflowInstanceId());
			Serializable currentStateProps = workflowContext.getProperties().get(
					TaskProperties.CURRENT_STATE_PROP_MAP);
			if (currentStateProps != null) {
				// get data for current task
				request.put(TaskProperties.CURRENT_STATE_PROP_MAP, taskConvertor
						.filterCMFProperties((Map<String, Serializable>) currentStateProps,
								DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL));
			}

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient.request(ServiceURIRegistry.CMF_WORKFLOW_CANCEL,
					createMethod);
			if (debugEnabled) {
				debug("Workflow with id:", workflowContext.getWorkflowInstanceId(), " result: ",
						restResult);
			}
			if (restResult != null) {
				JSONObject workflowInfo = new JSONObject(restResult);
				// get the required info
				String workflowInstanceId = workflowInfo.getString(KEY_ID);
				if (workflowContext.getWorkflowInstanceId().equals(workflowInstanceId)) {
					return;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Workflow " + workflowContext.getWorkflowInstanceId()
					+ " is not cancelled due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Workflow " + workflowContext.getWorkflowInstanceId()
					+ " is not cancelled!", e);
		}
		throw new DMSException("Workflow " + workflowContext.getWorkflowInstanceId()
				+ " is not cancelled!");
	}

	@Override
	public void deleteWorkflow(WorkflowInstanceContext workflowContext, boolean permanent)
			throws DMSException {
		// TODO: Implement workflow deletion in DMS
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#cancelTask(com.sirma.itt.cmf
	 * .beans.model.StandaloneTaskInstance)
	 */
	@Override
	public void cancelTask(StandaloneTaskInstance taskInstance) throws DMSException {
		if ((taskInstance == null) || (taskInstance.getTaskInstanceId() == null)) {
			throw new DMSException("Invalid data is provided for cancelling task!");
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_TASK_ID, taskInstance.getTaskInstanceId());
			request.put(KEY_PROPERTIES, taskConvertor.convertCMFtoDMSProperties(
					taskInstance.getProperties(), taskInstance,
					DMSTypeConverter.WORKFLOW_TASK_LEVEL));
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient
					.request(ServiceURIRegistry.CMF_TASK_CANCEL, createMethod);
			if (debugEnabled) {
				debug("Task with id:", taskInstance.getTaskInstanceId(), " result: ", restResult);
			}
			if (restResult != null) {
				JSONObject taskInfo = new JSONObject(restResult);
				JSONArray tasks = taskInfo.getJSONArray(KEY_ROOT_DATA);
				if (tasks.length() != 1) {
					throw new DMSException("Task " + taskInstance.getTaskInstanceId()
							+ " is not cancelled correctly!");
				}
				// get the required info
				String taskInstanceId = tasks.getJSONObject(0).getString(KEY_ID);
				if (taskInstance.getTaskInstanceId().equals(taskInstanceId)) {
					return;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Task " + taskInstance.getTaskInstanceId()
					+ " is not cancelled due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Task " + taskInstance.getTaskInstanceId()
					+ " is not cancelled!", e);
		}
		throw new DMSException("Task " + taskInstance.getTaskInstanceId() + " is not cancelled!");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#deleteTask(com.sirma.itt.cmf
	 * .beans.model.StandaloneTaskInstance)
	 */
	@Override
	public void deleteTask(StandaloneTaskInstance taskInstance) throws DMSException {
		if ((taskInstance == null) || (taskInstance.getTaskInstanceId() == null)) {
			throw new DMSException("Invalid data is provided for cancelling task!");
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_TASK_ID, taskInstance.getTaskInstanceId());

			Map<String, Serializable> convertCMFtoDMSProperties = taskConvertor
					.convertCMFtoDMSProperties(taskInstance.getProperties(), taskInstance,
							DMSTypeConverter.WORKFLOW_TASK_LEVEL);
			convertCMFtoDMSProperties.put("cmfwf:archiveReason", "Automatically deleted task!");
			request.put(KEY_PROPERTIES, convertCMFtoDMSProperties);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient
					.request(ServiceURIRegistry.CMF_TASK_CANCEL, createMethod);
			if (debugEnabled) {
				debug("Task with id:", taskInstance.getTaskInstanceId(), " result: ", restResult);
			}
			if (restResult != null) {
				JSONObject taskInfo = new JSONObject(restResult);
				JSONArray tasks = taskInfo.getJSONArray(KEY_ROOT_DATA);
				if (tasks.length() != 1) {
					throw new DMSException("Task " + taskInstance.getTaskInstanceId()
							+ " is not deleted correctly!");
				}
				// get the required info
				String taskInstanceId = tasks.getJSONObject(0).getString(KEY_ID);
				if (taskInstance.getTaskInstanceId().equals(taskInstanceId)) {
					return;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Task " + taskInstance.getTaskInstanceId()
					+ " is not deleted due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Task " + taskInstance.getTaskInstanceId() + " is not deleted!",
					e);
		}
		throw new DMSException("Task " + taskInstance.getTaskInstanceId() + " is not deleted!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#searchTasks
	 * (com.sirma.itt.cmf.beans.SearchArguments)
	 */
	/**
	 * Search tasks and return pair of <taskid,workflowid>.
	 *
	 * @param args
	 *            the args
	 * @return the search arguments with populated result
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public SearchArguments<Pair<String, String>> searchTasksLight(
			SearchArguments<Pair<String, String>> args) throws DMSException {
		return searchTasksInternal(ServiceURIRegistry.CMF_TASK_SEARCH_SERVICE, args, null,
				Pair.class);
	}

	/**
	 * Search tasks and constructs entities from result.
	 *
	 * @param args
	 *            the args
	 * @return the search arguments with populated result
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public SearchArguments<TaskInstance> searchTasks(SearchArguments<TaskInstance> args)
			throws DMSException {
		return searchTasksInternal(ServiceURIRegistry.CMF_TASK_SEARCH_SERVICE, args, null,
				TaskInstance.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaskInstance> getTasks(WorkflowInstanceContext context, TaskState state)
			throws DMSException {
		if ((context == null) || (context.getWorkflowInstanceId() == null)) {
			throw new DMSException("Invalid data is provided for searching workflow tasks!");
		}
		String baseURI = ServiceURIRegistry.CMF_TASK_FOR_WF_SEARCH_SERVICE.replace(
				WORKFLOW_INSTANCE_ID_TEMPLATE, context.getWorkflowInstanceId());

		SearchArguments<TaskInstance> args = new SearchArguments<TaskInstance>();
		if (state != null) {
			args.setQuery(new Query(TaskProperties.SEARCH_STATE, state.toString()));
		} else {
			args.setQuery(Query.getEmpty());
		}
		return searchDBTasksInternal(baseURI, args, context).getResult();
	}

	/**
	 * Search tasks internal. The task instances properties is filled with the mapped keys.
	 *
	 * @param <T>
	 *            the generic type
	 * @param baseURI
	 *            the base uri
	 * @param args
	 *            the args
	 * @param context
	 *            the context
	 * @param cls
	 *            the cls
	 * @return the search arguments
	 * @throws DMSException
	 *             the dMS exception
	 */
	@SuppressWarnings("unchecked")
	private <T> SearchArguments<T> searchTasksInternal(String baseURI, SearchArguments<T> args,
			WorkflowInstanceContext context, Class<?> cls) throws DMSException {
		try {
			JSONObject request = new JSONObject();
			TimeTracker tracker = new TimeTracker();
			tracker.begin();
			WorkflowVistor visitor = new WorkflowVistor(QueryMode.LUCENE_SEARCH);
			// Query paging = new Query("maxItems", 100).and("skipCount",
			// args.getSkipCount());
			Query instanceEntry = args.getQuery().getEntry(KEY_WORKFLOW_INSTANCE);
			if (instanceEntry != null) {
				request.put(KEY_WORKFLOW_INSTANCE, instanceEntry.getValue());
			}
			if (StringUtils.isNotNullOrEmpty(args.getContext())) {
				request.put(KEY_CONTEXT, args.getContext());
			}
			args.getQuery().visit(visitor);
			request.put(KEY_PAGING, getPaging(args));
			request.put(KEY_SORT, getSorting(args));
			request.put(KEY_QUERY, visitor.getQuery().toString());
			debug("QUERY: ", visitor.getQuery().toString());
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String requestURI = ServiceURIRegistry.CMF_SEARCH_SERVICE + "/task";
			debug(tracker.stopInSeconds() + "s for converting data");
			tracker.begin();
			String response = restClient.request(requestURI, createMethod);
			debug(tracker.stopInSeconds() + "s for receiving response");
			tracker.begin();
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (context != null) {
					if (cls == Pair.class) {
						args.setResult((List<T>) fetchTaskModelFromResponse(result,
								context.getRevision(), context));
					} else if (cls == TaskInstance.class) {
						args.setResult((List<T>) updateTaskModelFromResponse(result,
								context.getRevision(), context, TaskInstance.class));
					}
				} else {
					if (cls == Pair.class) {
						args.setResult((List<T>) fetchTaskModelFromResponse(result, null, null));
					} else {
						args.setResult((List<T>) updateTaskModelFromResponse(result, null, null,
								TaskInstance.class));
					}
				}
				AlfrescoUtils.populatePaging(result, args);
				debug(tracker.stopInSeconds() + "s for parsing response");
				return args;
			}
		} catch (DMSClientException e) {
			throw new DMSException("Search in DMS for task failed due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Search in DMS for task failed!", e);
		}
		throw new DMSException("DMS system does not respond to search!");
	}

	/**
	 * Search tasks internal. The task instances properties is filled with the mapped keys.
	 *
	 * @param baseURI
	 *            the base uri
	 * @param args
	 *            the args
	 * @param context
	 *            the context
	 * @return the search arguments
	 * @throws DMSException
	 *             the dMS exception
	 */
	private SearchArguments<TaskInstance> searchDBTasksInternal(String baseURI,
			SearchArguments<TaskInstance> args, WorkflowInstanceContext context)
			throws DMSException {
		try {
			JSONObject request = new JSONObject();
			TimeTracker tracker = new TimeTracker();
			tracker.begin();
			WorkflowVistor visitor = new WorkflowVistor(QueryMode.DB_SEARCH);
			Query paging = new Query("maxItems", args.getMaxSize()).and("skipCount",
					args.getSkipCount());
			args.getQuery().and(paging);
			args.getQuery().visit(visitor);
			request.put(KEY_PAGING, getPaging(args));
			request.put(KEY_SORT, getSorting(args));
			request.put(KEY_QUERY, visitor.getQuery().toString());
			HttpMethod createMethod = restClient.createMethod(new GetMethod(), "", true);
			String requestURI = baseURI + "?" + visitor.getQuery().toString();
			debug(tracker.stopInSeconds() + "s for converting data");
			tracker.begin();
			String response = restClient.request(requestURI, createMethod);
			debug(tracker.stopInSeconds() + "s for receiving response");
			tracker.begin();
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (context != null) {
					args.setResult(updateTaskModelFromResponse(result, context.getRevision(),
							context, TaskInstance.class));
				} else {
					args.setResult(updateTaskModelFromResponse(result, null, null,
							TaskInstance.class));
				}
				debug(tracker.stopInSeconds() + "s for parsing response");
				return args;
			}
		} catch (DMSClientException e) {
			throw new DMSException("Search in DMS for task failed due to dms error!", e);
		} catch (Exception e) {
			throw new DMSException("Search in DMS for task failed!", e);
		}
		throw new DMSException("DMS system does not respond to search!");
	}

	/**
	 * Fill task from response.
	 *
	 * @param <T>
	 *            the generic type
	 * @param result
	 *            the result
	 * @param revision
	 *            the revision
	 * @param path
	 *            the path
	 * @param type
	 *            the type
	 * @return the list
	 * @throws JSONException
	 *             the jSON exception
	 * @throws DMSException
	 *             the dMS exception
	 */
	private <T extends AbstractTaskInstance> List<T> updateTaskModelFromResponse(JSONObject result,
			Long revision, Instance path, Class<T> type) throws JSONException, DMSException {
		if (result.has(KEY_ROOT_DATA)) {
			List<T> results = new ArrayList<T>();
			JSONArray nodes = result.getJSONArray(KEY_ROOT_DATA);
			JSONObject cachedWorkflowInstance = null;
			for (int i = 0; i < nodes.length(); i++) {
				JSONObject task = (JSONObject) nodes.get(i);
				T instance = ReflectionUtils.newInstance(type);

				instance.setRevision(revision);
				instance.setTaskInstanceId(task.getString(KEY_ID));
				// set the dmsId
				if (task.has(KEY_NODEREF)) {
					instance.setDmsId(task.getString(KEY_NODEREF));
				}
				if ((type == StandaloneTaskInstance.class)
						&& (path instanceof AbstractTaskInstance)) {
					instance.setIdentifier(((AbstractTaskInstance) path).getIdentifier());
				} else if (task.has(KEY_DEFINITION_ID)) {
					instance.setIdentifier(task.getString(KEY_DEFINITION_ID));
				} else {
					// for backward compatibility or just in case
					instance.setIdentifier(task.getString(KEY_TITLE));
				}
				instance.setState(TaskState.valueOf(task.getString(TaskProperties.SEARCH_STATE)));
				if (task.has(KEY_WORKFLOW_INSTANCE)) {
					cachedWorkflowInstance = task.getJSONObject(KEY_WORKFLOW_INSTANCE);
				}

				if ((cachedWorkflowInstance != null) && type.equals(TaskInstance.class)
						&& ((path == null) || (path instanceof WorkflowInstanceContext))) {
					updateWorkflowModel((WorkflowInstanceContext) path, cachedWorkflowInstance,
							(TaskInstance) instance);
				}
				instance.setEditable(task.getBoolean("isEditable"));
				instance.setReassignable(task.getBoolean("isReassignable"));

				if ((path != null) && (revision != null)) {
					JSONObject properties = task.getJSONObject(KEY_PROPERTIES);

					Map<String, Serializable> responseProperties = taskConvertor
							.convertDMSToCMFProperties(properties, revision, instance,
									DMSTypeConverter.WORKFLOW_TASK_LEVEL);

					DefinitionModel model = dictionaryService.getInstanceDefinition(instance);
					Map<String, Serializable> defaultProperties = PropertiesUtil
							.evaluateDefaultPropertiesForModel(model, instance, manager);
					instance.setProperties(defaultProperties);
					PropertiesUtil.mergeProperties(responseProperties, instance.getProperties(),
							false);

					setType(task, instance, TaskProperties.TYPE);
					if (type.equals(TaskInstance.class)
							&& (path instanceof WorkflowInstanceContext)) {
						((TaskInstance) instance).setContext((WorkflowInstanceContext) path);
					}

					// here property is already cmf
					// TODO this should be expression
					if (!instance.getProperties().containsKey(TaskProperties.DESCRIPTION)) {
						CodeValue codeValue = codelistService.getCodeValues(taskDefinitionsCL).get(
								instance.getIdentifier());
						if (codeValue != null) {
							instance.getProperties().put(
									TaskProperties.DESCRIPTION,
									codeValue.getProperties().get(
											CodelistPropertiesConstants.COMMENT));
						}
					}

				} else {
					instance.setProperties(AlfrescoUtils.jsonObjectToMap(task
							.getJSONObject(KEY_PROPERTIES)));
					// DefinitionModel instanceDefinition =
					// dictionaryService.getInstanceDefinition((Instance) task);
					// List<PropertyDefinition> fields =
					// instanceDefinition.getFields();
					// for (PropertyDefinition propertyDefinition : fields) {
					// }

					// here property is dms, so convert it
					// for optimization do manual mapping -> if property is
					// changed may lead to bug.
					setType(task, instance, "cmfwf_" + TaskProperties.TYPE);
					// TODO here is may be not needed, as description in preview
					// page
					instance.getProperties().put(
							"cmfwf_" + TaskProperties.DESCRIPTION,
							codelistService.getDescription(taskDefinitionsCL,
									instance.getIdentifier()));
				}

				results.add(instance);
			}
			return results;
		}
		return Collections.emptyList();
	}

	/**
	 * Fetch task model from response by only retrieving the task and wf ids.
	 *
	 * @param result
	 *            the result from dms request
	 * @param revision
	 *            the revision for model
	 * @param path
	 *            the path for model
	 * @return the list of pairs <taskId,wfId>
	 * @throws Exception
	 *             the exception on any error
	 */
	private List<Pair<String, String>> fetchTaskModelFromResponse(JSONObject result, Long revision,
			Instance path) throws Exception {
		if (result.has(KEY_ROOT_DATA)) {
			JSONArray nodes = null;
			Object rootData = result.get(KEY_ROOT_DATA);
			List<Pair<String, String>> results = null;
			if (rootData instanceof JSONArray) {
				nodes = (JSONArray) rootData;// result.getJSONArray(KEY_ROOT_DATA);
				JSONObject cachedWorkflowInstance = null;
				results = new ArrayList<Pair<String, String>>(nodes.length());
				for (int i = 0; i < nodes.length(); i++) {
					JSONObject task = nodes.getJSONObject(i);

					Pair<String, String> taskWfPair = new Pair<String, String>(
							task.getString(KEY_ID), null);

					if (task.has(KEY_WORKFLOW_INSTANCE)) {
						cachedWorkflowInstance = task.getJSONObject(KEY_WORKFLOW_INSTANCE);
						taskWfPair.setSecond(cachedWorkflowInstance.getString(KEY_ID));
					}

					// XXX now this is automatic action and should not be null
					// (status set)
					results.add(taskWfPair);
				}
				return results;
			} else if (rootData instanceof JSONObject) {

				nodes = ((JSONObject) rootData).getJSONArray(KEY_DATA_ITEMS);
				results = new ArrayList<Pair<String, String>>(nodes.length());
				for (int i = 0; i < nodes.length(); i++) {
					JSONObject task = nodes.getJSONObject(i);
					String taskId = task.getString(KEY_ID);
					String workflowId = null;
					if (task.has(KEY_WORKFLOW_INSTANCE)) {
						workflowId = task.getString(KEY_WORKFLOW_INSTANCE);
					}
					results.add(new Pair<String, String>(taskId, workflowId));
				}
			}
			return results;
		}
		return Collections.emptyList();
	}

	/**
	 * Internal set properties.
	 *
	 * @param task
	 *            is the the current task model
	 * @param instance
	 *            is the current task instnace
	 * @param key
	 *            is the key value for task type
	 * @throws JSONException
	 *             on error
	 */
	private void setType(JSONObject task, AbstractTaskInstance instance, String key)
			throws JSONException {
		// dont overwrite
		if ((instance.getProperties().get(key) != null)
				&& (instance instanceof StandaloneTaskInstance)) {
			return;
		}
		if (instance.getIdentifier() != null) {
			instance.getProperties().put(key, instance.getIdentifier());
		} else if (task.has(KEY_DEFINITION_ID)) {
			instance.getProperties().put(key, task.getString(KEY_DEFINITION_ID));
		} else {
			instance.getProperties().put(key, task.getString(KEY_TITLE));
		}
	}

	/**
	 * Update workflow model.
	 *
	 * @param path
	 *            the path
	 * @param workflowInstance
	 *            the workflow instance
	 * @param instance
	 *            the instance
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void updateWorkflowModel(WorkflowInstanceContext path, JSONObject workflowInstance,
			TaskInstance instance) throws JSONException {

		instance.setWorkflowInstanceId(workflowInstance.getString(KEY_ID));
		instance.setWorkflowDefinitionId(workflowInstance.getString(KEY_NAME));

		if (workflowInstance.has(KEY_PROPERTIES)) {
			JSONObject props = workflowInstance.getJSONObject(KEY_PROPERTIES);
			if (props.has(WorkflowProperties.REVISION)) {
				Long revision = props.getLong(WorkflowProperties.REVISION);
				instance.setRevision(revision);
			}
			if (path != null) {
				if (props.has(WorkflowProperties.DESCRIPTION)) {
					String description = props.getString(WorkflowProperties.DESCRIPTION);
					path.getProperties().put(WorkflowProperties.DESCRIPTION, description);
				}
				if (workflowInstance.has("isActive")) {
					Boolean active = workflowInstance.getBoolean("isActive");
					path.setActive(Boolean.valueOf(active));
				}
			}
			// fetch any other workflow properties that we does not
			// have
		}
	}

	/**
	 * Gets the process diagram.
	 *
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the process diagram
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public BufferedImage getProcessDiagram(String workflowInstance) throws DMSException {

		if (workflowInstance == null) {
			throw new DMSException("Invalid data is provided for visualizing workflow!");
		}
		HttpMethod createMethod = null;
		try {
			String baseURI = ServiceURIRegistry.WORKFLOW_DIAGRAM.replace(
					WORKFLOW_INSTANCE_ID_TEMPLATE, workflowInstance);
			createMethod = restClient.createMethod(new GetMethod(), "", true);
			if (debugEnabled) {
				debug("Process diagram request: ", baseURI);
			}
			return ImageIO.read(restClient.request(createMethod, baseURI));
		} catch (DMSClientException e) {
			throw new DMSException("Process diagram could not be retrieved due to dms error", e);
		} catch (Exception e) {
			throw new DMSException("Process diagram could not be retrieved!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService# searchWorkflowTasks
	 * (com.sirma.itt.cmf.beans.model.WorkflowInstanceContext,
	 * com.sirma.itt.cmf.beans.SearchArguments)
	 */
	/**
	 * Search workflow tasks.
	 *
	 * @param workflowContext
	 *            the workflow context
	 * @param args
	 *            the args
	 * @return the search arguments
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public SearchArguments<TaskInstance> searchWorkflowTasks(
			WorkflowInstanceContext workflowContext, SearchArguments<TaskInstance> args)
			throws DMSException {
		if ((workflowContext == null) || (workflowContext.getWorkflowInstanceId() == null)) {
			throw new DMSException("Invalid data is provided for searching workflow tasks!");
		}
		// BB: removed WF db search with more accurate Lucene
		// String baseURI = ServiceURIRegistry.CMF_TASK_FOR_WF_SEARCH_SERVICE.replace(
		// WORKFLOW_INSTANCE_ID_TEMPLATE, workflowContext.getWorkflowInstanceId());
		// return searchDBTasksInternal(baseURI, args, workflowContext);
		return searchTasksInternal(ServiceURIRegistry.CMF_TASK_SEARCH_SERVICE, args,
				workflowContext, TaskInstance.class);

	}

	/**
	 * Append by value type to the final query.
	 *
	 * @param taskProps
	 *            the task props
	 * @param wfProps
	 *            the wf props
	 * @param finalQuery
	 *            the builder for query
	 * @param query
	 *            the query
	 * @param mode
	 *            is the query mode to process
	 * @return true on appended new property
	 */
	@SuppressWarnings("unchecked")
	private boolean appendByValueType(Map<String, String> taskProps, Map<String, String> wfProps,
			StringBuffer finalQuery, Query query, QueryMode mode) {

		// iterate against search type each argument
		// the modes from QueryMode are supported
		if (mode == QueryMode.DB_SEARCH) {
			String key = query.getKey();
			Serializable value = query.getValue();

			// process specific
			if (TaskProperties.SEARCH_ASSIGNEE.equals(key)) {
				value = query.getValue().toString();
			} else if (TaskProperties.SEARCH_STATE.equals(key)) {

			} else if (TaskProperties.SEARCH_DUE_AFTER.equals(query.getKey())) {
				if (query.getValue() instanceof Date) {
					value = taskConvertor.convertCMFtoDMSProperty(TaskProperties.PLANNED_END_DATE,
							value, DMSTypeConverter.ALLOW_ALL).getSecond();
				}
			} else if (TaskProperties.SEARCH_DUE_BEFORE.equals(query.getKey())) {
				if (query.getValue() instanceof Date) {
					// convert to due date as closet type
					value = taskConvertor.convertCMFtoDMSProperty(TaskProperties.PLANNED_END_DATE,
							value, DMSTypeConverter.ALLOW_ALL).getSecond();
				}
				// TODO
			} else if (TaskProperties.SEARCH_PRIORITY.equals(query.getKey())) {
			} else if ("maxItems".equals(key) || "skipCount".equals(key)) {
				// TODO
			} else {
				// // check if it is wf prop first
				// if (isWorkflowProperty(query, wfProps)) {
				// return false;
				// }
				Pair<String, Serializable> convertedProperty = taskConvertor
						.convertCMFtoDMSProperty(query.getKey(), query.getValue(),
								DMSTypeConverter.ALLOW_WITH_PREFIX);
				if (convertedProperty == null) {
					return false;
				}
				key = convertedProperty.getFirst();
				value = convertedProperty.getSecond();
				if ((key == null) || (value == null) || value.toString().isEmpty()) {
					return false;
				}
				taskProps.put(key, value.toString());
				return true;
			}
			if ((key == null) || (value == null) || value.toString().isEmpty()) {
				return false;
			}
			if (finalQuery.length() > 0) {
				finalQuery.append("&");
			}
			finalQuery.append(key + "=" + value);
			return true;
		} else if (mode == QueryMode.LUCENE_SEARCH) {
			String key = null;
			Serializable value = null;
			Pair<String, Serializable> convertedProperty = taskConvertor.convertCMFtoDMSProperty(
					query.getKey(), query.getValue(), DMSTypeConverter.PROPERTIES_MAPPING);
			if (convertedProperty == null) {
				return false;
			}
			key = convertedProperty.getFirst();
			value = convertedProperty.getSecond();
			if ((key == null) || (value == null) || value.toString().isEmpty()) {
				return false;
			}
			StringBuilder queryBuilder = new StringBuilder();

			if (query.getBoost() == QueryBoost.EXCLUDE) {
				queryBuilder.append(AlfrescoUtils.SEARCH_START).append(query.getBoost());
			} else {

				if (finalQuery.toString().trim().length() == 0) {
					queryBuilder.append(AlfrescoUtils.SEARCH_START);
				} else {
					int lastIndexOf = finalQuery.lastIndexOf("(");
					if (lastIndexOf > 0) {
						String substring = finalQuery.substring(lastIndexOf + 1);
						if (substring.trim().length() > 0) {
							queryBuilder.append(query.getBoost().toString());
						}
					}
					queryBuilder.append(AlfrescoUtils.SEARCH_START);
				}
			}

			boolean appended = false;
			if (key.endsWith(KEY_ASPECT) || key.endsWith("TYPE")) {
				Set<String> aspects = new HashSet<String>();
				if (value instanceof String) {
					aspects.add((String) value);
				} else if (value instanceof Collection) {
					aspects.addAll((Collection<? extends String>) value);
				}
				int index = aspects.size();
				if (index > 0) {
					for (String val : aspects) {
						convertedProperty = taskConvertor.convertCMFtoDMSProperty(val, "",
								DMSTypeConverter.PROPERTIES_MAPPING);
						queryBuilder.append(KEY_ASPECT).append(AlfrescoUtils.SEARCH_START_VALUE)
								.append(convertedProperty.getFirst().toString())
								.append(AlfrescoUtils.SEARCH_END_VALUE);
						index--;
						if (index > 0) {
							queryBuilder.append(AlfrescoUtils.SEARCH_OR);
						}
					}
					appended = true;
				}
				// the known dms reference ids
			} else if (key.endsWith("PARENT") || key.equals("cmfwf:contextId")) {
				// get the parent
				String dmsId = null;
				if (value instanceof String) {
					dmsId = value.toString();
				} else if (value instanceof DMSInstance) {
					dmsId = ((DMSInstance) value).getDmsId();
				}
				if (dmsId != null) {
					queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(dmsId)
							.append(AlfrescoUtils.SEARCH_END_VALUE);
					appended = true;
				}
			} else if (query.getKey().endsWith(CommonProperties.PROPERTY_NOTNULL)
					|| query.getKey().endsWith(CommonProperties.PROPERTY_ISNULL)) {
				@SuppressWarnings("rawtypes")
				Collection aspects = null;
				if (value instanceof Collection) {
					aspects = (Collection<?>) value;
				} else {
					aspects = Collections.singletonList(value);
				}
				int index = aspects.size();
				if (index > 0) {
					// queryBuilder.append(AlfrescoUtils.SEARCH_START);
					for (Object val : aspects) {
						convertedProperty = taskConvertor.convertCMFtoDMSProperty(val.toString(),
								"", DMSTypeConverter.PROPERTIES_MAPPING);
						queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE)
								.append(convertedProperty.getFirst().toString())
								.append(AlfrescoUtils.SEARCH_END_VALUE);
						index--;
						if (index > 0) {
							if (query.getBoost() == QueryBoost.EXCLUDE) {
								queryBuilder.append(AlfrescoUtils.SEARCH_AND);
							} else {
								queryBuilder.append(AlfrescoUtils.SEARCH_OR);
							}
						}
					}
					// queryBuilder.append(AlfrescoUtils.SEARCH_END);
					appended = true;
				}
			} else if (query.getValue() instanceof DateRange) {
				if ((((DateRange) query.getValue()).getFirst() != null)
						|| (((DateRange) query.getValue()).getSecond() != null)) {
					Object toAdd = value;
					if (toAdd instanceof DateRange) {
						toAdd = TypeConverterUtil.getConverter().convert(String.class, toAdd);
					}
					queryBuilder.append(key).append(":").append(toAdd);
					appended = true;
				}
			} else if (value instanceof String) {
				if (query.getValue() instanceof Collection) {
					appended = iterateCollection(query, key, query.getValue(), queryBuilder);
				} else {
					queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(value)
							.append(AlfrescoUtils.SEARCH_END_VALUE);
					appended = true;
				}
			} else if (value instanceof Date) {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE)
						.append(AlfrescoUtils.formatDate((Date) value))
						.append(AlfrescoUtils.DATE_FROM_SUFFIX)
						.append(AlfrescoUtils.SEARCH_END_VALUE);
				appended = true;
			} else if (value instanceof Number) {
				queryBuilder.append(key).append(":").append(value);
				appended = true;
			} else if (value instanceof Collection) {
				appended = iterateCollection(query, key, query.getValue(), queryBuilder);
			}
			if (appended) {
				finalQuery.append(queryBuilder).append(AlfrescoUtils.SEARCH_END);
			}
			return appended;
		}
		return false;
	}

	/**
	 * Iterate collection of values and append them with the specified key to the final query.
	 *
	 * @param query
	 *            the query to get values from
	 * @param key
	 *            the updated key
	 * @param value
	 *            the expected collection.
	 * @param queryBuilder
	 *            the final query
	 * @return true, if appended
	 */
	@SuppressWarnings("rawtypes")
	private boolean iterateCollection(Query query, String key, Serializable value,
			StringBuilder queryBuilder) {
		Collection collection = null;
		if (value instanceof Collection) {
			collection = (Collection) value;
		} else {
			collection = Collections.singletonList(value);
		}
		int index = collection.size();
		boolean appended = false;
		if (index > 0) {
			// queryBuilder.append(AlfrescoUtils.SEARCH_START);
			for (Object val : collection) {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(val)
						.append(AlfrescoUtils.SEARCH_END_VALUE);
				index--;
				if (index > 0) {
					if (query.getBoost() == QueryBoost.EXCLUDE) {
						queryBuilder.append(AlfrescoUtils.SEARCH_AND);
					} else {
						queryBuilder.append(AlfrescoUtils.SEARCH_OR);
					}
				}

			}
			// queryBuilder.append(AlfrescoUtils.SEARCH_END);
			appended = true;
		}
		return appended;
	}

	/**
	 * Gets the sorting query attribute, by default modified date is sorting field
	 *
	 * @param args
	 *            the args to use for params
	 * @return the sorting data object
	 * @throws JSONException
	 *             on init error
	 */
	private JSONArray getSorting(SearchArguments<? extends Object> args) throws JSONException {
		JSONArray sorting = null;
		Sorter sorter = args.getSorter();
		if (sorter != null) {
			Map<String, Serializable> sortArgs = new HashMap<String, Serializable>();
			sortArgs.put(sorter.getSortField(), sorter.isAscendingOrder());
			Map<String, Serializable> convertProperties = taskConvertor
					.convertCMFtoDMSPropertiesByValue(sortArgs, DMSTypeConverter.PROPERTIES_MAPPING);
			// add second sorter
			if (!convertProperties.containsKey("cm:modified")) {
				convertProperties.put("cm:modified", false);
			}
			Set<Entry<String, Serializable>> keySet = convertProperties.entrySet();
			sorting = new JSONArray();
			for (Entry<String, Serializable> string : keySet) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(string.getKey(), string.getValue());
				sorting.put(jsonObject);
			}
		}
		return sorting;
	}

	/**
	 * Gets the paging arguments from request as json object.
	 *
	 * @param args
	 *            the args
	 * @return the pagging
	 * @throws JSONException
	 *             the jSON exception
	 */
	private JSONObject getPaging(SearchArguments<? extends Object> args) throws JSONException {
		JSONObject paging = new JSONObject();
		paging.put(KEY_PAGING_TOTAL, args.getTotalItems());
		paging.put(KEY_PAGING_SIZE, args.getPageSize());
		paging.put(KEY_PAGING_SKIP, args.getSkipCount());
		paging.put(KEY_PAGING_MAX, args.getMaxSize());
		return paging;

	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService#
	 * filterTaskProperties(com.sirma.itt.cmf.beans.model.TaskInstance)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> filterTaskProperties(AbstractTaskInstance currentTask) {
		try {
			return taskConvertor.convertDMSToCMFProperties(currentTask.getProperties(),
					currentTask.getRevision(), currentTask, DMSTypeConverter.WORKFLOW_TASK_LEVEL);
		} catch (JSONException e) {
			LOGGER.error("Conversion error!", e);
		}
		return currentTask.getProperties();
	}

	/**
	 * Prints Debug msg.
	 *
	 * @param message
	 *            the message
	 */
	private void debug(Object... message) {
		if (debugEnabled) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string);
			}
			LOGGER.debug(builder.toString());
		}
	}

}
