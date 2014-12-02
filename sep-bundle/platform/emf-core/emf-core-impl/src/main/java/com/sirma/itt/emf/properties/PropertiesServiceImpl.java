/*
 *
 */
package com.sirma.itt.emf.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.dao.PropertiesDao;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.resources.EmfResourcesUtil;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Implementation of the public service for persisting and retrieving properties.
 *
 * @author BBonev
 */
@ApplicationScoped
public class PropertiesServiceImpl implements PropertiesService {

	/** The default instance callback. */
	@Inject
	@InstanceType(type = ObjectTypes.DEFAULT)
	private PropertyModelCallback<PropertyModel> defaultInstanceCallback;

	/** The properties dao. */
	@Inject
	private PropertiesDao propertiesDao;

	/** The callbacks. */
	@Inject
	@Any
	private javax.enterprise.inject.Instance<PropertyModelCallback<PropertyModel>> callbacks;

	/** The callback mapping. */
	private Map<Class<?>, PropertyModelCallback<PropertyModel>> callbackMapping;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesService.class);
	/** The trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private javax.enterprise.inject.Instance<InstanceDao<CommonInstance>> instanceDao;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;
	/** The resource service. */
	@Inject
	private Instance<ResourceService> resourceService;

	/** The inbound converter. */
	private final ModelConverter inboundConverter = new InputModelConverter();

	/** The outbound converter. */
	private final ModelConverter outboundConverter = new OutputModelConverter();

	/**
	 * Initialize some properties.
	 */
	@PostConstruct
	public void init() {
		callbackMapping = CollectionUtils.createHashMap(30);

		for (PropertyModelCallback<PropertyModel> modelCallback : callbacks) {
			Set<Class<?>> supportedObjects = modelCallback.getSupportedObjects();
			for (Class<?> supportedObject : supportedObjects) {
				if (callbackMapping.containsKey(supportedObject)) {
					throw new EmfConfigurationException("Ambiguous property model callback: "
							+ supportedObject + " already defined by "
							+ callbackMapping.get(supportedObject).getClass());
				}
				if (TRACE) {
					LOGGER.trace("Registering {} to {}", supportedObject, modelCallback.getClass());
				}
				callbackMapping.put(supportedObject, modelCallback);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> boolean isModelSupported(E instance) {
		try {
			PropertyModelCallback<E> callback = getCallback(instance, null, true, false);
			return callback != null;
		} catch (RuntimeException e) {
			// nothing to do here
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getEntityProperties(Entity entity, Long revision,
			PathElement path) {
		if (TRACE) {
			LOGGER.trace("Loading properties for {} : {}", entity.getClass().getSimpleName(),
					entity.getId());
		}
		return propertiesDao.getEntityProperties(entity, revision, path,
				getCallback(null, entity, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperties(Entity entity, Long revision, PathElement path) {
		if (TRACE) {
			LOGGER.trace("Deleting properties for {} : {}", entity.getClass().getSimpleName(),
					entity.getId());
		}
		propertiesDao.removeProperties(entity, revision, path,
				getCallback(null, entity, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProperties(Entity entity, Long revision, PathElement path,
			Map<String, Serializable> properties) {
		saveProperties(entity, revision, path, properties, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProperties(Entity entity, Long revision, PathElement path,
			Map<String, Serializable> properties, boolean addOnly) {
		if (TRACE) {
			LOGGER.trace("Saving properties with mode={} for {} : ",
					(addOnly ? "MERGE" : "REPLACE"), entity.getClass().getSimpleName(),
					entity.getId());
		}

		propertiesDao.saveProperties(entity, revision, path, properties, addOnly,
				getCallback(null, entity, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void saveProperties(E instance) {
		saveProperties(instance, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void saveProperties(E instance, boolean addOnly) {
		saveProperties(instance, addOnly, false);
	}

	/**
	 * Save properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 * @param saveFullGraph
	 *            the save full graph
	 */
	@Override
	public <E extends PropertyModel> void saveProperties(E instance, boolean addOnly,
			boolean saveFullGraph) {
		if (TRACE) {
			LOGGER.trace("Saving properties with mode={}{} for {}",
					(addOnly ? "MERGE" : "REPLACE"), (saveFullGraph ? "_FULL" : "_BASE"), instance
							.getClass().getSimpleName());
		}
		prepareForPropertiesPersist(instance.getProperties());

		propertiesDao.saveProperties(instance, addOnly,
				getCallback(instance, null, saveFullGraph, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void loadProperties(E instance) {
		propertiesDao.loadProperties(instance, getCallback(instance, null, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void loadProperties(E instance, boolean loadAll) {
		propertiesDao.loadProperties(instance, getCallback(instance, null, loadAll, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void loadProperties(List<E> instances) {
		loadPropertiesInternal(instances, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends PropertyModel> void loadProperties(List<E> instances, boolean loadAll) {
		loadPropertiesInternal(instances, loadAll);
	}

	/**
	 * Load properties internal.
	 * 
	 * @param <E>
	 *            the element type
	 * @param instances
	 *            the instances
	 * @param loadAll
	 *            the load all
	 */
	private <E extends PropertyModel> void loadPropertiesInternal(List<E> instances, boolean loadAll) {
		if (instances.isEmpty()) {
			return;
		}
		if (TRACE) {
			Set<Serializable> list = new LinkedHashSet<Serializable>(
					(int) (instances.size() * 1.2), 0.9f);
			for (E caseInstance : instances) {
				list.add(((Entity<?>) caseInstance).getId());
			}
			LOGGER.trace("Loading properties for " + instances.get(0).getClass().getSimpleName()
					+ " (" + list.size() + "): " + list);
		}
		propertiesDao.loadProperties(instances, getCallback(instances.get(0), null, loadAll, true));
		if (TRACE) {
			LOGGER.trace("Loaded properties for instances: " + instances);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, ?> convertToExternalModel(PropertyModel model,
			DefinitionModel definitionModel) {
		if ((model == null) || (model.getProperties() == null) || model.getProperties().isEmpty()) {
			return new HashMap<>(1);
		}
		if (definitionModel == null) {
			return new HashMap<>(model.getProperties());
		}
		Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(model
				.getProperties().size() << 1);

		if (definitionModel instanceof RegionDefinitionModel) {
			iterateModel((RegionDefinitionModel) definitionModel,
					escapeStringProperties(model.getProperties()), properties, outboundConverter);
		} else {
			iterateModel(definitionModel, escapeStringProperties(model.getProperties()),
					properties, outboundConverter);
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Serializable> convertToInternalModel(Map<String, ?> source,
			DefinitionModel definitionModel) {
		return convertToInternalModelInternal(source, definitionModel);
	}

	/**
	 * Convert to internal model internal.
	 * 
	 * @param source
	 *            the source
	 * @param definitionModel
	 *            the definition model
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Serializable> convertToInternalModelInternal(Map<String, ?> source,
			DefinitionModel definitionModel) {
		if ((source == null) || source.isEmpty()) {
			return new LinkedHashMap<>();
		}
		if (definitionModel == null) {
			return (Map<String, Serializable>) source;
		}
		Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(source.size());

		if (definitionModel instanceof RegionDefinitionModel) {
			iterateModel((RegionDefinitionModel) definitionModel, source, properties,
					inboundConverter);
		} else {
			iterateModel(definitionModel, source, properties, inboundConverter);
		}

		return properties;
	}

	/**
	 * Iterate model.
	 *
	 * @param <A>
	 *            the generic type
	 * @param <B>
	 *            the generic type
	 * @param model
	 *            the model
	 * @param source
	 *            the source
	 * @param output
	 *            the output
	 * @param converter
	 *            the converter
	 */
	@SuppressWarnings("unchecked")
	private <A, B> void iterateModel(DefinitionModel model, Map<String, A> source,
			Map<String, B> output, ModelConverter converter) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			if (converter.isAllowedForConvert(propertyDefinition)) {
				A sourceValue = converter.getValue(propertyDefinition, source);
				if (sourceValue == null) {
					// nothing to do with the missing values
					continue;
				}

				Object converted;
				if (sourceValue instanceof Collection) {
					converted = iterateCollection(converter, propertyDefinition, sourceValue);
				} else {
					converted = converter.convert(propertyDefinition, sourceValue);
				}
				// we does not handle null values
				// we set the internal name and ignore the source name
				output.put(propertyDefinition.getName(), (B) converted);
			}
		}
	}

	/**
	 * Iterate collection.
	 * 
	 * @param <A>
	 *            the generic type
	 * @param <B>
	 *            the generic type
	 * @param converter
	 *            the converter
	 * @param propertyDefinition
	 *            the property definition
	 * @param sourceValue
	 *            the source value
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	private <A, B> Object iterateCollection(ModelConverter converter,
			PropertyDefinition propertyDefinition, A sourceValue) {
		List<B> convertedValues = new ArrayList<>(((Collection<Object>) sourceValue).size());
		for (Object object : (Collection<Object>) sourceValue) {
			Object temp = converter.convert(propertyDefinition, object);
			// we does not handle null values
			if (temp != null) {
				convertedValues.add((B) temp);
			}
		}
		return convertedValues;
	}

	/**
	 * Iterate model.
	 * 
	 * @param <A>
	 *            the generic type
	 * @param <B>
	 *            the generic type
	 * @param model
	 *            the model
	 * @param source
	 *            the source
	 * @param output
	 *            the output
	 * @param converter
	 *            the converter
	 */
	private <A, B> void iterateModel(RegionDefinitionModel model, Map<String, A> source,
			Map<String, B> output, ModelConverter converter) {
		iterateModel((DefinitionModel) model, source, output, converter);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			iterateModel(regionDefinition, source, output, converter);
		}
	}

	/**
	 * Escapes document properties like title and createdBy names, so they cause js errors.
	 *
	 * @param properties
	 *            Properties that may need escaping.
	 * @return the map
	 */
	private Map<String, Serializable> escapeStringProperties(Map<String, Serializable> properties) {
		return properties;
	}

	/**
	 * Gets the callback for the given instance.
	 *
	 * @param <E>
	 *            the instance type
	 * @param instance
	 *            the instance to get the callback for (can be <code>null</code> but then the entity
	 *            should be provided)
	 * @param entity
	 *            the entity to get the callback for it no instance is present (optional if instance
	 *            is present)
	 * @param loadAll
	 *            the load all properties
	 * @param fail
	 *            if the method should fail if no callback is found
	 * @return the callback
	 */
	@SuppressWarnings("unchecked")
	protected <E extends PropertyModel> PropertyModelCallback<E> getCallback(E instance,
			Entity entity, boolean loadAll, boolean fail) {
		Object target = instance;
		if (target == null) {
			target = entity;
		}
		if (!loadAll && defaultInstanceCallback.canHandle(target)) {
			return (PropertyModelCallback<E>) defaultInstanceCallback;
		}
		Class<?> targetClass = target.getClass();
		PropertyModelCallback<PropertyModel> callback = callbackMapping.get(targetClass);
		if (callback == null) {
			callback = findCompatibleCallback(target, targetClass);
		}
		if (callback == null) {
			if (fail) {
				throw new EmfRuntimeException("The entity of type " + targetClass
						+ " is not supported for properties saving!");
			}
			return null;
		}
		return (PropertyModelCallback<E>) callback;
	}

	/**
	 * Find compatible callback.
	 * 
	 * @param target
	 *            the target
	 * @param targetClass
	 *            the target class
	 * @return the property model callback
	 */
	private PropertyModelCallback<PropertyModel> findCompatibleCallback(Object target,
			Class<?> targetClass) {
		if (TRACE) {
			LOGGER.trace("No callback registered for {}. Will try to lookup one.", targetClass);
		}
		PropertyModelCallback<PropertyModel> localCallback = null;
		for (PropertyModelCallback<PropertyModel> modelCallback : callbacks) {
			if (modelCallback.canHandle(target)) {
				// update the mapping model with the new discovery
				callbackMapping.put(targetClass, modelCallback);
				if (TRACE) {
					LOGGER.trace("Registering {} to {}", targetClass, modelCallback.getClass());
				}
				localCallback = modelCallback;
				break;
			}
		}
		return localCallback;
	}

	/**
	 * Checks for no persisted custom objects of type {@link CommonInstance}. If not already saved
	 * we will save them
	 *
	 * @param properties
	 *            the properties
	 */
	protected void prepareForPropertiesPersist(Map<String, Serializable> properties) {
		if ((properties == null) || properties.isEmpty()) {
			return;
		}
		for (Serializable serializable : properties.values()) {
			if (serializable instanceof CommonInstance) {
				saveCommonInstanceIfNeeded((CommonInstance) serializable);
			} else if (serializable instanceof Collection) {
				for (Object object : (Collection<?>) serializable) {
					if (object instanceof CommonInstance) {
						saveCommonInstanceIfNeeded((CommonInstance) object);
					} else {
						// no need to iterate more if the objects are not common instance. We does
						// not support non heterogeneous collections
						break;
					}
				}
			}
		}
	}

	/**
	 * Saves the given common instance if needed.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void saveCommonInstanceIfNeeded(CommonInstance instance) {
		if (instance == null) {
			return;
		}
		if (!SequenceEntityGenerator.isPersisted(instance)) {
			instanceDao.get().saveEntity(instance);
		}
		// we check for more properties on the go
		prepareForPropertiesPersist(instance.getProperties());
	}

	/**
	 * Interface that defines a means to convert a definition model properties to other format using
	 * the provided definition.
	 *
	 * @author BBonev
	 */
	interface ModelConverter {

		/**
		 * Checks if is allowed for convert.
		 *
		 * @param property
		 *            the property
		 * @return true, if is allowed for convert
		 */
		boolean isAllowedForConvert(PropertyDefinition property);

		/**
		 * Convert.
		 *
		 * @param property
		 *            the property
		 * @param source
		 *            the source
		 * @return the serializable
		 */
		Object convert(PropertyDefinition property, Object source);

		/**
		 * Gets the value.
		 *
		 * @param <A>
		 *            the generic type
		 * @param definition
		 *            the definition
		 * @param source
		 *            the source
		 * @return the value
		 */
		<A> A getValue(PropertyDefinition definition, Map<String, A> source);
	}

	/**
	 * Converter implementation for the input properties.
	 *
	 * @author BBonev
	 */
	class InputModelConverter implements ModelConverter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isAllowedForConvert(PropertyDefinition property) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object convert(PropertyDefinition property, Object source) {
			if (DataTypeDefinition.DATE.equals(property.getType())
					|| DataTypeDefinition.DATETIME.equals(property.getType())) {
				return typeConverter.convert(Date.class, source);
			} else if (property.getControlDefinition() != null) {
				ControlDefinition control = property.getControlDefinition();
				if ("USER".equals(control.getIdentifier())
						|| "PICKLIST".equals(control.getIdentifier())) {
					return convertUserControl(source);
				} else if ("INSTANCE".equals(control.getIdentifier())) {
					return convertInstanceControl(source, control);
				}
			} else if ((source instanceof String) && StringUtils.isBlank((String) source)) {
				// when converting data from REST to internal model when we have only white space in
				// the field we mark it as null and ignore the value at all.
				return null;
			}
			try {
				return typeConverter.convert(property.getDataType(), source);
			} catch (TypeConversionException e) {
				LOGGER.debug("Could not convert property {}={} to {}", property.getName(), source,
						property.getDataType().getJavaClassName());
				LOGGER.trace("And the exception is: ", e);
			}
			return source;
		}

		/**
		 * Convert user control.
		 * 
		 * @param source
		 *            the source
		 * @return the object
		 */
		private Object convertUserControl(Object source) {
			JSONObject object;
			String name;
			if ((source instanceof String) && source.toString().startsWith("{")) {
				object = JsonUtil.createObjectFromString(source.toString());
				if (object == null) {
					return source;
				}
				name = JsonUtil.getStringValue(object, ResourceProperties.USER_ID);
			} else if (source instanceof Map) {
				name = (String) ((Map<?, ?>) source).get(ResourceProperties.USER_ID);
			} else {
				return source;
			}

			Resource resource = resourceService.get().getResource(name, ResourceType.USER);
			if (resource != null) {
				return resource.getIdentifier();
			}
			// invalid user provided
			return null;
		}

		/**
		 * Convert instance control.
		 * 
		 * @param source
		 *            the source
		 * @param control
		 *            the control
		 * @return the object
		 */
		private Object convertInstanceControl(Object source, ControlDefinition control) {
			JSONObject object = null;
			Map<String, Object> map = null;
			if (source instanceof String) {
				object = JsonUtil.createObjectFromString(source.toString());
				if (object == null) {
					return source;
				}
			} else if (source instanceof Map) {
				map = (Map<String, Object>) source;
			}
			if (((map != null) && map.isEmpty()) || ((object != null) && (object.length() == 0))) {
				// no information
				return null;
			}
			CommonInstance instance = new CommonInstance();
			instance.setId(getProperty(String.class, object, map, "id"));
			instance.setIdentifier(getProperty(String.class, object, map, "identifier"));
			instance.setPath(getProperty(String.class, object, map, "path"));
			instance.setRevision(getProperty(Long.class, object, map, "revision"));
			instance.setVersion(getProperty(Long.class, object, map, "version"));

			Object properties = getProperty(Object.class, object, map, "properties");
			if (properties instanceof JSONObject) {
				// convert properties
				JSONObject propertiesObject = (JSONObject) properties;
				Map<String, String> subProperties = CollectionUtils
						.createLinkedHashMap(propertiesObject.length());
				if (propertiesObject.length() > 0) {
					Iterator<?> localIterator = propertiesObject.keys();
					while (localIterator.hasNext()) {
						String key = localIterator.next().toString();
						String value = JsonUtil.getStringValue(propertiesObject, key);
						subProperties.put(key, value);
					}
				}

				Map<String, Serializable> map2 = convertToInternalModelInternal(subProperties,
						control);
				instance.setProperties(map2);
			} else if (properties instanceof Map) {
				Map<String, Serializable> map2 = convertToInternalModelInternal(
						(Map<String, ?>) properties, control);
				instance.setProperties(map2);
			} else {
				instance.setProperties(new LinkedHashMap<String, Serializable>());
			}
			return instance;
		}

		/**
		 * Gets the property.
		 *
		 * @param <T>
		 *            the generic type
		 * @param result
		 *            the result
		 * @param object
		 *            the object
		 * @param map
		 *            the map
		 * @param key
		 *            the key
		 * @return the property
		 */
		private <T> T getProperty(Class<T> result, JSONObject object, Map<String, Object> map,
				String key) {
			if (map != null) {
				return typeConverter.convert(result, map.get(key));
			}
			return typeConverter.convert(result, JsonUtil.getValueOrNull(object, key));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A> A getValue(PropertyDefinition definition, Map<String, A> source) {
			A sourceValue = source.get(definition.getName());
			if (sourceValue == null) {
				// first we try to map it against the URI
				if (!DefaultProperties.NOT_USED_PROPERTY_VALUE.equals(definition.getUri())) {
					sourceValue = source.get(definition.getUri());
				}
				// if not found try for DMS id
				if (sourceValue == null) {
					String dmsType = definition.getDmsType();
					if (!DefaultProperties.NOT_USED_PROPERTY_VALUE.equals(dmsType)) {
						if (dmsType.startsWith("-")) {
							dmsType = dmsType.substring(1);
						}
						sourceValue = source.get(dmsType);
					}
				}
			}
			return sourceValue;
		}
	}

	/**
	 * Model converter implementation for the output properties.
	 *
	 * @author BBonev
	 */
	class OutputModelConverter implements ModelConverter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isAllowedForConvert(PropertyDefinition property) {
			return property.getDisplayType() != DisplayType.SYSTEM;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object convert(PropertyDefinition property, Object source) {
			if (DataTypeDefinition.DATE.equals(property.getType())
					|| DataTypeDefinition.DATETIME.equals(property.getType())) {
				return typeConverter.convert(String.class, source);
			} else if (property.getControlDefinition() != null) {
				ControlDefinition control = property.getControlDefinition();
				if ("USER".equals(control.getIdentifier())
						|| "PICKLIST".equals(control.getIdentifier())) {
					return convertUserControl(source);
				} else if (source instanceof CommonInstance) {
					return convertCommonInstance(source);
				}
			}
			return source;
		}

		/**
		 * Convert common instance.
		 * 
		 * @param source
		 *            the source
		 * @return the object
		 */
		private Object convertCommonInstance(Object source) {
			Map<String, Serializable> properties = ((CommonInstance) source).getProperties();
			Map<String, String> data = CollectionUtils.createHashMap(properties.size());
			// copy the resource data
			for (Entry<String, Serializable> entry : properties.entrySet()) {
				if (entry.getValue() != null) {
					data.put(entry.getKey(), entry.getValue().toString());
				}
			}
			if (properties.containsKey(DefaultProperties.HEADER_COMPACT)) {
				data.put("label", (String) properties.get(DefaultProperties.HEADER_COMPACT));
			}
			return data;
		}

		/**
		 * Convert user control.
		 * 
		 * @param source
		 *            the source
		 * @return the object
		 */
		private Object convertUserControl(Object source) {
			com.sirma.itt.emf.instance.model.Instance instance = null;

			if (source.toString().startsWith("{")) {
				InstanceReference reference = typeConverter
						.convert(InstanceReference.class, source);
				if ((reference != null) && (reference.toInstance() != null)) {
					instance = reference.toInstance();
				}
			} else {
				Resource resource = resourceService.get().getResource(source.toString(),
						ResourceType.UNKNOWN);
				instance = resource;
			}
			if (instance != null) {
				Map<String, String> data = CollectionUtils.createHashMap(instance.getProperties()
						.size());
				// copy the resource data
				for (Entry<String, Serializable> entry : instance.getProperties().entrySet()) {
					if (entry.getValue() != null) {
						data.put(entry.getKey(), entry.getValue().toString());
					}
				}
				data.put("label", EmfResourcesUtil.buildDisplayName(instance.getProperties()));
				return data;
			}
			// invalid user/group
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A> A getValue(PropertyDefinition definition, Map<String, A> source) {
			// no need to check other keys they should always be converted to the internal model
			return source.get(definition.getName());
		}

	}

}
