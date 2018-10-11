package com.sirma.sep.model.management;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.Objects;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Holds definition field model information.
 *
 * @author Mihail Radkov
 */
public class ModelField extends AbstractModelNode<ModelField, ModelField> {

	@JsonIgnore
	private ModelDefinition definitionReference;

	private String regionId;

	public ModelDefinition getDefinitionReference() {
		return definitionReference;
	}

	public void setDefinitionReference(ModelDefinition definitionReference) {
		this.definitionReference = definitionReference;
	}

	@JsonIgnore
	public String getUri() {
		return getAttributeValue(DefinitionModelAttributes.URI);
	}

	public ModelField setUri(String uri) {
		addAttribute(DefinitionModelAttributes.URI, ModelAttributeType.URI, uri);
		return this;
	}

	@JsonIgnore
	public String getValue() {
		return getAttributeValue(DefinitionModelAttributes.VALUE);
	}

	public ModelField setValue(String value) {
		addAttribute(DefinitionModelAttributes.VALUE, ModelAttributeType.STRING, value);
		return this;
	}

	@Override
	@JsonIgnore
	public Map<String, String> getLabels() {
		return super.getLabels();
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
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
		ModelField that = (ModelField) o;
		return Objects.equals(regionId, that.regionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), regionId);
	}
}
