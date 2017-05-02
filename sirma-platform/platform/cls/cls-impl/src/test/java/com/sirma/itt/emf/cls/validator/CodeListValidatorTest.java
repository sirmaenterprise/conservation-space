package com.sirma.itt.emf.cls.validator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.SheetParserImpl;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Testing the {@link CodeListValidatorTest} to verify that it validates codelist objects correctly.
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
		int index = 0;
		for (CLColumn column : CLColumn.values()) {
			Label label = new Label(index, 0, column.getName());
			sheet.addCell(label);
			index++;
		}
	}

	/**
	 * Test code list master value for a fail by providing a master code list value that does not exist in the given
	 * xls.
	 *
	 * @throws PersisterException
	 *             the persister exception
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	@Test(expected = PersisterException.class)
	public void testCodeListMasterValueFail() throws PersisterException, RowsExceededException, WriteException {
		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(sheet), 1, "2");
		Label value = createCodeListLabel(CLColumn.CL_VALUE.getColumn(sheet), 1, "1");
		Label displayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 1, "1");
		addCells(value, masterCL, displayType);

		SheetParserImpl parser = new SheetParserImpl();

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
	}

	/**
	 * Tests the code list validation when display type is not provided. {@link PersisterException} is expected
	 * indicating the missing display type.
	 *
	 * @throws PersisterException
	 *             thrown when display type is missing
	 * @throws RowsExceededException
	 *             when rows are exceeded
	 * @throws WriteException
	 *             when a problem with sheet writing occurs
	 */
	@Test(expected = PersisterException.class)
	public void testCodeListMissingDisplayType() throws PersisterException, RowsExceededException, WriteException {
		Label value = createCodeListLabel(CLColumn.CL_VALUE.getColumn(sheet), 1, "1");
		addCells(value);

		SheetParserImpl parser = new SheetParserImpl();

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
	}

	/**
	 * Test code list master value for success by giving a master code list value that exists in the given xls.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test
	public void testCodeListMasterValueSuccess() throws RowsExceededException, WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.CL_VALUE.getColumn(sheet), 1, "1");
		Label secondValue = createCodeListLabel(CLColumn.CL_VALUE.getColumn(sheet), 2, "2");
		Label masterCL = new Label(CLColumn.MASTERCL.getColumn(sheet), 1, "2");
		Label displayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 1, "1");
		Label displayType2 = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 2, "1");
		addCells(value, secondValue, masterCL, displayType, displayType2);

		SheetParserImpl parser = new SheetParserImpl();

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist but with
	 * non-overlaping date ranges.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testCodeValueValidationDuplicatedValueAndValidRanges()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("02.02.2016", "03.02.2016", "03.02.2016", "15.02.2016");
		testDateRanges("02.02.2016", "02.03.2016", "03.04.2016", "15.04.2016");
		testDateRanges("01.02.2016", "", "", "01.02.2016");
		testDateRanges("01.02.2016", "02.02.2016", "", "01.02.2016");
		testDateRanges("", "01.02.2016", "01.02.2016", "03.02.2016");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because the first date is valid from negative infinity to infinity.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesInfiniteDateRange()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("", "", "03.03.2016", "04.03.2016");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because both dates are valid from negative infinity.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesMissingValidFrom()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("", "03.03.2016", "", "04.03.2016");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because both dates are valid to infinity.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesMissingValidTo()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("01.02.2017", "", "02.02.2017", "");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because the second date is valid to infinity.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesInfiniteSecondRangeValidTo()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("03.03.2016", "05.03.2016", "02.03.2016", "");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because the second date is valid from negative infinity
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesInfiniteSecondRangeValidFrom()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("03.03.2016", "05.03.2016", "", "04.03.2016");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because the first date is valid from negative infinity
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesInfiniteFirstRangeValidFrom()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("", "05.03.2016", "03.03.2016", "04.03.2016");
	}

	/**
	 * Test the codevalue validation process when there are two codevalues with the same code in one codelist with date
	 * ranges that overlap because the first date is valid to infinity.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = PersisterException.class)
	public void testDateRangesInfiniteFirstRangeValidTo()
			throws RowsExceededException, WriteException, PersisterException, IOException {
		testDateRanges("01.03.2016", "", "03.03.2016", "04.03.2016");
	}

	/**
	 * Test if the the date ranges are valid meaning that they don't overlap
	 *
	 * @param validFrom
	 *            the start date of the first range
	 * @param validTo
	 *            the end date of the first range
	 * @param secondValidFrom
	 *            the start date of the second range
	 * @param secondValidTo
	 *            the end date of the second range
	 * @throws PersisterException
	 *             if the date ranges are not valid because they overlap
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws IOException
	 *             the io exception
	 */
	private void testDateRanges(String validFrom, String validTo, String secondValidFrom, String secondValidTo)
			throws PersisterException, RowsExceededException, WriteException, IOException {
		SheetParserImpl parser = new SheetParserImpl();
		CodeListValidator validator = new CodeListValidator();
		generateTestData(validFrom, validTo, secondValidFrom, secondValidTo);
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
		createWorkBook();
		initColumns();
	}

	/**
	 * Generate test data with 2 rows for 2 code values with the same code.
	 *
	 * @param validFrom
	 *            the start date of the first range
	 * @param validTo
	 *            the end date of the first range
	 * @param secondValidFrom
	 *            the start date of the second range
	 * @param secondValidTo
	 *            the end date of the second range
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	private void generateTestData(String validFrom, String validTo, String secondValidFrom, String secondValidTo)
			throws RowsExceededException, WriteException {
		Label value = createCodeListLabel(CLColumn.CV_VALUE.getColumn(sheet), 1, "1");
		Label displayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 1, "1");

		Label codeValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 2, "value");
		Label codeValidFrom = new Label(CLColumn.VALID_FROM.getColumn(sheet), 2, validFrom);
		Label codeValidTo = new Label(CLColumn.VALID_TO.getColumn(sheet), 2, validTo);

		Label duplicatedCodeValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 3, "value");
		Label duplicatedCodeValidFrom = new Label(CLColumn.VALID_FROM.getColumn(sheet), 3, secondValidFrom);
		Label duplicatedCodeValidTo = new Label(CLColumn.VALID_TO.getColumn(sheet), 3, secondValidTo);

		addCells(value, codeValue, codeValidFrom, codeValidTo, duplicatedCodeValue, duplicatedCodeValidFrom,
				duplicatedCodeValidTo, displayType);
	}

	/**
	 * Test code value master value for a fail by giving a master code value value that does not exist in the the
	 * current code list.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test(expected = PersisterException.class)
	public void testCodeValueMasterValueFail() throws RowsExceededException, WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.CV_VALUE.getColumn(sheet), 1, "1");
		Label masterCL = createCodeListLabel(CLColumn.MASTERCL.getColumn(sheet), 1, "2");
		Label displayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 1, "1");

		Label codeValueValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 2, "2");
		Label masterCV = new Label(CLColumn.MASTERCL.getColumn(sheet), 2, "3");
		// the second code list won't contain code value 3, so that exception will be thrown
		Label secondCodeList = createCodeListLabel(CLColumn.CV_VALUE.getColumn(sheet), 4, "2");
		Label secondDisplayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 4, "1");
		Label secondCLValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 5, "100");

		addCells(value, codeValueValue, masterCL, masterCV, secondCodeList, secondCLValue, displayType,
				secondDisplayType);

		SheetParserImpl parser = new SheetParserImpl();

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
	}

	/**
	 * Test code value master value for success by giving a master code value value that exists in the current code
	 * list.
	 *
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 * @throws PersisterException
	 *             the persister exception
	 */
	@Test
	public void testCodeValueMasterValueSuccess() throws RowsExceededException, WriteException, PersisterException {
		Label value = createCodeListLabel(CLColumn.CV_VALUE.getColumn(sheet), 1, "1");
		Label displayType = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 1, "1");
		Label masterCL = createCodeListLabel(CLColumn.MASTERCL.getColumn(sheet), 1, "2");
		// code list 1 will have two code values
		Label codeValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 2, "2");
		Label secondCodeValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 3, "3");
		// the first code value will have a master value: 100
		Label masterCV = new Label(CLColumn.MASTERCL.getColumn(sheet), 2, "100");
		// the second code list should contain the code value 100 so that the validation pass
		Label secondCodeList = createCodeListLabel(CLColumn.CV_VALUE.getColumn(sheet), 4, "2");
		Label secondCLValue = new Label(CLColumn.CV_VALUE.getColumn(sheet), 5, "100");
		Label displayType2 = new Label(CLColumn.DISPLAY_TYPE.getColumn(sheet), 4, "1");

		addCells(value, codeValue, secondCodeValue, masterCV, secondCodeList, secondCLValue, masterCL, displayType,
				displayType2);

		SheetParserImpl parser = new SheetParserImpl();

		CodeListValidator validator = new CodeListValidator();
		validator.validateCodeLists(parser.parseXLS(sheet).getCodeLists());
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
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, true);
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
	private void addCells(Label... labels) throws RowsExceededException, WriteException {
		for (Label label : labels) {
			sheet.addCell(label);
		}
	}

}
