package com.sirma.sep.model.management;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a {@link ModelDefinition} node holding a set of {@link ModelField}.
 *
 * @author Mihail Radkov
 */
public class ModelRegion extends AbstractModelNode<ModelRegion, ModelRegion> {

	private Set<String> fields;

	@JsonIgnore
	private ModelDefinition definitionReference;

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	public Set<String> getFields() {
		if (fields == null) {
			fields = new LinkedHashSet<>();
		}
		return fields;
	}

	public ModelRegion setFields(Set<String> fields) {
		this.fields = fields;
		return this;
	}

	public ModelDefinition getDefinitionReference() {
		return definitionReference;
	}

	public void setDefinitionReference(ModelDefinition definitionReference) {
		this.definitionReference = definitionReference;
	}

	/**
	 * Adds the provided field identifier to the set.
	 *
	 * @param fieldId field identifier
	 * @return the current model reference for chaining
	 */
	public ModelRegion addField(String fieldId) {
		getFields().add(fieldId);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModelRegion that = (ModelRegion) o;
		return Objects.equals(fields, that.fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fields);
	}
}
