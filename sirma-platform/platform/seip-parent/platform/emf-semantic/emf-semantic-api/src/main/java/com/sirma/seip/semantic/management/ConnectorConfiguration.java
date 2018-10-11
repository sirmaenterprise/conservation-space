package com.sirma.seip.semantic.management;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.Properties;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.semantic.model.vocabulary.Connectors;

/**
 * Configuration for connector creation or recreation in semantic database if supported.
 *
 * @author kirq4e
 */
public class ConnectorConfiguration implements Properties {

	public static final String IS_DEFAULT_CONNECTOR = Connectors.PREFIX + URI_SEPARATOR
			+ Connectors.IS_DEFAULT_CONNECTOR.getLocalName();
	public static final String CONNECTOR_NAME = Connectors.PREFIX + URI_SEPARATOR
			+ Connectors.CONNECTOR_NAME.getLocalName();
	public static final String ADDRESS = Connectors.PREFIX + URI_SEPARATOR + Connectors.ADDRESS.getLocalName();
	public static final String RECREATE = Connectors.PREFIX + URI_SEPARATOR + Connectors.RECREATE.getLocalName();

	private String id;
	private Map<String, Serializable> properties;
	private Map<String, ConnectorField> fields;

	/**
	 * Instantiates a new solr core configuration.
	 */
	public ConnectorConfiguration() {
		// default constructor
	}

	/**
	 * Instantiates a new solr core configuration.
	 * 
	 * @param connectorName
	 *            Name of the connector
	 */
	public ConnectorConfiguration(String connectorName) {
		add(CONNECTOR_NAME, connectorName);
	}

	/**
	 * Instantiates a new solr core configuration.
	 *
	 * @param connectorName
	 *            the connector name
	 * @param address
	 *            the solr server address
	 */
	public ConnectorConfiguration(String connectorName, String address) {
		add(CONNECTOR_NAME, connectorName);
		add(ADDRESS, address);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the solr server address.
	 *
	 * @return the solr server address
	 */
	public String getAddress() {
		return getAsString(ADDRESS);
	}

	/**
	 * Sets the solr server address
	 *
	 * @param address
	 *            the solr server address
	 */
	public void setAddress(String address) {
		add(ADDRESS, address);
	}

	/**
	 * Gets the connector name.
	 *
	 * @return the connector name
	 */
	public String getConnectorName() {
		return getAsString(CONNECTOR_NAME);
	}

	/**
	 * Sets the connector name.
	 *
	 * @param connectorName
	 *            the new connector name
	 */
	public void setConnectorName(String connectorName) {
		add(CONNECTOR_NAME, connectorName);
	}

	/**
	 * @return the isDefaultConnector
	 */
	public boolean isDefaultConnector() {
		return getBoolean(IS_DEFAULT_CONNECTOR);
	}

	/**
	 * @param isDefaultConnector
	 *            the isDefaultConnector to set
	 */
	public void setDefaultConnector(boolean isDefaultConnector) {
		add(IS_DEFAULT_CONNECTOR, isDefaultConnector);
	}

	/**
	 * @return the recreate flag
	 */
	public boolean getRecreate() {
		return getBoolean(RECREATE);
	}

	/**
	 * @param recreate
	 *            the isDefault to set
	 */
	public void setRecreate(boolean recreate) {
		add(RECREATE, recreate);
	}

	/**
	 * @return the properties
	 */
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	/**
	 * @return the fields
	 */
	public Map<String, ConnectorField> getFields() {
		if (fields == null) {
			fields = CollectionUtils.createHashMap(5);
		}
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(Map<String, ConnectorField> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConnectorConfiguration [id=");
		builder.append(id);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", fields=");
		builder.append(fields);
		builder.append("]");
		return builder.toString();
	}

	public static class ConnectorField {
		private Uri id;
		private String type;
		private List<String> descriptions;
		private boolean isSortable;

		/**
		 * @return the id
		 */
		public Uri getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(Uri id) {
			this.id = id;
		}

		/**
		 * @return the description
		 */
		public List<String> getDescriptions() {
			if (descriptions == null) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(descriptions);
		}

		/**
		 * Adds description to the field.
		 * 
		 * @param description
		 * @return
		 */
		public boolean addDescription(String description) {
			if (descriptions == null) {
				descriptions = new ArrayList<>();
			}
			return descriptions.add(description);
		}

		/**
		 * @return the isSortable
		 */
		public Boolean getIsSortable() {
			return isSortable;
		}

		/**
		 * @param isSortable
		 *            the isSortable to set
		 */
		public void setIsSortable(Boolean isSortable) {
			this.isSortable = isSortable;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type
		 *            the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConnectorField [id=");
			builder.append(id);
			builder.append(", type=");
			builder.append(type);
			builder.append(", description=");
			builder.append(descriptions);
			builder.append(", isSortable=");
			builder.append(isSortable);
			builder.append("]");
			return builder.toString();
		}
	}

}
