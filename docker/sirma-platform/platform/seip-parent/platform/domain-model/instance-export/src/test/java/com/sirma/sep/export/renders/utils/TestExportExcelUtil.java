package com.sirma.sep.export.renders.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;

/**
 * @author Boyan Tonchev.
 */
public class TestExportExcelUtil {

	public static File createEmptyTestFile(String extension) throws IOException {
		return File.createTempFile(UUID.randomUUID().toString(), extension);
	}

	public static void deleteFile(File file) {
		if (file != null && !file.delete()) {
			file.deleteOnExit();
		}
	}

	public static Sheet getSheet(File file, int sheetNumber) throws FileNotFoundException, IOException {
		FileInputStream inputStream = new FileInputStream(file);
		Workbook workbook = new XSSFWorkbook(inputStream);
		return workbook.getSheetAt(sheetNumber);
	}

	public static Sheet getHSSFSheet(File file, int sheetNumber) throws FileNotFoundException, IOException {
		FileInputStream inputStream = new FileInputStream(file);
		Workbook workbook = new HSSFWorkbook(inputStream);
		return workbook.getSheetAt(sheetNumber);
	}

	public static void assertStringValue(Sheet sheet, int rowNumber, int columnNumber, String expectedValue) {
		Assert.assertEquals(expectedValue, sheet.getRow(rowNumber).getCell(columnNumber).getStringCellValue().trim());
	}

	public static void assertCellIsNull(Sheet sheet, int rowNumber, int columnNumber) {
		Assert.assertNull(sheet.getRow(rowNumber).getCell(columnNumber));
	}

	public static void assertStringValue(Sheet sheet, int rowNumber, int columnNumber, String... expectedValue) {
		String cellValue = sheet.getRow(rowNumber).getCell(columnNumber).getStringCellValue().trim();
		for (String value : expectedValue) {
			Assert.assertTrue(cellValue.contains(value));
		}
	}

	public static void assertLinkValue(Sheet sheet, int rowNumber, int columnNumber, String expectedLinkLabel,
			String expectedLinkAddress) {
		Cell cell = sheet.getRow(rowNumber).getCell(columnNumber);
		Assert.assertEquals(expectedLinkAddress, cell.getHyperlink().getAddress().trim());
		Assert.assertEquals(expectedLinkLabel, cell.getStringCellValue().trim());
	}

	public static void assertStringValueNotExist(Sheet sheet, int rowNumber, String value) {
		Iterator<Cell> cellIterator = sheet.getRow(rowNumber).cellIterator();
		cellIterator.forEachRemaining(cell -> {
			String cellValue = cell.getStringCellValue().trim();
			Assert.assertFalse(cellValue.contains(value));
		});
	}
}
