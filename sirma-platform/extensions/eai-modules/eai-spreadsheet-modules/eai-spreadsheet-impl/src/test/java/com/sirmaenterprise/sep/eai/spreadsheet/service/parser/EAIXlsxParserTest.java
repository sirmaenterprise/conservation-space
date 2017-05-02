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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * Test for {@link EAIXlsxParser}
 *
 * @author gshevkedov
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIXlsxParserTest {

	@InjectMocks
	private EAIXlsxParser parser;

	private InputStream getDefaultStream() {
		return EAIXlsxParser.class.getResourceAsStream("dataImportFile.xlsx");
	}

	@Test
	public void testReadXlsxWholeFile() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(getDefaultStream());
		SpreadsheetSheet readExcel = parser.parseEntries(content, null);
		assertEquals(3, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertEquals("1", readExcel.getEntries().get(0).getSheet());
		assertEquals("2", readExcel.getEntries().get(0).getExternalId());
		assertEquals("3", readExcel.getEntries().get(1).getExternalId());
		assertEquals("4", readExcel.getEntries().get(2).getExternalId());
	}

	@Test
	public void testReadXlsx() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(getDefaultStream());

		Map<String, Collection<String>> request = new HashMap<>();
		List<String> rows = new ArrayList<>();
		rows.add("2");
		rows.add("4");
		request.put("1", rows);
		SpreadsheetSheet readExcel = parser.parseEntries(content, request);
		assertEquals(2, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertTrue(readExcel.getEntries().get(1).getExternalId().equals("4"));
	}

	@Test
	public void testXlsxIsSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(parser.isSupported(content));
	}

	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLS_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(parser.isSupported(content));
	}

	@Test
	public void testXlsxWithComment() throws Exception {
		InputStream is = EAIXlsParser.class.getResourceAsStream("invertar.xlsx");
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(mimetype);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(5, parseEntries.getEntries().size());
		assertEquals(1, parseEntries.getEntries().get(0).getConfiguration().size());
	}

	@Test
	public void testXlsxMultisheet() throws Exception {
		InputStream is = EAIXlsxParser.class.getResourceAsStream("multisheet.xlsx");
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(mimetype);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(8, parseEntries.getEntries().size());
		assertEquals(0, parseEntries.getEntries().get(0).getConfiguration().size());
		assertEquals(2, parseEntries.getEntries().get(1).getConfiguration().size());
	}

}
