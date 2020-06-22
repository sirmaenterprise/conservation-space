package com.sirma.itt.seip.mail.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.mail.attachments.MailAttachmentService;
import com.sirma.itt.seip.mail.events.MailSendEvent;

/**
 * Observer for mail attachments related events.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class MailAttachmentsObserver {

	@Inject
	private MailAttachmentService mailAttachmentService;

	/**
	 * Observer for {@link MailSendEvent} which will trigger the deleting of mail attachments content.
	 *
	 * @param event
	 *            the event that holds the attachments which content will be deleted
	 */
	public void clearAttachmentsContent(@Observes MailSendEvent event) {
		if (event.getMessage() != null) {
			mailAttachmentService.deleteMailAttachmentsContent(event.getMessage().getAttachments());
		}
	}

}
