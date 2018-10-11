package com.sirma.itt.seip.instance.validation;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This is a a POJO that is used to pass the necessary information for specific
 * field validation.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class FieldValidationContext {

	// Fields related data required for the validation
	private PropertyDefinition propertyDefinition;
	private Serializable value;
	private Instance instance;

	// Other validation related data that is not field specific
	/**
	 * A list with all mandatory fields, no matter if they are calculated with
	 * conditions or not.
	 */
	private Set<String> mandatoryFields;

	/**
	 * A set with all optional fields calculated with conditions.
	 */
	private Set<String> optionalFields;

	/**
	 * List of codelist filters, for more information see
	 * {@link DynamicCodeListFilter} comment
	 */
	private Map<String, DynamicCodeListFilter> dynamicClFilters;

	/**
	 * Copies an existing context. This is used when working in multithreaded
	 * environment so we won't have concurrency issues. We copy the values only
	 * from dynamicClFilters and mandatoryFields. This is done, because those
	 * fields are needed for all field validations.
	 *
	 * @return a copied object with only ser mandatory fields and dynamic cl
	 *         filters.
	 */
	public FieldValidationContext copy() {
		return new FieldValidationContext().setDynamicClFilters(dynamicClFilters).setMandatoryFields(mandatoryFields)
				.setOptionalFields(optionalFields);
	}

	public Set<String> getMandatoryFields() {
		return mandatoryFields;
	}

	public Set<String> getOptionalFields() {
		return optionalFields;
	}

	public FieldValidationContext setMandatoryFields(Set<String> mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
		return this;
	}

	public FieldValidationContext setOptionalFields(Set<String> optionalFields) {
		this.optionalFields = optionalFields;
		return this;
	}

	public Map<String, DynamicCodeListFilter> getDynamicClFilters() {
		return dynamicClFilters;
	}

	public FieldValidationContext setDynamicClFilters(Map<String, DynamicCodeListFilter> dynamicClFilters) {
		this.dynamicClFilters = dynamicClFilters;
		return this;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	public FieldValidationContext setPropertyDefinition(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
		return this;
	}

	public Serializable getValue() {
		return value;
	}

	public FieldValidationContext setValue(Serializable value) {
		this.value = value;
		return this;
	}

	public Instance getInstance() {
		return instance;
	}

	public FieldValidationContext setInstance(Instance instance) {
		this.instance = instance;
		return this;
	}

}
