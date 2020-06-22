package com.sirmaenterprise.sep.eai.spreadsheet.service;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Before;

public class SpreadsheetParserImplTest {

	private InputStream is = null;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		is = new FileInputStream("src/test/resources/dataImportFile.xlsx");
	}

	@Test
	public void testReadExcel(){
		EAIExcelReader reader = new EAIExcelReader();
		List<SpreadsheetEntry> readExcel = reader.readExcel(is);
		assertEquals(3, readExcel.size());

	}

}
