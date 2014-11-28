package com.sirma.itt.seip.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Executes emf operations using the /services/executor rest service.
 *
 * @author Adrian Mitev
 */
public class OperationExecutor {

	private static final String OPERATION = "operation";
	private static final String DEFINITION_TYPE = "definition";
	private static final String ENTITY_TYPE = "type";
	private static final String CONTENT = "content";

	private final List<JsonObject> operations;

	private final String cookie;

	private final String serverUrl;

	/**
	 * Constructs an instance of the executor.
	 *
	 * @param serverUrl
	 *            url of the application server.
	 * @param cookie
	 *            cookie to use.
	 */
	private OperationExecutor(String serverUrl, String cookie) {
		operations = new ArrayList<>();
		this.serverUrl = serverUrl;
		this.cookie = cookie;
	}

	/**
	 * Builds and instance of the class and returns it.
	 *
	 * @param serverUrl
	 *            url of the application server.
	 * @param cookie
	 *            cookie to use.
	 * @return created class instance.
	 */
	public static OperationExecutor build(String serverUrl, String cookie) {
		return new OperationExecutor(serverUrl, cookie);
	}

	/**
	 * Generates a random uuid for emf.
	 *
	 * @return generated id.
	 */
	public static String generateId() {
		return "emf:" + UUID.randomUUID().toString();
	}

	/**
	 * Adds a create project operation to create a project by given id and definition.
	 *
	 * @param definitionId
	 *            project definition type.
	 * @param properties
	 *            project's properties
	 * @return current executor.
	 */
	public OperationExecutor createProject(String definitionId, Map<String, String> properties) {
		JsonObject json = createInstanceJSon(EntityType.PROJECT, definitionId, properties);

		json.addProperty(OPERATION, EmfOperation.CREATE_PROJECT.getOperation());

		operations.add(json);

		return this;
	}

	/**
	 * Adds a create case operation to create a case by given id and definition.
	 *
	 * @param projectId
	 *            id of the project where the case will be created in.
	 * @param definitionId
	 *            project definition type.
	 * @param properties
	 *            project's properties
	 * @return current executor.
	 */
	public OperationExecutor createCase(String projectId, String definitionId,
			Map<String, String> properties) {
		JsonObject json = createInstanceJSon(EntityType.CASE, definitionId, properties);

		if (projectId != null) {
			json.addProperty("parentId", projectId);
			json.addProperty("parentType", EntityType.PROJECT.getSystemType());
		}

		json.addProperty(OPERATION, EmfOperation.CREATE_CASE.getOperation());

		operations.add(json);

		return this;
	}

	/**
	 * Creates a {@link JsonObject} for an instance and fills it with data.
	 *
	 * @param entityType
	 *            type of the entity the operation is executed for
	 * @param definitionId
	 *            definition to set.
	 * @param properties
	 *            properties to set.
	 * @return constructed json object.
	 */
	private JsonObject createInstanceJSon(EntityType entityType, String definitionId,
			Map<String, String> properties) {
		JsonObject json = new JsonObject();

		json.addProperty(DEFINITION_TYPE, definitionId);
		json.addProperty(ENTITY_TYPE, entityType.getSystemType());

		JsonObject propertiesJson = new JsonObject();

		for (Entry<String, String> property : properties.entrySet()) {
			propertiesJson.addProperty(property.getKey(), property.getValue());
		}

		json.add("properties", propertiesJson);

		return json;
	}

	/**
	 * Executes the added operations.
	 *
	 * @return json object containing the operation result.
	 */
	public JsonObject execute() {
		if (!operations.isEmpty()) {
			JsonObject batch = new JsonObject();
			JsonArray operationsToExecute = new JsonArray();
			for (JsonObject operation : operations) {
				operationsToExecute.add(operation);
			}

			batch.add("operations", operationsToExecute);

			HttpResponse response = Rest.jsonPost(serverUrl + "/service/executor", cookie,
					batch.toString());

			if (response.getStatus() != 200) {
				throw new IllegalStateException("Operation executor failed" + "\n"
						+ response.getText());
			}

			JsonObject responseState = response.getJson().getAsJsonObject("responseState");
			String status = responseState.get("status").getAsString();
			if (!"COMPLETED".equals(status)) {
				throw new IllegalStateException("Operation executor failed: \n"
						+ response.getJson());
			}

			return response.getJson();
		}

		return new JsonObject();
	}

	/**
	 * Contains a list of all available emf operations.
	 *
	 * @author Adrian Mitev
	 */
	public enum EmfOperation {
		CREATE_PROJECT("createProject"), CREATE_CASE("createCase"), CREATE_DOMAIN_OBJECT(
				"createObject"), CREATE_TASK("createTask"), CREATE_WORKFLOW("createWorkflow");

		private String operation;

		/**
		 * Initializes operation argument.
		 *
		 * @param operation
		 *            operation name.
		 */
		private EmfOperation(String operation) {
			this.operation = operation;
		}

		/**
		 * Getter method for operation.
		 *
		 * @return the operation
		 */
		public String getOperation() {
			return operation;
		}
	}

	/**
	 * Adds a create task operation to create a task by given id and definition.
	 *
	 * @param parentId
	 *            id of the parent instance where to store the task. If null, the task is not bound
	 *            within a context.
	 * @param definitionId
	 *            task definition type.
	 * @param properties
	 *            task's properties
	 * @param parentType
	 *            type of parent instance
	 * @return current executor.
	 */
	public OperationExecutor createTask(String parentId, String definitionId,
			Map<String, String> properties, EntityType parentType) {
		JsonObject json = createInstanceJSon(EntityType.STANDALONE_TASK, definitionId, properties);

		if (parentId != null && parentType != null) {
			json.addProperty("parentId", parentId);
			json.addProperty("parentType", parentType.getSystemType());
		}

		json.addProperty(OPERATION, EmfOperation.CREATE_TASK.getOperation());
		operations.add(json);

		return this;
	}

	/**
	 * Adds a create workflow operation to create a workflow by given id and definition.
	 *
	 * @param parentId
	 *            {@link String} parent instance ID
	 * @param parentType
	 *            {@link String} parent instance type
	 * @param definitionId
	 *            {@link String} ID of instance definition
	 * @param workflowProperties
	 *            {@link Map} properties of workflow
	 * @param taskProperties
	 *            {@link Map} properties of task
	 * @return {@link OperationExecutor} executor with operation to be executed
	 */
	public OperationExecutor startWorkflow(String parentId, EntityType parentType,
			String definitionId, Map<String, String> workflowProperties,
			Map<String, String> taskProperties) {
		JsonObject json = new JsonObject();
		json.addProperty(OPERATION, EmfOperation.CREATE_WORKFLOW.getOperation());
		json.addProperty(DEFINITION_TYPE, definitionId);
		json.add("revision", null);
		json.add("id", null);
		json.addProperty(ENTITY_TYPE, EntityType.WORKFLOW.getSystemType());

		if (parentType != null) {
			json.addProperty("parentId", parentId);
			json.addProperty("parentType", parentType.getSystemType());
		}

		JsonObject propertiesJson = new JsonObject();
		for (Entry<String, String> property : workflowProperties.entrySet()) {
			propertiesJson.addProperty(property.getKey(), property.getValue());
		}

		json.add("properties", propertiesJson);

		json.add("taskId", null);
		JsonObject taskPropertiesJson = new JsonObject();
		for (Entry<String, String> property : taskProperties.entrySet()) {
			taskPropertiesJson.addProperty(property.getKey(), property.getValue());
		}
		json.add("taskProperties", taskPropertiesJson);

		operations.add(json);

		return this;
	}

	/**
	 * Clears the operations list.
	 */
	public void clearOperations() {
		operations.clear();
	}

	/**
	 * Adds a create domain object operation to create a domain object by given id and definition.
	 *
	 * @param parentType
	 *            type of the parent where to store the domain object.
	 * @param parentId
	 *            id of the parent where to store the object. If null, the object is not bound
	 *            within a context.
	 * @param definitionId
	 *            domain object definition type.
	 * @param content
	 *            content of the object's idoc.
	 * @param properties
	 *            domain object's properties
	 * @return current executor.
	 */
	public OperationExecutor createDomainObject(EntityType parentType, String parentId,
			String definitionId, String content, Map<String, String> properties) {
		JsonObject json = createInstanceJSon(EntityType.DOMAIN_OBJECT, definitionId, properties);

		json.addProperty(CONTENT, content);

		if (parentType != null) {
			json.addProperty("parentId", parentId);
			json.addProperty("parentType", parentType.getSystemType());

		}

		json.addProperty(OPERATION, EmfOperation.CREATE_DOMAIN_OBJECT.getOperation());
		operations.add(json);

		return this;
	}
}
