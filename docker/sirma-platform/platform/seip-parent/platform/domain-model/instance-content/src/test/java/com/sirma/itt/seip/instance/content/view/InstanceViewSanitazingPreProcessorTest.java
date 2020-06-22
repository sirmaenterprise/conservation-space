/**
 *
 */
package com.sirma.itt.seip.instance.content.view;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ViewPreProcessorContext;
import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

public class InstanceViewSanitazingPreProcessorTest {

	@Mock
	private IdocSanitizer idocSanitizer;

	@InjectMocks
	private InstanceViewSanitazingPreProcessor preProcessor;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testSanitizeCall() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(null,
				Content.createEmpty().setContent("<xml/>", StandardCharsets.UTF_8.name()));
		preProcessor.process(context);
		verify(idocSanitizer).sanitize(any(Document.class), any());
	}

	@Test
	public void testNoContentToSanitize() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(null, null);
		preProcessor.process(context);
		verify(idocSanitizer, never()).sanitize(any(Document.class), any());
	}

}
