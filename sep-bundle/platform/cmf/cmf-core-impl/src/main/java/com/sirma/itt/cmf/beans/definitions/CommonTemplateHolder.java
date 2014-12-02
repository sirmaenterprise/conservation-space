package com.sirma.itt.cmf.beans.definitions;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * The Class CommonTemplateHolder.
 *
 * @param <E>
 *            the template type
 * @param <T>
 *            the implementation type
 * @author BBonev
 */
public abstract class CommonTemplateHolder<E extends TopLevelDefinition, T extends MergeableTopLevelDefinition<?>>
		implements DefinitionTemplateHolder<E>, MergeableTopLevelDefinition<T> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 404341553705798890L;

	/** The dms id. */
	private String dmsId;

	/** The container. */
	private String container;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PropertyDefinition> getFields() {
		return Collections.emptyList();
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
	public void setIdentifier(String identifier) {
		// not used
	}

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
	public T mergeFrom(T source) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbstract() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		// not used
	}

	@Override
	public Integer getHash() {
		return null;
	}

	@Override
	public void setHash(Integer hash) {
		// not used
	}
}
