package com.sirma.itt.seip.mail.attachments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link MailAttachmentService}.
 *
 * @author A. Kunchev
 */
public class MailAttachmentServiceImplTest {

	@InjectMocks
	private MailAttachmentService service;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private ConfigurationProperty<Long> maxAttachmentSize;

	@Before
	public void setup() {
		service = new MailAttachmentServiceImpl();
		MockitoAnnotations.initMocks(this);
		when(maxAttachmentSize.get()).thenReturn(15L);
	}

	@Test
	public void getAttachmentPart_nullAttachment_null() throws MessagingException {
		assertNull(service.getAttachmentPart(null));
	}

	@Test
	public void getAttachmentPart_nullAttachmentContentAndContentId_null() throws MessagingException {
		assertNull(service.getAttachmentPart(new MailAttachment()));
	}

	@Test
	public void getAttachmentPart_emptyAttachmentContentAndNullContentId_null() throws MessagingException {
		byte[] bytes = null;
		MailAttachment attachment = new MailAttachment("", "", bytes);
		assertNull(service.getAttachmentPart(attachment));
	}

	@Test
	public void getAttachmentPart_withContentNullMimeType_null() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", null, "testContent".getBytes());
		assertNull(service.getAttachmentPart(attachment));
	}

	@Test
	public void getAttachmentPart_withContentEmptyMimeType_null() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "", "testContent".getBytes());
		assertNull(service.getAttachmentPart(attachment));
	}

	@Test
	public void getAttachmentPart_withContent_bodyPart() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "text/plain", "testContent".getBytes());
		MimeBodyPart attachmentPart = service.getAttachmentPart(attachment);
		assertNotNull(attachmentPart);
		assertEquals("test", attachmentPart.getFileName());
	}

	@Test
	public void getAttachmentPart_withContent_emptyName_bodyPartWithGeneratedName() throws MessagingException {
		MailAttachment attachment = new MailAttachment("", "text/plain", "testContent".getBytes());
		MimeBodyPart attachmentPart = service.getAttachmentPart(attachment);
		assertNotNull(attachmentPart);
		assertTrue(attachmentPart.getFileName().startsWith("Generated_name-"));
	}

	@Test
	public void getAttachmentPart_withContent_nullName_bodyPartWithGeneratedName() throws MessagingException {
		MailAttachment attachment = new MailAttachment(null, "text/plain", "testContent".getBytes());
		MimeBodyPart attachmentPart = service.getAttachmentPart(attachment);
		assertNotNull(attachmentPart);
		assertTrue(attachmentPart.getFileName().startsWith("Generated_name-"));
	}

	@Test
	public void getAttachmentPart_withContentIdNullMimeType_null() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", null, "contentId");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn(null);
		when(instanceContentService.getContent(any(Serializable.class), eq(null))).thenReturn(content);
		assertNull(service.getAttachmentPart(attachment));
	}

	@Test
	public void getAttachmentPart_withContentIdEmptyMimeType_null() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "", "contentId");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("");
		when(instanceContentService.getContent(any(Serializable.class), eq(null))).thenReturn(content);
		assertNull(service.getAttachmentPart(attachment));
	}

	@Test
	public void getAttachmentPart_withContentIdBigLength_bodyPart() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "", "contentId");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/plain");
		when(content.getLength()).thenReturn(Long.MAX_VALUE);
		when(instanceContentService.getContent(any(Serializable.class), eq(null))).thenReturn(content);
		MimeBodyPart attachmentPart = service.getAttachmentPart(attachment);
		assertNull(attachmentPart);
	}

	@Test
	public void getAttachmentPart_withContentId_bodyPart() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "", "contentId");
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/plain");
		when(content.getLength()).thenReturn(10L);
		when(instanceContentService.getContent(any(Serializable.class), eq(null))).thenReturn(content);
		MimeBodyPart attachmentPart = service.getAttachmentPart(attachment);
		assertNotNull(attachmentPart);
		assertEquals("test", attachmentPart.getFileName());
	}

	@Test
	public void getAttachmentParts_nullAttachments_null() throws MessagingException {
		assertEquals(Collections.emptyList(), service.getAttachmentParts(null));
	}

	@Test
	public void getAttachmentParts_emptyAttachments_null() throws MessagingException {
		assertEquals(Collections.emptyList(), service.getAttachmentParts(new MailAttachment[0]));
	}

	@Test
	public void getAttachmentParts_withAttachments_notEmptyCollection() throws MessagingException {
		MailAttachment attachment = new MailAttachment("test", "text/plain", "testContent".getBytes());
		assertFalse(service.getAttachmentParts(new MailAttachment[] { null, attachment }).isEmpty());
	}

	@Test
	public void deleteAttachmentsContent_nullAttachments_noServiceCalled() {
		service.deleteMailAttachmentsContent(null);
		verify(instanceContentService, never()).deleteContent(anyString(), eq(null));
	}

	@Test
	public void deleteAttachmentsContent_emptyAttachments_noServiceCalled() {
		service.deleteMailAttachmentsContent(new MailAttachment[] {});
		verify(instanceContentService, never()).deleteContent(anyString(), eq(null));
	}

	@Test
	public void deleteAttachmentsContent_oneAttachmentNullContentId_noServiceCalled() {
		service.deleteMailAttachmentsContent(new MailAttachment[] { new MailAttachment() });
		verify(instanceContentService, never()).deleteContent(anyString(), eq(null));
	}

	@Test
	public void deleteAttachmentsContent_oneAttachmentWithContentId_serviceCalled() {
		service.deleteMailAttachmentsContent(
				new MailAttachment[] { new MailAttachment("name", "mimeType", "contentId") });
		verify(instanceContentService).deleteContent(anyString(), eq(null));
	}

}
