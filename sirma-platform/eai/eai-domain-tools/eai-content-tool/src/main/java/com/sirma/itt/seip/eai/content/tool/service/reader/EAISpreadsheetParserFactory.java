package com.sirma.itt.seip.eai.content.tool.service.reader;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * A factory for creating a proper {@link EAISpreadsheetParser}.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class EAISpreadsheetParserFactory {

	private EAISpreadsheetParserFactory() {
		// private constructor
	}

	/**
	 * Get parser for given content. Might throw {@link EAIRuntimeException} on missing parser
	 * 
	 * @param content
	 *            to get the parser by mimetype for
	 * @return the {@link EAISpreadsheetParser} to the
	 */
	public static EAISpreadsheetParser getParser(ContentInfo content) {
		if (content.getMimetype() != null && content.getMimetype().contains("csv")) {
			throw new EAIRuntimeException("Missing parser for spreadsheet: " + content.getMimetype());
		}
		return new EAIApachePoiParser();
	}
}
