package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADERS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INSTANCE_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_HIERARCHY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.instance.version.VersionProperties.HAS_VIEW_CONTENT;
import static com.sirma.itt.seip.instance.version.VersionProperties.IS_VERSION;
import static com.sirma.itt.seip.instance.version.VersionProperties.MANUALLY_SELECTED;
import static com.sirma.itt.seip.instance.version.VersionProperties.ORIGINAL_INSTANCE_ID;
import static com.sirma.itt.seip.instance.version.VersionProperties.PRIMARY_CONTENT_MIMETYPE;
import static com.sirma.itt.seip.instance.version.VersionProperties.QUERIES_RESULTS;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATION_DATE;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_MODE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.DEFINITION_ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.INSTANCE_HEADERS;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PARENT_ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PROPERTIES;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * {@link Instance} to {@link JsonObject} serializer.
 *
 * @author yasko
 * @author A. Kunchev
 */
@Singleton
public class InstanceToJsonSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private CodelistService codelistService;

	@Inject
	private InstanceContextService contextService;

	@Inject
	private InstanceRelationsService instanceRelationsService;

	/**
	 * Creates predicate which could be used for properties filtering. The passed collection should contain only
	 * properties that should be added to the model. If the passed collection is empty or null, generated predicate will
	 * be: <b>{@code (k,v) -> false }</b>, which will remove all the properties from the instance.
	 *
	 * @param properties properties to be filtered
	 * @return true if the passed properties contains the key of the entry, false otherwise
	 */
	public static PropertiesFilterBuilder onlyProperties(Collection<String> properties) {
		if (CollectionUtils.isEmpty(properties)) {
			return PropertiesFilterBuilder.MATCH_NONE;
		}

		Set<String> filter = new HashSet<>(properties);
		return new SelectedPropertiesFilter(filter);
	}

	/**
	 * Predicate that that matches all instance properties.
	 *
	 * @return the all properties filter
	 */
	public static PropertiesFilterBuilder allProperties() {
		return PropertiesFilterBuilder.MATCH_ALL;
	}

	/**
	 * Returns a predicate that will match the given properties or all properties if non are specified.
	 *
	 * @param projectionProperties the projection properties, optional
	 * @return the predicate tha matches the all properties or the given list of properties
	 */
	public static PropertiesFilterBuilder allOrGivenProperties(Collection<String> projectionProperties) {
		// by default return all properties
		PropertiesFilterBuilder propertiesFilter = allProperties();
		if (isNotEmpty(projectionProperties)) {
			propertiesFilter = new SelectedPropertiesFilter(projectionProperties);
		}
		return propertiesFilter;
	}

	/**
	 * Converts the provided collection of {@link Instance}s to a {@link JsonObject} with its properties and relations.
	 * The method is optimized for batch loading of relations and should be used for serializing more than one instance.
	 *
	 * @param instances the provided instances to serialize
	 * @param generator the JSON generator where the instance is serialized
	 */
	public void serialize(Collection<Instance> instances, JsonGenerator generator) {
		serialize(instances, allProperties(), generator);
	}

	/**
	 * Converts the provided collection of {@link Instance}s to a {@link JsonObject} with its properties and relations.
	 * The method is optimized for batch loading of relations and should be used for serializing more than one instance.
	 *
	 * @param instances the provided instances to serialize
	 * @param propertiesFilter filter properties that should be added to the model. If you want to add to the model for
	 *        example 'title', this predicate should return <b>true</b> for the 'title' test.
	 * @param generator the JSON generator where the instance is serialized
	 * @see #onlyProperties(Collection)
	 */
	public void serialize(Collection<Instance> instances, PropertiesFilterBuilder propertiesFilter,
			JsonGenerator generator) {
		for (Instance instance : instances) {
			// iterate in the given order because the data returned from load is not ordered in any way
			serialize(instance, propertiesFilter, generator);
		}
	}

	/**
	 * Converts the provided {@link Instance} to a {@link JsonObject} with its properties and relations.
	 *
	 * @param instance the provided instance
	 * @param generator the JSON generator where the instance is serialized
	 */
	public void serialize(Instance instance, JsonGenerator generator) {
		serialize(instance, allProperties(), generator);
	}

	/**
	 * Converts the provided {@link Instance} to a {@link JsonObject} with its properties and relations.
	 *
	 * @param instance the provided instance
	 * @param generator the JSON generator where the instance is serialized
	 * @param objectName - name for the key of the created json object. When null result is the same as
	 *        {@link #serialize(Instance, JsonGenerator)}
	 */
	public void serialize(Instance instance, JsonGenerator generator, String objectName) {
		if (objectName != null) {
			generator.writeStartObject(objectName);
		} else {
			generator.writeStartObject();
		}
		writeInstanceData(instance, allProperties(), generator);
		generator.writeEnd();
	}

	/**
	 * Writes the instance data to the given {@link JsonGenerator}. The data is written to a {@link JsonObject} with the
	 * given name. If this is not desired behavior use the {@link #serialize(Instance, JsonGenerator)} method that
	 * writes a simple {@link JsonObject}.
	 *
	 * @param fieldName the name of the generated {@link JsonObject} where the instance data to be written
	 * @param instance the provided instance
	 * @param generator the JSON generator where the instance is serialized
	 */
	public void serialize(String fieldName, Instance instance, JsonGenerator generator) {
		serialize(fieldName, instance, allProperties(), generator);
	}

	/**
	 * Writes the instance data to the given {@link JsonGenerator}. The data is written to a {@link JsonObject} with the
	 * given name. If this is not desired behavior use the {@link #serialize(Instance, JsonGenerator)} method that
	 * writes a simple {@link JsonObject}.
	 *
	 * @param fieldName the name of the generated {@link JsonObject} where the instance data to be written
	 * @param instance the provided instance
	 * @param propertiesFilter the list of properties to write for the given instance
	 * @param generator the JSON generator where the instance is serialized
	 */
	public void serialize(String fieldName, Instance instance, PropertiesFilterBuilder propertiesFilter, JsonGenerator generator) {
		generator.writeStartObject(fieldName);
		writeInstanceData(instance, propertiesFilter, generator);
		generator.writeEnd();
	}

	/**
	 * Converts the provided {@link Instance} to a {@link JsonObject} with its properties and relations. The properties
	 * could be filtered by passing the predicate, which returns true for the properties that should be added to the
	 * model.
	 *
	 * @param instance the provided instance
	 * @param propertiesFilter filter properties that should be added to the model. If you want to add to the model for
	 *        example 'title', this predicate should return <b>true</b> for the 'title' test.
	 * @param generator the JSON generator where the instance is serialized
	 */
	public void serialize(Instance instance, PropertiesFilterBuilder propertiesFilter, JsonGenerator generator) {
		generator.writeStartObject();
		writeInstanceData(instance, propertiesFilter, generator);
		generator.writeEnd();
	}

	private void writeInstanceData(Instance instance, PropertiesFilterBuilder requestedProperties,
			JsonGenerator generator) {
		writeTopLevelProperties(instance, generator);

		DefinitionModel model = definitionService.getInstanceDefinition(instance);
		if (model == null) {
			throw new EmfRuntimeException("Missing instance definition of type '" + instance.getIdentifier()
					+ "' for object with id: " + instance.getId());
		}

		Predicate<String> fieldsFilter = requestedProperties.buildFilter(model);
		writeHeaders(instance, fieldsFilter, generator);
		writeThumbnail(instance, generator);
		writeInstanceType(instance, generator);

		// wrap the requested properties in a filter that will remove any forbidden properties for serialization
		Predicate<String> propertiesFilter = new ForbiddenPropertiesFilter(requestedProperties).buildFilter(model);
		writeProperties(instance, model, propertiesFilter, generator);
	}

	private void writeTopLevelProperties(Instance instance, JsonGenerator generator) {
		JSON.addIfNotNull(generator, ID, (String) instance.getId());
		JSON.addIfNotNull(generator, DEFINITION_ID, instance.getIdentifier());
		JSON.addIfNotNull(generator, VERSION_MODE, instance.getString(VERSION_MODE));
		writeParentId(instance, generator);
		generator.write(JsonKeys.READ_ALLOWED, instance.isReadAllowed());
		generator.write(JsonKeys.WRITE_ALLOWED, instance.isWriteAllowed());
		if (instance.isDeleted()) {
			generator.write("deleted", instance.isDeleted());
		}
	}

	private void writeParentId(Instance instance, JsonGenerator generator) {
		Optional<InstanceReference> context = contextService.getContext(instance);
		context.ifPresent(reference -> JSON.addIfNotNull(generator, PARENT_ID, reference.getId()));
	}

	private void writeProperties(Instance instance, DefinitionModel model, Predicate<String> propertiesFilter,
			JsonGenerator generator) {
		generator.writeStartObject(PROPERTIES);
		writeVersionProperties(instance, generator);
		writeMandatoryProperties(instance, generator);
		filterAndWriteProperties(instance, model, propertiesFilter, generator);
		generator.writeEnd();
	}

	// we need this because there are specific version properties that aren't described in the model
	// and they are filtered. Thats why they should be serialized explicitly
	private static void writeVersionProperties(Instance instance, JsonGenerator generator) {
		if (!instance.getBoolean(IS_VERSION, false)) {
			return;
		}

		// most of this properties will be removed, when the functionality for idoc content processing is done
		generator
				.write(IS_VERSION, instance.getBoolean(IS_VERSION))
					.write(HAS_VIEW_CONTENT, instance.getBoolean(HAS_VIEW_CONTENT))
					.write(ORIGINAL_INSTANCE_ID, instance.getString(ORIGINAL_INSTANCE_ID));
		JSON.addIfNotNull(generator, MANUALLY_SELECTED, instance.getString(MANUALLY_SELECTED));
		String versionDateAsString = ISO8601DateFormat.format(instance.get(VERSION_CREATION_DATE, Date.class));
		JSON.addIfNotNull(generator, VERSION_CREATION_DATE, versionDateAsString);
		JSON.addIfNotNull(generator, QUERIES_RESULTS, instance.getString(QUERIES_RESULTS));
		JSON.addIfNotNull(generator, PRIMARY_CONTENT_MIMETYPE, instance.getString(PRIMARY_CONTENT_MIMETYPE));
	}

	private void writeMandatoryProperties(Instance instance, JsonGenerator generator) {
		String semanticType = instance.getAsString(SEMANTIC_TYPE);
		if (semanticType != null) {
			writeSemanticHierarchy(generator, semanticType);
		} else if (instance.type() != null) {
			writeSemanticHierarchy(generator, instance.type().getId().toString());
		}
	}

	private void writeSemanticHierarchy(JsonGenerator generator, String semanticType) {
		List<String> semanticHierarchy = semanticDefinitionService.getHierarchy(semanticType);
		generator.writeStartArray(SEMANTIC_HIERARCHY);
		semanticHierarchy.forEach(generator::write);
		generator.writeEnd();
	}

	private void filterAndWriteProperties(Instance instance, DefinitionModel definition,
			Predicate<String> allowedPropertiesFilter, JsonGenerator generator) {
		Set<String> objectPropertiesNames = new HashSet<>();
		Set<PropertyDefinition> dataProperties = new HashSet<>();
		Predicate<PropertyDefinition> propertiesFilter = buildFilter(instance, allowedPropertiesFilter);
		definition.fieldsStream().filter(propertiesFilter).forEach(propertyDefinition -> {
			if (PropertyDefinition.isObjectProperty().test(propertyDefinition)) {
				objectPropertiesNames.add(propertyDefinition.getName());
			} else {
				dataProperties.add(propertyDefinition);
			}
		});

		writeObjectProperties(objectPropertiesNames, instance, generator);
		dataProperties.forEach(field -> writeDataProperty(field, instance, generator));

		// write properties that are not in the model
		// probably this is not needed
		writeNonModelProperties(instance, definition, allowedPropertiesFilter, generator);
	}

	private static Predicate<PropertyDefinition> buildFilter(Instance instance, Predicate<String> additionalFilter) {
		return field -> instance.isPropertyPresent(field.getName()) && additionalFilter.test(field.getName());
	}

	private void writeObjectProperties(Collection<String> propertyNames, Instance instance, JsonGenerator generator) {
		Map<String, List<String>> relations = instanceRelationsService.evaluateRelations(instance, propertyNames);
		final int VALUES_LIMIT = instanceRelationsService.getDefaultLimitPerInstanceProperty();
		relations.forEach((property, values) -> {
			// do we need that TOTAL, I think it is not used ATM
			generator.writeStartObject(property).write(JsonKeys.TOTAL, values.size());
			generator.writeStartArray(JsonKeys.RESULTS);
			values.stream().limit(VALUES_LIMIT).forEach(generator::write);
			generator.writeEnd().writeEnd();
		});
	}

	private void writeDataProperty(PropertyDefinition field, Instance instance, JsonGenerator generator) {
		Serializable value = instance.get(field.getName());
		if (field.getCodelist() != null) {
			writeCodelistProperty(field.getName(), field.getCodelist(), value, generator);
		} else {
			writeLiteralProperty(field.getName(), value, generator);
		}
	}

	private void writeCodelistProperty(String name, Integer codelist, Serializable value, JsonGenerator generator) {
		if (value instanceof String) {
			String description = codelistService.getDescription(codelist, (String) value);

			generator.writeStartObject(name);
			writeCodelistField(value.toString(), description, generator);
			generator.writeEnd();
		} else if (value instanceof Collection) {
			generator.writeStartArray(name);

			for (Object item : (Collection<?>) value) {
				String code = Objects.toString(item, "");
				String description = codelistService.getDescription(codelist, code);

				generator.writeStartObject();
				writeCodelistField(code, description, generator);
				generator.writeEnd();
			}

			generator.writeEnd();
		} else {
			LOGGER.warn("Recieved non String and non collection literal for codelist property:"
					+ " name={}, CL={}, value={}", name, codelist, value);
		}
	}

	private static void writeCodelistField(String code, String label, JsonGenerator generator) {
		// uses this constants to be Select2 compatible
		generator.write(JsonKeys.ID, code);
		String description = getOrDefault(label, code);
		generator.write(JsonKeys.TEXT, description);
	}

	private void writeLiteralProperty(String name, Serializable value, JsonGenerator generator) {
		if (value instanceof Date) {
			generator.write(name, typeConverter.convert(String.class, value));
		} else if (value instanceof Long || value instanceof Integer) {
			generator.write(name, ((Number) value).longValue());
		} else if (value instanceof Float || value instanceof Double) {
			generator.write(name, Double.parseDouble(String.valueOf(value)));
		} else if (value instanceof Boolean) {
			generator.write(name, (Boolean) value);
		} else if (value instanceof String) {
			generator.write(name, value.toString());
		} else {
			writeNonGenericLiteral(name, value, generator);
		}
	}

	private void writeNonGenericLiteral(String name, Serializable value, JsonGenerator generator) {
		if (value == null) {
			// no need to handle null values
			return;
		}
		JsonValue jsonValue = typeConverter.tryConvert(JsonValue.class, value);
		if (jsonValue != null) {
			generator.write(name, jsonValue);
			return;
		}
		String asString = typeConverter.tryConvert(String.class, value);
		if (!JSON.addIfNotNull(generator, name, asString)) {
			LOGGER.warn("Could not convert value [{}] to supported JSON format!", value);
		}
	}

	private void writeNonModelProperties(Instance instance, DefinitionModel definition,
			Predicate<String> allowedPropertiesFilter, JsonGenerator generator) {
		Map<String, PropertyDefinition> fieldsAsMap = definition.getFieldsAsMap();
		instance
				.getOrCreateProperties()
					.keySet()
					.stream()
					.filter(key -> !fieldsAsMap.containsKey(key))
					.filter(allowedPropertiesFilter)
					.forEach(name -> writeLiteralProperty(name, instance.get(name), generator));
	}

	private static void writeHeaders(Instance instance, Predicate<String> allowedPropertiesFilter,
			JsonGenerator generator) {
		generator.writeStartObject(INSTANCE_HEADERS);
		HEADERS.stream().filter(allowedPropertiesFilter).forEach(
				header -> JSON.addIfNotNull(generator, header, instance.getString(header)));
		generator.writeEnd();
	}

	/**
	 * The thumbnail is extracted and set into the model, because it is filtered, when the instance properties are
	 * serialized.
	 *
	 * @param instance the instance from which the thumbnail will be extracted
	 * @param generator {@link JsonGenerator} which builds the model
	 */
	private static void writeThumbnail(Instance instance, JsonGenerator generator) {
		String thumbnailImage = instance.getString(THUMBNAIL_IMAGE);
		JSON.addIfNotNull(generator, THUMBNAIL_IMAGE, thumbnailImage);
	}

	private static void writeInstanceType(Instance instance, JsonGenerator generator) {
		// FIXME: we should no longer have to depend on this
		JSON.addIfNotNull(generator, INSTANCE_TYPE, instance.type().getCategory());
	}
}
