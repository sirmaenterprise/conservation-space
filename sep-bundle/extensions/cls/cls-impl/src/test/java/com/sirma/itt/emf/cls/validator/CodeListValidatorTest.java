package com.sirma.itt.emf.cls.validator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.validator.CodeListValidator;

/**
 * Testing the {@link CodeListValidatorTest} to verify that it validates
 * codelist objects correctly.
 * 
 * @author Nikolay Velkov
 */
public class CodeListValidatorTest {

	/** The work book. */
	private WritableWorkbook workBook;

	/** The sheet. */
	private WritableSheet sheet;

	/**
	 * Creates the work book.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Before
	public void createWorkBook() throws IOException {
		workBook = Workbook.createWorkbook(new ByteArrayOutputStream());
	}

	/**
	 * Inits the columns with names and column order taken from the enum.
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	@Before
	public void initColumns() throws RowsExceededException, WriteException {
		sheet = workBook.createSheet("codelists", 0);
		for (CLColumn column : CLColumn.values()) {
			Label label = new Label(column.getColumn(), 0, column.getName());
			sheet.addCell(label);
		}
	}

	/**
	 * Test code list master value for a fail by providing a master code list
	 * value that does not exist in the given xls.
	 * 
	 * @throws PersisterException
	 *             the persister exception
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	@Test(expected = PersisterException.class)
	public void testCodeListMasterValueFail() throws PersisterException,
			RowsExceededException, WriteException {

		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(), 1, "2");
		Label value = createCodeListLabel(CLColumn.VALUE.getColumn(), 1, "1");
		addCells(value, masterCL);

		SheetParser parser = new SheetParser();
		List<CodeList> codeLists = parser.parseXLS(sheet);

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(codeLists);
	}

	/**
	 * Test code list master value for success by giving a master code list
	 * value that exists in the given xls.
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test
	public void testCodeListMasterValueSuccess() throws RowsExceededException,
			WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.VALUE.getColumn(), 1, "1");
		Label secondValue = createCodeListLabel(CLColumn.VALUE.getColumn(), 2,
				"2");
		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(), 1, "2");
		addCells(value, secondValue, masterCL);

		SheetParser parser = new SheetParser();
		List<CodeList> codeLists = parser.parseXLS(sheet);

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(codeLists);
	}

	/**
	 * Test code value master value for a fail by giving a master code value
	 * value that does not exist in the the current code list.
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test(expected = PersisterException.class)
	public void testCodeValueMasterValueFail() throws RowsExceededException,
			WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.VALUE.getColumn(), 1, "1");
		Label codeValueValue = new Label(CLColumn.VALUE.getColumn(), 2, "2");
		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(), 2, "3");
		addCells(value, codeValueValue, masterCL);

		SheetParser parser = new SheetParser();
		List<CodeList> codeLists = parser.parseXLS(sheet);

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(codeLists);
	}

	/**
	 * Test code value master value for success by giving a master code value
	 * value that exists in the current code list.
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test
	public void testCodeValueMasterValueSuccess() throws RowsExceededException,
			WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.VALUE.getColumn(), 1, "1");
		Label codeValue = new Label(CLColumn.VALUE.getColumn(), 2, "2");
		Label secondCodeValue = new Label(CLColumn.VALUE.getColumn(), 3, "3");
		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(), 2, "3");
		addCells(value, codeValue, secondCodeValue, masterCL);

		SheetParser parser = new SheetParser();
		List<CodeList> codeLists = parser.parseXLS(sheet);

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(codeLists);
	}

	/**
	 * Creates the code list label. Creates a bold cell.
	 * 
	 * @param column
	 *            the column
	 * @param row
	 *            the row
	 * @param value
	 *            the value
	 * @return the label
	 */
	private Label createCodeListLabel(int column, int row, String value) {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);

		return new Label(column, row, value, times16format);
	}

	/**
	 * Adds the cells. Add the cells to the sheet.
	 * 
	 * @param labels
	 *            the labels
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	private void addCells(Label... labels) throws RowsExceededException,
			WriteException {
		for (Label label : labels) {
			sheet.addCell(label);
		}
	}

}
