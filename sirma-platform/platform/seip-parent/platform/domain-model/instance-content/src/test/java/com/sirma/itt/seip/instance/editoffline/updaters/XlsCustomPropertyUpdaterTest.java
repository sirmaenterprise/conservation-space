package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
 * Tests for XlsCustomPropertyUpdater.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class XlsCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xls-custom-property-updater-test-file.xls";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xls-custom-property-updater-test-corrupted-file.xls";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xls-custom-property-updater-test-file-with-property.xls";
	private static final String TEST_WITH_OTHER_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xls-custom-property-updater-test-file-wit-other-custom-property.xls";

	@InjectMocks
	private XlsCustomPropertyUpdater xlsCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_FileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xls";
		updateFileCustomPropertiesUpdateException(xlsCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_NotThrowException_When_FileContainsCustomPropertiesWithoutEditOfflineCustomProperties() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xls";
		update(xlsCustomPropertyUpdater, outputFileName, TEST_WITH_OTHER_PROPERTY_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileDidNotContainsEditOfflineCustomProerties() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xls";
		update(xlsCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProerties() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xls";
		update(xlsCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (FileInputStream inputDocument = new FileInputStream(file);
				HSSFWorkbook workBook = new HSSFWorkbook(inputDocument)) {

			DocumentSummaryInformation documentSummaryInformation = workBook.getDocumentSummaryInformation();
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
		Assert.assertTrue(scenarioInfo, xlsCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/vnd.ms-excel", Boolean.TRUE, "scenario MimetypeConstants.XLS_XLT with lower case"},
				{ "apPlication/vNd.ms-excel", Boolean.TRUE, "scenario MimetypeConstants.XLS_XLT with lower and upper cases"}
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(xlsCustomPropertyUpdater.canUpdate(MimetypeConstants.DOC_DOT));
	}
}
