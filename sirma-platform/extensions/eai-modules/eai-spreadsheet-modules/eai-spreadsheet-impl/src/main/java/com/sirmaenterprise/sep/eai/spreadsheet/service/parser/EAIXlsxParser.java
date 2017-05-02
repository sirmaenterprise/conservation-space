package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import java.util.Collection;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

/**
 * Xlsx parser for parsing excel rows to spreadsheet entries.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Singleton
@Extension(target = SpreadsheetParser.TARGET_NAME, order = 20)
public class EAIXlsxParser extends EAIApachePoiParser {

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedRows)
			throws EAIException {
		try (Workbook workbook = new XSSFWorkbook(content.getInputStream())) {
			return readSpreadsheet(workbook, requestedRows);
		} catch (Exception e) {
			throw new EAIException("Something went wrong with the spreadsheet processing!", e);
		}
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.XLSX_MIMETYPE.equals(content.getMimeType());
	}
}
