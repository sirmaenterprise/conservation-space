package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import java.util.Map;
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
	private Map<String, String> configuration;
	private DynamicProperties properties = new DynamicProperties();

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
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the previous value
	 */
	public Object put(String key, Object value) {
		return properties.put(key, value);
	}

	/**
	 * Gets the configurations for context selection keyed by the properties keys
	 * 
	 * @return the map of configurations, possibly null
	 */
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configurations for context selection keyed by the properties keys
	 * 
	 * @param configuration
	 *            is the configurations to set.
	 */
	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}

	@Override
	public String toString() {
		return "Entry [row=" + externalId + ", sheet=" + sheetId + ", properties=" + properties + "]";
	}

}
