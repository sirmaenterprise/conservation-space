package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.ContentInfo;
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

	@Mock
	private TempFileProvider tempFileProvider;

	@Before
	public void init() {
		when(tempFileProvider.createTempFile(any(), any())).then(a -> File.createTempFile(a.getArgumentAt(0, String.class), a.getArgumentAt(1, String.class)));
		doAnswer(a -> a.getArgumentAt(0, File.class).delete()).when(tempFileProvider).deleteFile(any());
	}

	private InputStream getDefaultStream() {
		return EAIXlsxParser.class.getResourceAsStream("dataImportFile.xlsx");
	}

	@Test
	public void testReadXlsxWholeFile() throws Exception {
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(getDefaultStream());
		SpreadsheetSheet readExcel = parser.parseEntries(content, null);
		assertEquals(3, readExcel.getEntries().size());
		assertTrue(readExcel.getEntries().get(0).getProperties().containsKey("emf:type"));
		assertEquals("1", readExcel.getEntries().get(0).getSheet());
		assertEquals("2", readExcel.getEntries().get(0).getExternalId());
		assertEquals("3", readExcel.getEntries().get(1).getExternalId());
		assertEquals("4", readExcel.getEntries().get(2).getExternalId());
	}

	@Test(expected = EAIReportableException.class)
	public void parsing_shouldFailOnDuplicateHeaderColumns() throws Exception {
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(EAIXlsxParser.class.getResourceAsStream("duplicateHeaderColumns.xlsx"));
		parser.parseEntries(content, null);
	}

	private ContentInfo mockContentInfo() throws IOException {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.writeTo(any(File.class))).then(a -> {
			try (OutputStream output = new FileOutputStream(a.getArgumentAt(0, File.class)); InputStream stream = contentInfo.getInputStream()) {
				return IOUtils.copyLarge(stream, output);
			}
		});
		return contentInfo;
	}

	@Test
	public void testReadXlsx() throws Exception {
		ContentInfo content = mockContentInfo();
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
		ContentInfo content = mockContentInfo();
		when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(parser.isSupported(content));
	}

	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLS_MIMETYPE;
		ContentInfo content = mockContentInfo();
		when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(parser.isSupported(content));
	}

	@Test
	public void testXlsxWithComment() throws Exception {
		InputStream is = EAIXlsParser.class.getResourceAsStream("invertar.xlsx");
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(mimetype);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(5, parseEntries.getEntries().size());
		assertEquals(1, parseEntries.getEntries().get(0).getBindings().size());
	}

	@Test
	public void testXlsxMultisheet() throws Exception {
		InputStream is = EAIXlsxParser.class.getResourceAsStream("multisheet.xlsx");
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(mimetype);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(8, parseEntries.getEntries().size());
		assertNull(parseEntries.getEntries().get(0).getBindings());
		assertEquals(2, parseEntries.getEntries().get(1).getBindings().size());
	}

	@Test
	public void testXlsxEmptyRow() throws Exception {
		InputStream is = EAIXlsxParser.class.getResourceAsStream("emptyline.xlsx");
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(EAISpreadsheetConstants.XLSX_MIMETYPE);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(3, parseEntries.getEntries().size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testXlsxDateFormat() throws Exception {
		InputStream is = EAIXlsxParser.class.getResourceAsStream("date-datetime.xlsx");
		ContentInfo content = mockContentInfo();
		when(content.getInputStream()).thenReturn(is);
		when(content.getMimeType()).thenReturn(EAISpreadsheetConstants.XLSX_MIMETYPE);
		SpreadsheetSheet parseEntries = parser.parseEntries(content, null);
		assertEquals(4, parseEntries.getEntries().size());
		Date object = (Date) parseEntries.getEntries().get(3).getProperties().get("emf:plannedStartDate");
		// object.getYear() Returns: the year represented by this date, minus 1900.
		assertEquals(117, object.getYear());
		assertEquals(1, object.getMonth());
		assertEquals(3, object.getDay());
	}

}
