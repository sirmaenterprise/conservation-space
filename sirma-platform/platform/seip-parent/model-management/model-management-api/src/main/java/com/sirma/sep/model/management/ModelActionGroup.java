package com.sirma.sep.model.management;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holds definition information of groups. The groups are holders of other groups or {@link ModelAction}s.
 *
 * @author Boyan Tonchev.
 */
public class ModelActionGroup extends AbstractModelNode<ModelActionGroup, ModelDefinition> implements Copyable<ModelActionGroup> {

	static final String MODEL_TYPE = "actionGroup";

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	public ModelActionGroup createCopy() {
		return copyNodeTo(new ModelActionGroup());
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 25;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelActionGroup>> getRemoveFunction() {
		return getContext()::removeActionGroup;
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getActionGroupsMapping();
	}
}
