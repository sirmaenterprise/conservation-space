package com.sirma.sep.export.xlsx.components;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a excel row.
 *
 * @author Boyan Tonchev.
 */
public class ExportExcelRow {

	private Map<String, ExportExcelCell> properties = new HashMap<>();

	/**
	 * Add cell value to properties with <code>propertyIdentifier</code>.
	 *
	 * @param propertyIdentifier
	 * 		- the property identifier.
	 * @param excelCell
	 * 		- the a cell of excel.
	 */
	public void addProperty(String propertyIdentifier, ExportExcelCell excelCell) {
		properties.put(propertyIdentifier, excelCell);
	}

	/**
	 * Fetch cell with <code>propertyIdentifier</code>. If it not exist empty property will be returned.
	 *
	 * @param propertyIdentifier
	 * 		- the property identifier.
	 * @return value associated with <code>propertyIdentifier</code> excel cell.
	 */
	public ExportExcelCell getProperty(String propertyIdentifier) {
		ExportExcelCell exportExcelCell = properties.get(propertyIdentifier);
		return exportExcelCell == null ?
				new ExportExcelCell(propertyIdentifier, "", ExportExcelCell.Type.OBJECT) :
				exportExcelCell;
	}
}
