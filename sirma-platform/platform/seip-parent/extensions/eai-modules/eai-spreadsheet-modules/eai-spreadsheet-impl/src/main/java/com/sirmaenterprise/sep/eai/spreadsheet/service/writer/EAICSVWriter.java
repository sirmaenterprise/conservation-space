package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.IMPORT_STATUS;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;
import com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil;

/**
 * Writes data back to csv file. Generates csv file report.
 *
 * @author gshevkedov
 */
@Extension(target = SpreadsheetWriter.TARGET_NAME, order = 3)
public class EAICSVWriter implements SpreadsheetWriter {

	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.CSV_MIMETYPE.equals(content.getMimeType())
				|| EAISpreadsheetConstants.TEXT_MIMETYPE.equals(content.getMimeType());
	}

	@Override
	public Content writerEntries(ContentInfo content, List<SpreadsheetEntry> entries) throws EAIException {
		Map<String, Map<String, SpreadsheetEntry>> mappedModels = new LinkedHashMap<>();
		for (SpreadsheetEntry spreadsheetEntry : entries) {
			mappedModels.computeIfAbsent(spreadsheetEntry.getSheet(), entry -> new LinkedHashMap<>()).put(
					spreadsheetEntry.getExternalId(), spreadsheetEntry);
		}
		try (Scanner source = new Scanner(content.getInputStream())) {
			StringBuilder sheet = new StringBuilder();
			for (Entry<String, Map<String, SpreadsheetEntry>> sheetEntry : mappedModels.entrySet()) {
				sheet.append(processSheet(sheetEntry, source));
			}
			return Content.createFrom(content).setContent(generateFile(sheet));
		} catch (Exception e) {
			throw new EAIException("Updating csv file failed", e);
		}
	}

	static String processSheet(Entry<String, Map<String, SpreadsheetEntry>> sheetEntry, Scanner source)
			throws EAIException {
		StringBuilder sheet = new StringBuilder();
		String headerRow = "";
		if (source.hasNext()) {
			headerRow = source.nextLine();
		}
		String delimiter = SpreadsheetUtil.getCSVDelimiter(headerRow);
		List<String> header = new LinkedList<>(Arrays.asList(headerRow.split(delimiter)));
		// we need to start writing from second row because the first one is for column headers
		int rowNum = 2;
		while (source.hasNext()) {
			String nextRow = source.nextLine();
			List<String> row = new LinkedList<>(Arrays.asList(nextRow.split(delimiter)));
			String rowId = String.valueOf(rowNum);
			if (sheetEntry.getValue().containsKey(rowId)) {
				addOrUpdateColumnValues(row, header, sheetEntry.getValue().get(rowId));
				setImportStatus(sheet, header, row, EAISpreadsheetConstants.SUCCESSFULLY_IMPORTED);
			} else {
				fillRowWithEmptyValues(header, row);
				setImportStatus(sheet, header, row, EAISpreadsheetConstants.NOT_IMPORTED);
			}
			rowNum++;
		}
		// we insert header row in 1st line
		sheet.insert(0, new StringBuilder(sanitizeRow(header)).append(System.lineSeparator()));
		return sheet.toString();
	}

	static void setImportStatus(StringBuilder sheet, List<String> header, List<String> row, String statusValue) {
		if (header.indexOf(IMPORT_STATUS) != -1) {
			row.set(header.indexOf(IMPORT_STATUS), statusValue);
			sheet.append(sanitizeRow(row)).append(System.lineSeparator());
		} else {
			header.add(IMPORT_STATUS);
			row.add(statusValue);
			sheet.append(sanitizeRow(row)).append(System.lineSeparator());
		}
	}

	static void addOrUpdateColumnValues(List<String> row, List<String> headerRow, SpreadsheetEntry entry) {
		StringBuilder builder = new StringBuilder();
		Set<Entry<String, Object>> entrySet = entry.getProperties().entrySet();
		for (Entry<String, Object> nextValue : entrySet) {
			builder.append(setIfDiff(headerRow, row, nextValue.getKey(), String.valueOf(nextValue.getValue())));
		}
	}

	static String setIfDiff(List<String> headerRow, List<String> row, String key, String value) {
		StringBuilder builder = new StringBuilder();
		int cellIndex = -1;
		for (int i = 0; i < headerRow.size(); i++) {
			if (key.equals(headerRow.get(i).trim())) {
				cellIndex = i;
				break;
			}
		}

		if (cellIndex == -1) {
			headerRow.add(key);
		}

		fillRowWithEmptyValues(headerRow, row);
		if (cellIndex == -1) {
			// we set new value to the last column
			row.set(headerRow.size() - 1, value);
		} else {
			row.set(cellIndex, value);
		}
		return builder.append(sanitizeRow(row)).toString();
	}

	static void fillRowWithEmptyValues(List<String> headerRow, List<String> row) {
		while (row.size() < headerRow.size()) {
			row.add("");
		}
	}

	private File generateFile(StringBuilder sheet) throws IOException {
		File file = tempFileProvider.createTempFile(UUID.randomUUID().toString(), ".csv");
		FileUtils.writeStringToFile(file, sheet.toString(), StandardCharsets.UTF_8);
		return file;
	}

	static String sanitizeRow(List<String> row) {
		return String.join(",", row);
	}
}
