package com.sirma.itt.emf.definition.model;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * The Class ControlParamImpl.
 *
 * @author BBonev
 */
public class ControlParamImpl extends MergeableBase<ControlParamImpl> implements Serializable,
		Mergeable<ControlParamImpl>, ControlParam, Cloneable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8820480848797950995L;

	/** The param id. */
	@Tag(1)
	protected String identifier;
	/** The name. */
	@Tag(2)
	protected String name;
	/** The value. */
	@Tag(3)
	protected String value;

	/** The control definition. */
	@Tag(4)
	protected ControlDefinitionImpl controlDefinition;

	/** The control definition. */
	@Tag(5)
	protected ControlDefinitionImpl uiControlDefinition;

	/**
	* {@inheritDoc}
	*/
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
		this.value = value;
	}

	/**
	* {@inheritDoc}
	*/
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
		name = value;
	}

	@Override
	public ControlParamImpl mergeFrom(ControlParamImpl source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		name = MergeHelper.replaceIfNull(name, source.getName());
		value = MergeHelper.replaceIfNull(value, source.getValue());
		return this;
	}

	/**
	* {@inheritDoc}
	*/
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
		this.controlDefinition = controlDefinition;
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
	 * @param uiControlDefinition the uiControlDefinition to set
	 */
	public void setUiControlDefinition(ControlDefinitionImpl uiControlDefinition) {
		this.uiControlDefinition = uiControlDefinition;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlParamImpl [");
		builder.append("name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append(", controlParam=");
		builder.append(controlDefinition != null);
		builder.append(", uiControlParam=");
		builder.append(uiControlDefinition != null);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public ControlParam clone() {
		ControlParamImpl copy = new ControlParamImpl();
		copy.identifier = identifier;
		copy.name = name;
		copy.value = value;
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

}
