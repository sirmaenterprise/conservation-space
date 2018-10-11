package com.sirma.sep.model.management;

import java.util.Map;

import com.sirma.sep.model.management.semantic.ClassModelAttributes;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a model node with information about a semantic class.
 *
 * @author Mihail Radkov
 */
public class ModelClass extends AbstractModelNode<ModelClass, ModelClass> {

	@JsonIgnore
	@Override
	public ModelClass getParentReference() {
		return super.getParentReference();
	}

	@Override
	@JsonIgnore
	public Map<String, String> getLabels() {
		return super.getLabels();
	}

	@Override
	protected String getLabelAttributeKey() {
		return ClassModelAttributes.LABEL;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
