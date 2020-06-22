package com.sirma.sep.model.management;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Holds definition information of control parameters. Control parameters are description of different filters which
 * user can apply on a field.
 *
 * @author Stella Djulgerova.
 */
public class ModelControlParam extends AbstractModelNode<ModelControlParam, ModelControl> implements Copyable<ModelControlParam> {

	static final String MODEL_TYPE = "controlParam";

	@Override
	public ModelControlParam createCopy() {
		return copyNodeTo(new ModelControlParam());
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelControlParam>> getRemoveFunction() {
		return getContext()::removeControlParam;
	}

	@Override
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getControlParamsMapping();
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 29;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
