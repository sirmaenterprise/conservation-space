package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.rest.utils.JsonKeys.CONTENT;
import static com.sirma.itt.seip.rest.utils.JsonKeys.DEFINITION_ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PARENT_ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Parser that reads a JSON structure and converts it to {@link Instance} instance.
 *
 * @author velikov
 */
@ApplicationScoped
public class InstanceResourceParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Key for the property that holds the type of the instance that will be imported. It is used only one time, when
	 * the imported instance is added in the system for the first time.
	 */
	private static final String IMPORT_INSTANCE_TYPE = "importInstanceType";

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private DefinitionService definitionService;

	/**
	 * Converts the provided stream to list of {@link Instance} objects.
	 *
	 * @param stream The stream that needs to be parsed.
	 * @return A list with {@link Instance}s
	 * @throws IOException If any I/O error is thrown during the parsing.
	 */
	public Collection<Instance> toInstanceList(InputStream stream) throws IOException {
		JsonArray array;
		try (JsonReader reader = Json.createReader(stream)) {
			array = reader.readArray();
		}

		return convertValues(array);
	}

	/**
	 * Converts the provided JsonArray structure to list of {@link Instance} objects.
	 *
	 * @param array JsonArray that contains {@link Instance}s data.
	 * @return A list with {@link Instance}s. Could return empty collection when the passed array is null or empty
	 */
	public Collection<Instance> toInstanceList(JsonArray array) {
		return convertValues(array);
	}

	private Collection<Instance> convertValues(JsonArray array) {
		if (array == null || array.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Instance> instances = new LinkedList<>();
		for (JsonObject jsonObject : array.getValuesAs(JsonObject.class)) {
			String id = jsonObject.getString(ID, null);
			Instance instance = toInstance(jsonObject, id);
			if (instance != null) {
				instances.add(instance);
			}
		}

		return instances;
	}

	/**
	 * Converts a json object to {@link Instance}.
	 *
	 * @param id The id for the instance to be used.
	 * @return {@link Instance} object.
	 */
	public Function<JsonObject, Instance> toSingleInstance(String id) {
		return value -> toInstance(value, id);
	}

	/**
	 * Parses {@link JsonObject} to instance. If the passed id is empty or null new instance will be generated.
	 *
	 * @param value the {@link JsonObject} from which will be get the information about the instance
	 * @param id the id of the target instance
	 * @return instance with the properties from the json or null if the instance with the passed id cannot be found
	 */
	public Instance toInstance(JsonObject value, String id) {
		Instance instance;
		if (StringUtils.isBlank(id)) {
			instance = createInstance(value);
		} else if (value.containsKey(IMPORT_INSTANCE_TYPE)) {
			// this branch covers the case where the instance is imported for the first time from external system
			// in this case the instance type is passed as specific property that is used only for such instances
			instance = InstanceReference
					.create(id, value.getString(IMPORT_INSTANCE_TYPE))
						.map(InstanceReference::toInstance)
						.orElse(null);
		} else {
			instance = domainInstanceService.loadInstance(id);
		}

		if (instance == null) {
			return null;
		}

		setVersionMode(instance, value);
		DefinitionModel definition = definitionService.getInstanceDefinition(instance);
		setProperties(instance, definition, value);
		fixMultiValueCompatibility(instance, definition);
		setContent(instance, value);
		return instance;
	}

	private Instance createInstance(JsonObject object) {
		String definitionId = object.getString(DEFINITION_ID, null);
		if (StringUtils.isBlank(definitionId)) {
			throw new BadRequestException("'" + DEFINITION_ID + "' field is mandatory for instance operations");
		}

		String parentId = object.getString(PARENT_ID, null);
		return domainInstanceService.createInstance(definitionId, parentId);
	}

	/**
	 * Sets the version mode in the instance. It is passed as top level property of the json object and it initialised
	 * by the save actions in the client. It is set as temporary property in the instance, which will not be
	 * persisted.Used to detect which operation is executed by the client and to set the correct version mode for the
	 * save process.
	 */
	private static void setVersionMode(Instance instance, JsonObject json) {
		instance.addIfNotNull(VersionProperties.VERSION_MODE, json.getString(VersionProperties.VERSION_MODE, null));
	}

	private void setProperties(Instance instance, DefinitionModel definition, JsonObject json) {
		JsonObject jsonProperties = json.getJsonObject(PROPERTIES);
		if (JSON.isBlank(jsonProperties)) {
			// at least create properties map
			instance.getOrCreateProperties();
			return;
		}

		Map<String, Serializable> fromJson = readProperties(instance, definition, jsonProperties);
		instance.addAllProperties(fromJson);

		JsonValue purposeValue = jsonProperties.get(DefaultProperties.EMF_PURPOSE);
		if (JSON.isType(purposeValue, ValueType.STRING)) {
			instance.addIfNullMapping(DefaultProperties.EMF_PURPOSE, ((JsonString) purposeValue).getString());
		}
	}

	private Map<String, Serializable> readProperties(Instance instance, DefinitionModel definition, JsonObject json) {
		// convert all properties from json
		Map<String, Serializable> properties = JSON.toMap(json, true);
		if (isEmpty(properties)) {
			return properties;
		}

		ObjectPropertiesChangeSetReader.transform(definition, properties, instance);

		// convert dates to java.util.Date from ISO string format
		Map<String, Serializable> dateProperties = definition
				.fieldsStream()
					.filter(PropertyDefinition.hasType(DataTypeDefinition.DATETIME).or(
							PropertyDefinition.hasType(DataTypeDefinition.DATE)))
					.map(PropertyDefinition::getName)
					.filter(properties::containsKey)
					.collect(HashMap::new, (map, name) -> map.put(name, convertDate(properties.get(name))),
							HashMap::putAll);

		properties.putAll(dateProperties);
		return properties;
	}

	@SuppressWarnings("unchecked")
	private Serializable convertDate(Serializable value) {
		if (value instanceof String) {
			return typeConverter.convert(Date.class, value);
		} else if (value instanceof Collection) {
			return (Serializable) ((Collection<Serializable>) value)
					.stream()
						.map(this::convertDate)
						.collect(Collectors.toList());
		}
		if (value != null) {
			LOGGER.warn("Found not converted value [{}] for date field", value);
		}
		return value;
	}

	private static void setContent(Instance instance, JsonObject value) {
		String content = value.getString(CONTENT, null);
		instance.addIfNotNullOrEmpty(DefaultProperties.TEMP_CONTENT_VIEW, content);
	}

	/**
	 * Remove me, I'm causing problems !!!
	 * <p>
	 * Util that converts multivalued instance properties to a single-valued property.
	 * <p>
	 * Some properties from the web UI come as an array even though they are declared in the definition as a single
	 * value. So basically we receive single valued in an array hat has only one element. Working with an instance with
	 * such properties can cause problems with the back-end services. For example when saving such instance. That's why
	 * we iterate all the instance's properties and convert all lists that contain only one value to a single value if
	 * those are declared as such in the corresponding definition.
	 *
	 * @param definitionModel the definition for the instance
	 * @param instance instance that comes directly through the web.
	 */
	public static void fixMultiValueCompatibility(Instance instance, DefinitionModel definitionModel) {
		if (definitionModel == null) {
			return;
		}

		Map<String, Serializable> instanceProperties = new HashMap<>(instance.getOrCreateProperties());
		for (Entry<String, Serializable> entry : instanceProperties.entrySet()) {
			String key = entry.getKey();
			Optional<PropertyDefinition> property = definitionModel.getField(key);
			// skip property if invalid or null
			if (!property.isPresent()) {
				continue;
			}

			fixMultiValueCompatibility(property.get(), entry.getValue(), instance::add);
		}
	}

	private static void fixMultiValueCompatibility(PropertyDefinition propertyDefinition, Serializable value, BiConsumer<String, Serializable> onChange) {
		// NOSONAR
		String propertyName = propertyDefinition.getName();
		boolean isMultiValue = propertyDefinition.isMultiValued();
		if (!isMultiValue && (value instanceof Collection)) {
			Collection<?> collection = (Collection<?>) value;
			if (collection.size() == 1) {
				onChange.accept(propertyName,(Serializable) collection.iterator().next());
			} else if (collection.isEmpty()) {
				onChange.accept(propertyName,null);
			} else {
				LOGGER.error("Incompatible multivalue {} for definition entry: {}", value, propertyDefinition);
			}
		} else if (isMultiValue && !(value instanceof Collection) && value != null) {
			ArrayList<Serializable> list = new ArrayList<>(1);
			list.add(value);
			onChange.accept(propertyName, list);
		}
	}
}
