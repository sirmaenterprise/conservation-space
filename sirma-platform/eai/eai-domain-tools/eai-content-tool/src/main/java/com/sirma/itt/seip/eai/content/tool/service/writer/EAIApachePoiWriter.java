package com.sirma.itt.seip.eai.content.tool.service.writer;

import static com.sirma.itt.seip.eai.content.tool.service.reader.SpreadsheetUtil.getSheetIndex;

import java.io.File;
import java.io.FileOutputStream;
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

import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetEntry;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;
import com.sirma.itt.seip.eai.content.tool.service.reader.SpreadsheetUtil;

/**
 * Writes data to Apache POI supported {@link Workbook}
 *
 * @author bbanchev
 */
public class EAIApachePoiWriter implements EAISpreadsheetWriter {
	@Override
	public File writerEntries(SpreadsheetSheet sheet, File file) throws EAIException {
		return store(writerEntries(sheet.getSource(), sheet.getEntries()), file);
	}

	protected static Workbook writerEntries(Workbook workbook, List<SpreadsheetEntry> entries) {
		Map<String, Map<String, SpreadsheetEntry>> mappedModels = new LinkedHashMap<>();
		for (SpreadsheetEntry spreadsheetEntry : entries) {
			mappedModels.computeIfAbsent(spreadsheetEntry.getId().getSheetId(), entry -> new LinkedHashMap<>()).put(
					spreadsheetEntry.getId().getExternalId(), spreadsheetEntry);
		}

		Set<Integer> processedSheets = new HashSet<>();
		for (Entry<String, Map<String, SpreadsheetEntry>> sheetEntry : mappedModels.entrySet()) {
			Sheet sheet = workbook.getSheetAt(SpreadsheetUtil.getSheetIndex(sheetEntry.getKey()));
			processSheet(sheetEntry, sheet);
			processedSheets.add(Integer.valueOf(getSheetIndex(sheetEntry.getKey())));
		}

		int sheetCount = workbook.getNumberOfSheets();
		if (processedSheets.size() < sheetCount) {
			for (int i = 0; i < sheetCount; i++) {
				if (!processedSheets.contains(Integer.valueOf(i))) {
					processSheet(workbook.getSheetAt(i));
				}
			}
		}
		return workbook;
	}

	static void processSheet(Entry<String, Map<String, SpreadsheetEntry>> sheetEntry, Sheet sheet) {
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row headerRow = null;
		if (rowIterator.hasNext()) {
			headerRow = rowIterator.next();
		}
		while (rowIterator.hasNext()) {
			Row nextRow = rowIterator.next();
			String rowId = SpreadsheetUtil.getRowId(nextRow.getRowNum());
			if (sheetEntry != null && sheetEntry.getValue().containsKey(rowId)) {
				addOrUpdateColumnValues(nextRow, headerRow, sheetEntry.getValue().get(rowId));
			}
		}
	}

	static void processSheet(Sheet sheet) {
		processSheet(null, sheet);
	}

	static void addOrUpdateColumnValues(Row row, Row header, SpreadsheetEntry entry) {
		Set<Entry<String, Object>> entrySet = entry.getProperties().entrySet();
		for (Entry<String, Object> nextValue : entrySet) {
			setIfDiff(header, row, nextValue.getKey(), nextValue.getValue());
		}
	}

	static void setIfDiff(Row header, Row row, String key, Object value) {
		Iterator<Cell> cellIterator = header.cellIterator();
		int cellIndex = -1;
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (key.equals(cell.getStringCellValue())) {
				cellIndex = cell.getColumnIndex();
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
			row.getCell(cellIndex).setCellValue(((Boolean) cellValue).booleanValue());
		} else if (cellValue instanceof Integer) {
			row.getCell(cellIndex).setCellValue(((Integer) cellValue).intValue());
		} else if (cellValue instanceof Date) {
			row.getCell(cellIndex).setCellValue((Date) cellValue);
		}
	}

	protected static File store(Workbook workbook, File location) throws EAIException {
		try (Workbook output = workbook; OutputStream outStream = new FileOutputStream(location)) {
			output.write(outStream);
		} catch (Exception e) {
			throw new EAIException("Failure during persistence of workbook at location: " + location, e);
		}
		return location;
	}
}