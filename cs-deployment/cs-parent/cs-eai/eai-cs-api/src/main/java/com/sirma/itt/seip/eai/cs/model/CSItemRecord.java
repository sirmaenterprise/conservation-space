package com.sirma.itt.seip.eai.cs.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.request.DynamicProperties;

/**
 * Represent an item record. The relations, namespace and classification could be accessed directly, all other
 * properties are set in a dynamic map and could be accessed by {@link #getProperties()}
 * 
 * @author bbanchev
 */
public class CSItemRecord {
	@JsonProperty(value = "references")
	private List<CSItemRelations> relationships;
	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("classification")
	private String classification;
	@JsonIgnore
	private final DynamicProperties properties = new DynamicProperties();

	/**
	 * Getter method for relationships.
	 *
	 * @return the relationships
	 */
	public List<CSItemRelations> getRelationships() {
		return relationships;
	}

	/**
	 * Setter method for relationships.
	 *
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(List<CSItemRelations> relationships) {
		this.relationships = relationships;
	}

	/**
	 * Put any non binded dynamic value.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the to set
	 */
	@JsonAnySetter
	public Object put(String key, Object value) {
		return properties.put(key, value);
	}

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @return the object
	 */
	public Object get(String key) {
		return properties.get(key);
	}

	/**
	 * Gets the dynamic data.
	 *
	 * @return the data
	 */
	public DynamicProperties getProperties() {
		return properties;
	}

	/**
	 * Getter method for namespace.
	 *
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Setter method for namespace.
	 *
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Getter method for classification.
	 *
	 * @return the classification
	 */
	public String getClassification() {
		return classification;
	}

	/**
	 * Setter method for classification.
	 *
	 * @param classification
	 *            the classification to set
	 */
	public void setClassification(String classification) {
		this.classification = classification;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classification == null) ? 0 : classification.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((relationships == null) ? 0 : relationships.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CSItemRecord)) {
			return false;
		}
		CSItemRecord other = (CSItemRecord) obj;
		if (classification == null) {
			if (other.classification != null) {
				return false;
			}
		} else if (!classification.equals(other.classification)) {
			return false;
		}
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!properties.equals(other.properties)) {
			return false;
		}
		if (namespace == null) {
			if (other.namespace != null) {
				return false;
			}
		} else if (!namespace.equals(other.namespace)) {
			return false;
		}
		if (relationships == null) {
			if (other.relationships != null) {
				return false;
			}
		} else if (!relationships.equals(other.relationships)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append(" [namespace=");
		builder.append(namespace);
		builder.append(", classification=");
		builder.append(classification);
		builder.append(", relationships=");
		builder.append(relationships);
		builder.append(", data=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

}
