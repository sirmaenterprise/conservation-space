package com.sirma.itt.seip.mail.observers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.mail.MailMessage;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.mail.attachments.MailAttachmentService;
import com.sirma.itt.seip.mail.events.MailSendEvent;

/**
 * Test for {@link MailAttachmentsObserver}.
 *
 * @author A. Kunchev
 */
public class MailAttachmentsObserverTest {

	@InjectMocks
	private MailAttachmentsObserver observer;

	@Mock
	private MailAttachmentService mailAttachmentService;

	@Before
	public void setup() {
		observer = new MailAttachmentsObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void clearAttachmentsContent_nullMessage_serviceNotCalled() {
		observer.clearAttachmentsContent(new MailSendEvent(null));
		verify(mailAttachmentService, never()).deleteMailAttachmentsContent(any(MailAttachment[].class));
	}

	@Test
	public void clearAttachmentsContent_oneAttachments_serviceCalled() {
		MailMessage message = new MailMessage();
		message.setAttachments(new MailAttachment[] { new MailAttachment() });
		observer.clearAttachmentsContent(new MailSendEvent(message));
		verify(mailAttachmentService).deleteMailAttachmentsContent(any(MailAttachment[].class));
	}

}
