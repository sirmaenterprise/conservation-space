package com.sirma.itt.seip.eai.content.tool.service.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;
import com.sirma.itt.seip.eai.content.tool.service.reader.EAISpreadsheetParser;
import com.sirma.itt.seip.eai.content.tool.service.reader.EAISpreadsheetParserFactory;

/**
 * Tests for {@link EAIXlsWriter}
 * 
 * @author gshevkedov
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIApachePoiWriterTest {

	@InjectMocks
	private EAIApachePoiWriter eaiWriter;
	private File storage;

	@Before
	public void setUp() throws Exception {
		storage = File.createTempFile("xlsx", ".xlsx");
	}

	@After
	public void tearDown() throws Exception {
		if (!storage.delete()) {
			storage.deleteOnExit();
		}
	}

	@Test
	public void testWriteEntries() throws Exception {
		try (InputStream is = EAIApachePoiWriter.class.getResourceAsStream("dataImportFile.xlsx")) {
			ContentInfo content = new ContentInfo("xlsx",
					EAIApachePoiWriter.class.getResource("dataImportFile.xlsx").toURI(), is);
			EAISpreadsheetParser parser = EAISpreadsheetParserFactory.getParser(content);
			SpreadsheetSheet sheet = parser.parseEntries(content, null);
			sheet.getEntries().get(0).getProperties().put(EAIContentConstants.PRIMARY_CONTENT_ID, "contentId1");
			sheet.getEntries().get(1).getProperties().put("emf:newIntValue", Integer.valueOf(100));
			File writerEntries = eaiWriter.writerEntries(sheet, storage);
			assertNotNull(writerEntries);
			assertTrue(writerEntries.canRead());
			try (Workbook workbook = WorkbookFactory.create(storage)) {
				Iterator<Row> rowIterator = workbook.getSheetAt(0).rowIterator();
				int contentIdColumn = 14;
				Cell cell = rowIterator.next().getCell(contentIdColumn);
				assertEquals(EAIContentConstants.PRIMARY_CONTENT_ID, cell.getStringCellValue());
				cell = rowIterator.next().getCell(contentIdColumn);
				assertEquals("contentId1", cell.getStringCellValue());
				int intValueColumn = 15;
				cell = rowIterator.next().getCell(intValueColumn);
				assertEquals(Integer.valueOf(100), Integer.valueOf((int) cell.getNumericCellValue()));

			}
		}
	}

}
