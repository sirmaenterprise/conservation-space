package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.semantic.queries.QueryBuilder;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Utility class that has methods for reading/converting properties results from semantic queries to
 * internal model.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class SemanticPropertiesReadConverter extends BasePropertiesConverter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(SemanticPropertiesReadConverter.class);

	/**
	 * Builds the query result model from the given tuple result. The methods reads result
	 * projection and builds a mapping by URI and mapping of properties for each returned URI.
	 * <p>
	 * Expected projection keys:
	 * <ul>
	 * <li> {@link QueryBuilder#URI}
	 * <li> {@link QueryBuilder#PROPERTY_NAME}
	 * <li> {@link QueryBuilder#PROPERTY_VALUE}
	 * <li> {@link QueryBuilder#PARENT} (optional)
	 * <li> {@link QueryBuilder#PARENT_TYPE} (optional)
	 * </ul>
	 * *
	 * 
	 * @param tupleQueryResult
	 *            the tuple query result to parse
	 * @return the mapping of URI and all properties information for that URI
	 * @throws QueryEvaluationException
	 *             the query evaluation exception
	 */
	public Map<String, Map<Value, Set<Value>>> buildQueryResultModel(
			TupleQueryResult tupleQueryResult)
			throws QueryEvaluationException {
		Map<String, Map<Value, Set<Value>>> instances = new LinkedHashMap<>(32);

		while (tupleQueryResult.hasNext()) {
			BindingSet row = tupleQueryResult.next();
			Value value = row.getValue(QueryBuilder.URI);
			String instanceUri = null;
			if (value instanceof URI) {
				instanceUri = namespaceRegistryService.getShortUri((URI) value);
			} else {
				instanceUri = value.stringValue();
			}
			Map<Value, Set<Value>> instanceMap = instances.get(instanceUri);

			if (instanceMap == null) {
				instanceMap = new LinkedHashMap<>(25);
				instances.put(instanceUri, instanceMap);
			}

			value = row.getValue(QueryBuilder.PROPERTY_NAME);
			CollectionUtils.addValueToSetMap(instanceMap, value,
					new ValueProxy(row.getValue(QueryBuilder.PROPERTY_VALUE)));

			value = row.getValue(QueryBuilder.PARENT);
			if (value != null) {
				CollectionUtils.addValueToSetMap(instanceMap, EMF.PARENT, value);
			}

			value = row.getValue(QueryBuilder.PARENT_TYPE);
			if (value != null) {
				CollectionUtils.addValueToSetMap(instanceMap, EMF.PARENT_TYPE, value);
			}
		}
		return instances;
	}

	/**
	 * Convert properties names from {@link Value} instance to short URI format. The method performs
	 * additional property filtering. NOTE: this should be updated and some of the hardcoded checks
	 * removed.
	 * 
	 * @param instanceProperties
	 *            the instance properties to updated
	 * @return the updated mapping
	 */
	public Map<String, Set<Value>> convertPropertiesNames(Map<Value, Set<Value>> instanceProperties) {
		Map<String, Set<Value>> convertedKeys = CollectionUtils
				.createLinkedHashMap(instanceProperties.size());

		for (Entry<Value, Set<Value>> entry : instanceProperties.entrySet()) {
			String shortUri = namespaceRegistryService.getShortUri((URI) entry.getKey());
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
		Set<String> processed = new LinkedHashSet<>();

		// if the definition is present we will process according to it otherwise will process the
		// arguments via general logic
		if (definition != null) {
			convertPropertiesByModel(definition, instanceProperties, properties, processed);
			if (definition instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) definition)
						.getRegions()) {
					convertPropertiesByModel(regionDefinition, instanceProperties, properties,
							processed);
				}
			}
		}

		// convert the remaining properties and keep the URIs
		instanceProperties.keySet().removeAll(processed);
		for (Entry<String, Set<Value>> entry : instanceProperties.entrySet()) {
			ArrayList<Serializable> list = new ArrayList<>(entry.getValue().size());
			for (Value value : entry.getValue()) {
				if (value instanceof ValueProxy) {
					value = ((ValueProxy) value).getValue();
				}
				list.add(ValueConverter.convertValue(value));
			}
			if (list.size() == 1) {
				properties.put(entry.getKey(), list.get(0));
			} else if (list.size() > 1) {
				// at some point move it as trace logging
				debug(LOGGER, "Converted multiple values for key: ", entry.getKey(), "=", list);
				// if we have more then one value then we add all to the list
				// NOTE: if this is not expected could lead to many unpredictable exceptions
				properties.put(entry.getKey(), list);
			}
		}
	}

	/**
	 * Convert properties by model.
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
	private void convertPropertiesByModel(DefinitionModel definition,
			Map<String, Set<Value>> instanceProperties, Map<String, Serializable> properties,
			Set<String> processed) {

		for (PropertyDefinition field : definition.getFields()) {
			if (StringUtils.isNotNullOrEmpty(field.getUri())) {
				if (EqualsHelper.nullSafeEquals(field.getUri(),
						DefaultProperties.NOT_USED_PROPERTY_VALUE, false)) {
					continue;
				}
				Set<Value> values = instanceProperties.get(field.getUri());
				if (values == null) {
					// nothing to do more
					continue;
				}

				// convert all values in the set and then we will check if the field is multi a
				// single value. Most of the time the values are going to be with single value.
				ArrayList<Serializable> list = new ArrayList<>(values.size());
				for (Value value : values) {
					Value aValue = value;
					if (aValue instanceof ValueProxy) {
						aValue = ((ValueProxy) aValue).getValue();
					}
					if (aValue != null) {
						Serializable convertValue = ValueConverter.convertValue(aValue);
						// if it's a resource then we should convert it
						if (isResourceField(field)) {
							// convert the resource to in system value
							String id = namespaceRegistryService.getShortUri(convertValue
									.toString());
							com.sirma.itt.emf.resources.model.Resource resource = resourceService
									.getResource(id);
							if (resource != null) {
								convertValue = resource.getIdentifier();
							}
						}
						list.add(convertValue);
					}
				}
				// if we have some valuable data to present then we set it
				if (!list.isEmpty()) {
					if (Boolean.TRUE.equals(field.isMultiValued())) {
						properties.put(field.getIdentifier(), list);
					} else {
						properties.put(field.getIdentifier(), list.get(0));
					}
					processed.add(field.getUri());
				}
			}
		}
	}
}
