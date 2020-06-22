package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.junit.Test;

import com.monitorjbl.xlsx.StreamingReader;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.parser.EAIApachePoiParser;

/**
 * Test for {@link StreamingXlsxWriter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 29/01/2018
 */
@SuppressWarnings("static-method")
public class StreamingXlsxWriterTest {

	@Test
	public void write_shouldProduceSimilarFile() throws Exception {
		File inputFile = new File("./src/test/resources/SourceFile.xlsx");
		File outputFile = new File("./src/test/resources/TestOutputFile1.xlsx");

		try {
			new StreamingXlsxWriter().write(inputFile, outputFile, Collections.emptyList());

			assertTrue(outputFile.length() > 0);

			SpreadsheetSheet expected = getSpreadsheetSheet(inputFile);
			assertFalse(expected.getEntries().isEmpty());
			SpreadsheetSheet actual = getSpreadsheetSheet(outputFile);
			assertFalse(actual.getEntries().isEmpty());
			assertEquals(expected, actual);
		} finally {
			outputFile.delete();
		}
	}

	private static SpreadsheetSheet getSpreadsheetSheet(File outputFile) throws IOException, EAIException {
		try (Workbook workbook = StreamingReader.builder().readComments().open(outputFile)) {
			return EAIApachePoiParser.readSpreadsheet(workbook, null);
		}
	}

	@Test
	public void write_shouldUpdateExistingCellsData() throws Exception {
		File inputFile = new File("./src/test/resources/SourceFile.xlsx");
		File outputFile = new File("./src/test/resources/TestOutputFile2.xlsx");

		try {
			SpreadsheetSheet spreadsheetSheet = getSpreadsheetSheet(inputFile);

			SpreadsheetEntry sheet1Row4 = selectCell("1", 4, spreadsheetSheet);
			sheet1Row4.getProperties().put("dcterms:title", "Updated title from test");

			SpreadsheetEntry sheet1Row5 = selectCell("1", 5, spreadsheetSheet);
			sheet1Row5.getProperties().put("cia:creationDate", "1926-1927");

			SpreadsheetEntry sheet2Row3 = selectCell("2", 3, spreadsheetSheet);
			sheet2Row3.getProperties().put("cia:hasAssignedConservator", "BBonev");

			SpreadsheetEntry sheet2Row4 = selectCell("2", 4, spreadsheetSheet);
			sheet2Row4.getProperties().put("cia:projectYear", Arrays.asList("1926", "1928"));

			new StreamingXlsxWriter().write(inputFile, outputFile, spreadsheetSheet.getEntries());

			assertTrue(outputFile.length() > 0);

			SpreadsheetSheet actual = getSpreadsheetSheet(outputFile);
			assertEquals(spreadsheetSheet, actual);
		} finally {
			outputFile.delete();
		}
	}

	@Test
	public void write_shouldAddNewColumns() throws Exception {
		File inputFile = new File("./src/test/resources/SourceFile.xlsx");
		File outputFile = new File("./src/test/resources/TestOutputFile3.xlsx");

		try {
			SpreadsheetSheet spreadsheetSheet = getSpreadsheetSheet(inputFile);

			SpreadsheetEntry sheet1Row4 = selectCell("1", 4, spreadsheetSheet);
			sheet1Row4.getProperties().put("emf:newColumn1", "row4");

			SpreadsheetEntry sheet1Row5 = selectCell("1", 5, spreadsheetSheet);
			sheet1Row5.getProperties().put("emf:newColumn1", new Date());
			sheet1Row5.getProperties().put("emf:newColumn2", 42.0);
			sheet1Row5.getProperties().put("emf:newColumn3", Boolean.TRUE);

			new StreamingXlsxWriter().write(inputFile, outputFile, spreadsheetSheet.getEntries());

			assertTrue(outputFile.length() > 0);

			SpreadsheetSheet actual = getSpreadsheetSheet(outputFile);
			assertEquals(spreadsheetSheet, actual);
		} finally {
			outputFile.delete();
		}
	}

	/**
	 * NOTE: takes about 2-3 minutes to complete, depending on environment resources
	 * <p>
	 * In this case the bigger files become invalid in case of excel standard and zip encoding (the file is considered
	 * zip bomb)
	 */
	@Test
	public void write_shouldNotProduceExcessiveFontsAndCellStyles() throws Exception {
		File inputFile = new File("./src/test/resources/tooManyFonts.xlsx");
		File outputFile = new File("./src/test/resources/TestOutputFile4.xlsx");

		try {
			SpreadsheetSheet spreadsheetSheet = getSpreadsheetSheet(inputFile);

			new StreamingXlsxWriter().write(inputFile, outputFile, spreadsheetSheet.getEntries());

			assertTrue(outputFile.length() > 0);

			SpreadsheetSheet actual = getSpreadsheetSheet(outputFile);
			assertEquals(spreadsheetSheet, actual);
		} finally {
			outputFile.delete();
		}
	}

	private SpreadsheetEntry selectCell(String sheetId, int row, SpreadsheetSheet spreadsheetSheet) {
		return spreadsheetSheet.getEntries().stream()
				.filter(entry -> sheetId.equals(entry.getSheet()))
				.filter(entry -> entry.getExternalId().equals(String.valueOf(row)))
				.findFirst().orElseThrow(IllegalArgumentException::new);
	}

	@Test
	public void write_shouldHaveSimillarStructure() throws Exception {
		File inputFile = new File("./src/test/resources/SourceFile.xlsx");
		File outputFile = new File("./src/test/resources/TestOutputFile5.xlsx");

		try {
			new StreamingXlsxWriter().write(inputFile, outputFile, Collections.emptyList());

			assertTrue(outputFile.length() > 0);

			try (Workbook expected = StreamingReader.builder().readComments().open(inputFile);
					Workbook actual = StreamingReader.builder().readComments().open(outputFile);
					Workbook actualStat = StreamingReader.builder().readComments().open(outputFile)) {
				assertEquals(expected.getNumberOfSheets(), actual.getNumberOfSheets());
				for (int sheetIndex = 0; sheetIndex < expected.getNumberOfSheets(); sheetIndex++) {
					Sheet expectedSheet = expected.getSheetAt(sheetIndex);
					Sheet actualSheet = actual.getSheetAt(sheetIndex);
					Sheet actualSheetStat = actualStat.getSheetAt(sheetIndex);
					Iterator<Row> expectedIt = expectedSheet.rowIterator();
					Iterator<Row> actualIt = actualSheet.rowIterator();
					Iterator<Row> actualItStat = actualSheetStat.rowIterator();
					Row expRow;
					Row resRow;
					Row expRowStat;
					int expRowLength =  actualItStat.next().getLastCellNum();
					int rowIndex = 0;
					while (expectedIt.hasNext() && actualIt.hasNext() && actualItStat.hasNext()) {
						expRow = expectedIt.next();
						resRow = actualIt.next();
						expRowStat = actualItStat.next();
						assertEquals("Result rows should have same columns count", expRowLength, expRowStat.getLastCellNum());
						assertTrue("Result sheet rows length should be more or equal to input row length", expRow.getLastCellNum() <= resRow.getLastCellNum());
						checkStyles(expRow.getRowStyle(), resRow.getRowStyle(), expected, actual, new CellAddress(rowIndex, 0));
						int numberOfColumns = resRow.getLastCellNum()-1;
						for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
							Cell expectedCell = expRow.getCell(columnIndex);
							Cell actualCell = resRow.getCell(columnIndex);
							if (expectedCell != null && actualCell != null) {
								compareCells(expectedCell, actualCell, expected, actual);
							} else if (expectedCell == null && actualCell != null && !("ImportStatus").equalsIgnoreCase(actualCell.getStringCellValue())){
								bothShouldBeNull("cells", expectedCell, actualCell, new CellAddress(rowIndex, columnIndex));
							}
						}
						rowIndex++;
					}
				}
			}
		} finally {
			outputFile.delete();
		}
	}

	private static void compareCells(Cell expectedCell, Cell actualCell, Workbook expected, Workbook actual) {
		assertEquals(expectedCell.getCellTypeEnum(), expectedCell.getCellTypeEnum());
		if (expectedCell.getCellTypeEnum() == CellType.STRING) {
			assertEquals(expectedCell.getStringCellValue(), actualCell.getStringCellValue());
		} else if (DateUtil.isCellDateFormatted(expectedCell) && DateUtil.isCellDateFormatted(actualCell)) {
			assertEquals(expectedCell.getDateCellValue(), actualCell.getDateCellValue());
		} else if (expectedCell.getCellTypeEnum() == CellType.NUMERIC) {
			assertEquals(expectedCell.getNumericCellValue(), actualCell.getNumericCellValue(), 0.00001);
		} else if (expectedCell.getCellTypeEnum() == CellType.BOOLEAN) {
			assertEquals(expectedCell.getBooleanCellValue(), actualCell.getBooleanCellValue());
		} else if (expectedCell.getCellTypeEnum() == CellType.FORMULA) {
			assertEquals(expectedCell.getCellFormula(), actualCell.getCellFormula());
		}
		Comment expectedComment = expectedCell.getCellComment();
		Comment actualComment = actualCell.getCellComment();
		if (expectedComment != null && actualComment != null) {
			assertEquals(expectedComment.getString().getString(), actualComment.getString().getString());
			// we cant compare authors as author field cannot be set in the new comment
			// assertEquals(expectedComment.getAuthor(), actualComment.getAuthor())
		} else {
			bothShouldBeNull("comments", expectedComment, actualComment, expectedCell.getAddress());
		}

		CellStyle expectedStyle = expectedCell.getCellStyle();
		CellStyle actualStyle = actualCell.getCellStyle();

		checkStyles(expectedStyle, actualStyle, expected, actual, expectedCell.getAddress());
	}

	private static void checkStyles(CellStyle expectedStyle, CellStyle actualStyle, Workbook expected, Workbook actual,
			CellAddress cellAddress) {
		if (expectedStyle != null && actualStyle != null) {

			assertEquals(expectedStyle.getAlignmentEnum(), actualStyle.getAlignmentEnum());
			assertEquals(expectedStyle.getVerticalAlignmentEnum(), actualStyle.getVerticalAlignmentEnum());
			assertEquals(expectedStyle.getBottomBorderColor(), actualStyle.getBottomBorderColor());
			assertEquals(expectedStyle.getTopBorderColor(), actualStyle.getTopBorderColor());
			assertEquals(expectedStyle.getRightBorderColor(), actualStyle.getRightBorderColor());
			assertEquals(expectedStyle.getLeftBorderColor(), actualStyle.getLeftBorderColor());
			assertEquals(expectedStyle.getBorderTopEnum(), actualStyle.getBorderTopEnum());
			assertEquals(expectedStyle.getBorderBottomEnum(), actualStyle.getBorderBottomEnum());
			assertEquals(expectedStyle.getBorderLeftEnum(), actualStyle.getBorderLeftEnum());
			assertEquals(expectedStyle.getBorderRightEnum(), actualStyle.getBorderRightEnum());
			assertEquals(expectedStyle.getDataFormatString(), actualStyle.getDataFormatString());
			assertEquals(expectedStyle.getFillBackgroundColor(), actualStyle.getFillBackgroundColor());
			assertEquals(expectedStyle.getFillForegroundColor(), actualStyle.getFillForegroundColor());

			assertEquals(expectedStyle.getWrapText(), actualStyle.getWrapText());
			assertEquals(expectedStyle.getRotation(), actualStyle.getRotation());
			assertEquals(expectedStyle.getIndention(), actualStyle.getIndention());
			assertEquals(expectedStyle.getShrinkToFit(), actualStyle.getShrinkToFit());
			assertEquals(expectedStyle.getQuotePrefixed(), actualStyle.getQuotePrefixed());
			assertEquals(expectedStyle.getLocked(), actualStyle.getLocked());
			assertEquals(expectedStyle.getHidden(), actualStyle.getHidden());

			Font expectedFont = expected.getFontAt(expectedStyle.getFontIndex());
			Font actualFont = actual.getFontAt(actualStyle.getFontIndex());

			if (expectedFont != null && actualFont != null) {
				assertEquals(expectedFont.getItalic(), actualFont.getItalic());
				assertEquals(expectedFont.getStrikeout(), actualFont.getStrikeout());
				assertEquals(expectedFont.getBold(), actualFont.getBold());
				// some files use old color indexing. there are some color duplications for index 0-7 and 8-15
				// as described in org.apache.poi.ss.usermodel.IndexedColors
				if (!(expectedFont.getColor() != actualFont.getColor()
						&& Math.abs(expectedFont.getColor() - actualFont.getColor()) == 8)) {
					assertEquals(expectedFont.getColor(), actualFont.getColor());
				}
				assertEquals(expectedFont.getFontHeightInPoints(), actualFont.getFontHeightInPoints());
				assertEquals(expectedFont.getFontName(), actualFont.getFontName());
				assertEquals(expectedFont.getUnderline(), actualFont.getUnderline());
				assertEquals(expectedFont.getCharSet(), actualFont.getCharSet());
			} else {
				bothShouldBeNull("cell fonts", expectedFont, actualFont, cellAddress);
			}
		} else {
			bothShouldBeNull("cell styles", expectedStyle, actualStyle, cellAddress);
		}
	}

	private static void bothShouldBeNull(String what, Object expected, Object actual, CellAddress address) {
		assertTrue(String.format("Both %s at row=%d, column%d should be null", what, address.getRow(), address.getColumn()), expected == null && actual == null);
	}
}
