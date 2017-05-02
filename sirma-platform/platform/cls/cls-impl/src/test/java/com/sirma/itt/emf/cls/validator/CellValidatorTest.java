package com.sirma.itt.emf.cls.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.cls.columns.CLColumn;

import jxl.Cell;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

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
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);
		int index = 0;
		for (CLColumn column : CLColumn.values()) {
			Label label = new Label(index, 0, column.getName(), times16format);
			sheet.addCell(label);
			index++;
		}
	}

	/**
	 * Testing the isCellBold method of the {@link CellValidator} with a bold cell.
	 */
	@Test
	public void testBoldCell() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);

		Cell cell = new Label(0, 0, "test", times16format);
		assertTrue(CellValidator.isCellBold(cell));
	}

	/**
	 * Testing the isCellBold method of the {@link CellValidator} with a normal (non-bold) cell.
	 */
	@Test
	public void testBoldCellFail() {
		Cell cell = new Label(0, 0, "test");
		assertFalse(CellValidator.isCellBold(cell));
	}

	/**
	 * Testing the isCellEmpty method of the {@link CellValidator} with a non-empty cell.
	 */
	@Test
	public void testCellNotEmpty() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);

		Cell cell = new Label(0, 0, "test", times16format);
		assertFalse(CellValidator.isCellEmpty(cell));
	}

	/**
	 * Testing the isCellEmpty method of the {@link CellValidator} with an empty cell.
	 */
	@Test
	public void testCellEmpty() {
		WritableFont times16font = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD, true);
		WritableCellFormat times16format = new WritableCellFormat(times16font);
		Cell cell = new Label(0, 0, "", times16format);
		assertTrue(CellValidator.isCellEmpty(cell));
	}
}
