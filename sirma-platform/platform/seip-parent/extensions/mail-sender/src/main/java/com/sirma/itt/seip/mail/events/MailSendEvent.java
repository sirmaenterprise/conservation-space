package com.sirma.itt.seip.mail.events;

import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.mail.MailMessage;

/**
 * Fired, when the message is successfully send to the mail server.
 *
 * @author A. Kunchev
 */
public class MailSendEvent implements EmfEvent {

	private final MailMessage message;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            the mail message that is send
	 */
	public MailSendEvent(MailMessage message) {
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public MailMessage getMessage() {
		return message;
	}

}
