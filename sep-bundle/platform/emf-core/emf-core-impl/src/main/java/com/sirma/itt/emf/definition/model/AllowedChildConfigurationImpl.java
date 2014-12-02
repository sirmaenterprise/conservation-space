/*
 *
 */
package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.MergeableBase;

/**
 * Default implementation for {@link AllowedChildConfiguration}
 * 
 * @author BBonev
 */
public class AllowedChildConfigurationImpl extends MergeableBase<AllowedChildConfigurationImpl>
		implements Serializable, AllowedChildConfiguration {

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

	/** The xml values. */
	protected transient String xmlValues;

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
		this.values = values;
	}

	/**
	 * Getter method for xmlValues.
	 *
	 * @return the xmlValues
	 */
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
		this.xmlValues = xmlValues;
		if (xmlValues != null) {
			String[] split = xmlValues.split("\\s*,\\s*");
			values = new LinkedHashSet<String>(Arrays.asList(split));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkflowIdFilter [");
		builder.append(", property=");
		builder.append(property);
		builder.append(", codelist=");
		builder.append(codelist);
		builder.append(", values=");
		builder.append(values);
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

}
