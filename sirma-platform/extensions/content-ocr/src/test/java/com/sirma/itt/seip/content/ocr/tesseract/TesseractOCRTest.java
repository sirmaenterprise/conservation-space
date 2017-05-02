package com.sirma.itt.seip.content.ocr.tesseract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.content.temp.TempFileProviderImpl;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sun.jna.Platform;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

/**
 * The Class TesseractOCRTest.
 * 
 * @author Hristo Lungov
 */
@RunWith(DataProviderRunner.class)
public class TesseractOCRTest {

	@InjectMocks
	private TesseractOCR tesseractOCR;

	@Mock
	private ConfigurationProperty<Boolean> isTesseractEnabled;

	@Mock
	private ConfigurationProperty<ITesseract> tesseractService;

	@Mock
	private TempFileProviderImpl tempFileProvider;

	private static final String RESULT_CORRECT_TEST = "Easytest\nFindme\nBig test\n\nKVO staaaaa momcheeeee";

	/**
	 * Before method.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Checks if is applicable test.
	 *
	 * @param mimetype
	 *            the mimetype
	 * @param result
	 *            the result
	 */
	@Test
	@SuppressWarnings("boxing")
	@UseDataProvider(location = TesseractOCRTest.class, value = "mimetypeProvider")
	public void isApplicableTest(String mimetype, Boolean result) {
		Mockito.when(isTesseractEnabled.get()).thenReturn(Boolean.TRUE);
		Assert.assertEquals(result.booleanValue(), tesseractOCR.isApplicable(mimetype));
	}

	/**
	 * Creates the types mimetypeProvider.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	@SuppressWarnings("boxing")
	public static Object[][] mimetypeProvider() {
		return new Object[][] { { null, false }, { MediaType.OCTET_STREAM.toString(), false }, { MediaType.audio("aiff").toString(), false }, { MediaType.video("avi").toString(), false },
				{ MediaType.image("bmp").toString(), true }, { MediaType.TEXT_HTML.toString(), false }, { MediaType.TEXT_PLAIN.toString(), false },
				{ MediaType.application("msword").toString(), false }, { MediaType.application("pdf").toString(), true }, { MediaType.image("png").toString(), true },
				{ MediaType.image("gif").toString(), true }, { MediaType.image("jpeg").toString(), true }, { MediaType.image("jpg").toString(), true } };
	}

	/**
	 * Do OCR small test with "test.pdf". Note: Under Windows we use the inner Tesseract Libs in tess4j api.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws TesseractException
	 *             the tesseract exception
	 */
	@Test
	public void doOcr_SmallCorrect_Test() throws IOException, URISyntaxException, TesseractException {
		FileDescriptor mockDescriptor = mockDescriptor("test.pdf");
		File tempFile = File.createTempFile("test", ".pdf");
		tempFile.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(mockDescriptor.getInputStream(), out);
			Mockito.when(tempFileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(tempFile);
			ITesseract tess = Mockito.mock(Tesseract.class);
			if (Platform.isWindows()) {
				tess = new Tesseract1();
				tess.setDatapath("src/test/resources");
			} else {
				Mockito.when(tess.doOCR(tempFile)).thenReturn(RESULT_CORRECT_TEST);
			}
			Mockito.when(tesseractService.get()).thenReturn(tess);
			String ocrContent = tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, mockDescriptor);
			Assert.assertEquals(RESULT_CORRECT_TEST.trim(), ocrContent.trim());
		}
	}

	/**
	 * Test when the tesseract is null.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws TesseractException
	 *             the tesseract exception
	 */
	@Test
	public void doOcr_Null_Tesseract_Test() throws IOException, URISyntaxException, TesseractException {
		FileDescriptor mockDescriptor = mockDescriptor("test.pdf");
		File tempFile = File.createTempFile("test", ".pdf");
		tempFile.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(mockDescriptor.getInputStream(), out);
			Mockito.when(tempFileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(tempFile);
			Mockito.when(tesseractService.get()).thenReturn(null);
			String ocrContent = tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, mockDescriptor);
			Assert.assertNull(ocrContent);
		}
	}

	/**
	 * Do OCR Tesseract Test with null Descriptor.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void doOcr_Null_Descriptor_Test() throws IOException {
		tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, null);
		Assert.fail("Should not pass!");
	}

	/**
	 * Do OCR Tesseract Test with null mimentype.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void doOcr_Null_Mimetype_Test() throws IOException {
		tesseractOCR.doOcr(null, mockDescriptor("test.pdf"));
		Assert.fail("Should not pass!");
	}

	/**
	 * Do OCR Tesseract Test with missing tessData path config.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void doOcr_Tesseract_Problem_Test() throws IOException {
		Mockito.when(tesseractService.get()).thenReturn(new Tesseract());
		tesseractOCR.doOcr(TesseractOCR.APPLICATION_PDF, mockDescriptor("test.pdf"));
		Assert.fail("Should not pass!");
	}

	/**
	 * Gets a new simple descriptor mock.
	 *
	 * @param testFile
	 *            is the file to get
	 * @return a descriptor mock
	 */
	private static FileDescriptor mockDescriptor(final String testFile) {
		FileDescriptor mock = Mockito.mock(FileDescriptor.class);
		Mockito.when(mock.getId()).thenReturn(testFile);
		Mockito.when(mock.getInputStream()).thenReturn(TesseractOCRTest.class.getResourceAsStream(testFile));
		return mock;
	}
}
