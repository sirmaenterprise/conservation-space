package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.sirma.itt.seip.Pair;
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

	protected static SpreadsheetSheet readSpreadsheet(Workbook workbook,
			Map<String, Collection<String>> requestedEntries) {
		SpreadsheetSheet spreadSheet = new SpreadsheetSheet();
		if (workbook.getNumberOfSheets() == 0) {
			return spreadSheet;
		}
		Map<String, Collection<String>> entriesToProcess = requestedEntries;
		Set<String> requestedSheets;
		if (entriesToProcess == null) {
			// iterate all sheets and add all indexes
			requestedSheets = new LinkedHashSet<>();
			workbook.sheetIterator().forEachRemaining(
					sheet -> requestedSheets.add(SpreadsheetUtil.getSheetId(workbook.getSheetIndex(sheet))));
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
			Map<String, Collection<String>> entriesToProcess, String sheetId) {
		Collection<String> sheetEntries = entriesToProcess.get(sheetId);
		Sheet firstSheet = workbook.getSheetAt(SpreadsheetUtil.getSheetIndex(sheetId));
		Iterator<Row> iterator = firstSheet.iterator();
		Pair<List<String>, Map<String, String>> columnHeaders = null;
		while (iterator.hasNext()) {
			Row nextRow = iterator.next();
			if (isItalicOrStrikeout(workbook, nextRow)) {
				continue;
			}
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			if (columnHeaders == null) {
				columnHeaders = processHeaderRow(cellIterator);
			} else {
				String rowId = SpreadsheetUtil.getRowId(nextRow);
				// null means, get all rows
				if (sheetEntries == null || sheetEntries.contains(rowId)) {
					spreadSheet.addEntry(processDataRow(columnHeaders, rowId, sheetId, cellIterator));
				}
			}
		}
	}

	private static boolean isItalicOrStrikeout(Workbook workbook, Cell cell) {
		CellStyle cellStyle = cell.getCellStyle();
		if (cellStyle != null) {
			return isItalicOrStrikeout(workbook, cellStyle.getFontIndex());
		}
		return false;
	}

	private static boolean isItalicOrStrikeout(Workbook workbook, Row row) {
		if (row.isFormatted()) {
			return isItalicOrStrikeout(workbook, row.getRowStyle().getFontIndex());
		}
		return false;
	}

	private static boolean isItalicOrStrikeout(Workbook workbook, short index) {
		Font entryFont = workbook.getFontAt(index);
		if (entryFont == null) {
			return false;
		}
		return entryFont.getStrikeout() || entryFont.getItalic();
	}

	private static Pair<List<String>, Map<String, String>> processHeaderRow(Iterator<Cell> cellIterator) {
		List<String> columnHeaders = new LinkedList<>();
		Map<String, String> columnConfiguration = new HashMap<>();
		while (cellIterator.hasNext()) {
			// we save column headers
			Cell nextHeader = cellIterator.next();
			if (nextHeader.getCellComment() != null) {
				columnConfiguration.put(nextHeader.getStringCellValue(),
						nextHeader.getCellComment().getString().getString());
			}
			columnHeaders.add(nextHeader.getStringCellValue());
		}
		return new Pair<>(Collections.unmodifiableList(columnHeaders),
				Collections.unmodifiableMap(columnConfiguration));
	}

	private static SpreadsheetEntry processDataRow(Pair<List<String>, Map<String, String>> columnHeaders, String rowId,
			String sheetId, Iterator<Cell> cellIterator) {
		SpreadsheetEntry entry = new SpreadsheetEntry(sheetId, rowId);
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (cell.getColumnIndex() < columnHeaders.getFirst().size()) {
				fillEntryProperties(columnHeaders, entry, cell);
			}
		}
		return entry;
	}

	private static void fillEntryProperties(Pair<List<String>, Map<String, String>> columnHeaders,
			SpreadsheetEntry entry, Cell cell) {
		if (isItalicOrStrikeout(cell.getRow().getSheet().getWorkbook(), cell)) {
			return;
		}
		CellType cellType = cell.getCellTypeEnum();
		int columnIndex = cell.getColumnIndex();
		String columnId = columnHeaders.getFirst().get(columnIndex);
		if (cellType == CellType.STRING) {
			// we save only valid values not empty values
			if (StringUtils.isNotBlank(cell.getStringCellValue())) {
				String[] multiLine = cell.getStringCellValue().split("[\\r\\n\\p{Zl}]+");
				if (multiLine.length > 1) {
					entry.put(columnId, Arrays.asList(multiLine));
				} else {
					entry.put(columnId, cell.getStringCellValue());
				}
			}
		} else if (cellType == CellType.BOOLEAN) {
			entry.put(columnId, Boolean.valueOf(cell.getBooleanCellValue()));
		} else if (cellType == CellType.NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				entry.put(columnId, cell.getDateCellValue());
			} else {
				entry.put(columnId, Double.valueOf(cell.getNumericCellValue()));
			}
		}
		entry.setConfiguration(columnHeaders.getSecond());
	}
}
