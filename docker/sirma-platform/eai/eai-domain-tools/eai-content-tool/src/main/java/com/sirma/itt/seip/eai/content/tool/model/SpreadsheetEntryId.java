package com.sirma.itt.seip.eai.content.tool.model;

import java.util.Objects;

/**
 * {@link SpreadsheetEntryId} is a uid for entry in content. Contains sheet and row identifier. {@link #equals(Object)}
 * and {@link #hashCode()} are overridden to support comparison of ids.
 * 
 * @author bbanchev
 */
public class SpreadsheetEntryId {
	private String externalId;
	private String sheetId;

	/**
	 * Instantiates a new {@link SpreadsheetEntryId} with two
	 *
	 * @param sheetId
	 *            the source sheet id
	 * @param externalId
	 *            - some uid of instance
	 */
	public SpreadsheetEntryId(String sheetId, String externalId) {
		Objects.requireNonNull(externalId, "External identifier is a required argument to construct model instance!");
		Objects.requireNonNull(sheetId,
				"External identifier sheet is a required argument to construct model instance!");
		this.sheetId = sheetId;
		this.externalId = externalId;
	}

	/**
	 * Gets the entry sheet location index
	 * 
	 * @return the sheet id
	 */
	public String getSheetId() {
		return sheetId;
	}

	/**
	 * Gets the entry row location index
	 * 
	 * @return the row id
	 */
	public String getExternalId() {
		return externalId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sheetId.toLowerCase().hashCode();
		result = prime * result + externalId.toLowerCase().hashCode();
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
		if (!(obj instanceof SpreadsheetEntryId)) {
			return false;
		}
		SpreadsheetEntryId other = (SpreadsheetEntryId) obj;
		return Objects.equals(sheetId, other.sheetId) && Objects.equals(externalId, other.externalId);
	}

	@Override
	public String toString() {
		return "Record[id=" + externalId + ", sheet " + sheetId + "]";
	}

}
