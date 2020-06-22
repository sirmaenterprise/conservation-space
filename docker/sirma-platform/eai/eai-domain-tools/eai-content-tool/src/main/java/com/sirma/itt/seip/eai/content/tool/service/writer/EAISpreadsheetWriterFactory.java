package com.sirma.itt.seip.eai.content.tool.service.writer;

import org.apache.poi.ss.SpreadsheetVersion;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;

/**
 * A factory for creating proper {@link EAISpreadsheetWriter}.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class EAISpreadsheetWriterFactory {

	private EAISpreadsheetWriterFactory() {
		//
	}

	/**
	 * Retrieves a writer for given workbook - {@link SpreadsheetSheet}
	 * 
	 * @param spreadsheet
	 *            the source data
	 * @return the writer for this sheet
	 */
	public static EAISpreadsheetWriter getWriter(SpreadsheetSheet spreadsheet) {
		SpreadsheetVersion spreadsheetVersion = spreadsheet.getSource().getSpreadsheetVersion();
		if (spreadsheetVersion == SpreadsheetVersion.EXCEL2007 || spreadsheetVersion == SpreadsheetVersion.EXCEL97) {
			return new EAIApachePoiWriter();
		}
		// return csv writer
		throw new EAIRuntimeException("Missing writer for spreadsheet: " + spreadsheet.getSource());
	}
}
