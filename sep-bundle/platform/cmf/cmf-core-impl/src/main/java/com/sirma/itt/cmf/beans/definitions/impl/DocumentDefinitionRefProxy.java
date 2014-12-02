package com.sirma.itt.cmf.beans.definitions.impl;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Proxy definition for the {@link DocumentDefinitionTemplate} to look like a
 * {@link DocumentDefinitionRef} so it can be used for document instance creation.<br>
 * REVIEW This probably is a WA but this is the best can do for the time being.
 *
 * @author BBonev
 */
public class DocumentDefinitionRefProxy implements DocumentDefinitionRef {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2183606869909717044L;

	private DocumentDefinitionTemplate template;

	/**
	 * Instantiates a new document definition ref proxy.
	 *
	 * @param template
	 *            the template
	 */
	public DocumentDefinitionRefProxy(DocumentDefinitionTemplate template) {
		this.template = template;
	}

	@Override
	public List<PropertyDefinition> getFields() {
		if (template != null) {
			return template.getFields();
		}
		return Collections.emptyList();
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
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<StateTransition> getStateTransitions() {
		if (template != null) {
			return template.getStateTransitions();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TransitionDefinition> getTransitions() {
		if (template != null) {
			return template.getTransitions();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPurpose(String purpose) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiple() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getMaxInstances() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean getMandatory() {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean getStructured() {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPurpose() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceId() {
		if (template != null) {
			return template.getIdentifier();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SectionDefinition getSectionDefinition() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		if (template != null) {
			return template.hasChildren();
		}
		return false;
	}

	@Override
	public Node getChild(String name) {
		if (template != null) {
			return template.getChild(name);
		}
		return null;
	}

	@Override
	public String getIdentifier() {
		if (template != null) {
			return template.getIdentifier();
		}
		return null;
	}

	@Override
	public void setIdentifier(String identifier) {

	}

	@Override
	public List<RegionDefinition> getRegions() {
		if (template != null) {
			return template.getRegions();
		}
		return Collections.emptyList();
	}

	@Override
	public Long getRevision() {
		if (template != null) {
			return template.getRevision();
		}
		return 0L;
	}

	@Override
	public Integer getHash() {
		if (template != null) {
			return template.getHash();
		}
		return 0;
	}

	@Override
	public void setHash(Integer hash) {

	}

	@Override
	public String getRenderAs() {
		if (template != null) {
			return template.getRenderAs();
		}
		return null;
	}

	@Override
	public String getExpression() {
		if (template != null) {
			return template.getExpression();
		}
		return null;
	}

	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		if (template != null) {
			return template.getAllowedChildren();
		}
		return Collections.emptyList();
	}

}
