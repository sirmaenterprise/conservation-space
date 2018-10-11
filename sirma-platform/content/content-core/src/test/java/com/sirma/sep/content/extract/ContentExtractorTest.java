package com.sirma.sep.content.extract;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.TextExtractor;

/**
 * Test for {@link ContentExtractor}
 *
 * @author BBonev
 */
public class ContentExtractorTest {

	@InjectMocks
	private ContentExtractor extractor;

	@Mock
	private ContentPersister persister;
	@Spy
	private InstanceProxyMock<ContentPersister> contentPersister = new InstanceProxyMock<>();
	@Mock
	private TextExtractor contentService;

	@Before
	public void beforeMethod() throws IOException {
		MockitoAnnotations.initMocks(this);
		contentPersister.set(persister);
		when(contentService.extract(anyString(), any()))
				.then(a -> Optional.of(a.getArgumentAt(1, FileDescriptor.class).asString()));
	}

	@Test
	public void extractFromContent__invalidData() throws Exception {
		assertFalse(extractor.extractAndPersist(null, (Content) null));
		Content content = Content.createEmpty();
		assertFalse(extractor.extractAndPersist(null, content));
		assertFalse(extractor.extractAndPersist("emf:instanceId", content));
		content.setMimeType("text/html");
		assertFalse(extractor.extractAndPersist("emf:instanceId", content));
		content.setContent("text", "UTF-8");
		assertFalse(extractor.extractAndPersist(null, content));

		contentPersister.set(null);
		assertFalse(extractor.extractAndPersist("emf:instanceId", content));

		verify(persister, never()).savePrimaryView(any(), anyString());
		verify(persister, never()).savePrimaryContent(any(), anyString());
	}

	@Test
	public void extractFromContent_NonView() throws Exception {
		Content content = Content
				.createEmpty()
					.setMimeType("text/plain")
					.setContent("text", "UTF-8")
					.setView(false)
					.setIndexable(true);
		assertTrue(extractor.extractAndPersist("emf:instanceId", content));
		verify(persister).savePrimaryContent("emf:instanceId", "text");
		verify(persister, never()).savePrimaryView("emf:instanceId", "text");
	}

	@Test
	public void extractFromContentInfo_InvalidData() throws Exception {
		assertFalse(extractor.extractAndPersist("emf:instanceId", (ContentInfo) null));

		ContentInfo content = mock(ContentInfo.class);
		assertFalse(extractor.extractAndPersist("emf:instanceId", content));

		verify(persister, never()).savePrimaryView(any(), anyString());
		verify(persister, never()).savePrimaryContent(any(), anyString());
	}

	@Test
	public void extractFromContentInfo_NonView() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/html");
		when(content.asString()).thenReturn("text");
		when(content.isIndexable()).thenReturn(Boolean.TRUE);
		assertTrue(extractor.extractAndPersist("emf:instanceId", content));

		verify(persister).savePrimaryContent("emf:instanceId", "text");
		verify(persister, never()).savePrimaryView("emf:instanceId", "text");
	}
}
