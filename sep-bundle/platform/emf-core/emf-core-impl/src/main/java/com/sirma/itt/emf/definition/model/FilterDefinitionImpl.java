package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.filter.Filter;

/**
 * Implementation class for filter definitions.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_filterDefinition")
@org.hibernate.annotations.Table(appliesTo = "emf_filterDefinition", indexes = @Index(name = "idx_fild_id", columnNames = "filterId"))
public class FilterDefinitionImpl implements Filter, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5645641080640106915L;

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The identifier. */
	@Column(name = "filterId", length = 100, nullable = false)
	private String identifier;

	/** The mode. */
	@Column(name = "filterMode", length = 20, nullable = true)
	private String mode;

	/** The filter values. */
	@Column(name = "filterValues", length = 2048, nullable = false)
	@Type(type = "com.sirma.itt.emf.entity.customType.StringSetCustomType")
	private Set<String> filterValues;

	/** The xml value. */
	@Transient
	private String xmlValue;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMode() {
		return mode;
	}

	/**
	 * Setter method for mode.
	 *
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Getter method for filterValues.
	 *
	 * @return the filterValues
	 */
	@Override
	public Set<String> getFilterValues() {
		if (filterValues == null) {
			filterValues = new LinkedHashSet<String>();
		}
		return filterValues;
	}

	/**
	 * Setter method for filterValues.
	 *
	 * @param filterValues
	 *            the filterValues to set
	 */
	public void setFilterValues(Set<String> filterValues) {
		this.filterValues = filterValues;
	}

	/**
	 * Getter method for xmlValue.
	 *
	 * @return the xmlValue
	 */
	public String getXmlValue() {
		return xmlValue;
	}

	/**
	 * Setter method for xmlValue.
	 *
	 * @param xmlValue
	 *            the xmlValue to set
	 */
	public void setXmlValue(String xmlValue) {
		this.xmlValue = xmlValue;
		if (xmlValue != null) {
			String[] split = xmlValue.split("\\s*,\\s*");
			filterValues = new LinkedHashSet<String>(Arrays.asList(split));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
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
		if (!(obj instanceof FilterDefinitionImpl)) {
			return false;
		}
		FilterDefinitionImpl other = (FilterDefinitionImpl) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
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
		builder.append("FilterDefinitionImpl [id=");
		builder.append(id);
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", filterValues=");
		builder.append(filterValues);
		builder.append("]");
		return builder.toString();
	}

}
