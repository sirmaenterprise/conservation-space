package com.sirma.sep.model.management;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Holds information about a definition model.
 *
 * @author Mihail Radkov
 */
public class ModelDefinition extends AbstractModelNode<ModelDefinition, ModelDefinition> {

	@JsonIgnore
	private List<ModelDefinition> children;

	private List<ModelField> fields;

	private List<ModelRegion> regions;

	public List<ModelDefinition> getChildren() {
		if (children == null) {
			children = new LinkedList<>();
		}
		return children;
	}

	public ModelDefinition setChildren(List<ModelDefinition> children) {
		this.children = children;
		return this;
	}

	@Override
	public ModelDefinition setId(String id) {
		addAttribute(DefinitionModelAttributes.IDENTIFIER, ModelAttributeType.IDENTIFIER, id);
		return super.setId(id);
	}

	public boolean isAbstract() {
		return getAttributeValue(DefinitionModelAttributes.ABSTRACT);
	}

	public ModelDefinition setAbstract(boolean anAbstract) {
		addAttribute(DefinitionModelAttributes.ABSTRACT, ModelAttributeType.BOOLEAN, anAbstract);
		return this;
	}

	public String getRdfType() {
		return getAttributeValue(DefinitionModelAttributes.RDF_TYPE);
	}

	public ModelDefinition setRdfType(String rdfType) {
		addAttribute(DefinitionModelAttributes.RDF_TYPE, ModelAttributeType.URI, rdfType);
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

	public List<ModelField> getFields() {
		return fields;
	}

	public ModelDefinition setFields(List<ModelField> fields) {
		this.fields = fields;
		return this;
	}

	public List<ModelRegion> getRegions() {
		if (regions == null) {
			regions = new LinkedList<>();
		}
		return regions;
	}

	public void setRegions(List<ModelRegion> regions) {
		this.regions = regions;
	}

	@JsonIgnore
	@Override
	public ModelDefinition getParentReference() {
		return super.getParentReference();
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
		ModelDefinition that = (ModelDefinition) o;
		return Objects.equals(fields, that.fields) && Objects.equals(regions, that.regions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fields, regions);
	}

	/**
	 * Tries to find a {@link ModelField} corresponding to the given name either in this model definition or in the parent hierarchy if this
	 * one has a parent.
	 *
	 * @param name the field name to search for
	 * @return the field or {@link Optional#empty()} if there is no such field
	 */
	public Optional<ModelField> getFieldByName(String name) {
		Optional<ModelField> field = fields.stream().filter(f -> f.getId().equals(name)).findFirst();
		if (field.isPresent()) {
			return field;
		}

		if (getParentReference() != null) {
			return getParentReference().getFieldByName(name);
		}

		return Optional.empty();
	}

	public Optional<ModelRegion> getRegionByName(String regionName) {
		Optional<ModelRegion> region = getRegions().stream().filter(r -> r.getId().equals(regionName)).findFirst();
		if (region.isPresent()) {
			return region;
		}

		if (getParentReference() != null) {
			return getParentReference().getRegionByName(regionName);
		}

		return Optional.empty();
	}
}
