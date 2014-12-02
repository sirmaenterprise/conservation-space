package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Document template definition.
 *
 * @author BBonev
 */
public class DocumentDefinitionImpl extends BaseRegionDefinition<DocumentDefinitionImpl> implements
		Serializable, BidirectionalMapping, Cloneable, DocumentDefinitionTemplate,
		MergeableTopLevelDefinition<DocumentDefinitionImpl> {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6394795359250469107L;

	/** The id. */
	@Tag(1)
	protected String identifier;

	/** The parent. */
	@Tag(2)
	protected String parent;

	/** The container. */
	@Tag(3)
	protected String container;
	/** The transitions. */
	@Tag(4)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();

	/** The state transitions. */
	@Tag(5)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();

	/** The allowed children. */
	@Tag(6)
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<AllowedChildDefinition>();

	/**
	 * Inits the bidirection.
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentDefinition [identifier=");
		builder.append(identifier);
		builder.append(", parent=");
		builder.append(parent);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * Getter method for parent.
	 *
	 * @return the parent
	 */
	@Override
	public String getParent() {
		return parent;
	}

	/**
	 * Setter method for parent.
	 *
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	@Override
	public DocumentDefinitionImpl clone() {
		DocumentDefinitionImpl definition = new DocumentDefinitionImpl();
		definition.identifier = getIdentifier();
		definition.parent = getParent();
		definition.setFields(new LinkedList<PropertyDefinition>());
		definition.container = container;
		for (PropertyDefinition fieldDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) fieldDefinition)
					.cloneProxy();
			definition.getFields().add(clone);
		}
		return definition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DocumentDefinitionImpl mergeFrom(DocumentDefinitionImpl source) {
		super.mergeFrom(source);
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		parent = MergeHelper.replaceIfNull(parent, source.getParent());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		MergeHelper.mergeLists(MergeHelper.convertToMergable(getTransitions()),
				MergeHelper.convertToMergable(source.getTransitions()),
				EmfMergeableFactory.TRANSITION_DEFINITION);
		if (getAllowedChildren().isEmpty() && !source.getAllowedChildren().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getAllowedChildren()),
					MergeHelper.convertToMergable(source.getAllowedChildren()),
					EmfMergeableFactory.ALLOWED_CHILDREN);
		}
		if (getStateTransitions().isEmpty() && !source.getStateTransitions().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getStateTransitions()),
					MergeHelper.convertToMergable(source.getStateTransitions()),
					EmfMergeableFactory.STATE_TRANSITION);
		}
		return this;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		super.setIdentifier(identifier);
	}

	@Override
	public String getDmsId() {
		// not used
		return null;
	}

	@Override
	public void setDmsId(String dmsId) {
		// not used
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public String getParentDefinitionId() {
		return getParent();
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public Long getRevision() {
		return 0L;
	}

	@Override
	public void setRevision(Long revision) {
		// not used
	}

	/**
	 * Getter method for transitions.
	 *
	 * @return the transitions
	 */
	@Override
	public List<TransitionDefinition> getTransitions() {
		if (transitions == null) {
			transitions = new LinkedList<TransitionDefinition>();
		}
		return transitions;
	}

	/**
	 * Setter method for transitions.
	 *
	 * @param transitions
	 *            the transitions to set
	 */
	public void setTransitions(List<TransitionDefinition> transitions) {
		this.transitions = transitions;
	}

	@Override
	public List<StateTransition> getStateTransitions() {
		if (stateTransitions == null) {
			stateTransitions = new LinkedList<StateTransition>();
		}
		return stateTransitions;
	}

	/**
	 * Setter method for stateTransitions.
	 * 
	 * @param stateTransitions
	 *            the stateTransitions to set
	 */
	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		if (allowedChildren == null) {
			allowedChildren = new LinkedList<AllowedChildDefinition>();
		}
		return allowedChildren;
	}

	/**
	 * Setter method for allowedChildren.
	 * 
	 * @param allowedChildren
	 *            the allowedChildren to set
	 */
	public void setAllowedChildren(List<AllowedChildDefinition> allowedChildren) {
		this.allowedChildren = allowedChildren;
	}

}
