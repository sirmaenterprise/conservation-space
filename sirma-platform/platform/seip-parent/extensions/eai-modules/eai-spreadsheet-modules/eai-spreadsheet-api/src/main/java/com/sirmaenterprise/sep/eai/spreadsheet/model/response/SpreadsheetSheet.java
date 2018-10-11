package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * Represents parsed spreadsheet like model as response of service request. Spreadsheet is represented as collection of
 * entries(rows)
 * 
 * @author bbanchev
 */
public class SpreadsheetSheet implements ServiceResponse {

	private List<SpreadsheetEntry> entries;

	/**
	 * Instantiates a new spreadsheet sheet.
	 */
	public SpreadsheetSheet() {
		entries = new LinkedList<>();
	}

	/**
	 * Instantiates a new spreadsheet sheet.
	 *
	 * @param size
	 *            the size for the sheet
	 */
	public SpreadsheetSheet(int size) {
		entries = new ArrayList<>(size);
	}

	/**
	 * Add entry to the sheet.
	 * 
	 * @param entry
	 *            - the entry to add, null values are skipped
	 */
	public void addEntry(SpreadsheetEntry entry) {
		CollectionUtils.addNonNullValue(entries, entry);
	}

	/**
	 * Return all sheet entries as list.
	 * 
	 * @return the parsed entries
	 */
	public List<SpreadsheetEntry> getEntries() {
		return entries;
	}

	@Override
	public String toString() {
		return entries != null ? entries.stream().map(Object::toString).collect(Collectors.joining(",\n")) : "No entries!";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpreadsheetSheet)) {
			return false;
		}
		SpreadsheetSheet that = (SpreadsheetSheet) o;
		return Objects.equals(entries, that.entries);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entries);
	}
}
