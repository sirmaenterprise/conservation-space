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
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Tests for PptCustomPropertyUpdater.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class PptCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/ppt-custom-property-updater-test-file.ppt";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/ppt-custom-property-updater-test-corrupted-file.ppt";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/ppt-custom-property-updater-test-file-with-property.ppt";

	@InjectMocks
	private PptCustomPropertyUpdater pptCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".ppt";
		updateFileCustomPropertiesUpdateException(pptCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileDidNotContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".ppt";
		update(pptCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".ppt";
		update(pptCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (BufferedInputStream bufIStream = new BufferedInputStream(new FileInputStream(file));
				POIFSFileSystem fileSystem = new POIFSFileSystem(bufIStream);
				HSLFSlideShowImpl slideShow = new HSLFSlideShowImpl(fileSystem);) {

			DocumentSummaryInformation documentSummaryInformation = slideShow.getDocumentSummaryInformation();
			CustomProperties customProperties = documentSummaryInformation.getCustomProperties();

			Assert.assertNotNull(errorMessage, customProperties.get(AbstractMSOfficeCustomPropertyUpdater.REST_URL));
		} catch (Exception e) {
			Assert.fail(errorMessage);
		}
	}

	@Test
	@UseDataProvider("acceptedMimetypes")
	public void should_ReturnTrue_When_MimetypeIsAccepted(String mimetype, String scenarioInfo) {
		Assert.assertTrue(scenarioInfo, pptCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/vnd.ms-powerpoint", Boolean.TRUE, "scenario MimetypeConstants.PPT_POT with lower case"},
				{ "Application/vnd.ms-powerpoint", Boolean.TRUE, "scenario MimetypeConstants.PPT_POT with lower and upper cases"}
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(pptCustomPropertyUpdater.canUpdate(MimetypeConstants.DOC_DOT));
	}
}
