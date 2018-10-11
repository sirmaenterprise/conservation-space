package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * Tests for {@link EAICSVParser}
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class EAICSVParserTest {
	@InjectMocks
	private EAICSVParser reader;

	@Test
	public void testReadCSV() throws EAIException, FileNotFoundException {
		InputStream inputStream = EAICSVParser.class.getResourceAsStream("dataImportFile.csv");
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getInputStream()).thenReturn(inputStream);
		Map<String, Collection<String>> request = new HashMap<>();
		List<String> rows = new ArrayList<>();
		rows.add("2");
		rows.add("4");
		request.put("0", rows);
		SpreadsheetSheet readCSV = reader.parseEntries(content, request);
		assertEquals(2, readCSV.getEntries().size());
		assertTrue(readCSV.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertTrue(readCSV.getEntries().get(1).getExternalId().equals("4"));
	}

	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(reader.isSupported(content));
	}

	@Test
	public void testCSVIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.CSV_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(reader.isSupported(content));
	}

	@Test
	public void testPlainIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.TEXT_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(reader.isSupported(content));
	}

	@Test
	public void testReadAll() throws EAIException, FileNotFoundException {
		InputStream inputStream = EAICSVParser.class.getResourceAsStream("dataImportFileSemiColonAsDelimiter.csv");
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getInputStream()).thenReturn(inputStream);
		SpreadsheetSheet readCSV = reader.parseEntries(content, null);
		assertEquals(3, readCSV.getEntries().size());
		assertFalse(readCSV.getEntries().get(0).getProperties().containsKey("ImportStatus"));
	}

	@Test
	public void testReadCSVWithDifferentSeparator() throws EAIException, FileNotFoundException {
		InputStream inputStream = EAICSVParser.class.getResourceAsStream("dataImportFileSemiColonAsDelimiter.csv");
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getInputStream()).thenReturn(inputStream);
		Map<String, Collection<String>> request = new HashMap<>();
		List<String> rows = new ArrayList<>();
		rows.add("2");
		rows.add("4");
		request.put("0", rows);
		SpreadsheetSheet readCSV = reader.parseEntries(content, request);
		assertEquals(2, readCSV.getEntries().size());
		assertTrue(readCSV.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertTrue(readCSV.getEntries().get(1).getExternalId().equals("4"));
	}

}
