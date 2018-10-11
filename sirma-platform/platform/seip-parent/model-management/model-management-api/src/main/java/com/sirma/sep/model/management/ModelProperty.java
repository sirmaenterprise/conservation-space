package com.sirma.sep.model.management;

import java.util.Map;

import com.sirma.sep.model.management.semantic.PropertyModelAttributes;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A model node representing semantic property.
 *
 * @author Mihail Radkov
 */
public class ModelProperty extends AbstractModelNode<ModelProperty, ModelProperty> {

	@Override
	@JsonIgnore
	public ModelProperty getParentReference() {
		// Currently properties have no parent information
		return null;
	}

	@Override
	@JsonIgnore
	public String getParent() {
		return super.getParent();
	}

	@Override
	@JsonIgnore
	public Map<String, String> getLabels() {
		return super.getLabels();
	}

	@Override
	protected String getLabelAttributeKey() {
		return PropertyModelAttributes.LABEL;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 11;
	}
}
