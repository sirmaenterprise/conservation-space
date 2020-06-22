package com.sirma.itt.seip.eai.content.tool.service.reader;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetEntry;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;

/**
 * Abstract class {@link EAIApachePoiParser} contains common bridge methods for processing .xlsx and .xls spreadsheets
 * with apache poi library.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class EAIApachePoiParser implements EAISpreadsheetParser {

	EAIApachePoiParser() {
		// protected constructor
	}

	/** Represents a spreadsheet header. */
	private static class SpreadsheetHeader {
		private List<String> columnHeaders;
		private Map<String, String> columnConfiguration;

		SpreadsheetHeader() {
			this.columnHeaders = new LinkedList<>();
			this.columnConfiguration = new LinkedHashMap<>();
		}

		List<String> getColumnHeaders() {
			return columnHeaders;
		}

		Map<String, String> getColumnConfiguration() {
			return columnConfiguration;
		}

	}

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> request)
			throws EAIException {
		try (InputStream inputStream = content.getInputStream()) {
			return readSpreadsheet(WorkbookFactory.create(inputStream), request);
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Failure during spreadsheet processing: " + e.getMessage(), e);
		}
	}

	protected static SpreadsheetSheet readSpreadsheet(Workbook workbook,
			Map<String, Collection<String>> requestedEntries) throws EAIException {
		SpreadsheetSheet spreadSheet = new SpreadsheetSheet(workbook);
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
			Map<String, Collection<String>> entriesToProcess, String sheetId) throws EAIException {
		Collection<String> sheetEntries = entriesToProcess.get(sheetId);
		Sheet firstSheet = workbook.getSheetAt(SpreadsheetUtil.getSheetIndex(sheetId));
		Iterator<Row> iterator = firstSheet.iterator();
		SpreadsheetHeader columnHeaders = null;
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
					spreadSheet.addEntry(processDataRow(columnHeaders, nextRow.getRowNum(), sheetId, cellIterator));
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

	private static SpreadsheetHeader processHeaderRow(Iterator<Cell> cellIterator) throws EAIException {
		SpreadsheetHeader header = new SpreadsheetHeader();
		while (cellIterator.hasNext()) {
			// we save column headers
			Cell nextHeader = cellIterator.next();
			if (nextHeader.getCellComment() != null) {
				header.getColumnConfiguration().put(nextHeader.getStringCellValue(),
						nextHeader.getCellComment().getString().getString());
			}
			header.getColumnHeaders().add(nextHeader.getStringCellValue());
		}
		if (!header.getColumnHeaders().contains(EAIContentConstants.CONTENT_SOURCE)) {
			throw new EAIException("There is no column " + EAIContentConstants.CONTENT_SOURCE
					+ " in the spreadsheet and no content could be uploaded");
		}
		return header;
	}

	private static SpreadsheetEntry processDataRow(SpreadsheetHeader columnHeader, int rowId, String sheetId,
			Iterator<Cell> cellIterator) {
		SpreadsheetEntry entry = new SpreadsheetEntry(SpreadsheetUtil.getSheetIndex(sheetId), rowId);
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (cell.getColumnIndex() < columnHeader.getColumnHeaders().size()) {
				fillEntryProperties(columnHeader, entry, cell);
			}
		}
		return entry;
	}

	private static void fillEntryProperties(SpreadsheetHeader columnHeader, SpreadsheetEntry entry, Cell cell) {
		if (isItalicOrStrikeout(cell.getRow().getSheet().getWorkbook(), cell)) {
			return;
		}
		CellType cellType = cell.getCellTypeEnum();
		int columnIndex = cell.getColumnIndex();
		String columnId = columnHeader.getColumnHeaders().get(columnIndex);
		if (cellType == CellType.STRING) {
			// we save only valid values not empty values
			if (!cell.getStringCellValue().isEmpty()) {
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
			setNumericValue(entry, cell, columnId);
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
				entry.put(columnId, Double.valueOf(cellValue));
			}
		}
	}

	private static boolean isValidDateFormat(Cell cell, double cellValue) {
		CellStyle cellStyle = cell.getCellStyle();
		if (cellStyle == null || !DateUtil.isValidExcelDate(cellValue)) {
			return false;
		}
		// workaround for apache poi failure to resolve various dates formats
		return cellStyle.getDataFormatString().contains("dd") || cellStyle.getDataFormatString().contains("yy")
				|| cellStyle.getDataFormatString().contains("mm");
	}
}
