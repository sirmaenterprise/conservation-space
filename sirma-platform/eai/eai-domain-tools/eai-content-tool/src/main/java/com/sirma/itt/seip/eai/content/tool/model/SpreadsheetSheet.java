package com.sirma.itt.seip.eai.content.tool.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Represents parsed spreadsheet like model as response of service request. Spreadsheet is represented as collection of
 * entries(rows)
 * 
 * @author bbanchev
 */
public class SpreadsheetSheet {
	private Workbook source;
	private List<SpreadsheetEntry> entries;

	/**
	 * Instantiates a new spreadsheet sheet.
	 * 
	 * @param source
	 *            - the source spreadsheet
	 */
	public SpreadsheetSheet(Workbook source) {
		this.source = source;
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
		Objects.requireNonNull(entry);
		entries.add(entry);
	}

	/**
	 * Gets the source workbook
	 * 
	 * @return the workbook
	 */
	public Workbook getSource() {
		return source;
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
		return entries != null ? entries.toString() : "No entries!";
	}

}
