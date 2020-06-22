package com.sirma.sep.model.management;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a {@link ModelDefinition} node holding a set of {@link ModelField}.
 *
 * @author Mihail Radkov
 */
public class ModelRegion extends AbstractModelNode<ModelRegion, ModelDefinition> implements Copyable<ModelRegion> {

	static final String MODEL_TYPE = "region";

	private Set<String> fields;

	/**
	 * Creates a path node to region identified by the given id.
	 *
	 * @param regionId the region id to use for the path creation
	 * @return the path that matches the given region id.
	 */
	public static Path createPath(String regionId) {
		return Path.create(MODEL_TYPE, regionId);
	}

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

	public boolean hasFields() {
		return CollectionUtils.isNotEmpty(getFields());
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

	/**
	 * Removes the given field from the fields set (if it exists)
	 *
	 * @param fieldId the field to remove
	 * @return the current model reference for chaining
	 */
	public ModelRegion removeField(String fieldId) {
		getFields().remove(fieldId);
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

	@Override
	public ModelRegion createCopy() {
		ModelRegion copy = new ModelRegion();
		copyNodeTo(copy);
		copy.setFields(new HashSet<>(getFields()));
		return copy;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelRegion>> getRemoveFunction() {
		return getContext()::removeRegion;
	}

	/**
	 * Checks if the region is `empty` in the terms of fields and attributes.
	 *
	 * @return true if the region does not have any fields and any attributes
	 */
	@Override
	@JsonIgnore
	public boolean isEmpty() {
		return !hasFields() && !hasAttributes();
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getRegionsMapping();
	}
}
