package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Utility class that has methods for reading/converting properties results from semantic queries to internal model.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticPropertiesReadConverter extends BasePropertiesConverter {

	public static final String IRI = "uri";
	public static final String PROPERTY_NAME = "propertyName";
	public static final String PROPERTY_VALUE = "propertyValue";
	public static final String PARENT = "parent";
	public static final String PARENT_TYPE = "parentType";

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticPropertiesReadConverter.class);

	/**
	 * Builds the query result model from the given tuple result. The methods reads result projection and builds a
	 * mapping by IRI and mapping of properties for each returned IRI.
	 * <p>
	 * Note that the results will be wrapped in {@link ValueProxy} instance in order for the maps and sets to work
	 * correctly. So when used for equality checks the values should be extracted with
	 * {@link ValueProxy#getValue(Value)}
	 * <p>
	 * For query select ?X ?Y where { ?X a ?Y }<br>
	 * And result:
	 * <table border="1">
	 * <tr>
	 * <td>X</td>
	 * <td>Y</td>
	 * </tr>
	 * <tr>
	 * <td>uri1</td>
	 * <td>class1</td>
	 * </tr>
	 * <tr>
	 * <td>uri2</td>
	 * <td>class3</td>
	 * </tr>
	 * <tr>
	 * <td>uri2</td>
	 * <td>class3</td>
	 * </tr>
	 * </table>
	 * will produce result
	 *
	 * <pre>
	 * uri1 -> {X=uri1, Y=[class1]},
	 * uri2 -> {X=uri2, Y=[class2, class3]}
	 * </pre>
	 *
	 * @param tupleQueryResult
	 *            the tuple query result to parse
	 * @param idKey
	 *            the binding name to be used for grouping the result
	 * @return the mapping of IRI and all bindings values
	 * @throws QueryEvaluationException
	 *             the query evaluation exception
	 */
	@SuppressWarnings("static-method")
	public Map<Value, Map<String, Set<Value>>> buildQueryResultModel(TupleQueryResult tupleQueryResult, String idKey)
			throws QueryEvaluationException {
		Map<Value, Map<String, Set<Value>>> results = new LinkedHashMap<>(32);

		while (tupleQueryResult.hasNext()) {
			BindingSet row = tupleQueryResult.next();
			Value id = row.getValue(idKey);
			if (id == null) {
				LOGGER.warn("Could not process query result row that does not have identifier under key: {}", idKey);
				continue;
			}

			Map<String, Set<Value>> instanceMap = results.computeIfAbsent(ValueProxy.of(id),
					uri -> new LinkedHashMap<>(25));
			for (String binding : row.getBindingNames()) {
				CollectionUtils.addValueToSetMap(instanceMap, binding, ValueProxy.of(row.getValue(binding)));
			}
		}
		return results;
	}

	/**
	 * Builds the query result model from the given tuple result. The methods reads result projection and builds a
	 * mapping by IRI and mapping of properties for each returned IRI.
	 * <p>
	 * Expected projection keys:
	 * <ul>
	 * <li>{@link #IRI}
	 * <li>{@link #PROPERTY_NAME}
	 * <li>{@link #PROPERTY_VALUE}
	 * <li>{@link #PARENT} (optional)
	 * <li>{@link #PARENT_TYPE} (optional)
	 * </ul>
	 * *
	 *
	 * @param tupleQueryResult
	 *            the tuple query result to parse
	 * @return the mapping of IRI and all properties information for that IRI
	 * @throws QueryEvaluationException
	 *             the query evaluation exception
	 */
	public Map<String, Map<Value, Set<Value>>> buildQueryResultModel(TupleQueryResult tupleQueryResult)
			throws QueryEvaluationException {
		Map<String, Map<Value, Set<Value>>> instances = new LinkedHashMap<>(32);

		while (tupleQueryResult.hasNext()) {
			BindingSet row = tupleQueryResult.next();
			Value value = row.getValue(IRI);
			String instanceUri = null;
			if (value instanceof IRI) {
				instanceUri = namespaceRegistryService.getShortUri((IRI) value);
			} else {
				instanceUri = value.stringValue();
			}
			Map<Value, Set<Value>> instanceMap = instances.computeIfAbsent(instanceUri, uri -> new LinkedHashMap<>(25));

			value = row.getValue(PROPERTY_NAME);
			CollectionUtils.addValueToSetMap(instanceMap, value, ValueProxy.of(row.getValue(PROPERTY_VALUE)));

			value = row.getValue(PARENT);
			CollectionUtils.addValueToSetMap(instanceMap, EMF.PARENT, value);

			value = row.getValue(PARENT_TYPE);
			CollectionUtils.addValueToSetMap(instanceMap, EMF.PARENT_TYPE, value);
		}
		return instances;
	}

	/**
	 * Convert properties names from {@link Value} instance to short IRI format. The method performs additional property
	 * filtering. NOTE: this should be updated and some of the hardcoded checks removed.
	 *
	 * @param instanceProperties
	 *            the instance properties to updated
	 * @return the updated mapping
	 */
	public Map<String, Set<Value>> convertPropertiesNames(Map<Value, Set<Value>> instanceProperties) {
		Map<String, Set<Value>> convertedKeys = CollectionUtils.createLinkedHashMap(instanceProperties.size());

		for (Entry<Value, Set<Value>> entry : instanceProperties.entrySet()) {
			String shortUri = namespaceRegistryService.getShortUri((IRI) entry.getKey());
			convertedKeys.put(shortUri, entry.getValue());
		}
		return convertedKeys;
	}

	/**
	 * Convert properties from semantic to internal model using the provided definition instance * *
	 *
	 * @param definition
	 *            the definition to use for properties names conversions
	 * @param instanceProperties
	 *            the instance properties to convert
	 * @param properties
	 *            the properties map to update with the read properties
	 */
	public void convertPropertiesFromSemanticToInternalModel(DefinitionModel definition,
			Map<String, Set<Value>> instanceProperties, Map<String, Serializable> properties) {
		Set<String> processed = CollectionUtils.createHashSet(instanceProperties.size());

		// if the definition is present we will process according to it otherwise will process the
		// arguments via general logic
		if (definition != null) {
			convertModelProperties(definition, instanceProperties, properties, processed);
		}

		// convert the remaining properties and keep the URIs
		instanceProperties.keySet().removeAll(processed);
		convertNonModelProperties(instanceProperties, properties);
	}

	/**
	 * Convert properties that match the provide model.
	 *
	 * @param definition
	 *            the definition
	 * @param instanceProperties
	 *            the instance properties
	 * @param properties
	 *            the properties
	 * @param processed
	 *            the processed
	 */
	private void convertModelProperties(DefinitionModel definition, Map<String, Set<Value>> instanceProperties,
			Map<String, Serializable> properties, Set<String> processed) {
		definition
				.fieldsStream()
					.filter(PropertyDefinition.hasUri())
					// note that the method is stateful and has side effects (modifies the collections)
					.forEach(field -> convertPropertiesByModel(field, instanceProperties, properties, processed));
	}

	/**
	 * Convert non model properties.
	 *
	 * @param instanceProperties
	 *            the instance properties
	 * @param properties
	 *            the properties
	 */
	private void convertNonModelProperties(Map<String, Set<Value>> instanceProperties,
			Map<String, Serializable> properties) {
		for (Entry<String, Set<Value>> entry : instanceProperties.entrySet()) {
			List<Serializable> list = convertValues(entry.getValue());
			if (list.size() == 1) {
				properties.put(entry.getKey(), list.get(0));
			} else if (list.size() > 1) {
				// at some point move it as trace logging
				LOGGER.debug("Converted multiple values for key: {}={}", entry.getKey(), list);
				// if we have more then one value then we add all to the list
				// NOTE: if this is not expected could lead to many unpredictable exceptions
				properties.put(entry.getKey(), (Serializable) list);
			}
		}
	}

	/**
	 * Gets the actual value.
	 *
	 * @param value
	 *            the value
	 * @return the actual value
	 */
	private static Value getActualValue(Value value) {
		return ValueProxy.getValue(value);
	}

	/**
	 * Convert properties by model.
	 *
	 * @param instanceProperties
	 *            the instance properties
	 * @param properties
	 *            the properties
	 * @param processed
	 *            the processed
	 */
	private void convertPropertiesByModel(PropertyDefinition field, Map<String, Set<Value>> instanceProperties,
			Map<String, Serializable> properties, Set<String> processed) {

		String uri = PropertyDefinition.resolveUri().apply(field);
		Set<Value> values = instanceProperties.get(uri);
		if (values != null) {
			List<Serializable> list;
			if (isMultiLanguage(values)) {
				// Remove the values without a language.
				values = values.stream().filter(value -> getLanguage(value) != null).collect(Collectors.toSet());
				MultiLanguageValue mValue = new MultiLanguageValue();
				for (Value value : values) {
					// the value should be a string but just in case
					mValue.addValue(getLanguage(value), Objects.toString(convertValue(value), null));
				}
				list = Collections.singletonList(mValue);
			} else {
				list = convertValues(values);
			}
			// if we have some valuable data to present then we set it
			if (addValues(properties, field, list)) {
				processed.add(uri);
			}
		}
	}

	/**
	 * Determines if the values are multilanguage. The values are multilanguage if two values have different languages.
	 *
	 * @param values
	 *            the values
	 * @return true if its multilanguage
	 */
	private static boolean isMultiLanguage(Set<Value> values) {
		for (Value value : values) {
			String valueLanguage = getLanguage(value);
			if (valueLanguage != null) {
				return true;
			}
		}
		return false;
	}

	private static String getLanguage(Value value) {
		if (value instanceof Literal) {
			return ((Literal) value).getLanguage().orElse(null);
		}
		return null;
	}

	/**
	 * Adds the values to the given properties map if there are any values to add.
	 *
	 * @param properties
	 *            the properties
	 * @param field
	 *            the field
	 * @param values
	 *            the values to add
	 * @return true, if the properties map has been modified and false if not.
	 */
	private static boolean addValues(Map<String, Serializable> properties, PropertyDefinition field,
			List<Serializable> values) {
		if (values.isEmpty()) {
			return false;
		}
		if (Boolean.TRUE.equals(field.isMultiValued())) {
			properties.put(field.getIdentifier(), (Serializable) values);
		} else {
			properties.put(field.getIdentifier(), values.get(0));
			if (values.size() > 1) {
				LOGGER.warn(
						"Found more than one value for field [{}] with IRI=[{}] that is defined as non multivalued: {}",
						field.getIdentifier(), field.getUri(), values);
			}
		}
		return true;
	}

	/**
	 * Convert the value and check if its a multi or a single value. Most of the time the
	 * value is going to be with single value.
	 *
	 * @param value
	 *            the value
	 * @return the converted value
	 */
	private Serializable convertValue(Value value){
		Value aValue = getActualValue(value);
		if (aValue != null) {
			boolean isObjectProperty = aValue instanceof IRI;
			Serializable convertValue = ValueConverter.convertValue(aValue);
			if (isObjectProperty && convertValue != null) {
				convertValue = namespaceRegistryService.getShortUri(convertValue.toString());
			}
			return convertValue;
		}
		return null;
	}

	/**
	 * Convert all values in the set and then we will check if the field is multi a single value. Most of the time the
	 * values are going to be with single value.
	 *
	 * @param values
	 *            the values
	 * @return the list
	 */
	private List<Serializable> convertValues(Set<Value> values) {
		List<Serializable> list = new ArrayList<>(values.size());
		for (Value value : values) {
			addNonNullValue(list, convertValue(value));
		}
		return list;
	}
}
