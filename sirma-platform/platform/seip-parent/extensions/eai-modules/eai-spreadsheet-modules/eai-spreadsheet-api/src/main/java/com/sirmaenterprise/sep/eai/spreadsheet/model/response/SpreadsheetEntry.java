package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import java.util.Objects;

import com.sirma.itt.seip.eai.model.request.DynamicProperties;

/**
 * Defines raw entry as parsed from the source sheet. The instance should contain as bare minimum non null values for
 * {@link #getExternalId()} and {@link #getProperties()} parameters
 * 
 * @author bbanchev
 */
public class SpreadsheetEntry {
	private String externalId;
	private String sheetId;
	private DynamicProperties properties = new DynamicProperties();
	private DynamicProperties bindings;

	/**
	 * Constructs new entry.
	 * 
	 * @param sheetId
	 *            - the id of sheet. Required non null
	 * @param externalId
	 *            - the unique id of that entry. Required non null
	 */
	public SpreadsheetEntry(String sheetId, String externalId) {
		Objects.requireNonNull(sheetId,
				"External identifier sheet is a required argument to construct model instance!");
		Objects.requireNonNull(externalId, "External identifier is a required argument to construct model instance!");
		this.externalId = externalId;
		this.sheetId = sheetId;
	}

	/**
	 * Getter method for externalId.
	 *
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Getter method for sheet.
	 *
	 * @return the sheet
	 */
	public String getSheet() {
		return sheetId;
	}

	/**
	 * Gets the properties model.
	 *
	 * @return the properties
	 */
	public DynamicProperties getProperties() {
		return properties;
	}

	/**
	 * Put a new key-value parameter. Old values might be overridden
	 *
	 * @param field
	 *            the field
	 * @param value
	 *            the value
	 * @return the previous value
	 */
	public Object put(String field, Object value) {
		return properties.put(field, value);
	}

	/**
	 * Remove a key and returns the value
	 *
	 * @param field
	 *            the field
	 * @return the previous value
	 */
	public Object remove(String field) {
		return properties.remove(field);
	}

	/**
	 * Return the bound fields to queries for object properties
	 * 
	 * @return the bound properties or null if none are bound
	 */
	public DynamicProperties getBindings() {
		return bindings;
	}

	/**
	 * Sets the configurations for context selection keyed by the properties keys
	 * 
	 * @param field
	 *            the configurations key to set.
	 * @param query
	 *            the configurations value to set
	 */
	public void bind(String field, String query) {
		if (field == null) {
			return;
		}
		Objects.requireNonNull(query, "Value is a required parameter to apply search configuration for " + field);
		// lazy create to optimize
		if (bindings == null) {
			bindings = new DynamicProperties();
		}
		this.bindings.put(field.toLowerCase(), query);
	}

	/**
	 * Retrieves a stored binding configuration for given entry
	 * 
	 * @param key
	 *            is the key to search for
	 * @return the configured value, might be null if key is null or value is not set
	 */
	public String getBinding(String key) {
		if (key == null || bindings == null) {
			return null;
		}
		return (String) bindings.get(key.toLowerCase());
	}

	/**
	 * Removes a stored binding configuration for given entry
	 * 
	 * @param fieldId
	 *            is the key to remove for
	 * @return the configured value, might be null if key is null or value is not set
	 */
	public String unbind(String fieldId) {
		if (fieldId == null || bindings == null) {
			return null;
		}
		return (String) bindings.remove(fieldId.toLowerCase());
	}

	@Override
	public String toString() {
		return "Entry [row=" + externalId + ", sheet=" + sheetId + ", properties=" + properties + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpreadsheetEntry)) {
			return false;
		}
		SpreadsheetEntry that = (SpreadsheetEntry) o;
		return Objects.equals(externalId, that.externalId) &&
				Objects.equals(sheetId, that.sheetId) &&
				Objects.equals(properties, that.properties) &&
				Objects.equals(bindings, that.bindings);
	}

	@Override
	public int hashCode() {
		return Objects.hash(externalId, sheetId, properties, bindings);
	}
}
