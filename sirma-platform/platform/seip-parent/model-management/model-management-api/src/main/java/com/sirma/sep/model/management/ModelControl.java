package com.sirma.sep.model.management;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Holds definition information of controls. The controls are holders of control parameters or
 * {@link ModelControlParam}s.
 *
 * @author Stella Djulgerova.
 */
public class ModelControl extends AbstractModelNode<ModelControl, ModelField> implements Copyable<ModelControl> {

	static final String MODEL_TYPE = "control";

	private Map<String, ModelControlParam> controlParams;

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	public ModelControl createCopy() {
		ModelControl copyOfModelControl = copyNodeTo(new ModelControl());
		getControlParams().stream().map(ModelControlParam::createCopy).forEach(copyOfModelControl::addControlParam);
		return copyOfModelControl;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 27;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	protected Function<String, Optional<ModelControl>> getRemoveFunction() {
		return getContext()::removeControl;
	}

	@Override
	public Object walk(Path step) {
		if (ModelControlParam.MODEL_TYPE.equals(step.getName())) {
			return step.proceed(getOrCreateControlParam(step.getValue()));
		}
		return super.walk(step);
	}

	@Override
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getControlsMapping();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	public void setControlParams(Map<String, ModelControlParam> controlParams) {
		this.controlParams = controlParams;
	}

	private ModelControlParam getOrCreateControlParam(String id) {
		return getControlParamsMap().computeIfAbsent(id, this::createControlParam);
	}

	private ModelControlParam createControlParam(String id) {
		ModelControlParam controlParamModel = new ModelControlParam();
		controlParamModel.setId(id);
		controlParamModel.setModelsMetaInfo(getModelsMetaInfo());
		controlParamModel.setContext(this);
		controlParamModel.setDetachedModelNodesStore(getDetachedModelNodesStore());
		return controlParamModel;
	}

	public Collection<ModelControlParam> getControlParams() {
		return getControlParamsMap().values();
	}

	public Map<String, ModelControlParam> getControlParamsMap() {
		if (controlParams == null) {
			controlParams = new LinkedHashMap<>();
		}
		return controlParams;
	}

	public ModelControlParam addControlParam(ModelControlParam controlParam) {
		controlParam.setContext(this);
		controlParam.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(controlParam);
		return getControlParamsMap().put(controlParam.getId(), controlParam);
	}

	public Optional<ModelControlParam> removeControlParam(String controlParamId) {
		return Optional.ofNullable(this.getControlParamsMap().remove(controlParamId));
	}
}
