package com.sirma.itt.seip.domain.instance;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;

/**
 * Implementation of the {@link Instance} interface that realize NULL pattern.
 *
 * @author BBonev
 */
public class NullInstance implements Instance {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8999258102795706915L;
	/** The Constant INSTANCE. */
	public static final Instance INSTANCE = new NullInstance();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, Serializable> properties) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getId() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Serializable id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InstanceReference toReference() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDeleted() {
		throw new UnsupportedOperationException();
	}
}
