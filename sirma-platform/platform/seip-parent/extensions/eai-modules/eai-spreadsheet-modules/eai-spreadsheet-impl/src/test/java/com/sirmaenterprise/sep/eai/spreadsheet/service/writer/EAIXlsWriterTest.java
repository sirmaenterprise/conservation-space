package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/**
 * Tests for {@link EAIXlsWriter}
 * 
 * @author gshevkedov
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIXlsWriterTest {

	@Mock
	private TempFileProvider tempFileProvider;

	@InjectMocks
	private EAIXlsWriter eaiXlsWriter;

	@Test
	public void testWriteEntries() throws EAIException, IOException {
		File target = new File("test1.xsl");
		try (InputStream is = EAIXlsWriter.class.getResourceAsStream("dataImportFile.xls")) {
			when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(target);
			ContentInfo content = Mockito.mock(ContentInfo.class);
			Mockito.when(content.getInputStream()).thenReturn(is);
			List<SpreadsheetEntry> entries = new LinkedList<>();
			SpreadsheetEntry entry1 = new SpreadsheetEntry("1", "1");
			SpreadsheetEntry entry2 = new SpreadsheetEntry("1", "4");
			SpreadsheetEntry entry3 = new SpreadsheetEntry("1", "3");
			entry1.put("id", "emf:111111111111111");
			entry2.put("id", "emf:222222222222222");
			entry3.put("id", "emf:333333333333333");
			entries.add(entry1);
			entries.add(entry2);
			entries.add(entry3);
			assertNotNull(eaiXlsWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}

	@Test
	public void testUpdateColumns() throws EAIException, IOException {
		File target = new File("test2.xsl");
		try (InputStream is = EAIXlsWriter.class.getResourceAsStream("dataForImportWithColumns.xls")) {
			when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(target);
			ContentInfo content = Mockito.mock(ContentInfo.class);
			Mockito.when(content.getInputStream()).thenReturn(is);
			List<SpreadsheetEntry> entries = new LinkedList<>();
			SpreadsheetEntry entry1 = new SpreadsheetEntry("1", "2");
			SpreadsheetEntry entry2 = new SpreadsheetEntry("1", "5");
			SpreadsheetEntry entry3 = new SpreadsheetEntry("1", "3");
			entry1.put("id", "emf:9999");
			entry2.put("id", "emf:4444");
			entry3.put("id", "emf:6666");
			entries.add(entry1);
			entries.add(entry2);
			entries.add(entry3);
			assertNotNull(eaiXlsWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}
	
	@Test
	public void testXlsIsSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLS_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(eaiXlsWriter.isSupported(content));
	}

	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(eaiXlsWriter.isSupported(content));
	}
}
