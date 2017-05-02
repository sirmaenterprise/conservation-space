package com.sirma.itt.seip.eai.cs.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class CSItemRelations.
 */
public class CSItemRelations {
	@JsonProperty(value = "predicate")
	private String type;
	@JsonProperty(value = "object")
	private CSItemRecord record;

	/**
	 * Getter method for relationship type.
	 *
	 * @return the relationship type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for relationship type.
	 *
	 * @param type
	 *            the relationship type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for record data.
	 *
	 * @return the record data
	 */
	public CSItemRecord getRecord() {
		return record;
	}

	/**
	 * Setter method for record data.
	 *
	 * @param record
	 *            the record to set
	 */
	public void setRecord(CSItemRecord record) {
		this.record = record;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((record == null) ? 0 : record.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (!(obj instanceof CSItemRelations)) {
			return false;
		}
		CSItemRelations other = (CSItemRelations) obj;
		if (record == null) {
			if (other.record != null) {
				return false;
			}
		} else if (!record.equals(other.record)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[predicate=");
		builder.append(type);
		builder.append(", record=");
		builder.append(record);
		builder.append("]");
		return builder.toString();
	}

}
