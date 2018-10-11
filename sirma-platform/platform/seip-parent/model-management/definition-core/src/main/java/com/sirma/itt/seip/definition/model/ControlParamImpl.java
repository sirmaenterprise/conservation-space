package com.sirma.itt.seip.definition.model;

import java.io.Serializable;

import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The Class ControlParamImpl.
 *
 * @author BBonev
 */
public class ControlParamImpl extends MergeableBase<ControlParamImpl>
		implements Serializable, Mergeable<ControlParamImpl>, ControlParam, Copyable<ControlParamImpl>, JsonRepresentable, Sealable {
	private static final long serialVersionUID = 8820480848797950995L;

	@Tag(1)
	protected String identifier;
	@Tag(2)
	protected String name;
	@Tag(3)
	protected String value;
	@Tag(6)
	protected String type;

	@Tag(4)
	protected ControlDefinitionImpl controlDefinition;
	@Tag(5)
	protected ControlDefinitionImpl uiControlDefinition;

	private boolean sealed;

	@Override
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setValue(String value) {
		if (!isSealed()) {
			this.value = value;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setName(String value) {
		if (!isSealed()) {
			name = value;
		}
	}

	@Override
	public ControlParamImpl mergeFrom(ControlParamImpl source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		name = MergeHelper.replaceIfNull(name, source.getName());
		value = MergeHelper.replaceIfNull(value, source.getValue());
		type = MergeHelper.replaceIfNull(type, source.getType());
		return this;
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return controlDefinition;
	}

	/**
	 * Setter method for controlDefinition.
	 *
	 * @param controlDefinition
	 *            the controlDefinition to set
	 */
	public void setControlDefinition(ControlDefinitionImpl controlDefinition) {
		if (!isSealed()) {
			this.controlDefinition = controlDefinition;
		}
	}

	@Override
	public PathElement getParentElement() {
		return getControlDefinition();
	}

	@Override
	public String getPath() {
		return getName();
	}

	/**
	 * Getter method for uiControlDefinition.
	 *
	 * @return the uiControlDefinition
	 */
	public ControlDefinitionImpl getUiControlDefinition() {
		return uiControlDefinition;
	}

	/**
	 * Setter method for uiControlDefinition.
	 *
	 * @param uiControlDefinition
	 *            the uiControlDefinition to set
	 */
	public void setUiControlDefinition(ControlDefinitionImpl uiControlDefinition) {
		if (!isSealed()) {
			this.uiControlDefinition = uiControlDefinition;
		}
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		if (!isSealed()) {
			this.identifier = identifier;
		}
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlParamImpl [");
		builder.append("name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append(", type=");
		builder.append(type);
		builder.append(", controlParam=");
		builder.append(controlDefinition != null);
		builder.append(", uiControlParam=");
		builder.append(uiControlDefinition != null);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public ControlParamImpl createCopy() {
		ControlParamImpl copy = new ControlParamImpl();
		copy.identifier = identifier;
		copy.name = name;
		copy.value = value;
		copy.type = type;
		return copy;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "value", getValue());
		JsonUtil.addToJson(object, "name", getName());
		JsonUtil.addToJson(object, "type", getType());
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		sealed = true;
	}
}
