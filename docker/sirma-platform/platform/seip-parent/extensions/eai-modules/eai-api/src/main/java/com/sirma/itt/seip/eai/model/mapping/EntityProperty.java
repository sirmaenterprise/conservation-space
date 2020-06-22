package com.sirma.itt.seip.eai.model.mapping;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.SealedModel;

/**
 * The EntityProperty is representation of property that is available during communication process with an external
 * system. It is specific for a particular system and tenant. All relevant data for mapping and converting should be set
 * 
 * @author bbanchev
 */
public class EntityProperty extends SealedModel {

	/**
	 * Holds the mapping data for different scenarios
	 * 
	 * @author bbanchev
	 */
	public enum EntityPropertyMapping {
		/** Mapping of property as data value. */
		AS_DATA;
	}

	private Map<EntityPropertyMapping, String> mappings = new EnumMap<>(EntityPropertyMapping.class);
	private String uri;
	private String title;
	private String propertyId;
	private String type;
	private Integer codelist;
	private boolean mandatory;

	/**
	 * Getter method for title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return a specific mapping for given type. If values is not mapped return null
	 *
	 * @param property
	 *            is the property type to get mapping for
	 * @return the mapping for given key
	 */
	public String getMapping(EntityPropertyMapping property) {
		return mappings.get(property);
	}

	/**
	 * Return mapping as datavalue for property. Shortcut to access {@link EntityPropertyMapping#AS_DATA}
	 * 
	 * @return the property value
	 */
	@JsonIgnore
	public String getDataMapping() {
		return mappings.get(EntityPropertyMapping.AS_DATA);
	}

	/**
	 * Adds new mapping or override the previous mapping for particular {@link EntityPropertyMapping} mappingType
	 * 
	 * @param mappingType
	 *            the mapping usage - one of {@link EntityPropertyMapping}
	 * @param value
	 *            the value to assign to that mapping
	 */
	public void addMapping(EntityPropertyMapping mappingType, String value) {
		if (isSealed()) {
			return;
		}
		mappings.put(mappingType, value);
	}

	/**
	 * Gets all mappings for that property
	 *
	 * @return the map of existing mappings
	 */
	@JsonProperty("externals")
	public Map<EntityPropertyMapping, String> getMappings() {
		return mappings;
	}

	/**
	 * Setter method for title.
	 *
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		if (isSealed()) {
			return;
		}
		this.title = title;
	}

	/**
	 * Getter method for codelist.
	 *
	 * @return the codelist
	 */
	public Integer getCodelist() {
		return codelist;
	}

	/**
	 * Setter method for codelist.
	 *
	 * @param codelist
	 *            the codelist to set
	 */
	public void setCodelist(Integer codelist) {
		if (isSealed()) {
			return;
		}
		this.codelist = codelist;
	}

	/**
	 * Getter method for mandatory.
	 *
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Setter method for mandatory.
	 *
	 * @param mandatory
	 *            the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		if (isSealed()) {
			return;
		}
		this.mandatory = mandatory;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type - as 'an..60'.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		if (isSealed()) {
			return;
		}
		this.type = type;
	}

	/**
	 * Getter method for uri - as in definition 'uri'.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Setter method for uri.
	 *
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		if (isSealed()) {
			return;
		}
		this.uri = uri;
	}

	/**
	 * Getter method for propertyId - as in definition 'name'.
	 *
	 * @return the propertyId
	 */
	public String getPropertyId() {
		return propertyId;
	}

	/**
	 * Setter method for propertyId.
	 *
	 * @param propertyId
	 *            the propertyId to set
	 */
	public void setPropertyId(String propertyId) {
		if (isSealed()) {
			return;
		}
		this.propertyId = propertyId;
	}

	@Override
	public void seal() {
		mappings = Collections.unmodifiableMap(mappings);
		super.seal();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[uri=");
		builder.append(getUri());
		builder.append(", title=");
		builder.append(getTitle());
		builder.append(", mappings=");
		builder.append(mappings);
		builder.append(", codelist=");
		builder.append(getCodelist());
		builder.append(", mandatory=");
		builder.append(isMandatory());
		builder.append(", type=");
		builder.append(getType());
		builder.append("]\n");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappings == null) ? 0 : mappings.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		if (!(obj instanceof EntityProperty)) {
			return false;
		}
		EntityProperty other = (EntityProperty) obj;
		if (mappings == null) {
			if (other.mappings != null) {
				return false;
			}
		} else if (!mappings.equals(other.mappings)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
