package com.sirma.itt.emf.codelist.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * The Class CodeValue.
 * 
 * @author BBonev
 */
public class CodeValue implements Serializable, PropertyModel, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2827111152991530423L;

	/** The value. */
	private String value;

	/** The codelist. */
	private Integer codelist;

	/** The descriptions. */
	private Map<String, Serializable> descriptions;

	/**
	 * Getter method for value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Setter method for value.
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
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
		this.codelist = codelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		return descriptions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, Serializable> properties) {
		descriptions = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CodeValue [codelist=");
		builder.append(codelist);
		builder.append(", value=");
		builder.append(value);
		builder.append(", descriptions=");
		builder.append(descriptions);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((codelist == null) ? 0 : codelist.hashCode());
		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CodeValue)) {
			return false;
		}
		CodeValue other = (CodeValue) obj;
		if (codelist == null) {
			if (other.codelist != null) {
				return false;
			}
		} else if (!codelist.equals(other.codelist)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public CodeValue clone() {
		CodeValue clone = new CodeValue();
		clone.codelist = codelist;
		clone.value = value;
		clone.descriptions = new LinkedHashMap<>((int) (descriptions.size() * 1.2), 0.95f);
		for (Entry<String, Serializable> entry : descriptions.entrySet()) {
			clone.descriptions.put(entry.getKey(), entry.getValue());
		}
		clone.descriptions = Collections.unmodifiableMap(clone.descriptions);
		return clone;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return codelist.toString();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getValue();
	}

	@Override
	public void setIdentifier(String identifier) {
		setValue(identifier);
	}

}
