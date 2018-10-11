package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;
import com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil;

/**
 * Abstract class {@link EAIApachePoiParser} contains common bridge methods for processing .xlsx and .xls spreadsheets
 * with apache poi library.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public abstract class EAIApachePoiParser implements SpreadsheetParser {

	protected EAIApachePoiParser() {
		// protected constructor
	}

	public static SpreadsheetSheet readSpreadsheet(Workbook workbook,
			Map<String, Collection<String>> requestedEntries) throws EAIException {
		SpreadsheetSheet spreadSheet = new SpreadsheetSheet();
		if (workbook.getNumberOfSheets() == 0) {
			return spreadSheet;
		}
		Map<String, Collection<String>> entriesToProcess = requestedEntries;
		Set<String> requestedSheets;
		if (entriesToProcess == null) {
			// iterate all sheets and add all indexes
			requestedSheets = new LinkedHashSet<>();

			for (Sheet sheet : workbook) {
				requestedSheets.add(SpreadsheetUtil.getSheetId(workbook.getSheetIndex(sheet)));
			}
			entriesToProcess = Collections.emptyMap();
		} else {
			requestedSheets = requestedEntries.keySet();
		}

		for (String sheetId : requestedSheets) {
			processSheet(workbook, spreadSheet, entriesToProcess, sheetId);
		}
		return spreadSheet;
	}

	private static void processSheet(Workbook workbook, SpreadsheetSheet spreadSheet,
			Map<String, Collection<String>> entriesToProcess, String sheetId) throws EAIReportableException {
		Collection<String> sheetEntries = entriesToProcess.get(sheetId);
		Sheet firstSheet = workbook.getSheetAt(SpreadsheetUtil.getSheetIndex(sheetId));
		Iterator<Row> iterator = firstSheet.iterator();
		Pair<Map<Integer, String>, Map<String, String>> columnHeaders = null;
		while (iterator.hasNext()) {
			Row nextRow = iterator.next();
			if (SpreadsheetUtil.isItalicOrStrikeout(workbook, nextRow)) {
				continue;
			}
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			if (columnHeaders == null) {
				columnHeaders = processHeaderRow(sheetId, cellIterator);
			} else {
				String rowId = SpreadsheetUtil.getRowId(nextRow);
				// null means, get all rows
				if (sheetEntries == null || sheetEntries.contains(rowId)) {
					addDataEntry(spreadSheet, processDataRow(workbook, columnHeaders, rowId, sheetId, cellIterator));
				}
			}
		}
	}

	private static Pair<Map<Integer, String>, Map<String, String>> processHeaderRow(String sheetId,
			Iterator<Cell> cellIterator) throws
			EAIReportableException {
		Map<Integer, String> columnHeaders = new LinkedHashMap<>();
		Map<String, String> queryBindings = new HashMap<>();
		while (cellIterator.hasNext()) {
			// we save column headers
			Cell nextHeader = cellIterator.next();
			String cellValue = nextHeader.getStringCellValue();
			if (StringUtils.isBlank(cellValue)) {
				// some of the files could have trailing empty cells
				continue;
			}
			if (nextHeader.getCellComment() != null) {
				String configKey = cellValue.toLowerCase();
				queryBindings.put(configKey, nextHeader.getCellComment().getString().getString());
			}
			if (columnHeaders.containsValue(cellValue)) {
				throw new EAIReportableException(
						String.format("Record[id=0, sheet=%s]:%nFound duplicate column %s", sheetId, cellValue));
			}
			columnHeaders.put(nextHeader.getColumnIndex(), cellValue);
		}
		return new Pair<>(Collections.unmodifiableMap(columnHeaders), Collections.unmodifiableMap(queryBindings));
	}

	private static SpreadsheetEntry processDataRow(Workbook workbook,
			Pair<Map<Integer, String>, Map<String, String>> columnHeaders, String rowId,
			String sheetId, Iterator<Cell> cellIterator) {
		SpreadsheetEntry entry = new SpreadsheetEntry(sheetId, rowId);
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (columnHeaders.getFirst().containsKey(cell.getColumnIndex())) {
				fillEntryProperties(workbook, columnHeaders, entry, cell);
			}
		}
		return entry;
	}

	private static void addDataEntry(SpreadsheetSheet spreadSheet, SpreadsheetEntry entry) {
		if (!entry.getProperties().isEmpty()) {
			spreadSheet.addEntry(entry);
		}
	}

	private static void fillEntryProperties(Workbook workbook, Pair<Map<Integer, String>, Map<String, String>> columnHeaders,
			SpreadsheetEntry entry, Cell cell) {
		if (SpreadsheetUtil.isItalicOrStrikeout(workbook, cell)) {
			return;
		}
		CellType cellType = cell.getCellTypeEnum();
		int columnIndex = cell.getColumnIndex();
		String columnId = columnHeaders.getFirst().get(columnIndex);
		if (cellType == CellType.STRING) {
			setStringValue(entry, cell, columnId);
		} else if (cellType == CellType.BOOLEAN) {
			entry.put(columnId, cell.getBooleanCellValue());
		} else if (cellType == CellType.NUMERIC) {
			setNumericValue(entry, cell, columnId);
		}
		columnHeaders.getSecond().forEach(entry::bind);
	}

	private static void setStringValue(SpreadsheetEntry entry, Cell cell, String columnId) {
		// we save only valid values not empty values
		if (StringUtils.isNotBlank(cell.getStringCellValue())) {
			String[] multiLine = cell.getStringCellValue().split("[\\r\\n\\p{Zl}]+");
			if (multiLine.length > 1) {
				entry.put(columnId, Arrays.asList(multiLine));
			} else {
				entry.put(columnId, cell.getStringCellValue());
			}
		}
	}

	private static void setNumericValue(SpreadsheetEntry entry, Cell cell, String columnId) {
		if (DateUtil.isCellDateFormatted(cell)) {
			entry.put(columnId, cell.getDateCellValue());
		} else {
			double cellValue = cell.getNumericCellValue();
			if (isValidDateFormat(cell, cellValue)) {
				entry.put(columnId, DateUtil.getJavaDate(cellValue));
			} else {
				entry.put(columnId, cellValue);
			}
		}
	}

	private static boolean isValidDateFormat(Cell cell, double cellValue) {
		CellStyle cellStyle = cell.getCellStyle();
		if (cellStyle == null || !DateUtil.isValidExcelDate(cellValue)) {
			return false;
		}
		// workaround for apache poi failure to resolve various dates formats
		String dataFormatString = cellStyle.getDataFormatString();
		return dataFormatString.contains("dd") || dataFormatString.contains("yy") || dataFormatString.contains("mm");
	}
}
