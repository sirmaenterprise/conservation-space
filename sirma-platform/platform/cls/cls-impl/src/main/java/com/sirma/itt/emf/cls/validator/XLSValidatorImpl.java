package com.sirma.itt.emf.cls.validator;

import java.io.IOException;
import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Implementation of XLSValidator. Validates an excel file with code lists and values.
 *
 * @author Mihail Radkov
 * @author Vilizar Tsonev
 */
public class XLSValidatorImpl implements XLSValidator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sheet getValidatedCodeListSheet(InputStream inputStream) throws XLSValidatorException {
		if (inputStream == null) {
			throw new XLSValidatorException("No attached file");
		}
		Workbook workBook = null;

		try {
			workBook = Workbook.getWorkbook(inputStream);
		} catch (BiffException | IOException e) {
			throw new XLSValidatorException("Incorrect excel file.", e);
		}

		Sheet[] sheets = workBook.getSheets();
		for (Sheet sheet : sheets) {
			String name = sheet.getName();
			if ("codelists".equalsIgnoreCase(name)) {
				validateSheet(sheet);
				return sheet;
			}
		}
		throw new XLSValidatorException("Incorrect excel file. No sheet with name codelists was found.");
	}

	/**
	 * Validates a provided sheet by calling multiple validations.
	 *
	 * @param sheet
	 *            the provided sheet
	 */
	private void validateSheet(Sheet sheet) throws XLSValidatorException {
		if (isEmpty(sheet)) {
			throw new XLSValidatorException("The codelist sheet is empty.");
		} else if (!CellValidator.validateAllColumnsPresent(sheet.getRow(0))) {
			throw new XLSValidatorException(
					"The codelist sheet has missing column titles (at row 1) or the column titles are not named as expected.");
		} else if (!CellValidator.valdiateNoDuplicateColumns(sheet.getRow(0))) {
			throw new XLSValidatorException("The codelist sheet has duplicate column titles (at row 1).");
		}
	}

	/**
	 * Checks if provided sheet is empty or not.
	 *
	 * @param sheet
	 *            the provided sheet
	 * @return true if empty or false if not
	 */
	private boolean isEmpty(Sheet sheet) {
		if (sheet.getColumns() < 1) {
			return true;
		} else if (sheet.getRows() < 2) {
			return true;
		}
		return false;
	}

}
