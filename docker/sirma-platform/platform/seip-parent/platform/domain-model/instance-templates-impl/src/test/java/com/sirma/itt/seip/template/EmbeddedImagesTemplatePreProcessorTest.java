package com.sirma.itt.seip.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.nodes.image.ImageNode;
import com.sirma.sep.content.idoc.nodes.image.ImageNodeBuilder;

/**
 * Test for {@link EmbeddedImagesTemplatePreProcessor}
 *
 * @author BBonev
 */
public class EmbeddedImagesTemplatePreProcessorTest {

	private static final String VALID_TEMPLATE = "template-with-images.html";
	private static final String INVALID_TEMPLATE = "template-with-invalid-images.html";

	@InjectMocks
	private EmbeddedImagesTemplatePreProcessor preProcessor;

	@Mock
	private InstanceContentService contentService;
	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	@BeforeClass
	public static void initClass() {
		ContentNodeFactory.getInstance().registerBuilder(new ImageNodeBuilder());
	}

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(contentService.getContent("emf:valid-id", null)).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.exists()).thenReturn(Boolean.TRUE);
			when(info.getMimeType()).thenReturn("image/png");
			when(info.getInputStream()).then(aa -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
			return info;
		});
		when(contentService.getContent("emf:fail-id", null)).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.exists()).thenReturn(Boolean.TRUE);
			when(info.getMimeType()).thenReturn("image/png");
			when(info.getInputStream()).thenThrow(IOException.class);
			return info;
		});
		when(contentService.getContent("emf:invalid-id", null)).then(a -> ContentInfo.DO_NOT_EXIST);
	}

	@Test
	public void processTemplate() throws Exception {
		Template template = new Template();
		template.setContent(loadContent(VALID_TEMPLATE));
		TemplateContext context = new TemplateContext(template);
		preProcessor.process(context);

		Idoc idoc = Idoc.parse(template.getContent());
		long images = idoc
				.children()
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(ImageNode::isEmbedded)
					.count();
		assertEquals(3, images);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void processTemplateWithInvalidImages() throws Exception {
		Template template = new Template();
		template.setContent(loadContent(INVALID_TEMPLATE));
		TemplateContext context = new TemplateContext(template);
		preProcessor.process(context);
	}

	@Test
	public void processTemplate_noContent() throws Exception {
		Template template = new Template();
		template.setContent("");
		TemplateContext context = new TemplateContext(template);
		preProcessor.process(context);
	}

	@Test
	public void processTemplate_nothingToProcess() throws Exception {
		Template template = new Template();
		template.setContent("<div data-tabs-counter=\"1\"><section data-id=\"tab-1\" data-title=\"Report\"/></div>");
		TemplateContext context = new TemplateContext(template);
		preProcessor.process(context);
		Idoc idoc = Idoc.parse(template.getContent());
		long images = idoc
				.children()
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(ImageNode::isEmbedded)
					.count();
		assertEquals(0, images);
	}

	private static String loadContent(String template) {
		try (InputStream stream = EmbeddedImagesTemplatePreProcessor.class
				.getClassLoader()
					.getResourceAsStream(template)) {
			return IOUtils.toString(stream);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
}
