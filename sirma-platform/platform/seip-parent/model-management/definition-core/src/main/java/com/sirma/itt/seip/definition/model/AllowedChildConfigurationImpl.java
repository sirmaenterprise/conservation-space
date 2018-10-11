/*
 *
 */
package com.sirma.itt.seip.definition.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.definition.FilterMode;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default implementation for {@link AllowedChildConfiguration}
 *
 * @author BBonev
 */
public class AllowedChildConfigurationImpl extends MergeableBase<AllowedChildConfigurationImpl>
		implements Serializable, AllowedChildConfiguration, Copyable<AllowedChildConfigurationImpl>, Sealable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6015358729826991028L;

	/** The codelist. */
	@Tag(1)
	protected Integer codelist;

	/** The property. */
	@Tag(2)
	protected String property;

	/** The values. */
	@Tag(3)
	protected Set<String> values;

	@Tag(4)
	protected FilterMode filterMode;

	/** The xml values. Store the original value that is set in the XML */
	@Tag(5)
	protected String xmlValues;

	private boolean sealed = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public String getProperty() {
		return property;
	}

	/**
	 * Setter method for property.
	 *
	 * @param property
	 *            the property to set
	 */
	public void setProperty(String property) {
		if (isSealed()) {
			return;
		}
		this.property = property;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getValues() {
		return values;
	}

	/**
	 * Setter method for values.
	 *
	 * @param values
	 *            the values to set
	 */
	public void setValues(Set<String> values) {
		if (isSealed()) {
			return;
		}
		this.values = values;
	}

	/**
	 * Getter method for xmlValues.
	 *
	 * @return the xmlValues
	 */
	@Override
	public String getXmlValues() {
		return xmlValues;
	}

	/**
	 * Setter method for xmlValues.
	 *
	 * @param xmlValues
	 *            the xmlValues to set
	 */
	public void setXmlValues(String xmlValues) {
		if (isSealed()) {
			return;
		}
		this.xmlValues = xmlValues;
		if (xmlValues != null) {
			String[] split = xmlValues.split("\\s*,\\s*");
			values = new LinkedHashSet<>(Arrays.asList(split));
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AllowedChildConfiguration [");
		builder.append("property=");
		builder.append(property);
		builder.append(", codelist=");
		builder.append(codelist);
		builder.append(", values=");
		builder.append(values);
		builder.append(", filterMode=");
		builder.append(filterMode);
		builder.append(", xmlValues=");
		builder.append(xmlValues);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public AllowedChildConfigurationImpl mergeFrom(AllowedChildConfigurationImpl source) {
		codelist = MergeHelper.replaceIfNull(codelist, source.codelist);
		property = MergeHelper.replaceIfNull(property, source.property);
		values = MergeHelper.replaceIfNull(values, source.values);
		return this;
	}

	@Override
	public String getIdentifier() {
		return getProperty();
	}

	@Override
	public void setIdentifier(String identifier) {
		setProperty(identifier);
	}

	@Override
	public FilterMode getFilterMode() {
		return filterMode;
	}

	/**
	 * Sets the filter mode.
	 *
	 * @param filterMode
	 *            the new filter mode
	 */
	public void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (codelist == null ? 0 : codelist.hashCode());
		result = prime * result + (filterMode == null ? 0 : filterMode.hashCode());
		result = prime * result + (xmlValues == null ? 0 : xmlValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AllowedChildConfigurationImpl)) {
			return false;
		}
		AllowedChildConfigurationImpl other = (AllowedChildConfigurationImpl) obj;
		if (!EqualsHelper.nullSafeEquals(codelist, other.codelist)) {
			return false;
		}
		if (!EqualsHelper.nullSafeEquals(filterMode, other.filterMode)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(xmlValues, other.xmlValues);
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public AllowedChildConfigurationImpl createCopy() {
		AllowedChildConfigurationImpl clone = new AllowedChildConfigurationImpl();
		clone.codelist = codelist;
		clone.property = property;
		clone.filterMode = filterMode;
		clone.xmlValues = xmlValues;

		if (clone.getValues() != null) {
			clone.setValues(new LinkedHashSet<>(getValues()));
		}

		clone.sealed = false;

		return clone;
	}


	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}

		if (getValues() != null) {
			setValues(Collections.unmodifiableSet(getValues()));
		}

		sealed = true;
	}

}
