package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.IMPORT_STATUS;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil.getSheetIndex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;
import com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil;

/**
 * Writes data to Apache POI supported {@link Workbook}
 *
 * @author bbanchev
 */
public abstract class EAIApachePoiWriter implements SpreadsheetWriter {

	protected static void writerEntries(Workbook workbook, List<SpreadsheetEntry> entries) {
		Map<String, Map<String, SpreadsheetEntry>> mappedModels = new LinkedHashMap<>();
		for (SpreadsheetEntry spreadsheetEntry : entries) {
			mappedModels.computeIfAbsent(spreadsheetEntry.getSheet(), entry -> new LinkedHashMap<>()).put(
					spreadsheetEntry.getExternalId(), spreadsheetEntry);
		}

		Set<Integer> processedSheets = new HashSet<>();
		for (Entry<String, Map<String, SpreadsheetEntry>> sheetEntry : mappedModels.entrySet()) {
			int sheetIndex = getSheetIndex(sheetEntry.getKey());
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			processSheet(sheetEntry, sheet);
			processedSheets.add(sheetIndex);
		}

		int sheetCount = workbook.getNumberOfSheets();
		if (processedSheets.size() < sheetCount) {
			for (int i = 0; i < sheetCount; i++) {
				if (!processedSheets.contains(i)) {
					processSheet(workbook.getSheetAt(i));
				}
			}
		}
	}

	static void processSheet(Entry<String, Map<String, SpreadsheetEntry>> sheetEntry, Sheet sheet) {
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row headerRow = null;
		if (rowIterator.hasNext()) {
			headerRow = rowIterator.next();
		}
		while (rowIterator.hasNext()) {
			Row nextRow = rowIterator.next();
			String rowId = SpreadsheetUtil.getRowId(nextRow);
			if (!SpreadsheetUtil.isEmptyRow(nextRow)) {
				processRow(sheetEntry, headerRow, nextRow, rowId);
			}
		}
	}

	static void processSheet(Sheet sheet) {
		processSheet(null, sheet);
	}

	private static void processRow(Entry<String, Map<String, SpreadsheetEntry>> sheetEntry, Row headerRow, Row nextRow,
			String rowId) {
		if (sheetEntry != null && sheetEntry.getValue().containsKey(rowId)) {
			addOrUpdateColumnValues(nextRow, headerRow, sheetEntry.getValue().get(rowId));
			setIfDiff(headerRow, nextRow, IMPORT_STATUS, EAISpreadsheetConstants.SUCCESSFULLY_IMPORTED);
		} else {
			setIfDiff(headerRow, nextRow, IMPORT_STATUS, EAISpreadsheetConstants.NOT_IMPORTED);
		}
	}

	static void addOrUpdateColumnValues(Row row, Row header, SpreadsheetEntry entry) {
		Set<Entry<String, Object>> entrySet = entry.getProperties().entrySet();
		for (Entry<String, Object> nextValue : entrySet) {
			setIfDiff(header, row, nextValue.getKey(), nextValue.getValue());
		}
	}

	static void setIfDiff(Row header, Row row, String key, Object value) {
		Iterator<Cell> headerCellIterator = header.cellIterator();
		int cellIndex = -1;
		while (headerCellIterator.hasNext()) {
			Cell headerCell = headerCellIterator.next();
			if (key.equals(headerCell.getStringCellValue())) {
				cellIndex = headerCell.getColumnIndex();
				break;
			}
		}
		if (cellIndex == -1) {
			short count = header.getLastCellNum();
			header.createCell(count);
			row.createCell(count);
			cellIndex = count;
			header.getCell(cellIndex).setCellValue(key);
		}
		if (row.getCell(cellIndex) == null) {
			row.createCell(cellIndex);
		}
		fillCellValue(row, value, cellIndex);
	}

	static void fillCellValue(Row row, Object cellValue, int cellIndex) {
		if (cellValue instanceof String) {
			row.getCell(cellIndex).setCellValue((String) cellValue);
		} else if (cellValue instanceof Boolean) {
			row.getCell(cellIndex).setCellValue((Boolean) cellValue);
		} else if (cellValue instanceof Integer) {
			row.getCell(cellIndex).setCellValue((Integer) cellValue);
		} else if (cellValue instanceof Date) {
			row.getCell(cellIndex).setCellValue((Date) cellValue);
		}
	}

	protected abstract File generateFile(Workbook workbook) throws IOException;

	protected static File generateFile(Workbook workbook, File output) throws IOException {
		try (OutputStream outStream = new FileOutputStream(output)) {
			workbook.write(outStream);
		}
		return output;
	}
}
