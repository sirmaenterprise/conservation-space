package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link TextExtractor}
 *
 * @author BBonev
 */
public class TextExtractorTest {
	@InjectMocks
	private TextExtractor textExtractor;

	@Mock
	private TextExtractorExtension extractor;

	private List<TextExtractorExtension> extractorsList = new LinkedList<>();
	@Spy
	private Plugins<TextExtractorExtension> extractors = new Plugins<>("", extractorsList);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		extractorsList.clear();
		extractorsList.add(extractor);
	}

	@Test
	public void test_extractContent() throws IOException {
		when(extractor.isApplicable(Matchers.eq("text/plain"), Matchers.any(FileDescriptor.class))).thenReturn(Boolean.TRUE);
		when(extractor.extract(any(FileDescriptor.class))).thenReturn("Content");

		Optional<String> content = textExtractor.extract("text/plain", mock(FileDescriptor.class));
		assertNotNull(content);
		assertEquals("Content", content.get());
	}

	@Test
	public void test_extractContent_error() throws IOException {
		when(extractor.isApplicable("text/plain", Mockito.mock(FileDescriptor.class))).thenReturn(Boolean.TRUE);
		when(extractor.extract(any(FileDescriptor.class))).thenThrow(IOException.class);

		Optional<String> content = textExtractor.extract("text/plain", mock(FileDescriptor.class));
		assertNotNull(content);
		assertFalse(content.isPresent());
	}

}
