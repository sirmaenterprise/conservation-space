package com.sirma.itt.cmf.beans.definitions.impl;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Proxy class to hide the standalone section definition behind the old {@link SectionDefinition}
 * class.
 * 
 * @author BBonev
 */
public class SectionDefinitionProxy implements SectionDefinition {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2732439240845616086L;
	/** The definition. */
	private final GenericDefinition definition;

	/**
	 * Instantiates a new section definition proxy.
	 * 
	 * @param definition
	 *            the definition
	 */
	public SectionDefinitionProxy(GenericDefinition definition) {
		this.definition = definition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		if (definition != null) {
			return definition.getIdentifier();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		if (definition != null) {
			return definition.getIdentifier();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		if (definition != null) {
			definition.setIdentifier(identifier);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RegionDefinition> getRegions() {
		if (definition != null) {
			return definition.getRegions();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PropertyDefinition> getFields() {
		if (definition != null) {
			return definition.getFields();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		if (definition != null) {
			return definition.getRevision();
		}
		return 0L;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getHash() {
		if (definition != null) {
			return definition.getHash();
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHash(Integer hash) {
		if (definition != null) {
			definition.setHash(hash);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StateTransition> getStateTransitions() {
		if (definition != null) {
			return definition.getStateTransitions();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TransitionDefinition> getTransitions() {
		if (definition != null) {
			return definition.getTransitions();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		if (definition != null) {
			return definition.getAllowedChildren();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPurpose() {
		if (definition != null) {
			return definition.getPurpose();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPurpose(String purpose) {
		if (definition != null) {
			definition.setPurpose(purpose);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DocumentDefinitionRef> getDocumentDefinitions() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CaseDefinition getCaseDefinition() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceId() {
		if (definition != null) {
			return definition.getReferenceId();
		}
		return null;
	}

}
