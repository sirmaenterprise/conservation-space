package com.sirma.sep.model.management;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Holds definition field model information.
 *
 * @author Mihail Radkov
 */
public class ModelField extends AbstractModelNode<ModelField, ModelDefinition> implements Copyable<ModelField> {

	static final String MODEL_TYPE = "field";

	private Map<String, ModelControl> controls;

	/**
	 * Creates a path node to field identified by the given id.
	 *
	 * @param fieldId the field id to use for the path creation
	 * @return the path that matches the given field id.
	 */
	public static Path createPath(String fieldId) {
		return Path.create(MODEL_TYPE, fieldId);
	}

	@JsonIgnore
	public String getUri() {
		return findAttributeValue(DefinitionModelAttributes.URI);
	}

	public ModelField setUri(String uri) {
		addAttribute(DefinitionModelAttributes.URI, uri);
		return this;
	}

	@JsonIgnore
	public String getValue() {
		return findAttributeValue(DefinitionModelAttributes.VALUE);
	}

	public ModelField setValue(String value) {
		addAttribute(DefinitionModelAttributes.VALUE, value);
		return this;
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	/**
	 * Retrieves the {@link DefinitionModelAttributes#REGION_ID} attribute in the current field (if any). This will follow the parent
	 * hierarchy if the current field lacks such attribute.
	 *
	 * @return the region ID or <code>null</code> if none is present in the whole hierarchy
	 */
	public String findRegionId() {
		return findAttributeValue(DefinitionModelAttributes.REGION_ID);
	}

	/**
	 * Retrieves the {@link DefinitionModelAttributes#REGION_ID} attribute in the current field (if any).
	 * <p>
	 * This will <b>NOT</b> follow the parent hierarchy if the current field lacks such attribute.
	 *
	 * @return the region ID or <code>null</code> if none is present in the current hierarchy
	 */
	public String getRegionId() {
		return getAttributeValue(DefinitionModelAttributes.REGION_ID);
	}

	/**
	 * Change the region id of the current field. <ul>
	 * <li>If the value is non null then it will override the current region identifier of the field and effectively move the field to that region.</li>
	 * <li>If the value is null then the region identifier will be removed and the field moved out of any region</li>
	 * </ul>
	 *
	 * @param regionId the region identifier to set or null to remove any value set
	 */
	public void setRegionId(String regionId) {
		if (regionId == null) {
			removeAttribute(DefinitionModelAttributes.REGION_ID);
		} else {
			addAttribute(DefinitionModelAttributes.REGION_ID, regionId);
		}
	}

	/**
	 * Returns if the current field is in region by checking if it has {@link DefinitionModelAttributes#REGION_ID} attribute.
	 *
	 * @return true if the field has assigned region attribute or false if not
	 */
	public boolean hasRegionId() {
		return hasAttribute(DefinitionModelAttributes.REGION_ID);
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o != null && getClass() == o.getClass() && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), 41);
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getFieldsMapping();
	}

	@Override
	public ModelField createCopy() {
		ModelField copyOfModelField = copyNodeTo(new ModelField());
		getControls().stream().map(ModelControl::createCopy).forEach(copyOfModelField::addControl);
		return copyOfModelField;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelField>> getRemoveFunction() {
		return getContext()::removeField;
	}

	@Override
	public Object walk(Path step) {
		if (ModelControl.MODEL_TYPE.equals(step.getName())) {
			return step.proceed(getOrCreateControl(step.getValue()));
		}
		return super.walk(step);
	}

	private ModelControl getOrCreateControl(String id) {
		return getControlsMap().computeIfAbsent(id, this::createControl);
	}

	private ModelControl createControl(String id) {
		ModelControl controlModel = new ModelControl();
		controlModel.setId(id);
		controlModel.setModelsMetaInfo(getModelsMetaInfo());
		controlModel.setContext(this);
		controlModel.setDetachedModelNodesStore(getDetachedModelNodesStore());
		return controlModel;
	}

	public void setControls(Map<String, ModelControl> controls) {
		this.controls = controls;
	}

	public Collection<ModelControl> getControls() {
		return getControlsMap().values();
	}

	public Map<String, ModelControl> getControlsMap() {
		if (controls == null) {
			controls = new LinkedHashMap<>();
		}
		return controls;
	}

	public ModelControl addControl(ModelControl control) {
		control.setContext(this);
		control.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(control);
		return getControlsMap().put(control.getId(), control);
	}

	public Optional<ModelControl> removeControl(String controlId) {
		ModelControl removedControl = this.getControlsMap().remove(controlId);
		getDetachedModelNodesStore().addDetached(removedControl);
		return Optional.ofNullable(removedControl);
	}
}
