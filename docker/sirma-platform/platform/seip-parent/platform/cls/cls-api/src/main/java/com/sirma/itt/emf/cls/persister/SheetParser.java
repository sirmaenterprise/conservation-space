package com.sirma.itt.emf.cls.persister;

import com.sirma.sep.cls.parser.CodeListSheet;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import jxl.Sheet;
import jxl.write.WritableWorkbook;

/**
 * Parses an excel sheet and creates {@link com.sirma.sep.cls.model.CodeList} and {@link com.sirma.sep.cls.model.CodeValue} objects from it's rows.
 *
 * @author nvelkov
 */
public interface SheetParser {

	/**
	 * Parses a sheet and converts it's rows into {@link com.sirma.sep.cls.model.CodeList} and {@link com.sirma.sep.cls.model.CodeValue} objects.
	 *
	 * @param sheet
	 *            the sheet to be parsed
	 * @return the parsed {@link com.sirma.sep.cls.model.CodeList} and {@link com.sirma.sep.cls.model.CodeValue} objects
	 * @throws EmfRuntimeException
	 *             if there is malformed sheet data
	 */
	CodeListSheet parseFromSheet(Sheet sheet);

	/**
	 * Parse a list of code lists and converts it to excel {@link Sheet}.
	 *
	 * @param sheet
	 *            the sheet representing list of code lists
	 * @return created excel sheet as {@link Sheet}
	 */
	Sheet parseFromList(CodeListSheet sheet);

	/**
	 * Parse a list of code lists and converts it to excel {@link Sheet}. The client should provide a Workbook which
	 * would be used to insert the created sheet to. When an outside workbook is provided the client takes care of
	 * closing the book after finishing working with it. This is to avoid leaving a provided workbook in an unusable
	 * state once it has been consumed by this method and allow the client to use the workbook for further write
	 * operations
	 *
	 * @param sheet
	 *            the sheet representing list of code lists
	 * @param workbook
	 *            the workbook to which for create the parsed sheet
	 * @return created excel sheet as {@link Sheet}
	 */
	Sheet parseFromList(CodeListSheet sheet, WritableWorkbook workbook);

}