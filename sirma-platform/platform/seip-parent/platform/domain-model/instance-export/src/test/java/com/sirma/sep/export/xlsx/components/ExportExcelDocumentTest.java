package com.sirma.sep.export.xlsx.components;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.utils.TestExportExcelUtil;

/**
 * @author Boyan Tonchev.
 */
public class ExportExcelDocumentTest {

	private static final String INSTANCE_ONE_LINK_LABEL = "Label of link to first entity";
	private static final String INSTANCE_ONE_LINK_ADDRESS = "http://host:port/emf:4809832c-2d40-4329-8369-f8b2df549cd0";
	private static final String INSTANCE_TWO_LINK_LABEL = "Label of link to second entity";
	private static final String INSTANCE_TWO_LINK_ADDRESS = "http://host:port/emf:4809832c-2d40-4329-8369-324324";

	@Test
	public void should_PopulateExcelFile_When_PropertyIsListWithStrings() throws IOException {
		String headerObject = "Header of object";
		String propertyName = "object";

		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(propertyName, headerObject);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			exportExcelRow.addProperty(propertyName, new ExportExcelCell(propertyName,
					(Serializable) Arrays.asList("ValueOne", "ValueTwo"), ExportExcelCell.Type.OBJECTS));
			exportExcelDocument.populateRow(exportExcelRow);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerObject);
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, "ValueOne", "ValueTwo");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateExcelFile_When_PropertyIsEmptyList() throws IOException {
		String headerObject = "Header of object";
		String propertyName = "object";

		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(propertyName, headerObject);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			exportExcelRow.addProperty(propertyName, new ExportExcelCell(propertyName,
					(Serializable) Collections.emptyList(), ExportExcelCell.Type.OBJECTS));
			exportExcelDocument.populateRow(exportExcelRow);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerObject);
			TestExportExcelUtil.assertCellIsNull(sheet, 0, 1);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateExcelFile_When_PropertyIsNull() throws IOException {
		String headerObject = "Header of object";
		String propertyName = "object";

		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(propertyName, headerObject);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			exportExcelRow.addProperty(propertyName,
					new ExportExcelCell(propertyName, null, ExportExcelCell.Type.OBJECT));
			exportExcelDocument.populateRow(exportExcelRow);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerObject);
			TestExportExcelUtil.assertCellIsNull(sheet, 0, 1);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateExcelFile_When_PropertyIsString() throws IOException {
		String linkLabel = "Label of link to entity";
		String headerObject = "Header of object";
		String propertyName = "object";

		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(propertyName, headerObject);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			exportExcelRow.addProperty(propertyName,
					new ExportExcelCell(propertyName, linkLabel, ExportExcelCell.Type.OBJECT));
			exportExcelDocument.populateRow(exportExcelRow);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerObject);
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, linkLabel);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateExcelFile_When_RowContainsEntityValue() throws IOException {
		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(DefaultProperties.HEADER_COMPACT, IdocRenderer.ENTITY_LABEL);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			exportExcelRow.addProperty(DefaultProperties.HEADER_COMPACT,
					new ExportExcelCell(DefaultProperties.HEADER_COMPACT,
							createLinkData(INSTANCE_ONE_LINK_ADDRESS, INSTANCE_ONE_LINK_LABEL),
							ExportExcelCell.Type.LINK));

			exportExcelDocument.populateRow(exportExcelRow);

			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, IdocRenderer.ENTITY_LABEL);
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_ONE_LINK_LABEL, INSTANCE_ONE_LINK_ADDRESS);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateHeaderRow_When_HaveNotOtherRow() throws IOException {
		String headerOneText = "Header One";
		String headerTwoText = "Header Two";
		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put("headerOne", headerOneText);
			headersInfo.put("headerTwo", headerTwoText);
			ExportExcelDocument exportExcelDocument = new SXSSExportExcelDocument(headersInfo, 100);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerOneText);
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, headerTwoText);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_PopulateExcelFile_When_PropertyIsRichtext() throws IOException {
		String headerObject = "Header of object";
		String propertyName = "description";

		File testFile = TestExportExcelUtil.createEmptyTestFile(".xlsx");
		try {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			headersInfo.put(propertyName, headerObject);
			ExportExcelDocument exportExcelDocument = new HSSFExportExcelDocument(headersInfo);
			ExportExcelRow exportExcelRow = new ExportExcelRow();
			String testString = "<b>Bold </b><strong>Strong </strong><i>Italic1 </i><em>Italic2 </em>"
					+ "<span style=\"color: #FF0000\">Style </span><span style=\"background-color: #FF0000\">Background </span>"
					+ "<span style=\"font-size: 26px\">Big </span>"
					+ "<font color=\"#0000ff\">Font </font><font style=\"font-size: 12pt\">Points </font>"
					+ "<u>Underline </u><tag>Unknown </tag>Unformatted &lt; &gt; &amp;<br>";
			exportExcelRow.addProperty(propertyName,
					new ExportExcelCell(propertyName, testString, ExportExcelCell.Type.RICHTEXT));
			exportExcelDocument.populateRow(exportExcelRow);
			exportExcelDocument.writeToFile(testFile);

			Sheet sheet = TestExportExcelUtil.getHSSFSheet(testFile, 0);

			TestExportExcelUtil.assertStringValue(sheet, 0, 0, headerObject);
			TestExportExcelUtil.assertStringValue(sheet, 1, 0,
					"Bold Strong Italic1 Italic2 Style Background Big Font Points Underline Unknown Unformatted < > &");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	private ExportExcelLink createLinkData(String linkAddress, String linkLabel) {
		return new ExportExcelLink(linkAddress, linkLabel);
	}
}