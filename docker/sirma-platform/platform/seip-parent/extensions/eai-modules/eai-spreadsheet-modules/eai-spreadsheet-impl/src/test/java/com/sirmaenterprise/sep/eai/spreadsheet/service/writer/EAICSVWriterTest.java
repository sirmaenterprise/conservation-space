package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/**
 * Tests for {@link EAICSVWriter}
 * 
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class EAICSVWriterTest {

	@Mock
	private TempFileProvider tempFileProvider;

	@InjectMocks
	private EAICSVWriter eAICsvWriter;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.writer.EAICSVWriter#writerEntries(com.sirma.sep.content.ContentInfo, java.util.List)}
	 * .
	 * @throws IOException 
	 */
	@Test
	public void testWriteEntries() throws EAIException, IOException {
		File target = new File("test1.csv");
		try (InputStream is = EAICSVWriter.class.getResourceAsStream("dataImportFileCSV.csv")) {
			when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(target);
			ContentInfo content = Mockito.mock(ContentInfo.class);
			Mockito.when(content.getInputStream()).thenReturn(is);
			List<SpreadsheetEntry> entries = new LinkedList<>();
			SpreadsheetEntry entry1 = new SpreadsheetEntry("0", "1");
			SpreadsheetEntry entry2 = new SpreadsheetEntry("0", "3");
			SpreadsheetEntry entry3 = new SpreadsheetEntry("0", "2");
			entry1.put("id", "emf:9999");
			entry2.put("id", "emf:4444");
			entry3.put("id", "emf:6666");
			entries.add(entry1);
			entries.add(entry2);
			entries.add(entry3);
			assertNotNull(eAICsvWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}

	@Test
	public void testUpdateColumns() throws EAIException, IOException {
		File target = new File("test2.csv");
		try (InputStream is = EAICSVWriter.class.getResourceAsStream("dataForImportWithColumns.csv")) {
			when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(target);
			ContentInfo content = Mockito.mock(ContentInfo.class);
			Mockito.when(content.getInputStream()).thenReturn(is);
			List<SpreadsheetEntry> entries = new LinkedList<>();
			SpreadsheetEntry entry1 = new SpreadsheetEntry("0", "1");
			SpreadsheetEntry entry2 = new SpreadsheetEntry("0", "3");
			SpreadsheetEntry entry3 = new SpreadsheetEntry("0", "2");
			entry1.put("id", "emf:9999");
			entry2.put("id", "emf:4444");
			entry3.put("id", "emf:6666");
			entries.add(entry1);
			entries.add(entry2);
			entries.add(entry3);
			assertNotNull(eAICsvWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}
	
	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(eAICsvWriter.isSupported(content));

	}

	@Test
	public void testXlsIsNotSupported() throws Exception {
		String mimetype = "application/vnd.ms-excel";
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(eAICsvWriter.isSupported(content));
	}
}
