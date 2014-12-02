package com.sirma.itt.cmf.content.extract;


import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.adapter.FileDescriptor;

/**
 * @author bbanchev
 */
public class TikaContentExtractorTest {

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.parser.TikaContentExtractor#extractContentFromFile(com.sirma.itt.emf.adapter.FileDescriptor)}
	 * .
	 */
	@Test
	public final void testExtractContentFromFile() throws Exception {
		TikaContentExtractor tikaContentExtractor = new TikaContentExtractor();
		String extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("250+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("500+.doc"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("alfresco.exe"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("contentmodel.zip"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("ProductDetails.png"));
		Assert.assertNotNull(extractContentFromFile);
		extractContentFromFile = tikaContentExtractor.extract(mockDescriptor("null"));
		Assert.assertNotNull(extractContentFromFile);
	}

	/**
	 * Gets a new simple descriptor mock
	 *
	 * @param testFile
	 *            is the file to get
	 * @return a descriptor mock
	 */
	private FileDescriptor mockDescriptor(final String testFile) {
		FileDescriptor mock = Mockito.mock(FileDescriptor.class);
		Mockito.when(mock.getId()).thenReturn(testFile);
		Mockito.when(mock.getInputStream()).thenReturn(
				TikaContentExtractorTest.class.getResourceAsStream(testFile));
		return mock;
	}

}
