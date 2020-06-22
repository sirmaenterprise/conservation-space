package com.sirma.itt.cmf.content.extract;

import static org.testng.Assert.assertNotNull;

import java.util.regex.Pattern;

import org.apache.tika.mime.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author bbanchev
 */
@Test
public class JsoapContentExtractorTest {

	@InjectMocks
	JsoapTextExtraction extractor;

	@Spy
	ConfigurationProperty<Pattern> mimetypeMatchPattern = new ConfigurationPropertyMock<>(
			Pattern.compile(JsoapTextExtraction.DEFAULT_JSOAP_MIMETYPE_PATTERN));

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test method for
	 * {@link TikaContentExtractor#extract(FileDescriptor)}
	 * .
	 */
	public final void testExtractContentFromFile() throws Exception {
		String extractContentFromFile = extractor.extract(mockDescriptor("idoc.html"));
		assertNotNull(extractContentFromFile);
		extractContentFromFile = extractor.extract(mockDescriptor("topic.xml"));
		assertNotNull(extractContentFromFile);
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
	@Test(dataProvider = "mimetypeProvider")
	public void testAccept(String mimetype, Boolean result) throws Exception {
		Assert.assertEquals(extractor.isApplicable(mimetype, Mockito.mock(FileDescriptor.class)), result.booleanValue());
	}

	/**
	 * Creates the types provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "mimetypeProvider")
	public Object[][] createTypesProvider() {
		return new Object[][] { { null, false }, { MediaType.OCTET_STREAM.toString(), false },
				{ MediaType.audio("aiff").toString(), false }, { MediaType.video("avi").toString(), false },
				{ MediaType.image("bmp").toString(), false }, { MediaType.TEXT_HTML.toString(), true },
				{ MediaType.TEXT_PLAIN.toString(), false }, { MediaType.APPLICATION_XML.toString(), true } };
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
		Mockito.when(mock.getInputStream()).thenReturn(JsoapContentExtractorTest.class.getResourceAsStream(testFile));
		return mock;
	}

}
