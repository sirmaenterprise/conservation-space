package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Utility class used to process the object properties passed from the client, when we are parsing {@link JsonObject} to
 * {@link Instance}. Object properties are passed as change set, which requires additional transformation, before
 * setting them into the instance.
 *
 * @author A. Kunchev
 * @see InstanceResourceParser
 */
class ObjectPropertiesChangeSetReader {

	// change set map keys
	private static final String ADD = "add";
	private static final String REMOVE = "remove";
	private static final String ID = "id";

	/**
	 * Used to mark that all of the old values are removed and we should put {@code null} for that property in the
	 * result map so that it could be cleared from the instance, when it is persisted.
	 */
	private static final String REMOVED_ALL_VALUES_MARKER = "$removed$";

	private ObjectPropertiesChangeSetReader() {
		// utility
	}

	/**
	 * Transforms the model of the object properties to model suitable for further processing by the internal logic. The
	 * transformation is required, because the client passes those properties as change set. The method will process
	 * every property and apply the passed change set to the current values of the properties stored in the instance.
	 * <br>
	 * Note that only properties that are changed in some way (removed or added value) will be stored back in the result
	 * properties map.
	 * <p>
	 * Properties model coming form the client:
	 *
	 * <pre>
	 * "propertyName":{
	 *     "add":["value-1", "value-2" ... "value-n"],
	 *     "remove":["value-1", "value-2" ... "value-n"]
	 * }
	 * </pre>
	 *
	 * is transformed to:
	 *
	 * <pre>
	 * "propertyName":["value-1", "value-2" ... "value-n"]
	 * </pre>
	 *
	 * @param definition used to retrieve the names of the object properties
	 * @param properties passed from the client, contains the values of the object properties as change set. Will be
	 *        updated with changed object properties, if there are any, because it is used as result properties in the
	 *        parsing process of the instance
	 * @param instance used to retrieve the old values so that we could apply the changes correctly
	 * @throws NullPointerException if any of the input arguments is null
	 */
	static void transform(DefinitionModel definition, Map<String, Serializable> properties, Instance instance) {
		Objects.requireNonNull(definition, "Definition model is required.");
		Objects.requireNonNull(properties, "Properties should not be null.");
		Objects.requireNonNull(instance, "Instance is required.");

		Set<String> objectPropertiesNames = getObjectPropertyNames(definition);
		Map<String, Serializable> changedProperties = transformChangeSet(properties, objectPropertiesNames, instance);
		objectPropertiesNames.forEach(properties::remove);
		objectPropertiesNames.forEach(name -> {
			Serializable value = changedProperties.get(name);
			if (value != null && !REMOVED_ALL_VALUES_MARKER.equals(value)) {
				properties.put(name, value);
			} else if (instance.get(name) != null && REMOVED_ALL_VALUES_MARKER.equals(value)) {
				properties.put(name, null);
			}
		});
	}

	private static Set<String> getObjectPropertyNames(DefinitionModel definition) {
		return definition
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
	}

	private static Map<String, Serializable> transformChangeSet(Map<String, Serializable> properties,
			Collection<String> names, Instance instance) {
		return properties
				.keySet()
					.stream()
					.filter(names::contains)
					.filter(name -> filterUnchanged(instance.get(name), properties.get(name)))
					.map(name -> new Pair<>(name, readObjectProperty(instance.get(name), properties.get(name))))
					.filter(Pair.nonNullSecond())
					.collect(Pair.toMap());
	}

	private static boolean filterUnchanged(Serializable oldValue, Serializable newValue) {
		if (newValue == null && oldValue == null) {
			return false;
		}

		if (newValue instanceof Map && isNotEmpty((Map<?, ?>) newValue)) {
			Map<?, ?> valueMap = (Map<?, ?>) newValue;
			return hasValue(valueMap, ADD) || hasValue(valueMap, REMOVE) || valueMap.containsKey(ID);
		}

		return false;
	}

	private static boolean hasValue(Map<?, ?> map, String key) {
		return isNotEmpty(((Collection<?>) map.get(key)));
	}

	@SuppressWarnings("unchecked")
	private static Serializable readObjectProperty(Serializable oldValue, Serializable value) {
		Map<?, ?> valueMap = (Map<?, ?>) value;
		if (valueMap.containsKey(ID)) {
			// users have different model which contains only id
			// their model will be fixed, but it requires additional refactoring of functionalities
			return (Serializable) valueMap.get(ID);
		}

		return handleAsChangeSet(oldValue, (Map<String, Collection<Serializable>>) value);
	}

	@SuppressWarnings("unchecked")
	private static Serializable handleAsChangeSet(Serializable oldValue,
			Map<String, Collection<Serializable>> changeSet) {
		Collection<Serializable> added = changeSet.getOrDefault(ADD, emptyList());
		if (oldValue == null) {
			return added.isEmpty() ? null : (Serializable) added;
		}

		Set<Serializable> propertyAsSet = new HashSet<>();
		if (!(oldValue instanceof Collection<?>)) {
			propertyAsSet.add(oldValue);
		} else {
			propertyAsSet.addAll((Collection<Serializable>) oldValue);
		}

		propertyAsSet.addAll(added);
		propertyAsSet.removeAll(changeSet.getOrDefault(REMOVE, emptyList()));
		// if there is a value we returning it as list, because we don't have type converter for sets
		return propertyAsSet.isEmpty() ? REMOVED_ALL_VALUES_MARKER : new ArrayList<>(propertyAsSet);
	}
}
