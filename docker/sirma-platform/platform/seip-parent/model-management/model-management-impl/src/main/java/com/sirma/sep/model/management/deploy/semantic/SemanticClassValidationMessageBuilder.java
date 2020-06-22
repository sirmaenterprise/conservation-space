package com.sirma.sep.model.management.deploy.semantic;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

public class SemanticClassValidationMessageBuilder extends ValidationMessageBuilder {

	public static final String MISSING_CLASS_PROPERTY_VALUE = "model.management.deploy.class.property.missing.value";
	public static final String MISSING_CLASS_PROPERTY_LABEL = "model.management.deploy.class.property.missing.label";
	public static final String MISSING_CLASS_PROPERTY_MISMATCH = "model.management.deploy.class.property.value.mismatch";

	private final Instance classInstance;

	public SemanticClassValidationMessageBuilder(Instance classInstance) {
		this.classInstance = classInstance;
	}

	public void missingValue(String propertyId, Object value) {
		warning(getId(), MISSING_CLASS_PROPERTY_VALUE, getId(), propertyId, value.toString());
	}

	public void missingLabel(String propertyId, String lang, String label) {
		warning(getId(), MISSING_CLASS_PROPERTY_LABEL, getId(), propertyId, lang, label);
	}

	public void propertyTypeMismatch(String propertyId) {
		warning(getId(), MISSING_CLASS_PROPERTY_MISMATCH, getId(), propertyId);
	}

	private String getId() {
		return classInstance.getId().toString();
	}

}
