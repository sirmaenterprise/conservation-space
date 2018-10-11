package com.sirma.itt.emf.cls.rest;

import java.io.Serializable;
import java.util.Map;

/**
 * A POJO for carrying basic code value data.
 * 
 * @author Vilizar Tsonev
 */
public class CodeValueInfo {

	private String value;

	private String label;

	private Integer codelist;

	private Map<String, Serializable> descriptions;

	/**
	 * Constructs the code value.
	 * 
	 * @param value
	 *            is the value
	 * @param label
	 *            is the label according to the user language
	 * @param codelist
	 *            is the codelist to which this code value belongs
	 * @param descriptions
	 *            are the code value descriptions
	 */
	public CodeValueInfo(String value, String label, Integer codelist, Map<String, Serializable> descriptions) {
		super();
		this.value = value;
		this.label = label;
		this.codelist = codelist;
		this.descriptions = descriptions;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getCodelist() {
		return codelist;
	}

	public void setCodelist(Integer codelist) {
		this.codelist = codelist;
	}

	public Map<String, Serializable> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Map<String, Serializable> descriptions) {
		this.descriptions = descriptions;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int res = 1;
		res = PRIME * res + ((codelist == null) ? 0 : codelist.hashCode());
		res = PRIME * res + ((descriptions == null) ? 0 : descriptions.hashCode());
		res = PRIME * res + ((label == null) ? 0 : label.hashCode());
		res = PRIME * res + ((value == null) ? 0 : value.hashCode());
		return res;
	}

	@Override
	public boolean equals(Object object) { // NOSONAR
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		CodeValueInfo other = (CodeValueInfo) object;
		if (codelist == null) {
			if (other.codelist != null)
				return false;
		} else if (!codelist.equals(other.codelist))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
