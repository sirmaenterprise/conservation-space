package com.sirma.itt.emf.cls.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.util.JxlUtils;
import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Implementation of {@link SheetValidator}. Validates an excel file with code lists and values.
 *
 * @author Mihail Radkov
 * @author Vilizar Tsonev
 * @author svetlozar.iliev
 */
public class SheetValidatorImpl implements SheetValidator {

	@Override
	public Sheet getValidatedCodeListSheet(InputStream inputStream) throws SheetValidatorException {
		if (inputStream == null) {
			throw new SheetValidatorException("No attached file");
		}

		Workbook workBook;
		try {
			workBook = JxlUtils.getWorkbook(inputStream);
		} catch (BiffException | IOException e) {
			throw new SheetValidatorException("Incorrect excel file.", e);
		}

		Sheet[] sheets = workBook.getSheets();
		for (Sheet sheet : sheets) {
			String name = sheet.getName();
			if ("codelists".equalsIgnoreCase(name)) {
				validateSheet(sheet);
				return sheet;
			}
		}
		throw new SheetValidatorException("Incorrect excel file. No sheet with name codelists was found.");
	}

	/**
	 * Validates a provided sheet by calling multiple validations.
	 *
	 * @param sheet
	 *            the provided sheet
	 */
	private static void validateSheet(Sheet sheet) throws SheetValidatorException {
		if (isEmpty(sheet)) {
			throw new SheetValidatorException("The codelist sheet is empty.");
		} else if (!validateAllColumnsPresent(sheet.getRow(0))) {
			throw new SheetValidatorException(
					"The codelist sheet has missing columns or the columns are not named as expected.");
		} else if (!valdiateNoDuplicateColumns(sheet.getRow(0))) {
			throw new SheetValidatorException("The codelist sheet has duplicate column titles (at row 1).");
		}
	}

	/**
	 * Checks if provided sheet is empty or not.
	 *
	 * @param sheet
	 *            the provided sheet
	 * @return true if empty or false if not
	 */
	private static boolean isEmpty(Sheet sheet) {
		return sheet.getColumns() < 1 || sheet.getRows() < 2;
	}

	/**
	 * Validates that all required column names exist in the first row of the sheet.
	 *
	 * @param cells
	 *            the first row's columns
	 * @return true if yes or false if not
	 */
	private static boolean validateAllColumnsPresent(Cell[] cells) {
		for (int i = 0; i < CLColumn.values().length; i++) {
			if (CLColumn.values()[i].isMandatory() && !rowContainsValue(cells, CLColumn.values()[i].getName())) {
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
	private static boolean valdiateNoDuplicateColumns(Cell[] row) {
		Set<String> distinct = new HashSet<>();
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

}
