package com.sirma.itt.seip.instance.actions;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_PARENT;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_ROLLBACK;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_TARGET;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.DEFINITION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.DMS_ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.IS_USER_OPERATION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.OPERATION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PARENT_ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PARENT_TYPE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PROPERTIES;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.REVISION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.TYPE;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.KryoException;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.exceptions.StaleDataModificationException;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.properties.PropertiesConverter;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * Base operation executor for actions that realize create/save operations. The implementation realizes a instance save
 * using the {@link InstanceService} proxy. The method realizes the rollback operation by backing up the state of the
 * instance before the update.
 *
 * <pre>
 * <code>{
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
 * 			}
 * 		}</code>
 * </pre>
 *
 * @author BBonev
 */
public abstract class BaseInstanceExecutor extends BaseExecutableOperation {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceExecutor.class);

	@Inject
	protected PropertiesConverter propertiesService;

	@Inject
	protected DefinitionService definitionService;

	@Inject
	private InstanceService instanceService;

	@Inject
	private SerializationHelper serializationHelper;

	@Inject
	private InstanceContextService contextService;

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation extracts information for the target and parent instances, target instance properties
	 * and returns them in the form of the produces {@link OperationContext}.
	 */
	@Override
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = new OperationContext();
		context.put(OPERATION, JsonUtil.getStringValue(data, OPERATION));

		String isUserOperation = JsonUtil.getStringValue(data, IS_USER_OPERATION);
		context.put(IS_USER_OPERATION, Boolean.valueOf(isUserOperation));

		InstanceReference target = extractReference(data, ID, TYPE, false);

		context.put(CTX_TARGET, target);
		InstanceReference parentRef = extractReference(data, PARENT_ID, PARENT_TYPE, true);
		if (parentRef != null) {
			context.put(CTX_PARENT, parentRef);
		}
		String definition = JsonUtil.getStringValue(data, DEFINITION);
		context.put(DEFINITION, definition);
		Long revision = JsonUtil.getLongValue(data, REVISION);
		if (revision != null) {
			context.put(REVISION, revision);
		}
		Map<String, String> properties = extractProperties(data, PROPERTIES);
		if (!properties.isEmpty()) {
			context.put(PROPERTIES, (Serializable) properties);
		}
		return context;
	}

	/**
	 * Gets the or creates instance from the given context using the target default properties. The method fuses the
	 * properties into the loaded/created instance. It also creates a restore point from the loaded instance if
	 * possible.
	 *
	 * @param context
	 *            the context
	 * @return the instance
	 */
	protected Instance getOrCreateInstance(OperationContext context) {
		return getOrCreateInstance(context, CTX_TARGET, CTX_PARENT, PROPERTIES);
	}

	/**
	 * Gets the or create instance using the custom keys. The methods loads an existing instance or creates new one
	 * using the data located on the given properties.
	 *
	 * @param context
	 *            the context to get the information from
	 * @param refKey
	 *            the target {@link InstanceReference} to use for loading or creation
	 * @param parentKey
	 *            the reference for the parent instance if any
	 * @param propertiesKey
	 *            the properties key to fetch and set to loaded or created instance. For loaded instance the properties
	 *            will be merged with the existing ones.
	 * @return the loaded or create instance
	 */
	protected Instance getOrCreateInstance(OperationContext context, String refKey, String parentKey,
			String propertiesKey) {

		InstanceReference reference = context.getIfSameType(refKey, InstanceReference.class);
		// load the instance from db if the id is persisted
		Instance instance = null;
		if (reference != null && idManager.isIdPersisted(reference.getId())) {
			instance = reference.toInstance();
			createRestorePoint(context, instance);
		}

		DefinitionModel definitionModel = null;
		if (instance == null && reference != null) {
			definitionModel = getDefinition(context);
			Instance parent = null;
			InstanceReference parentRef = null;
			if (parentKey != null && parentKey.endsWith("_instance")) {
				parent = context.getIfSameType(parentKey, Instance.class);
				if (parent != null) {
					parentRef = parent.toReference();
				}
			} else {
				parentRef = context.getIfSameType(parentKey, InstanceReference.class);
				if (parentRef != null) {
					parent = parentRef.toInstance();
				}
			}
			instance = getInstanceService().createInstance(definitionModel, parent);
			if (instance != null) {
				// if id was passed from external or generated in advance we should use it
				idManager.unregisterId(instance.getId());
				instance.setId(reference.getId());
				// force parent linkage for imported elements
				contextService.bindContext(instance, parentRef);
			}
		} else if (instance != null) {
			String contextDefIdentifier = context.getIfSameType("definition", String.class);
			// this will return the max revision of the definition any way
			definitionModel = definitionService.getInstanceDefinition(instance);
			if (definitionModel != null) {
				instance.setRevision(definitionModel.getRevision());
				Map<String, Serializable> currentInstanceProperties = instance.getProperties();
				if (contextDefIdentifier != null && currentInstanceProperties.containsKey(TYPE)) {
					currentInstanceProperties.put(TYPE, contextDefIdentifier);
					instance.setIdentifier(contextDefIdentifier);
					instance.setProperties(currentInstanceProperties);
				}
			}
		}

		// set instance properties using converter
		Map<String, ?> properties = context.getIfSameType(propertiesKey, Map.class);
		if (properties != null && instance != null && definitionModel != null) {
			Map<String, Serializable> convertFromRest = propertiesService.convertToInternalModel(properties,
					definitionModel);

			PropertiesUtil.mergeProperties(convertFromRest, instance.getProperties(), true);
		}

		return instance;
	}

	/**
	 * Gets the definition for the given context and instance class
	 *
	 * @param context
	 *            the context
	 * @return the definition
	 */
	protected DefinitionModel getDefinition(OperationContext context) {
		String definitionId = context.getIfSameType(DEFINITION, String.class);
		DefinitionModel definitionModel = getLatestDefinition(definitionId);
		if (definitionModel == null) {
			throw new EmfRuntimeException("Could not find definition " + definitionId);
		}
		return definitionModel;
	}

	/**
	 * Gets the latest definition.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @return the latest definition
	 */
	private DefinitionModel getLatestDefinition(String definitionId) {
		return definitionService.find(definitionId);
	}

	/**
	 * Gets the definition class for the given instance class
	 *
	 * @param instanceClass
	 *            the instance class
	 * @return the definition class
	 */
	@SuppressWarnings("rawtypes")
	protected Class getDefinitionClass(Class<?> instanceClass) {
		return instanceClass;
	}

	@Override
	public OperationResponse execute(OperationContext context) {
		Instance instance = getOrCreateInstance(context);
		if (instance == null) {
			LOGGER.error("Error fetching instance with context {} ", context);
		}
		Instance savedInstance = getInstanceService().save(instance, createOperation(context));
		// backup the dms ID so we could delete it from DMS if needed
		if (savedInstance instanceof DmsAware) {
			context.put(DMS_ID, ((DmsAware) savedInstance).getDmsId());
		}
		return new OperationResponse(OperationStatus.COMPLETED, toJson(savedInstance));
	}

	/**
	 * Creates the operation used to pass to the save or other method that require the operation object.
	 *
	 * @param context
	 *            the context
	 * @return the operation
	 */
	protected Operation createOperation(OperationContext context) {
		return new Operation(getOperation());
	}

	/**
	 * Converts an instance to JSONObject for returning an operation result.
	 *
	 * @param instance
	 *            converted instance.
	 * @return JSONObject
	 */
	public JSONObject toJson(Instance instance) {
		return JsonUtil.buildFrom(instance.getId(), instance::get, "id", "title");
	}

	@Override
	public boolean rollback(OperationContext context) {
		Instance instance = getRestorePoint(context);
		if (instance != null) {
			try {
				LOGGER.info("Trying to rollback to older version of the instane with id {}", instance.getId());
				// if we have backup instance we could just save the old version
				// note that if the instance is modified after the backup this will be an outdated
				// instance and the method will throw a StaleDataModificationException in this case
				// we cane ignore the revert request because we have newer data.
				getInstanceService().save(instance, createOperation(context));
			} catch (StaleDataModificationException e) {
				// if this happens we are just late to revert the state
				LOGGER.info("The instance has newer state and the rollback is ignored: {}", e.getMessage());
				LOGGER.trace("Not latest version", e);
			} catch (Exception e) {
				// all other exceptions are considered as errors
				LOGGER.error("Failed to revert instance {} with id {} due to error: {}",
						instance.getClass().getSimpleName(), instance.getId(), e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates the restore point if possible for the given instance.
	 *
	 * @param context
	 *            the context
	 * @param instance
	 *            the instance
	 */
	protected void createRestorePoint(OperationContext context, Instance instance) {
		if (instance != null) {
			try {
				// backup the old state if present
				// clone the instance to separate if from the changes applied bellow
				context.put(CTX_ROLLBACK, serializationHelper.copy(instance));
			} catch (KryoException e) {
				LOGGER.warn("Failed to create a restore point for instance {} with id {} due to {}",
						instance.getClass().getSimpleName(), instance.getId(), e.getMessage());
				LOGGER.debug("Failed to create a restore point due to: ", e);
			}
		}
	}

	/**
	 * Gets the restore point from the given context if present.
	 *
	 * @param data
	 *            the data
	 * @return the restore point
	 */
	protected Instance getRestorePoint(OperationContext data) {
		return data.getIfSameType(CTX_ROLLBACK, Instance.class);
	}

	/**
	 * Getter method for instanceService - factory for correct instance service.
	 * @return the instanceService to use
	 */
	protected InstanceService getInstanceService() {
		return instanceService;
	}

}
