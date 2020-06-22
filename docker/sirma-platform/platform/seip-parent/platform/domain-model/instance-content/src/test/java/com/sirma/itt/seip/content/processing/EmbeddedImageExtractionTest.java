package com.sirma.itt.seip.content.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.IdResolver;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.InstanceViewPreProcessor;
import com.sirma.sep.content.ViewPreProcessorContext;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.nodes.image.ImageNode;
import com.sirma.sep.content.idoc.nodes.image.ImageNodeBuilder;

/**
 * Test for {@link EmbeddedImageExtraction}
 *
 * @author BBonev
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({IdResolver.class, EmbeddedImageExtractor.class, SecurityContextManagerFake.class, TransactionSupportFake.class})
@AdditionalPackages({ TypeConverterImpl.class})
@AdditionalClasspaths({})
public class EmbeddedImageExtractionTest {

	@Inject
	@Extension(target = InstanceViewPreProcessor.TARGET_NAME, order = 50)
	private EmbeddedImageExtraction extension;

	@Mock
	@Produces
	private InstanceContentService contentService;

	@Mock
	@Produces
	private SecurityContext securityContext;

	@Inject
	private SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();

	@Spy
	@Produces
	@Configuration
	private ConfigurationProperty<String[]> whitelistPatterns = new ConfigurationPropertyMock<>(new String[]{});

	@Mock
	@Produces
	private ImageDownloader imageDownloader;

	@BeforeClass
	public static void beforeClass() {
		ContentNodeFactory.getInstance().registerBuilder(new ImageNodeBuilder());
	}

	@Before
	public void beforeMethod() {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
		when(contentService.saveContent(any(Serializable.class), any(Content.class)))
				.then(a -> mockContentInfo(true));
		securityContextManager.setCurrentContext(securityContext);

		when(imageDownloader.download(any(), any(), any())).then(a -> {
			BiFunction<String, byte[], String> function = a.getArgumentAt(1, BiFunction.class);
			return function.apply("image/png", "same image data".getBytes(StandardCharsets.UTF_8));
		});
	}

	@Test
	public void should_ExtractEmbeddedImages() throws Exception {

		when(contentService.getContent("already-embedded", null)).then(a -> mockContentInfo(true));
		when(contentService.getContent("already-embedded-somewhere-else", null)).then(a -> mockContentInfo(false));

		ViewPreProcessorContext context = new ViewPreProcessorContext(new EmfInstance(), createIdocContent());
		extension.process(context);

		// wait for up to 2 seconds for 4 invocations of saveContent to happen
		verify(contentService, timeout(2000).times(4)).saveContent(any(Serializable.class), any(Content.class));

		Idoc idoc = Idoc.parse(context.getView().getContent().getInputStream());
		List<ImageNode> imageLinks = idoc
				.children()
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.collect(Collectors.toList());

		assertEquals(5, imageLinks.size());

		assertFalse(imageLinks.get(0).isEmbedded());
		assertFalse(imageLinks.get(1).isEmbedded());
		assertFalse(imageLinks.get(2).isEmbedded());
		assertFalse(imageLinks.get(3).isEmbedded());
		assertFalse(imageLinks.get(4).isEmbedded());

		assertTrue(imageLinks.get(0).hasImageDimensions());
		assertTrue(imageLinks.get(1).hasImageDimensions());
		assertTrue(imageLinks.get(2).hasImageDimensions());
		assertTrue(imageLinks.get(3).hasImageDimensions());
		assertTrue(imageLinks.get(4).hasImageDimensions());

		assertEquals("", imageLinks.get(0).getSource());
		assertEquals("", imageLinks.get(1).getSource());
		assertEquals("", imageLinks.get(2).getSource());
		assertEquals("", imageLinks.get(3).getSource());
		assertEquals("", imageLinks.get(4).getSource());
	}

	@Test
	public void should_DoNothing_OnMissingOrInvalidContent() throws Exception {
		ViewPreProcessorContext context = new ViewPreProcessorContext(new EmfInstance(), null);
		extension.process(context);
		context = new ViewPreProcessorContext(new EmfInstance(), createDummyContent(""));
		extension.process(context);
	}

	@Test
	public void should_IgnoreFailingContentSave() throws Exception {
		AtomicInteger integer = new AtomicInteger(0);
		reset(contentService);
		// simulate failure of content save without and with exception
		when(contentService.saveContent(any(Serializable.class), any(Content.class))).then(a -> {
			if (integer.getAndIncrement() % 2 == 0) {
				return mockContentInfo(false);
			}
			throw new IOException();
		});
		when(contentService.getContent("already-embedded", null)).then(a -> mockContentInfo(true));
		when(contentService.getContent("already-embedded-somewhere-else", null)).then(a -> mockContentInfo(false));

		ViewPreProcessorContext context = new ViewPreProcessorContext(new EmfInstance(), createIdocContent());
		extension.process(context);

		verify(contentService, atLeastOnce()).saveContent(any(Serializable.class), any(Content.class));

		// this test is hard to check as the processing is done asynchronously and we cannot guarantee the order of
		// processed elements, but by definition we will have at least one processed element (the first one)
	}

	private static Content createIdocContent() {
		return Content.create("idoc-with-image.html", FileDescriptor
				.create(() -> EmbeddedImageExtractionTest.class.getResourceAsStream("/idoc-with-image.html"), -1));
	}

	private static Content createDummyContent(String content) {
		return Content.create("idoc-with-image.html",
				FileDescriptor.create(() -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), -1));
	}

	private static Object mockContentInfo(boolean exists) {
		ContentInfo info = mock(ContentInfo.class);
		when(info.getContentId()).thenReturn(UUID.randomUUID().toString());
		when(info.exists()).thenReturn(exists);
		return info;
	}
}
