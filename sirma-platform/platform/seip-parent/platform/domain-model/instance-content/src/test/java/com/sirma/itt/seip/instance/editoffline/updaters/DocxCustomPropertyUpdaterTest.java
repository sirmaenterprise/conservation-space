package com.sirma.itt.seip.instance.editoffline.updaters;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Tests for DocxCustomPropertyUpdater.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class DocxCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/docx-custom-property-updater-test-file.docx";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/docx-custom-property-updater-test-corrupted-file.docx";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/docx-custom-property-updater-test-file-with-property.docx";

	@InjectMocks
	private DocxCustomPropertyUpdater docxCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".docx";
		updateFileCustomPropertiesUpdateException(docxCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileDidNotContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".docx";
		update(docxCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".docx";
		update(docxCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (FileInputStream inputDocument = new FileInputStream(file);
				XWPFDocument document = new XWPFDocument(inputDocument);) {
			POIXMLProperties properties = document.getProperties();
			POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
			Assert.assertNotNull(errorMessage, customProperties.getProperty(AbstractMSOfficeCustomPropertyUpdater.REST_URL));
		} catch (IOException e) {
			Assert.fail(errorMessage);
		}
	}

	@Test
	@UseDataProvider("acceptedMimetypes")
	public void should_ReturnTrue_When_MimetypeIsAccepted(String mimetype, String scenarioInfo) {
		Assert.assertTrue(scenarioInfo, docxCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Boolean.TRUE, "scenario MimetypeConstants.DOCX with lower case"},
				{ "Application/vnd.openxmlformats-officedocument.wordprocessingml.document", Boolean.TRUE, "scenario MimetypeConstants.DOCX with lower and upper cases"},
				{ "application/vnd.ms-word.document.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.DOCM with lower case"},
				{ "Application/vnd.openxmlformats-officedocument.wordprocessingml.document", Boolean.TRUE, "scenario MimetypeConstants.DOCM with lower and upper cases"},
				{ "application/vnd.ms-word.template.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.DOTM with lower case"},
				{ "Application/vnd.ms-word.template.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.DOTM with lower and upper cases"},
				{ "application/vnd.openxmlformats-officedocument.wordprocessingml.template", Boolean.TRUE, "scenario MimetypeConstants.DOTX with lower case"},
				{ "Application/vnd.openxmlformats-officedocument.wordprocessingml.template", Boolean.TRUE, "scenario MimetypeConstants.DOTX with lower and upper cases"}
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(docxCustomPropertyUpdater.canUpdate(MimetypeConstants.DOC_DOT));
	}
}
