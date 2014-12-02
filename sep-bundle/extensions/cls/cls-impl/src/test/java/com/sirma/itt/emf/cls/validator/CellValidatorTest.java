package com.sirma.itt.emf.cls.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jxl.Cell;
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

/**
 * Testing the cell validating functionality.
 * 
 * @author Nikolay Velkov
 */
public class CellValidatorTest {

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
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);
		for (CLColumn column : CLColumn.values()) {
			Label label = new Label(column.getColumn(), 0, column.getName(),
					times16format);
			sheet.addCell(label);
		}
	}

	/**
	 * Testing the isCellBold method of the {@link CellValidator} with a bold
	 * cell.
	 */
	@Test
	public void testBoldCell() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);

		Cell cell = new Label(0, 0, "test", times16format);
		assertTrue(CellValidator.isCellBold(cell));
	}

	/**
	 * Testing the isCellBold method of the {@link CellValidator} with a normal
	 * (non-bold) cell.
	 */
	@Test
	public void testBoldCellFail() {
		Cell cell = new Label(0, 0, "test");
		assertFalse(CellValidator.isCellBold(cell));
	}

	/**
	 * Testing the isCellEmpty method of the {@link CellValidator} with a
	 * non-empty cell.
	 */
	@Test
	public void testCellNotEmpty() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);

		Cell cell = new Label(0, 0, "test", times16format);
		assertFalse(CellValidator.isCellEmpty(cell));
	}

	/**
	 * Testing the isCellEmpty method of the {@link CellValidator} with an empty
	 * cell.
	 */
	@Test
	public void testCellEmpty() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);
		Cell cell = new Label(0, 0, "", times16format);
		assertTrue(CellValidator.isCellEmpty(cell));
	}

	/**
	 * Testing the validateBoldCLDataRows with valid rows that are both not
	 * empty and bold.
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	@Test
	public void testValidateColumnsSuccess() throws RowsExceededException,
			WriteException {
		sheet.addCell(createBoldLabel(0, 1, "asd"));
		sheet.addCell(createBoldLabel(0, 5, "asd"));
		assertTrue(CellValidator.validateBoldCLDataRows(sheet.getColumn(0)));
	}

	/**
	 * Testing the validateBoldCLDataRows with invalid rows where the first
	 * element of the row is not empty but it's also not bold. The first
	 * column's cell have to be bold if they have some content .
	 * 
	 * @throws RowsExceededException
	 *             the rows exceeded exception
	 * @throws WriteException
	 *             the write exception
	 */
	@Test
	public void testValidateColumnsFailEmpty() throws RowsExceededException,
			WriteException {
		sheet.addCell(new Label(0, 2, "qwe"));
		assertFalse(CellValidator.validateBoldCLDataRows(sheet.getColumn(0)));

	}

	/**
	 * Creates a bold label.
	 * 
	 * @param column
	 *            the column
	 * @param row
	 *            the row
	 * @param contents
	 *            the contents
	 * @return the label
	 */
	private Label createBoldLabel(int column, int row, String contents) {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16,
				WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);
		return new Label(column, row, contents, times16format);
	}
}
