package com.sirma.itt.seip.instance.content.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.extensions.DefaultContentAccessProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Tests for {@link DefaultContentAccessProvider}
 *
 * @author BBonev
 */
public class DefaultContentAccessProviderTest {

	@InjectMocks
	private DefaultContentAccessProvider accessProvider;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getAddress() throws Exception {
		assertNull(accessProvider.getContentURI(null));

		Instance instance = new EmfInstance();
		assertNull(accessProvider.getContentURI(instance));

		instance.setId("emf:instance");
		assertEquals("/share/content/emf:instance", accessProvider.getContentURI(instance));
	}

	@Test
	public void getDescriptor() throws Exception {
		ContentInfo previewInfo = mock(ContentInfo.class);
		ContentInfo contentInfo = mock(ContentInfo.class);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		when(instanceContentService.getContent(instance, Content.PRIMARY_VIEW)).thenReturn(previewInfo);
		when(instanceContentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

		assertNull(accessProvider.getDescriptor(instance));
		when(previewInfo.exists()).thenReturn(Boolean.TRUE);

		assertEquals(previewInfo, accessProvider.getDescriptor(instance));

		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		assertEquals(contentInfo, accessProvider.getDescriptor(instance));
	}
}
