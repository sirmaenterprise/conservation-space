package com.sirma.itt.seip.template;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.idoc.extract.WidgetContentExtractor;

/**
 * Test for {@link TemplateWidgetContentExtractor}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/09/2018
 */
public class TemplateWidgetContentExtractorTest {
	@InjectMocks
	private TemplateWidgetContentExtractor decorator = new TemplateWidgetContentExtractor() {};

	@Mock
	private WidgetContentExtractor delegate;

	@Mock
	private TemplateService templateService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(templateService.getTemplate("emf:template-id")).thenReturn(new Template());
		when(templateService.getTemplate("emf:not-a-template-id")).thenReturn(null);
	}

	@Test
	public void extractWidgetsContent_shouldCallActualExtractorIfNotTemplate() throws Exception {
		decorator.extractWidgetsContent("emf:not-a-template-id", mock(FileDescriptor.class));
		verify(delegate).extractWidgetsContent(any(), any());
	}

	@Test
	public void extractWidgetsContent_shouldNotCallActualExtractorIfTemplate() throws Exception {
		decorator.extractWidgetsContent("emf:template-id", mock(FileDescriptor.class));
		verify(delegate, never()).extractWidgetsContent(any(), any());
	}
}
