package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Control definition implementation.
 *
 * @author BBonev
 */
public class ControlDefinitionImpl extends BaseDefinition<ControlDefinitionImpl> implements
		Serializable, BidirectionalMapping, ControlDefinition, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1950145372218815883L;

	/** The control param. */
	@Tag(1)
	protected List<ControlParam> controlParams = new LinkedList<ControlParam>();

	/** The ui param. */
	@Tag(2)
	protected List<ControlParam> uiParams = new LinkedList<ControlParam>();

	protected transient PathElement parentEntity;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ControlDefinitionImpl mergeFrom(ControlDefinitionImpl source) {
		super.mergeFrom(source);

		controlParams = MergeHelper.mergeLists(MergeHelper.convertToMergable(controlParams),
				MergeHelper.convertToMergable(source.getControlParams()),
				EmfMergeableFactory.CONTROL_PARAM);

		uiParams = MergeHelper.mergeLists(MergeHelper.convertToMergable(uiParams),
				MergeHelper.convertToMergable(source.getUiParams()),
				EmfMergeableFactory.CONTROL_PARAM);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		if ((controlParams != null) && !controlParams.isEmpty()) {
			for (ControlParam param : controlParams) {
				((ControlParamImpl) param).setControlDefinition(this);
			}
		}
		if ((uiParams != null) && !uiParams.isEmpty()) {
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
		this.controlParams = controlParams;
	}

	/**
	* {@inheritDoc}
	*/
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
		this.uiParams = uiParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return getParentEntity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		if (getParentElement() == null) {
			return getIdentifier();
		} else {
			PathElement parentElement = getParentElement();
			if (parentElement instanceof PropertyDefinition) {
				return ((Identity) parentElement).getIdentifier();
			}
			return parentElement.getPath();
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @param parentEntity the parentEntity to set
	 */
	public void setParentEntity(PathElement parentEntity) {
		this.parentEntity = parentEntity;
	}

	@Override
	public ControlDefinition clone() {
		ControlDefinitionImpl definition = new ControlDefinitionImpl();
		definition.setIdentifier(getIdentifier());
		definition.setFields(new LinkedList<PropertyDefinition>());
		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition)
					.cloneProxy();
			definition.getFields().add(clone);
		}
		definition.controlParams = new LinkedList<ControlParam>();
		if (getControlParams() != null) {
			for (ControlParam param : getControlParams()) {
				ControlParam clone = ((ControlParamImpl) param).clone();
				definition.controlParams.add(clone);
			}
		}
		definition.uiParams = new LinkedList<ControlParam>();
		if (getUiParams() != null) {
			for (ControlParam param : getUiParams()) {
				ControlParam clone = ((ControlParamImpl) param).clone();
				definition.uiParams.add(clone);
			}
		}
		return definition;
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

}
