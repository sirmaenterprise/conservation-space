/**
 * 
 */
package com.sirma.itt.seip.eai.content.tool.service.writer;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;
import com.sirma.itt.seip.eai.content.tool.service.reader.EAISpreadsheetParserFactory;

/**
 * Tests for {@link EAISpreadsheetWriterFactory}
 * 
 * @author gshevkedov
 */
public class EAISpreadsheetWriterFactoryTest {

	@Test
	public void getWriter() throws IOException {
		SpreadsheetSheet sheet = new SpreadsheetSheet(
				new XSSFWorkbook(EAISpreadsheetParserFactory.class.getResourceAsStream("dataImportFile.xlsx")));
		assertTrue(EAISpreadsheetWriterFactory.getWriter(sheet) instanceof EAIApachePoiWriter);
	}
}
