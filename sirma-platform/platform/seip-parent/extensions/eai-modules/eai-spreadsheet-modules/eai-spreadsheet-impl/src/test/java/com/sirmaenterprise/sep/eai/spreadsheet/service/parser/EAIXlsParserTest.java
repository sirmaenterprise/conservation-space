package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * Test for {@link EAIXlsxParser}
 *
 * @author gshevkedov
 */
public class EAIXlsParserTest {

	private InputStream is = null;

	@Test
	public void testReadXlsWholeFile() throws Exception {
		is = EAIXlsParser.class.getResourceAsStream("dataImportFile.xls");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(is);
		EAIXlsParser reader = new EAIXlsParser();
		SpreadsheetSheet readExcel = reader.parseEntries(content, null);
		assertEquals(3, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertTrue(readExcel.getEntries().get(0).getExternalId().equals("2"));
		assertTrue(readExcel.getEntries().get(1).getExternalId().equals("3"));
		assertTrue(readExcel.getEntries().get(2).getExternalId().equals("4"));
	}

	@Test
	public void testReadXls() throws Exception {
		is = EAIXlsParser.class.getResourceAsStream("dataImportFile.xls");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(is);
		EAIXlsParser reader = new EAIXlsParser();
		Map<String, Collection<String>> request = new HashMap<>();
		List<String> rows = new ArrayList<>();
		rows.add("2");
		rows.add("4");
		request.put("1", rows);
		SpreadsheetSheet readExcel = reader.parseEntries(content, request);
		assertEquals(2, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertTrue(readExcel.getEntries().get(1).getExternalId().equals("4"));
	}

	@Test
	public void testXlsIsSupported() throws Exception {
		EAIXlsParser reader = new EAIXlsParser();
		String mimetype = EAISpreadsheetConstants.XLS_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(reader.isSupported(content));
	}

	@Test
	public void testXlsIsNotSupported() throws Exception {
		EAIXlsParser reader = new EAIXlsParser();
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(reader.isSupported(content));
	}

}
