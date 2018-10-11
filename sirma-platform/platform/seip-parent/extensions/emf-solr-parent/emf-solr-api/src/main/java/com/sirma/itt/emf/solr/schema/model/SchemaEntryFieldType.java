package com.sirma.itt.emf.solr.schema.model;

import java.util.Collections;
import java.util.Map;

/**
 * The SchemaEntryFieldType represents fieldType entry.
 *
 * @author bbanchev
 */
public class SchemaEntryFieldType extends AbstractTypeSchemaEntry {

	/**
	 * Instantiates a new schema entry field type.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param configs
	 *            the configs
	 */
	public SchemaEntryFieldType(String name, String clazz, Map<?, ?> configs) {
		super(SolrSchemaMode.READONLY, name, clazz, configs);
	}

	@Override
	protected String getModelInternal() {
		return getModelInternal(Collections.singletonMap("clazz", type));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getOperationId() {
		return operation.toString().toLowerCase() + "-field-type";
	}

	@Override
	public SchemaEntry createCopy() {
		return new SchemaEntryFieldType(name, type, configs);
	}

}