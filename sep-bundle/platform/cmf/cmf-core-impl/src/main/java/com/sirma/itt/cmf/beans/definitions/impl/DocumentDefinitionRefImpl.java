package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Concrete document definition that is part of a case
 *
 * @author BBonev
 */
public class DocumentDefinitionRefImpl extends BaseRegionDefinition<DocumentDefinitionRefImpl>
		implements Serializable, DocumentDefinitionRef, BidirectionalMapping,
		Mergeable<DocumentDefinitionRefImpl> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3126262088423834159L;

	/** The mandatory. */
	@Tag(1)
	protected Boolean mandatory;

	/** The reference id. */
	@Tag(2)
	protected String referenceId;

	/** The section def. */
	protected transient SectionDefinitionImpl sectionDefinition;

	/** The document definition - used only for loading/compiling definitions. */
	protected transient DocumentDefinitionImpl documentDefinition;

	/** The max instances. */
	@Tag(3)
	protected Integer maxInstances;

	/** The structured. */
	@Tag(4)
	protected Boolean structured = Boolean.FALSE;

	/** The purpose. */
	@Tag(5)
	protected String purpose;

	/**
	 * The document definition id. The ID that identifies the different document definitions in one
	 * section
	 */
	@Tag(6)
	protected String identifier;

	/** The parent path. */
	@Tag(7)
	protected String parentPath;

	/** The revision. */
	@Tag(8)
	protected Long revision;

	/** The transitions. */
	@Tag(9)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();

	/** The state transitions. */
	@Tag(10)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();

	/** The allowed children. */
	@Tag(11)
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<AllowedChildDefinition>();

	/**
	 * Getter method for multiple.
	 *
	 * @return the multiple
	 */
	@Override
	public boolean isMultiple() {
		return (getMaxInstances() == null)
				|| ((getMaxInstances() != 1) && (getMaxInstances() != 0));
	}

	/**
	 * Getter method for mandatory.
	 *
	 * @return the mandatory
	 */
	@Override
	public Boolean getMandatory() {
		return mandatory;
	}

	/**
	 * Setter method for mandatory.
	 *
	 * @param mandatory
	 *            the mandatory to set
	 */
	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * Getter method for referenceId.
	 *
	 * @return the referenceId
	 */
	@Override
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Setter method for referenceId.
	 *
	 * @param referenceId
	 *            the referenceId to set
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	/**
	 * Getter method for sectionDefinition.
	 *
	 * @return the sectionDefinition
	 */
	@Override
	public SectionDefinition getSectionDefinition() {
		return sectionDefinition;
	}

	/**
	 * Setter method for sectionDefinition.
	 *
	 * @param sectionDefinitionImpl
	 *            the sectionDefinition to set
	 */
	public void setSectionDefinition(SectionDefinitionImpl sectionDefinitionImpl) {
		sectionDefinition = sectionDefinitionImpl;
	}

	/**
	 * Getter method for documentDefinition.
	 *
	 * @return the documentDefinition
	 */
	public DocumentDefinitionTemplate getDocumentDefinition() {
		return documentDefinition;
	}

	/**
	 * Setter method for documentDefinition.
	 *
	 * @param documentDefinitionImpl
	 *            the documentDefinition to set
	 */
	public void setDocumentDefinition(DocumentDefinitionImpl documentDefinitionImpl) {
		documentDefinition = documentDefinitionImpl;
		mergeTemplate(documentDefinitionImpl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return getSectionDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentDefinitionRef [identifier=");
		builder.append(identifier);
		builder.append(", multiple=");
		builder.append(isMultiple());
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", mandatory=");
		builder.append(mandatory);
		builder.append(", structured=");
		builder.append(structured);
		builder.append(", maxInstances=");
		builder.append(maxInstances);
		builder.append(", parentPath=");
		builder.append(parentPath);
		builder.append(", referenceId=");
		builder.append(referenceId);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append(", documentDefinition=");
		builder.append(documentDefinition);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DocumentDefinitionRefImpl mergeFrom(DocumentDefinitionRefImpl source) {
		mandatory = MergeHelper.replaceIfNull(mandatory, source.getMandatory());
		referenceId = MergeHelper.replaceIfNull(referenceId, source.getReferenceId());
		maxInstances = MergeHelper.replaceIfNull(maxInstances, source.getMaxInstances());
		structured = MergeHelper.replaceIfNull(structured, source.getStructured());
		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		setExpression(MergeHelper.replaceIfNull(getExpression(), source.getExpression()));

		// merge incoming properties on this level
		// first we copy/merge the fields from the parent overridden definition
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(super.getFields()),
				MergeHelper.convertToMergable(source.getFields()));

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

	/**
	 * Merge template definition to the local definition. The method copies the properties from the
	 * template to the local level
	 *
	 * @param documentDefinitionImpl
	 *            the document definition impl
	 */
	private void mergeTemplate(DocumentDefinitionImpl documentDefinitionImpl) {
		if (documentDefinitionImpl != null) {
			setExpression(MergeHelper.replaceIfNull(getExpression(),
					documentDefinitionImpl.getExpression()));

			MergeHelper.mergeTemplate(this, documentDefinitionImpl);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getMaxInstances() {
		return maxInstances;
	}

	/**
	 * Setter method for maxInstances.
	 *
	 * @param maxInstances
	 *            the maxInstances to set
	 */
	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
	}

	/**
	 * Getter method for structured.
	 *
	 * @return the structured
	 */
	@Override
	public Boolean getStructured() {
		return structured;
	}

	/**
	 * Setter method for structured.
	 *
	 * @param structured
	 *            the structured to set
	 */
	public void setStructured(Boolean structured) {
		this.structured = structured;
	}

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		super.setIdentifier(identifier);
	}

	/**
	 * Getter method for parentPath.
	 *
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Setter method for parentPath.
	 *
	 * @param parentPath
	 *            the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(Long revision) {
		this.revision = revision;
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
