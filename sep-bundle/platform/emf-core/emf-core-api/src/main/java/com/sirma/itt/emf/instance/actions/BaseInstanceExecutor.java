package com.sirma.itt.emf.instance.actions;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_PARENT;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_ROLLBACK;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_TARGET;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.DEFINITION;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.DMS_ID;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.ID;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.PARENT_ID;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.PARENT_TYPE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.PROPERTIES;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.REVISION;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.TYPE;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.KryoException;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.executors.BaseExecutableOperation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Base operation executor for actions that realize create/save operations. The implementation
 * realizes a instance save using the {@link InstanceService} proxy. The method realizes the
 * rollback operation by backing up the state of the instance before the update.
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

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceExecutor.class);

	/** The properties service. */
	@Inject
	protected PropertiesService propertiesService;

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation extracts information for the target and parent instances, target
	 * instance properties and returns them in the form of the produces {@link SchedulerContext}.
	 */
	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		SchedulerContext context = new SchedulerContext();
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
	 * Gets the or creates instance from the given context using the target default properties. The
	 * method fuses the properties into the loaded/created instance. It also creates a restore point
	 * from the loaded instance if possible.
	 * 
	 * @param context
	 *            the context
	 * @return the instance
	 */
	protected Instance getOrCreateInstance(SchedulerContext context) {
		return getOrCreateInstance(context, CTX_TARGET, CTX_PARENT, PROPERTIES);
	}

	/**
	 * Gets the or create instance using the custom keys. The methods loads an existing instance or
	 * creates new one using the data located on the given properties.
	 * 
	 * @param context
	 *            the context to get the information from
	 * @param refKey
	 *            the target {@link InstanceReference} to use for loading or creation
	 * @param parentKey
	 *            the reference for the parent instance if any
	 * @param propertiesKey
	 *            the properties key to fetch and set to loaded or created instance. For loaded
	 *            instance the properties will be merged with the existing ones.
	 * @return the loaded or create instance
	 */
	@SuppressWarnings({ "unchecked", "cast", "rawtypes" })
	protected Instance getOrCreateInstance(SchedulerContext context, String refKey,
			String parentKey, String propertiesKey) {

		InstanceReference reference = context.getIfSameType(refKey, InstanceReference.class);
		// load the instance from db if the id is persisted
		Instance instance = null;
		if ((reference != null) && InstanceUtil.isIdPersisted(reference.getIdentifier())) {
			instance = reference.toInstance();
			createRestorePoint(context, instance);
		}

		DefinitionModel definitionModel;
		if (instance == null) {
			Class javaClass = reference.getReferenceType().getJavaClass();
			definitionModel = getDefinition(context, javaClass);
			Instance parent = null;
			InstanceReference parentRef = null;
			if ((parentKey != null) && parentKey.endsWith("_instance")) {
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
			instance = instanceService.createInstance(definitionModel, parent);
			if (instance != null) {
				// if id was passed from external or generated in advance we should use it
				SequenceEntityGenerator.unregister(instance.getId());
				instance.setId(reference.getIdentifier());
			}
			// force parent linkage for imported elements
			if ((instance instanceof OwnedModel)
					&& (((OwnedModel) instance).getOwningReference() == null) && (parentRef != null)) {
				((OwnedModel) instance).setOwningReference(parentRef);
			}
		} else {
			String contextDefIdentifier = context.getIfSameType("definition", String.class);
			definitionModel = dictionaryService.getInstanceDefinition(instance);
			if (definitionModel != null) {
				// if the caller send different definition id from the one in the instance we should
				// change the definition in the instance
				String currentDefIdentifier = definitionModel.getIdentifier();
				if (contextDefIdentifier != null && !contextDefIdentifier.equalsIgnoreCase(currentDefIdentifier)) {
					instance.setIdentifier(contextDefIdentifier);
					if (instance.getRevision() > 0) {
						// get the max definition revision
						definitionModel = getLatestDefinition(instance.getClass(), contextDefIdentifier);
						if (definitionModel != null) {
							instance.setRevision(definitionModel.getRevision());
						}
					}
					definitionModel = dictionaryService.getInstanceDefinition(instance);
				}
				Map<String, Serializable> currentInstanceProperties = instance.getProperties();
				if (contextDefIdentifier != null && currentInstanceProperties.containsKey(TYPE)) {
					currentInstanceProperties.put(TYPE, contextDefIdentifier);
					instance.setProperties(currentInstanceProperties);
				}
			}
		}

		// set instance properties using converter
		Map<String, ?> properties = context.getIfSameType(propertiesKey, Map.class);
		if ((properties != null) && (instance != null)) {
			Map<String, Serializable> convertFromRest = propertiesService.convertToInternalModel(
					properties, definitionModel);

			PropertiesUtil.mergeProperties(convertFromRest, instance.getProperties(), true);
		}

		return instance;
	}

	/**
	 * Gets the definition for the given context and instance class
	 * 
	 * @param context
	 *            the context
	 * @param javaClass
	 *            the java class
	 * @return the definition
	 */
	@SuppressWarnings("unchecked")
	protected DefinitionModel getDefinition(SchedulerContext context, Class<?> javaClass) {
		DefinitionModel definitionModel;
		String definitionId = context.getIfSameType(DEFINITION, String.class);
		Long revision = context.getIfSameType(REVISION, Long.class);
		if (revision == null) {
			definitionModel = getLatestDefinition(javaClass, definitionId);
		} else {
			definitionModel = dictionaryService.getDefinition(getDefinitionClass(javaClass),
					definitionId, revision);
		}
		return definitionModel;
	}

	/**
	 * Gets the latest definition.
	 *
	 * @param javaClass the java class
	 * @param definitionId the definition id
	 * @return the latest definition
	 */
	@SuppressWarnings("unchecked")
	protected DefinitionModel getLatestDefinition(Class<?> javaClass, String definitionId) {
		return dictionaryService.<DefinitionModel>getDefinition(getDefinitionClass(javaClass),
				definitionId);
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
	public OperationResponse execute(SchedulerContext context) {
		Instance instance = getOrCreateInstance(context);

		Instance savedInstance = instanceService.save(instance, createOperation());
		// backup the dms ID so we could delete it from DMS if needed
		if (savedInstance instanceof DMSInstance) {
			context.put(DMS_ID, ((DMSInstance) savedInstance).getDmsId());
		}
		return new OperationResponse(SchedulerEntryStatus.COMPLETED, toJson(savedInstance));
	}

	/**
	 * Creates the operation used to pass to the save or other method that require the operation
	 * object.
	 * 
	 * @return the operation
	 */
	protected Operation createOperation() {
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
		return JsonUtil.transformInstance(instance, "id", "title");
	}

	@Override
	public boolean rollback(SchedulerContext data) {
		Instance instance = getRestorePoint(data);
		if (instance != null) {
			try {
				LOGGER.info("Trying to rollback to older version of the instane with id {}",
						instance.getId());
				// if we have backup instance we could just save the old version
				// note that if the instance is modified after the backup this will be an outdated
				// instance and the method will throw a StaleDataModificationException in this case
				// we cane ignore the revert request because we have newer data.
				instanceService.save(instance, createOperation());
			} catch (StaleDataModificationException e) {
				// if this happens we are just late to revert the state
				LOGGER.info("The instance has newer state and the rollback is ignored: {}",
						e.getMessage());
				LOGGER.trace("Not latest version", e);
			} catch (Exception e) {
				// all other exceptions are considered as errors
				LOGGER.error("Failed to revert instance {} with id {} due to error: {}", instance
						.getClass().getSimpleName(), instance.getId(), e.getMessage(), e);
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
	protected void createRestorePoint(SchedulerContext context, Instance instance) {
		if (instance != null) {
			try {
				// backup the old state if present
				// clone the instance to separate if from the changes applied bellow
				context.put(CTX_ROLLBACK, SerializationUtil.copy(instance));
			} catch (KryoException e) {
				LOGGER.warn(
						"Failed to create a restore point for instance {} with id {} due to {}",
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
	protected Instance getRestorePoint(SchedulerContext data) {
		return data.getIfSameType(CTX_ROLLBACK, Instance.class);
	}

}
