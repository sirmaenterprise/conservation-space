package com.sirma.itt.cmf.content.extract;

import java.util.regex.Pattern;

import org.apache.tika.mime.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * @author bbanchev
 */
@RunWith(DataProviderRunner.class)
public class TikaContentExtractorTest {

	@InjectMocks
	TikaContentExtractor extractor;

	@Spy
	ConfigurationProperty<Pattern> mimetypeMatchPattern = new ConfigurationPropertyMock<>(
			Pattern.compile(TikaContentExtractor.DEFAULT_TIKA_MIMETYPE_PATTERN));

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.parser.TikaContentExtractor#extractContentFromFile(com.sirma.itt.seip.io.FileDescriptor)}
	 * .
	 */
	@Test
	public final void testExtractContentFromFile() throws Exception {
		String extractContentFromFile = extractor.extract(mockDescriptor("250+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("500+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("contentmodel.zip"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("alfresco.exe"));
		Assert.assertNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("ProductDetails.png"));
		Assert.assertNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("null"));
		Assert.assertNull(extractContentFromFile);

	}

	/**
	 * Test accept.
	 *
	 * @param mimetype
	 *            the mimetype
	 * @param result
	 *            the result
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@UseDataProvider(location = TikaContentExtractorTest.class, value = "mimetypeProvider")
	public void testAccept(String mimetype, Boolean result) throws Exception {
		Assert.assertEquals(result.booleanValue(), extractor.isApplicable(mimetype));
	}

	/**
	 * Creates the types provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public static Object[][] mimetypeProvider() {
		return new Object[][] { { null, false }, { MediaType.OCTET_STREAM.toString(), false },
				{ MediaType.audio("aiff").toString(), false }, { MediaType.video("avi").toString(), false },
				{ MediaType.image("bmp").toString(), false }, { MediaType.TEXT_HTML.toString(), false },
				{ MediaType.TEXT_PLAIN.toString(), true }, { MediaType.application("msword").toString(), true } };
	}

	/**
	 * Gets a new simple descriptor mock
	 *
	 * @param testFile
	 *            is the file to get
	 * @return a descriptor mock
	 */
	private static FileDescriptor mockDescriptor(final String testFile) {
		FileDescriptor mock = Mockito.mock(FileDescriptor.class);
		Mockito.when(mock.getId()).thenReturn(testFile);
		Mockito.when(mock.getInputStream()).thenReturn(TikaContentExtractorTest.class.getResourceAsStream(testFile));
		return mock;
	}

}
