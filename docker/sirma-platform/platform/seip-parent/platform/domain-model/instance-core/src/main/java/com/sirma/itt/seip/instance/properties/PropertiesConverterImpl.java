package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Default implementation of {@link PropertiesConverter}
 *
 * @author BBonev
 */
public class PropertiesConverterImpl implements PropertiesConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConverterImpl.class);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private Instance<ResourceService> resourceService;

	private final ModelConverter inboundConverter = new InputModelConverter();

	private final ModelConverter outboundConverter = new OutputModelConverter();

	@Override
	public Map<String, Serializable> fromJson(JsonObject properties, DefinitionModel definition,
			boolean allowNonModelProperties) {
		return convertToInternalModelInternal(properties, definition, allowNonModelProperties);
	}

	@Override
	public JsonObject toJson(PropertyModel model, DefinitionModel definition) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		// current implementation cannot handle conversion to external model of non model properties
		convertToExternalModel(model, definition, new OutputModelConverter(builder), false);
		return builder.build();
	}

	@Override
	public Map<String, ?> convertToExternalModel(PropertyModel model, DefinitionModel definitionModel) {
		return convertToExternalModel(model, definitionModel, outboundConverter, false);
	}

	private static Map<String, ?> convertToExternalModel(PropertyModel model, DefinitionModel definitionModel,
			ModelConverter converter, boolean allowNonModelProperties) {
		if (model == null || model.getProperties() == null || model.getProperties().isEmpty()) {
			return new HashMap<>(1);
		}
		if (definitionModel == null) {
			return new HashMap<>(model.getProperties());
		}
		Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(model.getProperties().size() << 1);

		if (definitionModel instanceof RegionDefinitionModel) {
			iterateModel((RegionDefinitionModel) definitionModel, escapeStringProperties(model.getProperties()),
					properties, converter);
		} else {
			iterateModel(definitionModel, escapeStringProperties(model.getProperties()), properties, converter);
		}

		// only valid for input properties
		if (allowNonModelProperties) {
			Set<String> inputNames = new HashSet<>(model.getProperties().keySet());
			inputNames.removeAll(properties.keySet());
			processNonModelProperties(model.getProperties(), properties, inputNames, converter, Serializable.class);
		}
		return properties;
	}

	@Override
	public Map<String, Serializable> convertToInternalModel(Map<String, ?> source, DefinitionModel definitionModel) {
		return convertToInternalModelInternal(source, definitionModel, false);
	}

	@Override
	public Map<String, Serializable> convertToInternalModel(Map<String, ?> source, DefinitionModel definitionModel,
			boolean allowNonModelProperties) {
		return convertToInternalModelInternal(source, definitionModel, allowNonModelProperties);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Serializable> convertToInternalModelInternal(Map<String, ?> source,
			DefinitionModel definitionModel, boolean allowNonModelProperties) {
		if (source == null || source.isEmpty()) {
			return new LinkedHashMap<>();
		}
		if (definitionModel == null) {
			return (Map<String, Serializable>) source;
		}
		Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(source.size());

		if (definitionModel instanceof RegionDefinitionModel) {
			iterateModel((RegionDefinitionModel) definitionModel, source, properties, inboundConverter);
		} else {
			iterateModel(definitionModel, source, properties, inboundConverter);
		}

		if (allowNonModelProperties) {
			Set<String> inputNames = new HashSet<>(source.keySet());
			inputNames.removeAll(properties.keySet());

			processNonModelProperties(source, properties, inputNames, inboundConverter, Serializable.class);
		}

		return properties;
	}

	/**
	 * Process non model properties.
	 *
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source properties for conversion
	 * @param destination
	 *            the destination properties for the result
	 * @param inputNames
	 *            the input names to process
	 * @param converter
	 *            the converter to use
	 * @param expectedBaseType
	 *            the expected base type. Converted properties that are not of the base type will be ignored
	 */
	private static <T> void processNonModelProperties(Map<String, ?> source, Map<String, T> destination,
			Set<String> inputNames, ModelConverter converter, Class<T> expectedBaseType) {
		for (String key : inputNames) {
			Object converted = converter.convert(source.get(key));
			if (expectedBaseType.isInstance(converted)) {
				destination.put(key, expectedBaseType.cast(converted));
			}
		}
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
	private static <A, B> void iterateModel(DefinitionModel model, Map<String, A> source, Map<String, B> output,
			ModelConverter converter) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			if (!converter.isAllowedForConvert(propertyDefinition)) {
				continue;
			}

			A sourceValue = converter.getValue(propertyDefinition, source);
			if (sourceValue == null) {
				// nothing to do with the missing values
				continue;
			}
			Object converted = converter.convert(propertyDefinition, sourceValue);
			// we does not handle null values
			// we set the internal name and ignore the source name
			output.put(propertyDefinition.getName(), (B) converted);
		}
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
	private static <A, B> void iterateModel(RegionDefinitionModel model, Map<String, A> source, Map<String, B> output,
			ModelConverter converter) {
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
	private static Map<String, Serializable> escapeStringProperties(Map<String, Serializable> properties) {
		return properties;
	}

	/**
	 * Interface that defines a means to convert a definition model properties to other format using the provided
	 * definition.
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
		 * Convert non model property
		 *
		 * @param source
		 *            the source value to convert
		 * @return the result of the conversion or <code>null</code> if this should be skipped
		 */
		Object convert(Object source);

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

		@Override
		public boolean isAllowedForConvert(PropertyDefinition property) {
			return true;
		}

		@Override
		public Object convert(PropertyDefinition property, Object source) {
			if (source instanceof String && StringUtils.isBlank((String) source)) {
				// when converting data from REST to internal model when we have only white space in
				// the field we mark it as null and ignore the value at all.
				return null;
			}

			Object v = source;
			try {
				if (v instanceof JsonValue) {
					v = getActualValue((JsonValue) source);
				}

				if (source instanceof Collection) {
					return getConvertedCollection(property, source);
				}

				if (DataTypeDefinition.DATE.equals(property.getType())
						|| DataTypeDefinition.DATETIME.equals(property.getType())) {
					return typeConverter.convert(Date.class, v);
				} else if (property.getControlDefinition() != null) {
					ControlDefinition control = property.getControlDefinition();
					if ("USER".equals(control.getIdentifier()) || "PICKLIST".equals(control.getIdentifier())) {
						return convertUserControl(source);
					} else if ("INSTANCE".equals(control.getIdentifier())) {
						return convertInstanceControl(source, control);
					}
				}

				return typeConverter.convert(property.getDataType().getJavaClass(), v);
			} catch (TypeConversionException e) {
				LOGGER.debug("Could not convert property {}={} to {}", property.getName(), v,
						property.getDataType().getJavaClassName());
				LOGGER.trace("And the exception is: ", e);
			}
			return v;
		}

		private List<Object> getConvertedCollection(PropertyDefinition property, Object source) {
			List<Object> convertedValues = new ArrayList<>(((Collection<?>) source).size());
			for (Object object : (Collection<?>) source) {
				Object temp = convert(property, object);
				// we does not handle null values
				if (temp != null) {
					convertedValues.add(temp);
				}
			}
			return convertedValues;
		}

		private Object getActualValue(JsonValue source) {
			return JSON.readJsonValue(source);
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
			if (source instanceof String && source.toString().startsWith("{")) {
				object = JsonUtil.createObjectFromString(source.toString());
				if (object == null) {
					return source;
				}
				name = JsonUtil.getStringValue(object, ResourceProperties.USER_ID);
			} else if (source instanceof JsonObject) {
				name = ((JsonObject) source).getString(ResourceProperties.USER_ID);
			} else if (source instanceof Map) {
				name = (String) ((Map<?, ?>) source).get(ResourceProperties.USER_ID);
			} else {
				return source;
			}

			Resource resource = resourceService.get().findResource(name);
			if (resource != null) {
				return resource.getName();
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
		@SuppressWarnings("unchecked")
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
			if (map != null && map.isEmpty() || object != null && object.length() == 0) {
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
				Map<String, String> subProperties = jsonToMap(properties);

				Map<String, Serializable> map2 = convertToInternalModelInternal(subProperties, control, false);
				instance.setProperties(map2);
			} else if (properties instanceof Map) {
				Map<String, Serializable> map2 = convertToInternalModelInternal((Map<String, ?>) properties, control,
						false);
				instance.setProperties(map2);
			} else {
				instance.setProperties(new LinkedHashMap<String, Serializable>());
			}
			return instance;
		}

		/**
		 * Json to map.
		 *
		 * @param properties
		 *            the properties
		 * @return the map
		 */
		private Map<String, String> jsonToMap(Object properties) {
			JSONObject propertiesObject = (JSONObject) properties;
			Map<String, String> subProperties = CollectionUtils.createLinkedHashMap(propertiesObject.length());
			if (propertiesObject.length() > 0) {
				Iterator<?> localIterator = propertiesObject.keys();
				while (localIterator.hasNext()) {
					String key = localIterator.next().toString();
					String value = JsonUtil.getStringValue(propertiesObject, key);
					subProperties.put(key, value);
				}
			}
			return subProperties;
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
		private <T> T getProperty(Class<T> result, JSONObject object, Map<String, Object> map, String key) {
			if (map != null) {
				return typeConverter.convert(result, map.get(key));
			}
			return typeConverter.convert(result, JsonUtil.getValueOrNull(object, key));
		}

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
						dmsType = cleanDmsId(dmsType);
						sourceValue = source.get(dmsType);
					}
				}
			}
			return sourceValue;
		}

		/**
		 * Clean dms id.
		 *
		 * @param dmsType
		 *            the dms type
		 * @return the string
		 */
		private String cleanDmsId(String dmsType) {
			String type = dmsType;
			if (type.startsWith("-")) {
				type = type.substring(1);
			}
			return type;
		}

		@Override
		public Object convert(Object source) {
			if (source instanceof JsonValue) {
				return getActualValue((JsonValue) source);
			} else if (source instanceof String || source instanceof Number || source instanceof Boolean) {
				return source;
			}
			return null;
		}
	}

	/**
	 * Model converter implementation for the output properties.
	 *
	 * @author BBonev
	 */
	class OutputModelConverter implements ModelConverter {
		private JsonObjectBuilder builder;

		/**
		 * Constructor.
		 */
		public OutputModelConverter() {
			this(null);
		}

		/**
		 * Constructor.
		 *
		 * @param builder
		 *            a json builder to write converted properties with.
		 */
		public OutputModelConverter(JsonObjectBuilder builder) {
			this.builder = builder;
		}

		@Override
		public boolean isAllowedForConvert(PropertyDefinition property) {
			// it's wrong to return the tooltip because is may contains not evaluated expressions
			return !DefaultProperties.HEADER_TOOLTIP.equals(property.getIdentifier());
		}

		private void write(PropertyDefinition definition, Object value) {
			if (builder == null) {
				return;
			}

			String name = definition.getName();
			if (value == null) {
				builder.addNull(name);
				return;
			}

			writeAsDefinitionType(definition.getType(), name, value);
		}

		@SuppressWarnings("boxing")
		private void writeAsDefinitionType(String type, String name, Object value) {
			switch (type) {
				case DataTypeDefinition.DATE:
				case DataTypeDefinition.DATETIME:
				case DataTypeDefinition.TEXT:
				case DataTypeDefinition.URI:
					builder.add(name, value.toString());
					break;
				case DataTypeDefinition.BOOLEAN:
					builder.add(name, Boolean.valueOf(value.toString()));
					break;
				case DataTypeDefinition.INT:
					builder.add(name, (Integer) value);
					break;
				case DataTypeDefinition.LONG:
					builder.add(name, (Long) value);
					break;
				case DataTypeDefinition.FLOAT:
					builder.add(name, (Float) value);
					break;
				case DataTypeDefinition.DOUBLE:
					builder.add(name, (Double) value);
					break;
				default:
					writeObject(name, value);
					break;
			}
		}

		private void writeObject(String name, Object value) {
			JSON.addIfNotNull(builder, name, value);
		}

		@Override
		public Object convert(PropertyDefinition property, Object source) { // NOSONAR

			if (source instanceof Collection) {
				List<Object> convertedValues = new ArrayList<>(((Collection<?>) source).size());
				for (Object object : (Collection<?>) source) {
					Object temp = convert(property, object);
					// we does not handle null values
					if (temp != null) {
						convertedValues.add(temp);
					}
				}
				write(property, source);
				return convertedValues;
			}

			Object value = null;
			if (DataTypeDefinition.DATE.equals(property.getType())
					|| DataTypeDefinition.DATETIME.equals(property.getType())) {
				value = typeConverter.convert(String.class, source);
			} else if (DataTypeDefinition.URI.equals(property.getType())) {
				value = convertUserControl(source);
				if (value == null) {
					value = typeConverter.convert(String.class, source);
				}
			} else if (property.getControlDefinition() != null) {
				ControlDefinition control = property.getControlDefinition();
				if ("USER".equals(control.getIdentifier()) || "PICKLIST".equals(control.getIdentifier())) {
					value = convertUserControl(source);
				} else if (source instanceof CommonInstance) {
					value = convertCommonInstance(source);
				} else if ("RELATED_FIELDS".equals(control.getIdentifier())) {
					value = source;
				} else if ("RADIO_BUTTON_GROUP".equals(control.getIdentifier())) {
					value = source;
				} else if ("BYTE_FORMAT".equals(control.getIdentifier())) {
					value = FileUtil.humanReadableByteCount(Long.parseLong(source.toString()));
				}
			} else {
				value = source;
			}
			write(property, value);
			return value;
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
			Map<String, String> data = copyInstanceProperties((CommonInstance) source);
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
			com.sirma.itt.seip.domain.instance.Instance instance = resourceService
					.get()
						.findResource(source.toString());
			if (instance != null) {
				Map<String, String> data = copyInstanceProperties(instance);
				data.put("label", EmfResourcesUtil.buildDisplayName(instance.getProperties()));
				return data;
			}
			// invalid user/group
			return null;
		}

		private Map<String, String> copyInstanceProperties(com.sirma.itt.seip.domain.instance.Instance instance) {
			Map<String, String> data = CollectionUtils.createHashMap(instance.getProperties().size());
			// copy the resource data
			for (Entry<String, Serializable> entry : instance.getProperties().entrySet()) {
				if (entry.getValue() != null) {
					data.put(entry.getKey(), entry.getValue().toString());
				}
			}
			return data;
		}

		@Override
		public <A> A getValue(PropertyDefinition definition, Map<String, A> source) {
			// no need to check other keys they should always be converted to the internal model
			return source.get(definition.getName());
		}

		@Override
		public Object convert(Object source) {
			return source;
		}
	}

}