package com.sirma.itt.cmf.beans.definitions.impl;

import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.emf.definition.model.BaseDefinition;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * Default implementation for the template definition.
 * 
 * @author BBonev
 */
public class TemplateDefinitionImpl extends BaseDefinition<TemplateDefinitionImpl> implements
		TemplateDefinition, PathElement {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3015343782613271098L;

	/** The dms id. */
	private String dmsId;

	/** The container. */
	private String container;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentDefinitionId() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbstract() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		// no revision support
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

}
