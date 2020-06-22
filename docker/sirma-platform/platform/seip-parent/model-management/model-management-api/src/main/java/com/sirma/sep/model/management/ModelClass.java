package com.sirma.sep.model.management;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.semantic.ClassModelAttributes;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a model node with information about a semantic class.
 *
 * @author Mihail Radkov
 */
public class ModelClass extends AbstractModelNode<ModelClass, ModelNode> implements Copyable<ModelClass> {

	static final String MODEL_TYPE = "class";

	@JsonIgnore
	private List<ModelClass> children;

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
	@JsonIgnore
	public List<ModelClass> getChildren() {
		if (children == null) {
			children = new LinkedList<>();
		}
		return children;
	}

	@Override
	public void addChild(ModelNode child) {
		if (child instanceof ModelClass) {
			getChildren().add((ModelClass) child);
			((ModelClass) child).setParentReference(this);
		} else {
			throw new IllegalArgumentException(
					"Incompatible child type. Expected " + this.getClass().getSimpleName() + " but got "
							+ child.getClass().getSimpleName());
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	public ModelClass createCopy() {
		return copyNodeTo(new ModelClass());
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getSemanticsMapping();
	}

	@Override
	protected Function<String, Optional<ModelClass>> getRemoveFunction() {
		return null;
	}
}
