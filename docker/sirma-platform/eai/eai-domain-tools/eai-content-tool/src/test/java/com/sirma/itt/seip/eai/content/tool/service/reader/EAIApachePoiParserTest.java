package com.sirma.itt.seip.eai.content.tool.service.reader;

import static org.junit.Assert.assertEquals;
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

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;

/**
 * Test for {@link EAIApachePoiParser}
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIApachePoiParserTest {
 
	@InjectMocks
	private EAIApachePoiParser parser;

	private InputStream getDefaultStream() {
		return EAIApachePoiParser.class.getResourceAsStream("dataImportFile.xlsx");
	}

	@Test
	public void testReadXlsxWholeFile() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getInputStream()).thenReturn(getDefaultStream());
		SpreadsheetSheet readExcel = parser.parseEntries(content, null);
		assertEquals(3, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertEquals("2", readExcel.getEntries().get(0).getId().getExternalId());
		assertEquals("3", readExcel.getEntries().get(1).getId().getExternalId());
		assertEquals("4", readExcel.getEntries().get(2).getId().getExternalId());
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
	}

}
