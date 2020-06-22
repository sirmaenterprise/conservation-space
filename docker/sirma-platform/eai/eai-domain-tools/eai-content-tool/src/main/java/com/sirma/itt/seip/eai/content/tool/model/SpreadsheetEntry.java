package com.sirma.itt.seip.eai.content.tool.model;

import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.eai.content.tool.service.reader.SpreadsheetUtil;

/**
 * * Defines raw entry as parsed from the source sheet. The instance should contain as bare minimum non null values for
 * {@link #getSheetId()} and {@link #getRowId()} parameters
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class SpreadsheetEntry {
	private SpreadsheetEntryId id;
	private Map<String, Object> properties = new HashMap<>();

	/**
	 * Instantiates a new spreadsheet entry.
	 *
	 * @param sheetId
	 *            the sheet id
	 * @param rowId
	 *            the row id
	 */
	public SpreadsheetEntry(int sheetId, int rowId) {
		this.id = new SpreadsheetEntryId(SpreadsheetUtil.getSheetId(sheetId), SpreadsheetUtil.getRowId(rowId));
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
	 * Gets the entry id
	 *
	 * @return the id
	 */
	public SpreadsheetEntryId getId() {
		return id;
	}

	/**
	 * Gets the properties for this entry
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SpreadsheetEntry [id=").append(id).append("]");
		return builder.toString();
	}

}
