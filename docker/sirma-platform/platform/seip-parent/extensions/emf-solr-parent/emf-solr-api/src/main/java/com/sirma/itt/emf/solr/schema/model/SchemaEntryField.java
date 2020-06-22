package com.sirma.itt.emf.solr.schema.model;

import java.util.Map;

/**
 * The SchemaEntryField represents field entry.
 *
 * @author bbanchev
 */
public class SchemaEntryField extends AbstractTypeSchemaEntry {

	/**
	 * Instantiates a new schema entry field.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param configs
	 *            the configs
	 */
	public SchemaEntryField(String name, String type, Map<?, ?> configs) {
		super(SolrSchemaMode.READONLY, name, type, configs);
	}

	@Override
	protected String getOperationId() {
		return operation.toString().toLowerCase() + "-field";
	}

	@Override
	public SchemaEntry createCopy() {
		return new SchemaEntryField(name, type, configs);
	}

}
