package com.sirma.itt.seip.eai.model.request.query;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link RawQueryEntry} is holder for single query entry - its criteria name, value and the desired operator
 * 
 * @author bbanchev
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RawQueryEntry implements QueryEntry {
	private static final long serialVersionUID = -5683322985603508130L;
	@JsonProperty(value = "operator", required = true)
	private String operator;
	@JsonProperty(value = "values")
	private Collection<Object> values;
	@JsonProperty(value = "property")
	private String property;
	@JsonProperty(value = "type")
	private String type;

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * Sets the operator.
	 *
	 * @param operator
	 *            the new operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public Collection<Object> getValues() {
		return values;
	}

	/**
	 * Sets the values.
	 *
	 * @param values
	 *            the new values
	 */
	public void setValues(Collection<Object> values) {
		this.values = values;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for property.
	 *
	 * @return the property
	 */
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RawQueryEntry [");
		if (operator != null) {
			builder.append("operator=");
			builder.append(operator);

		}
		if (values != null) {
			builder.append(", ");
			builder.append("values=");
			builder.append(values);

		}
		if (getProperty() != null) {
			builder.append(", ");
			builder.append("property=");
			builder.append(getProperty());
		}
		if (type != null) {
			builder.append(", ");
			builder.append("type=");
			builder.append(type);
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RawQueryEntry))
			return false;
		RawQueryEntry other = (RawQueryEntry) obj;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

}
