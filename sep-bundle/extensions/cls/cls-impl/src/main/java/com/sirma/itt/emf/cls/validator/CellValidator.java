package com.sirma.itt.emf.cls.validator;

import jxl.Cell;
import jxl.format.BoldStyle;
import jxl.format.CellFormat;

import com.sirma.itt.emf.cls.columns.CLColumn;

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
	public static boolean validateColumns(Cell[] cells) {
		for (int i = 0; i < CLColumn.values().length; i++) {
			if (!cells[i].getContents().equalsIgnoreCase(CLColumn.values()[i].getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Validates that all rows defining the code list data are in bold.
	 * 
	 * @param cells
	 *            is the sheet column that will be used for validation
	 * @return true if all code list data rows are in bold or false if not
	 */
	public static boolean validateBoldCLDataRows(Cell[] cells) {
		for (Cell cell : cells) {
			if (!isCellEmpty(cell) && !isCellBold(cell)) {
				return false;
			}
		}
		return true;
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
		return cellFormat != null
				&& cellFormat.getFont().getBoldWeight() == BoldStyle.BOLD.getValue();
	}
}
