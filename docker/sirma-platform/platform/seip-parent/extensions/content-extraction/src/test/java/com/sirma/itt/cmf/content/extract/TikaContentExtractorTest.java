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

	private static final Long POSSIBLE_MAX_FILE_SIZE = 100L;
	private static final Long SMALL_FILE_SIZE = 10L;
	private static final Long BIG_FILE_SIZE = 1000L;

	@InjectMocks
	TikaContentExtractor extractor;

	@Spy
	ConfigurationProperty<Pattern> mimetypeMatchPattern = new ConfigurationPropertyMock<>(
			Pattern.compile(TikaContentExtractor.DEFAULT_TIKA_MIMETYPE_PATTERN));

	@Spy
	ConfigurationProperty<Long> maxFileSize = new ConfigurationPropertyMock<>(POSSIBLE_MAX_FILE_SIZE);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test method for
	 * {@link TikaContentExtractor#extract(FileDescriptor)}
	 * .
	 */
	@Test
	public final void testExtractContentFromFile() throws Exception {
		String extractContentFromFile = extractor.extract(mockDescriptor("250+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("250+.docx"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("500+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("sample_Doc.doc"));
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
	 * @param expectedResult
	 *            the expectedResult
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@UseDataProvider(location = TikaContentExtractorTest.class, value = "mimetypeProvider")
	public void testAccept(String mimetype, Long fileSize, Boolean expectedResult) throws Exception {
		FileDescriptor fileDescriptor = Mockito.mock(FileDescriptor.class);
		Mockito.when(fileDescriptor.length()).thenReturn(fileSize);
		Assert.assertEquals(expectedResult.booleanValue(), extractor.isApplicable(mimetype, fileDescriptor));
	}

	/**
	 * Creates the types provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public static Object[][] mimetypeProvider() {
		return new Object[][] {
				{ null, SMALL_FILE_SIZE, false },
				{ MediaType.OCTET_STREAM.toString(), SMALL_FILE_SIZE, false },
				{ MediaType.audio("aiff").toString(), SMALL_FILE_SIZE, false },
				{ MediaType.video("avi").toString(), SMALL_FILE_SIZE, false },
				{ MediaType.image("bmp").toString(), SMALL_FILE_SIZE, false },
				{ MediaType.TEXT_HTML.toString(), SMALL_FILE_SIZE, false },
				{ MediaType.TEXT_PLAIN.toString(), SMALL_FILE_SIZE, true },
				{ MediaType.application("msword").toString(), SMALL_FILE_SIZE, true },
				{ null, BIG_FILE_SIZE, false },
				{ MediaType.OCTET_STREAM.toString(), BIG_FILE_SIZE, false },
				{ MediaType.audio("aiff").toString(), BIG_FILE_SIZE, false },
				{ MediaType.video("avi").toString(), BIG_FILE_SIZE, false },
				{ MediaType.image("bmp").toString(), BIG_FILE_SIZE, false },
				{ MediaType.TEXT_HTML.toString(), BIG_FILE_SIZE, false },
				{ MediaType.TEXT_PLAIN.toString(), BIG_FILE_SIZE, false },
				{ MediaType.application("msword").toString(), BIG_FILE_SIZE, false }
		};
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
