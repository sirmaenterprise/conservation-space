package com.sirma.itt.emf.solr.schema.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The AbstractSchemaEntry is base implementation for schema entry object.
 *
 * @author bbanchev
 */
public abstract class AbstractSchemaEntry implements SchemaEntry {

	protected SolrSchemaMode operation;

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

	@Override
	public String build() {
		if (operation == SolrSchemaMode.READONLY) {
			return getModelInternal();
		}
		return "\"" + getOperationId() + "\":" + getModelInternal();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (name == null ? 0 : name.hashCode());
		result = PRIME * result + (operation == null ? 0 : operation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractSchemaEntry)) {
			return false;
		}
		AbstractSchemaEntry other = (AbstractSchemaEntry) obj;
		return nullSafeEquals(name, other.name) && operation == other.operation;
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
