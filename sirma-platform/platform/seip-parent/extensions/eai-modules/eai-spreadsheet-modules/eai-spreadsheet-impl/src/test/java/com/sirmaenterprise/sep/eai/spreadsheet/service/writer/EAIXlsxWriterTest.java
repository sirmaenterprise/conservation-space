/**
 *
 */
package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static org.junit.Assert.assertEquals;
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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * Tests for {@link EAIXlsxWriter}
 * 
 * @author gshevkedov
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIXlsxWriterTest {

	@Mock
	private TempFileProvider tempFileProvider;

	@InjectMocks
	private EAIXlsxWriter eaiXlsxWriter;

	@Test
	public void testWriteEntries() throws EAIException, IOException {
		File target = new File("test1.xlsx");
		try (InputStream is = EAIXlsxWriter.class.getResourceAsStream("dataImportFile.xlsx")) {
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
			assertNotNull(eaiXlsxWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}
	
	@Test
	public void testWriteEntriesMultisheet() throws EAIException, IOException, InvalidFormatException {
		File target = new File("test1.xlsx");
		try (InputStream is = EAIXlsxWriter.class.getResourceAsStream("dataImportFileMultisheet.xlsx")) {
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
			assertNotNull(eaiXlsxWriter.writerEntries(content, entries));
			Workbook workbook = new XSSFWorkbook(target);
			Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets()-1);
			Row secondRow = sheet.getRow(2);
			Cell cell = secondRow.getCell(secondRow.getLastCellNum()-1);
			assertEquals(EAISpreadsheetConstants.NOT_IMPORTED, cell.getStringCellValue());
			workbook.close();
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}

	@Test
	public void testUpdateColumns() throws EAIException, IOException {
		File target = new File("test2.xlsx");
		try (InputStream is = EAIXlsxWriter.class.getResourceAsStream("dataForImportWithColumns.xlsx")) {
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
			assertNotNull(eaiXlsxWriter.writerEntries(content, entries));
		} finally {
			target.delete();
			target.deleteOnExit();
		}
	}
	
	@Test
	public void testXlsxIsSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLSX_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertTrue(eaiXlsxWriter.isSupported(content));
	}

	@Test
	public void testXlsxIsNotSupported() throws Exception {
		String mimetype = EAISpreadsheetConstants.XLS_MIMETYPE;
		ContentInfo content = Mockito.mock(ContentInfo.class);
		Mockito.when(content.getMimeType()).thenReturn(mimetype);
		assertFalse(eaiXlsxWriter.isSupported(content));
	}
}
