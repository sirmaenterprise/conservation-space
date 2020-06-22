package com.sirma.sep.model.management;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.semantic.PropertyModelAttributes;

/**
 * A model node representing semantic property.
 *
 * @author Mihail Radkov
 */
public class ModelProperty extends AbstractModelNode<ModelProperty, ModelNode> implements Copyable<ModelProperty> {

	static final String MODEL_TYPE = "property";

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

	@Override
	public ModelProperty createCopy() {
		return copyNodeTo(new ModelProperty());
	}

	@JsonIgnore
	public String getDomain() {
		return getAttributeValue(PropertyModelAttributes.DOMAIN);
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getPropertiesMapping();
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelProperty>> getRemoveFunction() {
		return null;
	}
}
