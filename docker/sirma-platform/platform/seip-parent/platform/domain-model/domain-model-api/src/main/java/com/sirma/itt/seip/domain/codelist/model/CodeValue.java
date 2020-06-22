package com.sirma.itt.seip.domain.codelist.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a single code value. It has a value field that represents the particular code value pay load and the
 * number of the codelist that belongs to. Also the value could have additional properties.
 *
 * @author BBonev
 */
public class CodeValue implements Serializable, PropertyModel, Copyable<CodeValue>, JsonRepresentable {

	private static final long serialVersionUID = -2827111152991530423L;

	private String value;

	private Integer codelist;

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

	@Override
	public Map<String, Serializable> getProperties() {
		return descriptions;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		descriptions = properties;
	}

	@Override
	public Long getRevision() {
		return null;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (codelist == null ? 0 : codelist.hashCode());
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeValue)) {
			return false;
		}
		CodeValue other = (CodeValue) obj;
		return EqualsHelper.nullSafeEquals(codelist, other.codelist) && EqualsHelper.nullSafeEquals(value, other.value);
	}

	@Override
	public CodeValue createCopy() {
		CodeValue clone = new CodeValue();
		clone.codelist = codelist;
		clone.value = value;
		clone.descriptions = Collections.unmodifiableMap(new HashMap<>(descriptions));
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

	/**
	 * Gets the description.
	 *
	 * @param forLocale
	 *            the for locale
	 * @return the description
	 */
	public String getDescription(Locale forLocale) {
		return getString(forLocale.getLanguage(), () -> getString("en"));
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "value", value);
		JsonUtil.addToJson(object, "codelist", codelist);
		if (descriptions != null) {
			JsonUtil.addToJson(object, "descriptions", JsonUtil.toJsonObject((Serializable) descriptions));
		}
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// implement later if needed
	}

}
