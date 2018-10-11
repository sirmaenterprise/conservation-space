package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import java.util.Collection;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

/**
 * Xls parser for parsing excel rows to spreadsheet entries.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Singleton
@Extension(target = SpreadsheetParser.TARGET_NAME, order = 30)
public class EAIXlsParser extends EAIApachePoiParser {

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedRows)
			throws EAIException {
		try (Workbook workbook = new HSSFWorkbook(content.getInputStream())) {
			return readSpreadsheet(workbook, requestedRows);
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Something went wrong with the spreadsheet processing!", e);
		}
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.XLS_MIMETYPE.equals(content.getMimeType());
	}
}
