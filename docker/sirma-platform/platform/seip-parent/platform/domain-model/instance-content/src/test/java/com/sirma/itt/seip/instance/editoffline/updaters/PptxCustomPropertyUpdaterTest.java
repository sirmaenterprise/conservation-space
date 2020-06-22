package com.sirma.itt.seip.instance.editoffline.updaters;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Tests for PptxCustomPropertyUpdater.
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class PptxCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/pptx-custom-property-updater-test-file.pptx";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/pptx-custom-property-updater-test-corrupted-file.pptx";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/pptx-custom-property-updater-test-file-with-property.pptx";

	@InjectMocks
	private PptxCustomPropertyUpdater pptxCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".pptx";
		updateFileCustomPropertiesUpdateException(pptxCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileDidNotContainsEditOfflineCustomProperties() throws IOException {
		String outputFileName = UUID.randomUUID() + ".pptx";
		update(pptxCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProperties() throws IOException {
		String outputFileName = UUID.randomUUID() + ".pptx";
		update(pptxCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (OPCPackage pkg = OPCPackage.open(file); ) {
			XSLFSlideShow slideshow = new XSLFSlideShow(pkg);
			POIXMLProperties properties = slideshow.getProperties();
			POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
			Assert.assertNotNull(errorMessage, customProperties.getProperty(AbstractMSOfficeCustomPropertyUpdater.REST_URL));

		} catch (Exception e) {
			Assert.fail(errorMessage);
		}
	}

	@Test
	@UseDataProvider("acceptedMimetypes")
	public void should_ReturnTrue_When_MimetypeIsAccepted(String mimetype, String scenarioInfo) {
		Assert.assertTrue(scenarioInfo, pptxCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/vnd.openxmlformats-officedocument.presentationml.template", Boolean.TRUE, "scenario MimetypeConstants.POTX with lower case"},
				{ "Application/vnd.openxmlformats-officedocument.presentationml.template", Boolean.TRUE, "scenario MimetypeConstants.XLS_XLT with lower and upper cases"},
				{ "application/vnd.ms-powerpoint.template.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.POTM with lower case"},
				{ "Application/vnd.ms-powerpoint.template.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.POTM with lower and upper cases"},
				{ "application/vnd.ms-powerpoint.presentation.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.PPTM with lower case"},
				{ "Application/vnd.ms-powerpoint.presentation.macroEnabled.12", Boolean.TRUE, "scenario MimetypeConstants.PPTM with lower and upper cases"},
				{ "application/vnd.openxmlformats-officedocument.presentationml.presentation", Boolean.TRUE, "scenario MimetypeConstants.PPTX with lower case"},
				{ "Application/vnd.openxmlformats-officedocument.presentationml.presentation", Boolean.TRUE, "scenario MimetypeConstants.PPTX with lower and upper cases"}
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(pptxCustomPropertyUpdater.canUpdate(MimetypeConstants.DOC_DOT));
	}
}
