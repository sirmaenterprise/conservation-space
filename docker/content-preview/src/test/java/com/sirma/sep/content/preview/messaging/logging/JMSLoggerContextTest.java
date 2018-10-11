package com.sirma.sep.content.preview.messaging.logging;

import com.sirma.sep.content.preview.messaging.ContentMessageAttributes;
import com.sirma.sep.content.preview.messaging.MessageMock;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Tests the diagnostic context assignment in {@link JMSLoggerContext} from incoming {@link Message}.
 *
 * @author Mihail Radkov
 */
public class JMSLoggerContextTest {

	@Test
	public void shouldSetDiagnosticContextFromMessageAttributes() throws JMSException {
		Message message = MessageMock.mockMessage("mimetype");

		JMSLoggerContext.onMessage(message);
		Assert.assertEquals("request-id", MDC.get(ContentMessageAttributes.REQUEST_ID));
		Assert.assertEquals("instance-id", MDC.get(ContentMessageAttributes.INSTANCE_ID));
		Assert.assertEquals("content-id", MDC.get(ContentMessageAttributes.CONTENT_ID));
		Assert.assertEquals("instance-version-id", MDC.get(ContentMessageAttributes.INSTANCE_VERSION_ID));
		Assert.assertEquals("auth-user", MDC.get(ContentMessageAttributes.AUTHENTICATED_USER));
		Assert.assertEquals("effective-user", MDC.get(ContentMessageAttributes.EFFECTIVE_USER));
		Assert.assertEquals("tenant-id", MDC.get(ContentMessageAttributes.TENANT_ID));
	}

	@Test
	public void emptyMessages_shouldNotBeProcessed() throws JMSException {
		JMSLoggerContext.onMessage(null);
	}

	@Test(expected = JMSException.class)
	public void shouldRethrowExceptions() throws JMSException {
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getStringProperty(Matchers.anyString())).thenThrow(new JMSException(""));
		JMSLoggerContext.onMessage(message);
	}
}
