package com.sirma.itt.emf.solr.schema.model;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The SchemaCopyField represents copyField.
 *
 * @author bbanchev
 */
public class SchemaEntryCopyField extends AbstractSchemaEntry {

	private String[] dest;

	/**
	 * Instantiates a new schema copy field.
	 *
	 * @param name
	 *            the name
	 * @param dest
	 *            the dest
	 */
	public SchemaEntryCopyField(String name, String... dest) {
		this(SolrSchemaMode.READONLY, name, dest);
	}

	/**
	 * Internal constructor - Instantiates a new schema copy field.
	 *
	 * @param operation
	 *            the operation
	 * @param name
	 *            the name
	 * @param dest
	 *            the dest
	 */
	protected SchemaEntryCopyField(SolrSchemaMode operation, String name, String... dest) {
		super(operation, name);
		this.dest = dest;
	}

	@Override
	protected String getModelInternal() {
		if (dest == null) {
			throw new EmfRuntimeException("CopyField operation failed - missing destination" + getId());
		}
		JSONObject typeModel = null;
		switch (operation) {
			case REPLACE:
				return null;
			case DELETE:
			case ADD:
				typeModel = new JSONObject();
				JsonUtil.addToJson(typeModel, "source", name);
				JsonUtil.addToJson(typeModel, "dest", new JSONArray(Arrays.asList(dest)));
				return typeModel.toString();
			default:
				throw new EmfRuntimeException("CopyField unsupported opeartion replace: " + getId());

		}
	}

	@Override
	public String build() {
		if (operation == SolrSchemaMode.REPLACE) {
			return null;
		}
		return super.build();
	}

	@Override
	protected String getOperationId() {
		return operation.toString().toLowerCase() + "-copy-field";
	}

	@Override
	public String getId() {
		return name + Arrays.asList(dest);
	}

	@Override
	public SchemaEntry createCopy() {
		return new SchemaEntryCopyField(name, dest);
	}

}
