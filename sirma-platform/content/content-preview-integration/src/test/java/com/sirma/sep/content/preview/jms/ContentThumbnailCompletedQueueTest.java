package com.sirma.sep.content.preview.jms;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.sep.content.preview.PreviewIntegrationTestUtils;
import com.sirma.sep.content.rendition.ThumbnailService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

/**
 * Tests the logic in {@link ContentThumbnailCompletedQueue} after a thumbnail has been generated and received in {@link
 * Message}.
 *
 * @author Mihail Radkov
 */
public class ContentThumbnailCompletedQueueTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private ThumbnailService thumbnailService;

	@InjectMocks
	private ContentThumbnailCompletedQueue contentThumbnailCompletedQueue;

	@Before
	public void initialize() throws IOException {
		MockitoAnnotations.initMocks(this);
		PreviewIntegrationTestUtils.stubInstanceTypeResolver(instanceTypeResolver, new EmfInstance("emf:123"));
	}

	@Test
	public void shouldAddGeneratedThumbnail() throws JMSException {
		String expectedThumbnail = "data:image/png;base64,thumbnail";
		contentThumbnailCompletedQueue.onContentThumbnailCompleted(stubMessage(false));
		Mockito.verify(thumbnailService, Mockito.times(2))
				.addThumbnail(Matchers.any(InstanceReference.class), Matchers.eq(expectedThumbnail));
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldNotAddThumbnailForMissingInstance() throws JMSException {
		PreviewIntegrationTestUtils.stubInstanceTypeResolver(instanceTypeResolver, null);
		contentThumbnailCompletedQueue.onContentThumbnailCompleted(stubMessage(false));
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldBlowDuringThumbnailRetrieval() throws JMSException {
		contentThumbnailCompletedQueue.onContentThumbnailCompleted(stubMessage(true));
	}

	private Message stubMessage(boolean blowDuringCopy) throws JMSException {
		return PreviewIntegrationTestUtils.stubMessage("thumbnail", blowDuringCopy);
	}

}
