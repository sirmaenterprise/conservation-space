package com.sirma.sep.ocr.entity;

import static com.sirma.sep.ocr.entity.ContentMessageAttributes.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.EFFECTIVE_USER_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.FILE_EXTENSION;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.FILE_NAME;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.INSTANCE_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.INSTANCE_VERSION_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.MIMETYPE;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.OCRED_CONTENT_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.OCRED_VERSION_CONTENT_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.REQUEST_ID_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.TENANT_ID_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;

import org.junit.Test;

/**
 * Test for {@link MessageBuilder}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
public class MessageBuilderTest {

	@Test
	public void testBuildOutputMessage() throws Exception {
		File file = File.createTempFile("some-prefix", ".pdf");
		file.deleteOnExit();

		InputDocument document = mock(InputDocument.class);
		when(document.getFileName()).thenReturn(file.getName());
		when(document.getFileExtension()).thenReturn(
				"." + org.apache.commons.io.FilenameUtils.getExtension(file.getName()));
		when(document.getInstanceId()).thenReturn("id");
		when(document.getTenantId()).thenReturn("tenantId");
		when(document.getRequestId()).thenReturn("requestId");
		when(document.getAuthenticatedUser()).thenReturn("user");
		when(document.getEffectiveUser()).thenReturn("effectiveUser");
		when(document.getOcredContentId()).thenReturn("contentId");
		when(document.getOcredVersionContentId()).thenReturn("versionContentId");
		when(document.getInstanceVersionId()).thenReturn("versionId");

		Session session = mock(Session.class);

		BytesMessage bytesMessage = mock(BytesMessage.class);
		when(session.createBytesMessage()).thenReturn(bytesMessage);

		javax.jms.Message message = MessageBuilder.buildOutputMessage(document, session, file);

		verify(bytesMessage).setStringProperty(FILE_NAME.toString(), file.getName());
		verify(bytesMessage).setStringProperty(FILE_EXTENSION.toString(), ".pdf");
		verify(bytesMessage).setStringProperty(MIMETYPE.toString(), "application/pdf");
		verify(bytesMessage).setStringProperty(INSTANCE_ID.toString(), "id");
		verify(bytesMessage).setStringProperty(TENANT_ID_KEY.toString(), "tenantId");
		verify(bytesMessage).setStringProperty(REQUEST_ID_KEY.toString(), "requestId");
		verify(bytesMessage).setStringProperty(AUTHENTICATED_USER_KEY.toString(), "user");
		verify(bytesMessage).setStringProperty(EFFECTIVE_USER_KEY.toString(), "effectiveUser");
		verify(bytesMessage).setStringProperty(OCRED_CONTENT_ID.toString(), "contentId");
		verify(bytesMessage).setStringProperty(OCRED_VERSION_CONTENT_ID.toString(), "versionContentId");
		verify(bytesMessage).setStringProperty(INSTANCE_VERSION_ID.toString(), "versionId");
		verify(bytesMessage).writeBytes(any());
	}

	@Test(expected = MessageBuildException.class)
	public void testBuildOutputMessage_messageCannotBeConstructed_cantGetBytes() throws Exception {
		InputDocument document = mock(InputDocument.class);
		Session session = mock(Session.class);
		when(session.createBytesMessage()).thenThrow(new JMSException("error"));

		File file = File.createTempFile("some-prefix", "some-ext");
		file.deleteOnExit();

		MessageBuilder.buildOutputMessage(document, session, file);
	}

	@Test(expected = MessageBuildException.class)
	public void testBuildOutputMessage_messageCannotBeConstructed() throws Exception {
		InputDocument document = mock(InputDocument.class);
		Session session = mock(Session.class);

		BytesMessage bytesMessage = mock(BytesMessage.class);
		doThrow(new JMSException("error")).when(bytesMessage).writeBytes(any());

		when(session.createBytesMessage()).thenReturn(bytesMessage);

		File file = File.createTempFile("some-prefix", "some-ext");
		file.deleteOnExit();
		MessageBuilder.buildOutputMessage(document, session, file);
	}
}