package com.sirma.sep.content.extract;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;

/**
 * Test for {@link ContentExtractHandlerTest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/07/2018
 */
public class ContentExtractHandlerTest {
	@InjectMocks
	private ContentExtractHandler handler;

	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private ContentExtractor contentExtractor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onContent() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/html");
		when(content.asString()).thenReturn("text");
		when(content.exists()).thenReturn(Boolean.TRUE);
		when(content.isIndexable()).thenReturn(Boolean.TRUE);
		when(instanceContentService.getContent("emf:contentId", null)).thenReturn(content);

		handler.onContent(buildMessage("emf:instanceId", "emf:contentId"));
		verify(contentExtractor).extractAndPersist(eq("emf:instanceId"), any(ContentInfo.class));
	}

	@Test(expected = ContentNotFoundRuntimeException.class)
	public void onContent_ShouldFailOnNonExistingContent() throws Exception {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/html");
		when(content.asString()).thenReturn("text");
		when(content.exists()).thenReturn(Boolean.FALSE);
		when(content.isIndexable()).thenReturn(Boolean.TRUE);
		when(instanceContentService.getContent("emf:contentId", null)).thenReturn(content);

		handler.onContent(buildMessage("emf:instanceId", "emf:contentId"));
	}

	private Message buildMessage(String instanceId, String contentId) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn(contentId);
		when(message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID)).thenReturn(instanceId);
		return message;
	}

}
