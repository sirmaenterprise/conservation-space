package com.sirma.itt.seip.instance.content.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.extensions.DefaultContentLoader;
import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * Test {@link DefaultContentLoader}
 *
 * @author BBonev
 */
public class DefaultContentLoaderTest {
	@InjectMocks
	private DefaultContentLoader contentLoader;
	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void loadContent() throws Exception {
		ContentInfo previewInfo = mock(ContentInfo.class);
		ContentInfo contentInfo = mock(ContentInfo.class);

		when(previewInfo.asString()).thenReturn("preview");
		when(contentInfo.asString()).thenReturn("content");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		when(instanceContentService.getContent(instance, Content.PRIMARY_VIEW)).thenReturn(previewInfo);
		when(instanceContentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

		assertNull(contentLoader.loadContent(instance));

		when(previewInfo.exists()).thenReturn(Boolean.TRUE);
		assertEquals("preview", contentLoader.loadContent(instance));

		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getMimeType()).thenReturn("text/plain");
		assertEquals("previewcontent", contentLoader.loadContent(instance));
	}

	@Test
	public void loadContent_withError() throws Exception {
		ContentInfo previewInfo = mock(ContentInfo.class);
		ContentInfo contentInfo = mock(ContentInfo.class);

		when(previewInfo.asString()).thenThrow(IOException.class);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		when(instanceContentService.getContent(instance, Content.PRIMARY_VIEW)).thenReturn(previewInfo);
		when(instanceContentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		when(previewInfo.exists()).thenReturn(Boolean.TRUE);

		assertNull(contentLoader.loadContent(instance));
	}

	@Test
	public void loadContent_notText() throws Exception {
		ContentInfo previewInfo = mock(ContentInfo.class);
		ContentInfo contentInfo = mock(ContentInfo.class);

		when(previewInfo.asString()).thenReturn("preview");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		when(instanceContentService.getContent(instance, Content.PRIMARY_VIEW)).thenReturn(previewInfo);
		when(instanceContentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);
		when(previewInfo.exists()).thenReturn(Boolean.TRUE);

		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getMimeType()).thenReturn(null);
		assertEquals("preview", contentLoader.loadContent(instance));
	}
}
