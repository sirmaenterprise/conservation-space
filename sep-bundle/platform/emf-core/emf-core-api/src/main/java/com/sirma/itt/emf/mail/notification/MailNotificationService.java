package com.sirma.itt.emf.mail.notification;

import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * The MailNotificationService is class that dynamically builds a ftl template with the details
 * provided in the {@link MailNotificationContext}.
 *
 * @author bbanchev
 */
public interface MailNotificationService {

	/**
	 * Send email to the user specified by the delegate's data.
	 *
	 * @param delegate
	 *            the delegate
	 * @throws EmfRuntimeException
	 *             on unexpected error during build of model - wrong users or model
	 */
	public void sendEmail(MailNotificationContext delegate) throws EmfRuntimeException;

	/**
	 * Synchronize templates.
	 * 
	 * @param event
	 *            the event
	 */
	@Asynchronous
	public void synchronizeTemplates(@Observes SyncMailNotificationTemplatesEvent event);

}
