package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.poi.ss.usermodel.Workbook;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

import com.monitorjbl.xlsx.StreamingReader;

/**
 * Xlsx parser for parsing excel rows to spreadsheet entries.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Singleton
@Extension(target = SpreadsheetParser.TARGET_NAME, order = 20)
public class EAIXlsxParser extends EAIApachePoiParser {

	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedRows)
			throws EAIException {
		File tempFile = tempFileProvider.createTempFile("xlsxParserTemp", ".xlsx");
		try {
			content.writeTo(tempFile);
		} catch (IOException e) {
			throw new EAIException("Something went wrong with the spreadsheet processing!", e);
		}
		try (Workbook workbook = StreamingReader.builder().readComments().open(tempFile)) {
			return readSpreadsheet(workbook, requestedRows);
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Something went wrong with the spreadsheet processing!", e);
		} finally {
			tempFileProvider.deleteFile(tempFile);
		}
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.XLSX_MIMETYPE.equals(content.getMimeType());
	}
}
