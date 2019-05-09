package com.sirma.itt.seip.content.processing;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * test for {@link ContentMigrationRest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/01/2019
 */
public class ContentMigrationRestTest {

	@InjectMocks
	private ContentMigrationRest contentMigrationRest;
	@Mock
	private ContentMigrator downloader;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void downloadEmbeddedImages_shouldConvertPatternsToRegex() throws Exception {
		contentMigrationRest.downloadEmbeddedImages(Arrays.asList("*", "*sirma.bg*", "http*sirma*"));
		ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
		verify(downloader).downloadEmbeddedImages(captor.capture());
		assertTrue(Arrays.stream(captor.getValue())
				.map(Pattern::compile)
				.allMatch(pattern -> pattern.matcher("https://ses.sirma.bg/sep").matches()));
	}
}