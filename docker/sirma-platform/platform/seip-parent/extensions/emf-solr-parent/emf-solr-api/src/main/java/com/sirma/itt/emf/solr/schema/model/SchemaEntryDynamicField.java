package com.sirma.itt.emf.solr.schema.model;

import java.util.Map;

/**
 * The SchemaEntryDynamicField represents dynamicField entry.
 *
 * @author bbanchev
 */
public class SchemaEntryDynamicField extends AbstractTypeSchemaEntry {

	/**
	 * Instantiates a new schema entry dynamic field.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param configs
	 *            the configs
	 */
	public SchemaEntryDynamicField(String name, String type, Map<?, ?> configs) {
		super(SolrSchemaMode.READONLY, name, type, configs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getOperationId() {
		return operation.toString().toLowerCase() + "-dynamic-field";
	}

	@Override
	public SchemaEntry createCopy() {
		return new SchemaEntryDynamicField(name, type, configs);
	}
}
