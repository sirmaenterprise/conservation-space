package com.sirma.itt.seip.definition.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Control definition implementation.
 *
 * @author BBonev
 */
public class ControlDefinitionImpl extends BaseDefinition<ControlDefinitionImpl>
		implements ControlDefinition, Copyable<ControlDefinition> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1950145372218815883L;

	/** The control param. */
	@Tag(1)
	protected List<ControlParam> controlParams = new LinkedList<>();

	/** The ui param. */
	@Tag(2)
	protected List<ControlParam> uiParams = new LinkedList<>();

	protected transient PathElement parentEntity;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ControlDefinitionImpl mergeFrom(ControlDefinitionImpl source) {
		super.mergeFrom(source);

		controlParams = MergeHelper.mergeLists(MergeHelper.convertToMergable(controlParams),
				MergeHelper.convertToMergable(source.getControlParams()), EmfMergeableFactory.CONTROL_PARAM);

		uiParams = MergeHelper.mergeLists(MergeHelper.convertToMergable(uiParams),
				MergeHelper.convertToMergable(source.getUiParams()), EmfMergeableFactory.CONTROL_PARAM);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		if (controlParams != null && !controlParams.isEmpty()) {
			for (ControlParam param : controlParams) {
				((ControlParamImpl) param).setControlDefinition(this);
			}
		}
		if (uiParams != null && !uiParams.isEmpty()) {
			for (ControlParam param : uiParams) {
				((ControlParamImpl) param).setUiControlDefinition(this);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ControlParam> getControlParams() {
		return controlParams;
	}

	/**
	 * Setter method for controlParams.
	 *
	 * @param controlParams
	 *            the controlParams to set
	 */
	public void setControlParams(List<ControlParam> controlParams) {
		if (!isSealed()) {
			this.controlParams = controlParams;
		}
	}

	@Override
	public List<ControlParam> getUiParams() {
		return uiParams;
	}

	/**
	 * Setter method for uiParams.
	 *
	 * @param uiParams
	 *            the uiParams to set
	 */
	public void setUiParams(List<ControlParam> uiParams) {
		if (!isSealed()) {
			this.uiParams = uiParams;
		}
	}

	@Override
	public PathElement getParentElement() {
		return getParentEntity();
	}

	@Override
	public String getPath() {
		if (getParentElement() == null) {
			return getIdentifier();
		}
		PathElement parentElement = getParentElement();
		if (parentElement instanceof PropertyDefinition) {
			return ((Identity) parentElement).getIdentifier();
		}
		return parentElement.getPath();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlDefinitionImpl [");
		builder.append("identifier=");
		builder.append(getIdentifier());
		builder.append(", super=");
		builder.append(super.toString());
		builder.append(", controlParams=");
		builder.append(controlParams);
		builder.append(", uiParams=");
		builder.append(uiParams);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for parentEntity.
	 *
	 * @return the parentEntity
	 */
	public PathElement getParentEntity() {
		return parentEntity;
	}

	/**
	 * Setter method for parentEntity.
	 *
	 * @param parentEntity
	 *            the parentEntity to set
	 */
	public void setParentEntity(PathElement parentEntity) {
		if (!isSealed()) {
			this.parentEntity = parentEntity;
		}
	}

	@Override
	public ControlDefinition createCopy() {
		ControlDefinitionImpl copy = new ControlDefinitionImpl();
		copy.setIdentifier(getIdentifier());
		copy.setFields(new LinkedList<PropertyDefinition>());
		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition).cloneProxy();
			copy.getFields().add(clone);
		}
		DefinitionUtil.sort(copy.getFields());

		copy.controlParams = new LinkedList<>();
		if (getControlParams() != null) {
			for (ControlParam param : getControlParams()) {
				ControlParam clone = ((ControlParamImpl) param).createCopy();
				copy.controlParams.add(clone);
			}
		}
		copy.uiParams = new LinkedList<>();
		if (getUiParams() != null) {
			for (ControlParam param : getUiParams()) {
				ControlParam clone = ((ControlParamImpl) param).createCopy();
				copy.uiParams.add(clone);
			}
		}
		return copy;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || !getControlParams().isEmpty() || !getUiParams().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		Node node = super.getChild(name);
		if (node == null) {
			node = PathHelper.find(getControlParams(), name);
			if (node == null) {
				node = PathHelper.find(getUiParams(), name);
			}
		}
		return node;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		Collection<JSONObject> fieldsData = TypeConverterUtil.getConverter().convert(JSONObject.class, getFields());
		JsonUtil.addToJson(object, "fields", new JSONArray(fieldsData));
		Collection<JSONObject> controlParamsData = TypeConverterUtil.getConverter().convert(JSONObject.class,
				getControlParams());
		JsonUtil.addToJson(object, "controlParams", new JSONArray(controlParamsData));
		Collection<JSONObject> uiPramData = TypeConverterUtil.getConverter().convert(JSONObject.class, getUiParams());
		JsonUtil.addToJson(object, "uiPram", new JSONArray(uiPramData));
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	@Override
	public void seal() {
		if (isSealed()) {
			// nothing to do
			return;
		}
		super.seal();
		controlParams = Collections.unmodifiableList(Sealable.seal(getControlParams()));
		uiParams = Collections.unmodifiableList(Sealable.seal(getUiParams()));
	}

}
