package com.sirma.itt.seip.eai.service.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sirma.itt.seip.eai.model.SealedModel;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The {@link SearchModelConfiguration} represents a sealed model that contains the supported searchable types in
 * external system, the possible form fields and order by fields. In addition each of the field in the form are mapped
 * to {@link ModelConfiguration} properties.
 *
 * @author bbanchev
 */
public class SearchModelConfiguration extends SealedModel {
	private Map<EntitySearchCriterion, EntityProperty> criteriaToProperty = new LinkedHashMap<>();
	private Map<String, EntitySearchCriterion> propertyIdToCriteria = new LinkedHashMap<>();
	private Map<EntitySearchType, List<EntitySearchCriterion>> typesToCriteria = new LinkedHashMap<>();
	private List<EntitySearchType> typesData = Collections.emptyList();
	private List<EntitySearchFormCriterion> formData = Collections.emptyList();
	private List<EntitySearchOrderCriterion> orderData = Collections.emptyList();

	/**
	 * Adds the criterion to the search model.
	 * 
	 * @param type
	 *            is the type associated with this criterion - require non null
	 * @param criterion
	 *            the criterion is the created type - require non null
	 * @param entityProperty
	 *            the mapped property - require non null
	 */
	public void addCriterion(EntitySearchType type, EntitySearchCriterion criterion, EntityProperty entityProperty) {
		Objects.requireNonNull(criterion, "Invalid search/order criterion. Check your model!");
		Objects.requireNonNull(entityProperty,
				"Missing property mapping for search/order criterion. Check your model!");
		typesToCriteria.computeIfAbsent(type, key -> new LinkedList<>()).add(criterion);
		criteriaToProperty.put(criterion, entityProperty);
		propertyIdToCriteria.put(criterion.getPropertyId(), criterion);
	}

	/**
	 * Gets the criterion by internal name.
	 *
	 * @param key
	 *            the key
	 * @return the criterion by internal name
	 */
	public EntitySearchCriterion getCriterionByInternalName(String key) {
		return propertyIdToCriteria.get(key);
	}

	/**
	 * Gets the property by criterion.
	 *
	 * @param criterion
	 *            the criterion
	 * @return the property by criterion
	 */
	public EntityProperty getPropertyByCriteration(EntitySearchCriterion criterion) {
		return criteriaToProperty.get(criterion);
	}

	/**
	 * Gets the types data.
	 *
	 * @return the types data
	 */
	public List<EntitySearchType> getTypesData() {
		return typesData;
	}

	/**
	 * Gets the form data.
	 *
	 * @return the form data
	 */
	public List<EntitySearchFormCriterion> getFormData() {
		return formData;
	}

	/**
	 * Gets the order data.
	 *
	 * @return the order data
	 */
	public List<EntitySearchOrderCriterion> getOrderData() {
		return orderData;
	}

	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}
		typesData = Collections.unmodifiableList(new ArrayList<>(typesToCriteria.keySet()));
		fillModel();
		formData = Collections.unmodifiableList(formData);
		orderData = Collections.unmodifiableList(orderData);
		typesData = Collections.unmodifiableList(typesData);
		super.seal();
	}

	private void fillModel() {
		formData = criteriaToProperty
				.keySet()
					.stream()
					.filter(EntitySearchFormCriterion.class::isInstance)
					.map(EntitySearchFormCriterion.class::cast)
					.filter(criteria -> criteria.getMapping() != null)
					.collect(Collectors.toList());
		orderData = criteriaToProperty
				.keySet()
					.stream()
					.filter(EntitySearchOrderCriterion.class::isInstance)
					.map(EntitySearchOrderCriterion.class::cast)
					.sorted((EntitySearchOrderCriterion o1, EntitySearchOrderCriterion o2) -> EqualsHelper
							.nullCompare(o1.getOrderPosition(), o2.getOrderPosition()))
					.collect(Collectors.toList());
	}

}
