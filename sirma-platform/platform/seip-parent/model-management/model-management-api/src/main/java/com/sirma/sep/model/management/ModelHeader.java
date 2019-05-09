package com.sirma.sep.model.management;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Model node holding information about {@link ModelDefinition} header.
 *
 * @author Mihail Radkov
 */
public class ModelHeader extends AbstractModelNode<ModelHeader, ModelDefinition> implements Copyable<ModelHeader> {

	static final String HEADER_TYPE = "header";

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	public ModelHeader createCopy() {
		return copyNodeTo(new ModelHeader());
	}

	@Override
	protected String getTypeName() {
		return HEADER_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelHeader>> getRemoveFunction() {
		return getContext()::removeHeader;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 17;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getHeadersMapping();
	}
}
