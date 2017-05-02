package com.sirma.itt.emf.solr.schema.model;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The AbstractSchemaEntry is base implementation for schema entry object.
 *
 * @author bbanchev
 */
public abstract class AbstractSchemaEntry implements SchemaEntry {

	/** The operation. */
	protected SolrSchemaMode operation;

	/** The name. */
	protected String name;

	/**
	 * Instantiates a new abstract schema entry.
	 *
	 * @param name
	 *            the name
	 * @param operation
	 *            the operation
	 */
	protected AbstractSchemaEntry(SolrSchemaMode operation, String name) {
		this.name = name;
		if (name == null) {
			throw new EmfRuntimeException("Missing required argument - name");
		}
		this.operation = operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String build() {
		if (operation == SolrSchemaMode.READONLY) {
			return getModelInternal();
		}
		return "\"" + getOperationId() + "\":" + getModelInternal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (operation == null ? 0 : operation.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractSchemaEntry other = (AbstractSchemaEntry) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (operation != other.operation) {
			return false;
		}
		return true;
	}

	/**
	 * Clone.
	 *
	 * @param mode
	 *            the mode
	 * @return the abstract schema entry
	 */
	@Override
	public SchemaEntry clone(SolrSchemaMode mode) {
		AbstractSchemaEntry clone;
		try {
			clone = (AbstractSchemaEntry) this.clone();
			clone.operation = mode;
			return clone;
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

	@Override
	public String getId() {
		return name;
	}

	/**
	 * Gets the model internal - json representation of the configurations
	 *
	 * @return the model internal
	 */
	protected abstract String getModelInternal();

	/**
	 * Gets the operation id.
	 *
	 * @return the operation id
	 */
	protected abstract String getOperationId();

}
