package com.sirma.itt.emf.cls.validator;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.cls.columns.CLColumn;

import jxl.Cell;
import jxl.format.BoldStyle;
import jxl.format.CellFormat;

/**
 * Performs common validations on {@link Cell} objects.
 *
 * @author Nikolay Velkov
 * @author V. Tsonev
 */
public class CellValidator {

	/**
	 * Private constructor for utility class.
	 */
	private CellValidator() {
	}

	/**
	 * Validates that all required column names exist in the first row of the sheet.
	 *
	 * @param cells
	 *            the first row's columns
	 * @return true if yes or false if not
	 */
	public static boolean validateAllColumnsPresent(Cell[] cells) {
		for (int i = 0; i < CLColumn.values().length; i++) {
			if (!rowContainsValue(cells, CLColumn.values()[i].getName()) && CLColumn.values()[i].isMandatory()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the given row of {@link Cell} has cells which names are duplicating.
	 *
	 * @param row
	 *            is the row
	 * @return true if there are cells which have duplicated names
	 */
	public static boolean valdiateNoDuplicateColumns(Cell[] row) {
		Set<String> distinct = new HashSet<String>();
		for (Cell cell : row) {
			if (distinct.contains(cell.getContents().toLowerCase())) {
				return false;
			}
			distinct.add(cell.getContents().toLowerCase());
		}
		return true;
	}

	/**
	 * Checks if the given row of cells contains the given value in its content.
	 *
	 * @param row
	 *            is the row
	 * @param value
	 *            is the value
	 * @return true if the value exits in the row
	 */
	private static boolean rowContainsValue(Cell[] row, String value) {
		for (Cell cell : row) {
			if (cell.getContents().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given cell has any content.
	 *
	 * @param cell
	 *            the cell to check
	 * @return true if the cell is empty or false if not
	 */
	public static boolean isCellEmpty(Cell cell) {
		return cell == null || cell.getContents().trim().isEmpty();
	}

	/**
	 * Checks if a content of a cell is in bold.
	 *
	 * @param cell
	 *            the provided cell
	 * @return true if the content of the cell is bold or false if not
	 */
	public static boolean isCellBold(Cell cell) {
		CellFormat cellFormat = cell.getCellFormat();
		return cellFormat != null && cellFormat.getFont().getBoldWeight() == BoldStyle.BOLD.getValue();
	}
}
