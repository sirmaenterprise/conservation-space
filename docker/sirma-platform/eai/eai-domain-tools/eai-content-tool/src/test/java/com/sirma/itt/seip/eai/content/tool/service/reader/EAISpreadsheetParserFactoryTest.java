/**
 * 
 */
package com.sirma.itt.seip.eai.content.tool.service.reader;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * Tests for {@link EAISpreadsheetParserFactory}
 * 
 * @author gshevkedov
 */
public class EAISpreadsheetParserFactoryTest {

	@Test(expected = EAIRuntimeException.class)
	public void testGetParserWithInvalidParams() throws URISyntaxException, IOException {
		try (InputStream inputStream = EAISpreadsheetParserFactory.class.getResourceAsStream("dataImportFile.xlsx")) {
			ContentInfo content = new ContentInfo("text/csv", new URI("http://localhost/test"), inputStream);
			EAISpreadsheetParserFactory.getParser(content);
		}
	}

	@Test
	public void testGetParser() throws URISyntaxException, IOException {
		try (InputStream inputStream = EAISpreadsheetParserFactory.class.getResourceAsStream("dataImportFile.xlsx")) {
			ContentInfo content = new ContentInfo("text/plain", new URI("http://localhost/test"), inputStream);
			assertTrue(EAISpreadsheetParserFactory.getParser(content) instanceof EAIApachePoiParser);
		}
	}
}
