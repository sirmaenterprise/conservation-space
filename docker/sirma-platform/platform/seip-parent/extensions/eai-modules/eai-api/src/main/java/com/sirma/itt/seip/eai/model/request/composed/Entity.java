package com.sirma.itt.seip.eai.model.request.composed;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.request.DynamicProperties;

/**
 * Represent a top model configuration (specific object type) with its properties
 * 
 * @author bbanchev
 */
public class Entity implements Serializable {
	private static final long serialVersionUID = -3412245337980527770L;
	@JsonProperty(value = "name")
	private String name;
	@JsonProperty(value = "relations")
	private List<DynamicProperties> relations;
	@JsonProperty(value = "properties")
	private List<DynamicProperties> properties;

	/**
	 * Getter method for name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method for name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter method for relations.
	 *
	 * @return the relations
	 */
	public List<DynamicProperties> getRelations() {
		return relations;
	}

	/**
	 * Setter method for relations.
	 *
	 * @param relations
	 *            the relations to set
	 */
	public void setRelations(List<DynamicProperties> relations) {
		this.relations = relations;
	}

	/**
	 * Getter method for properties.
	 *
	 * @return the properties
	 */
	public List<DynamicProperties> getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 *
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(List<DynamicProperties> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((relations == null) ? 0 : relations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Entity))
			return false;
		Entity other = (Entity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (relations == null) {
			if (other.relations != null)
				return false;
		} else if (!relations.equals(other.relations))
			return false;
		return true;
	}

}
