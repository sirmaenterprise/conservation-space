package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.ContentPersistProvider.PreviousVersion;

/**
 * Test for {@link ContentPersistProvider}.
 *
 * @author A. Kunchev
 */
public class ContentPersistProviderTest {

	@Mock
	private ContentStoreProvider contentStoreProvider;

	@Mock
	private ContentStore contentStore;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void contentProxyCheck() {
		when(contentStoreProvider.getTempStore()).thenReturn(contentStore);
		ContentInfo previousVersion = mock(ContentInfo.class);
		when(previousVersion.getContentId()).thenReturn("content-id");
		when(previousVersion.getLength()).thenReturn(128L);
		when(previousVersion.getCharset()).thenReturn(StandardCharsets.UTF_8.name());
		when(previousVersion.getMimeType()).thenReturn(MediaType.APPLICATION_JSON);
		when(previousVersion.getName()).thenReturn("file-name");
		when(previousVersion.getContentPurpose()).thenReturn("content-purpose");
		when(previousVersion.isView()).thenReturn(false);
		when(previousVersion.isIndexable()).thenReturn(false);

		PreviousVersion version = new PreviousVersion(contentStoreProvider, new EmfInstance(), previousVersion);
		assertNotNull(version);
		verify(contentStore).add(any(Instance.class), argThat(CustomMatcher.of((Content content) -> {
			assertEquals("content-id", content.getContentId());
			assertEquals(Long.valueOf(128L), content.getContentLength());
			assertEquals(MediaType.APPLICATION_JSON, content.getMimeType());
			assertEquals("file-name", content.getName());
			assertEquals("content-purpose", content.getPurpose());
			assertEquals(StandardCharsets.UTF_8.name(), content.getCharset());
			assertFalse(content.isView());
			assertFalse(content.isIndexable());
		})));
	}

}
