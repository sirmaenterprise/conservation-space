package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Tests for DocCustomPropertyUpdater.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class DocCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/doc-custom-property-updater-test-file.doc";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/doc-custom-property-updater-test-corrupted-file.doc";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/doc-custom-property-updater-test-file-with-property.doc";
	private static final String TEST_WITH_OTHER_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/doc-custom-property-updater-test-file-wit-other-custom-property.doc";

	@InjectMocks
	private DocCustomPropertyUpdater docCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".doc";
		updateFileCustomPropertiesUpdateException(docCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_NotThrowException_When_FileContainsCustomPropertyWithoutEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".doc";
		update(docCustomPropertyUpdater, outputFileName, TEST_WITH_OTHER_PROPERTY_FILE_NAME);
	}

	@Test
	public void should_NotThrowException_When_FileDidNotContainsCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".doc";
		update(docCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".doc";
		update(docCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (BufferedInputStream bufIStream = new BufferedInputStream(new FileInputStream(file));
				POIFSFileSystem fileSystem = new POIFSFileSystem(bufIStream);
				HWPFDocument document = new HWPFDocument(fileSystem);) {
			DocumentSummaryInformation documentSummaryInformation = document.getDocumentSummaryInformation();
			CustomProperties customProperties = documentSummaryInformation.getCustomProperties();
			if (customProperties == null) {
				Assert.fail(errorMessage);
			} else {
				Assert.assertNotNull(errorMessage,
						customProperties.get(AbstractMSOfficeCustomPropertyUpdater.REST_URL));
			}
		} catch (Exception e) {
			Assert.fail(errorMessage);
		}
	}

	@Test
	@UseDataProvider("acceptedMimetypes")
	public void should_ReturnTrue_When_MimetypeIsAccepted(String mimetype, String scenarioInfo) {
		Assert.assertTrue(scenarioInfo, docCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/msword", Boolean.TRUE, "scenario MimetypeConstants.DOC_DOT with lower case" },
				{ "Application/msword", Boolean.TRUE, "scenario MimetypeConstants.DOC_DOT with lower and upper cases" },
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(docCustomPropertyUpdater.canUpdate(MimetypeConstants.DOCM));
	}
}
