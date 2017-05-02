package com.sirma.itt.emf.solr.schema.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * The AbstractTypeSchemaEntry is base class for configurable entries as types or fields
 *
 * @author bbanchev
 */
public abstract class AbstractTypeSchemaEntry extends AbstractSchemaEntry {

	/** The configs. */
	protected Map<?, ?> configs;

	/** The type. */
	protected String type;

	/**
	 * Instantiates a new abstract type schema entry.
	 *
	 * @param operation
	 *            the operation
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param configs
	 *            the configs
	 */
	protected AbstractTypeSchemaEntry(SolrSchemaMode operation, String name, String type, Map<?, ?> configs) {
		super(operation, name);
		this.type = type;
		this.configs = configs;
	}

	/**
	 * Gets the model internal.
	 *
	 * @param baseModel
	 *            the base model is used as preferable configuration provider if mode is different than delete
	 * @return the model internal build as json string
	 */
	protected String getModelInternal(Map<?, ?> baseModel) {
		Map<Object, Object> typeModel = null;
		if (operation == SolrSchemaMode.DELETE) {
			typeModel = new HashMap<>(1);
		} else {
			typeModel = new HashMap<>(configs);
			typeModel.putAll(baseModel);
		}
		typeModel.put("name", name);
		return new JSONObject(typeModel).toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getModelInternal() {
		return getModelInternal(Collections.singletonMap("type", type));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (configs == null ? 0 : configs.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractTypeSchemaEntry other = (AbstractTypeSchemaEntry) obj;
		if (configs == null) {
			if (other.configs != null) {
				return false;
			}
		} else if (!configs.equals(other.configs)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
