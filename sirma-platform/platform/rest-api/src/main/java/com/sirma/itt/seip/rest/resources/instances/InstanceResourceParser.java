package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.rest.utils.JsonKeys.CONTENT;
import static com.sirma.itt.seip.rest.utils.JsonKeys.DEFINITION_ID;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Parser that reads a JSON structure and converts it to {@link Instance} instance.
 *
 * @author velikov
 */
@ApplicationScoped
public class InstanceResourceParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private DictionaryService dictionaryService;
	
	/**
	 * Converts the provided stream to list of {@link Instance} objects.
	 *
	 * @param stream
	 *            The stream that needs to be parsed.
	 * @return A list with {@link Instance}s
	 * @throws IOException
	 *             If any I/O error is thrown during the parsing.
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
	 * @param array
	 *            JsonArray that contains {@link Instance}s data.
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
			String id = jsonObject.getString(JsonKeys.ID, null);
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
	 * @param id
	 *            The id for the instance to be used.
	 * @return {@link Instance} object.
	 */
	public Function<JsonObject, Instance> toSingleInstance(String id) {
		return value -> toInstance(value, id);
	}

	/**
	 * Parses {@link JsonObject} to instance. If the passed id is empty or null new instance will be generated.
	 *
	 * @param value
	 *            the {@link JsonObject} from which will be get the information about the instance
	 * @param id
	 *            the id of the target instance
	 * @return instance with the properties from the json or null if the instance with the passed id cannot be found
	 */
	public Instance toInstance(JsonObject value, String id) {
		Instance instance;
		if (StringUtils.isBlank(id)) {
			instance = createInstance(value);
		} else if (value.containsKey(PROPERTIES)
				&& value.getJsonObject(PROPERTIES).containsKey(DefaultProperties.SEMANTIC_TYPE)) {
			instance = extractInstance(value, id);
		} else {
			instance = domainInstanceService.loadInstance(id);
		}

		if (instance == null) {
			return null;
		}

		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
		setProperties(instance, definition, value);
		fixMultiValueCompatability(instance);
		setContent(instance, value);
		return instance;
	}

	
	
	private Instance extractInstance(JsonObject value, String id) {
		Instance instance;
		JsonObject jsonObject = value.getJsonObject(PROPERTIES);

		String rdfType;
		JsonValue jsonValue = jsonObject.get(DefaultProperties.SEMANTIC_TYPE);
		if (jsonValue.getValueType() == ValueType.ARRAY) {
			JsonValue jObject = jsonObject.getJsonArray(DefaultProperties.SEMANTIC_TYPE).get(0);
			if(jObject.getValueType() == ValueType.OBJECT){
				//We need to convert the semantic value to Json Object.
				JsonObject jVal = (JsonObject) jObject ;
				rdfType = typeConverter.convert(Uri.class, jVal.getString(RequestParams.KEY_ID)).toString();
			}else {
				rdfType = ((JsonString) jObject).getString();
			}
		} else {
			// Do NOT remove! Added back the logic for extracting semantic type as single string value. This most
			// probably needed for the import functionality but since it cannot be tested we'll have to support
			// both for the time being.
			rdfType = jsonObject.getString(DefaultProperties.SEMANTIC_TYPE);
		}

		InstanceReference reference = typeConverter.convert(InstanceReference.class, rdfType);
		reference.setIdentifier(id);
		instance = reference.toInstance();
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

	private void setProperties(Instance instance, DefinitionModel definition, JsonObject json) {
		JsonObject jsonProperties = json.getJsonObject(PROPERTIES);
		if (JSON.isBlank(jsonProperties)) {
			// at least create properties map
			instance.getOrCreateProperties();
			return;
		}

		Map<String, Serializable> fromJson = readProperties(definition, jsonProperties);
		instance.addAllProperties(fromJson);

		JsonValue purposeValue = jsonProperties.get(DefaultProperties.EMF_PURPOSE);
		if (JSON.isType(purposeValue, ValueType.STRING)) {
			instance.addIfNullMapping(DefaultProperties.EMF_PURPOSE, ((JsonString) purposeValue).getString());
		}
	}

	private Map<String, Serializable> readProperties(DefinitionModel definition, JsonObject json) {
		// convert all properties from json
		Map<String, Serializable> properties = jsonToMap(json);
		if (isEmpty(properties)) {
			return properties;
		}

		Set<String> objectPropertyNames = definition
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());

		// convert special the object properties
		Map<String, Serializable> objectPropertyValues = properties
				.keySet()
					.stream()
					.filter(objectPropertyNames::contains)
					.collect(Collectors.toMap(Function.identity(), name -> readObjectProperty(properties.get(name))));

		// convert dates to java.util.Date from ISO string format
		Map<String, Serializable> dateProperties = definition
				.fieldsStream()
					.filter(PropertyDefinition.hasType(DataTypeDefinition.DATETIME).or(
							PropertyDefinition.hasType(DataTypeDefinition.DATE)))
					.map(PropertyDefinition::getName)
					.filter(properties::containsKey)
					.collect(HashMap::new, (map, name) -> map.put(name, convertDate(properties.get(name))),
							HashMap::putAll);

		properties.putAll(objectPropertyValues);
		properties.putAll(dateProperties);
		return properties;
	}

	private static Map<String, Serializable> jsonToMap(JsonObject json) {
		if (json == null) {
			return Collections.emptyMap();
		}
		Map<String, Serializable> result = createHashMap(json.size());
		for (Entry<String, JsonValue> entry : json.entrySet()) {
			result.put(entry.getKey(), JSON.readJsonValue(entry.getValue()));
		}
		return result;
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

	@SuppressWarnings("unchecked")
	private static Serializable readObjectProperty(Serializable value) {
		// if for some reason the object property is send as json object instead of string
		if (value instanceof Map) {
			return toInstanceId(value);
		}
		if (value instanceof Collection) {
			return (Serializable) ((Collection<Serializable>) value)
					.stream()
						.map(v -> toInstanceId(v))
						.collect(Collectors.toList());
		}
		if (value == null) {
			return (Serializable) Collections.emptyList();
		}
		return value;
	}

	private static Serializable toInstanceId(Serializable value) {
		if (value instanceof Map && ((Map<?, ?>) value).containsKey("id")) {
			return (Serializable) ((Map<?, ?>) value).get("id");
		}
		return value;
	}

	private static void setContent(Instance instance, JsonObject value) {
		String content = value.getString(CONTENT, null);
		instance.addIfNotNullOrEmpty(DefaultProperties.TEMP_CONTENT_VIEW, content);
	}

	/**
	 * Util that converts multivalued instance properties to a single-valued property.
	 * <p>
	 * Some properties from the web UI come as an array even though they are declared in the definition as a single
	 * value. So basically we receive single valued in an array hat has only one element. Working with an instance with
	 * such properties can cause problems with the back-end services. For example when saving such instance. That's why
	 * we iterate all the instance's properties and convert all lists that contain only one value to a single value if
	 * those are declared as such in the corresponding definition.
	 *
	 * @param instance
	 *            instance that comes directly through the web.
	 * @return instance with converted multi-valued to single-valued properties when it is allowed.
	 */
	public Instance fixMultiValueCompatability(Instance instance) {
		DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);
		if (definitionModel == null) {
			return instance;
		}

		Map<String, Serializable> instanceProperties = new HashMap<>(instance.getOrCreateProperties());
		for (Entry<String, Serializable> entry : instanceProperties.entrySet()) {
			String key = entry.getKey();
			Optional<PropertyDefinition> property = definitionModel.getField(key);
			// skip property if invalid or null
			if (!property.isPresent()) {
				continue;
			}

			Serializable fixedValue = fixMultiValueCompatability(property.get(), entry.getValue());
			instance.addIfNotNull(key, fixedValue);
		}
		return instance;
	}

	private static Serializable fixMultiValueCompatability(PropertyDefinition propertyDefinition, Serializable value) {
		// NOSONAR
		boolean isMultivalue = propertyDefinition.isMultiValued().booleanValue();
		if (!isMultivalue && (value instanceof Collection)) {
			Collection<?> collection = (Collection<?>) value;
			if (collection.size() == 1) {
				return (Serializable) collection.iterator().next();
			} else if (collection.isEmpty()) {
				return null;
			}
			LOGGER.error("Incompatible multivalue {} for definition entry: {}", value,  propertyDefinition);
		}

		if (isMultivalue && !(value instanceof Collection) && value != null) {
			List<Serializable> list = new ArrayList<>(1);
			list.add(value);
			return (Serializable) list;
		}

		return value;
	}

}