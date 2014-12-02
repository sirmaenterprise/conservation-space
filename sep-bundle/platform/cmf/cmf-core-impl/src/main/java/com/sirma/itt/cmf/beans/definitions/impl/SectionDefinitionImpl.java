package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.util.PathHelper;

/**
 * The Class SectionDefinition.
 *
 * @author BBonev
 */
public class SectionDefinitionImpl extends BaseRegionDefinition<SectionDefinitionImpl> implements
		Serializable, BidirectionalMapping, SectionDefinition, Mergeable<SectionDefinitionImpl> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6725453577198776535L;

	/** The document defs. */
	@Tag(1)
	protected List<DocumentDefinitionRef> documentDefinitions = new LinkedList<DocumentDefinitionRef>();

	/** The transitions. */
	@Tag(2)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();

	/** The case def. */
	protected transient CaseDefinitionImpl caseDefinition;

	/** The state transitions. */
	@Tag(3)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();

	/** The workflow ids. */
	@Tag(4)
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<AllowedChildDefinition>();
	/** The purpose. */
	@Tag(5)
	protected String purpose;
	/** The reference id. */
	@Tag(6)
	protected String referenceId;

	/**
	 * Gets the value of the documentDefs property.
	 *
	 * @return the document defs possible object is {@link List
	 *         <DocumentDefinition> }
	 */
	@Override
	public List<DocumentDefinitionRef> getDocumentDefinitions() {
		if (documentDefinitions == null) {
			documentDefinitions = new LinkedList<DocumentDefinitionRef>();
		}
		return documentDefinitions;
	}

	/**
	 * Sets the value of the documentDefs property.
	 *
	 * @param value
	 *            allowed object is {@link List<DocumentDefinition> }
	 */
	public void setDocumentDefinitions(List<DocumentDefinitionRef> value) {
		documentDefinitions = value;
	}

	/**
	 * Getter method for caseDefinition.
	 *
	 * @return the caseDefinition
	 */
	@Override
	public CaseDefinitionImpl getCaseDefinition() {
		return caseDefinition;
	}

	/**
	 * Setter method for caseDefinition.
	 *
	 * @param caseDefinitionImpl
	 *            the caseDefinition to set
	 */
	public void setCaseDefinition(CaseDefinitionImpl caseDefinitionImpl) {
		caseDefinition = caseDefinitionImpl;
	}

	/**
	 * Inits the bidirection.
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		if (documentDefinitions != null) {
			for (DocumentDefinitionRef definition : documentDefinitions) {
				DocumentDefinitionRefImpl impl = (DocumentDefinitionRefImpl) definition;
				impl.setSectionDefinition(this);
				impl.initBidirection();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SectionDefinition [super=");
		builder.append(super.toString());
		builder.append(", identifier=");
		builder.append(getIdentifier());
		builder.append(", documentDefinitions=");
		builder.append(documentDefinitions);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Gets the parent element.
	 *
	 * @return the parent element
	 */
	@Override
	public PathElement getParentElement() {
		return getCaseDefinition();
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	@SuppressWarnings("unchecked")
	public SectionDefinitionImpl mergeFrom(SectionDefinitionImpl source) {
		super.mergeFrom(source);

		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		referenceId = MergeHelper.replaceIfNull(referenceId, source.getReferenceId());

		documentDefinitions = MergeHelper.mergeLists(
				MergeHelper.convertToMergable(documentDefinitions),
				MergeHelper.convertToMergable(source.getDocumentDefinitions()),
				CmfMergeableFactory.DOCUMENT_DEFINITION_REF);

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
	public boolean hasChildren() {
		return !getDocumentDefinitions().isEmpty() || super.hasChildren();
	}

	@Override
	public Node getChild(String name) {
		Node find = PathHelper.find(getDocumentDefinitions(), name);
		if (find == null) {
			find = super.getChild(name);
		}
		return find;
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

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	/**
	 * Getter method for referenceId.
	 *
	 * @return the referenceId
	 */
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Setter method for referenceId.
	 *
	 * @param referenceId the referenceId to set
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
}
