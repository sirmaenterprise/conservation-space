package com.sirma.itt.seip.instance.editoffline.updaters;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Tests for XlsxCustomPropertyUpdater.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class XlsxCustomPropertyUpdaterTest extends AbstractCustomPropertyTest {

	private static final String TEST_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xlsx-custom-property-updater-test-file.xlsx";
	private static final String TEST_CORRUPTED_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xlsx-custom-property-updater-test-corrupted-file.xlsx";
	private static final String TEST_WITH_PROPERTY_FILE_NAME = "com/sirma/itt/seip/instance/editoffline/updaters/xlsx-custom-property-updater-test-file-with-propery.xlsx";

	@InjectMocks
	private XlsxCustomPropertyUpdater xlsxCustomPropertyUpdater;

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_FileIsCorrupted() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xlsx";
		updateFileCustomPropertiesUpdateException(xlsxCustomPropertyUpdater, outputFileName, TEST_CORRUPTED_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileDidNotContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xlsx";
		update(xlsxCustomPropertyUpdater, outputFileName, TEST_FILE_NAME);
	}

	@Test
	public void should_UpdateFile_When_FileContainsEditOfflineCustomProperty() throws IOException {
		String outputFileName = UUID.randomUUID() + ".xlsx";
		update(xlsxCustomPropertyUpdater, outputFileName, TEST_WITH_PROPERTY_FILE_NAME);
	}

	@Test(expected = FileCustomPropertiesUpdateException.class)
	public void should_ThrowFileCustomPropertiesUpdateException_When_CannotCreateTempFile() throws IOException {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(instanceContentService.getContent(INSTANCE_ID, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		Mockito.when(contentInfo.writeTo(Matchers.any(File.class))).thenThrow(IOException.class);
		Mockito.when(tempFileProvider.createTempFile(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Mockito.mock(File.class));
		xlsxCustomPropertyUpdater.update(INSTANCE_ID);
	}

	@Override
	public void assertCustomProperty(File file, String errorMessage) {
		try (FileInputStream inputDocument = new FileInputStream(file);
				XSSFWorkbook workBook = new XSSFWorkbook(inputDocument)) {
			POIXMLProperties properties = workBook.getProperties();
			POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
			Assert.assertNotNull(errorMessage, customProperties.getProperty(AbstractMSOfficeCustomPropertyUpdater.REST_URL));
		} catch (Exception e) {
			Assert.fail(errorMessage);
		}
	}

	@Test
	@UseDataProvider("acceptedMimetypes")
	public void should_ReturnTrue_When_MimetypeIsAccepted(String mimetype, String scenarioInfo) {
		Assert.assertTrue(scenarioInfo, xlsxCustomPropertyUpdater.canUpdate(mimetype));
	}

	@DataProvider
	public static Object[][] acceptedMimetypes() {
		return  new Object[][] {
				{ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "scenario MimetypeConstants.XLSX with lower case"},
				{ "application/vnd.Openxmlformats-officEdocument.spreadsheetml.sheet", "scenario MimetypeConstants.XLSX with lower and upper cases"},
				{ "application/vnd.openxmlformats-officedocument.spreadsheetml.template", "scenario MimetypeConstants.XLTX with lower case"},
				{ "applicatiOn/vnd.openxmlformats-Officedocument.spreadsheetml.template", "scenario MimetypeConstants.XLTX with lower and upper cases"},
				{ "application/vnd.ms-excel.sheet.macroEnabled.12", "scenario MimetypeConstants.XLSM with lower case"},
				{ "application/Vnd.ms-exceL.sheet.macroEnabled.12", "scenario MimetypeConstants.XLSM with lower and upper cases"},
				{ "application/vnd.ms-excel.template.macroEnabled.12", "scenario MimetypeConstants.XLTM with lower case"},
				{ "application/vnd.mS-excel.template.maCroEnabled.12", "scenario MimetypeConstants.XLTM with lower and upper cases"},
		};
	}

	@Test
	public void should_ReturnFalse_When_MimetypeIsNotAccepted() {
		Assert.assertFalse(xlsxCustomPropertyUpdater.canUpdate(MimetypeConstants.DOC_DOT));
	}
}