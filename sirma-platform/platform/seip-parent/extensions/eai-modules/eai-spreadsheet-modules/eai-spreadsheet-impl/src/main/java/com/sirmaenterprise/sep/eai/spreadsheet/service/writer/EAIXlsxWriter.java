package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;

/**
 * Writes data to xlsx file.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Extension(target = SpreadsheetWriter.TARGET_NAME, order = 2)
public class EAIXlsxWriter extends EAIApachePoiWriter {

	@Inject
	private TempFileProvider tempFileProvider;

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.XLSX_MIMETYPE.equals(content.getMimeType());
	}

	@Override
	public Content writerEntries(ContentInfo content, List<SpreadsheetEntry> entries) throws EAIException {
		try (XSSFWorkbook workbook = new XSSFWorkbook(content.getInputStream())) {
			writerEntries(workbook, entries);
			return Content.createFrom(content).setContent(generateFile(workbook));
		} catch (Exception e) {
			throw new EAIException("Updating xlsx file failed", e);
		}
	}

	@Override
	protected File generateFile(Workbook workbook) throws IOException {
		return generateFile(workbook, tempFileProvider.createTempFile(UUID.randomUUID().toString(), ".xlsx"));
	}

}
