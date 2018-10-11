package com.sirma.itt.seip.template;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Test for {@link TemplateSanitizerPreProcessor}
 *
 * @author BBonev
 */
public class TemplateSanitizerPreProcessorTest {

	@InjectMocks
	private TemplateSanitizerPreProcessor preProcessor;

	@Mock
	private IdocSanitizer sanitizer;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(sanitizer.sanitizeTemplate(anyString(), any())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void shouldCallSanitizerWithContent() throws Exception {
		Template template = new Template();
		template.setContent("templateContent");

		preProcessor.process(new TemplateContext(template));

		verify(sanitizer).sanitizeTemplate(anyString(), any());
	}

	@Test
	public void shouldNotCallSanitizerWithoutContent() throws Exception {
		preProcessor.process(new TemplateContext(new Template()));

		verify(sanitizer, never()).sanitizeTemplate(anyString(), any());
	}
}
