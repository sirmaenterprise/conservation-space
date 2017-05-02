package com.sirma.itt.seip.mail.service.mock;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationServiceImpl;

/**
 * Mock service for mails notifications
 *
 * @author bbanchev
 */
@ApplicationScoped
@Specializes
public class MailNotificationServiceMock extends MailNotificationServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Send email to the user specified by the delegate's data.
	 *
	 * @param delegate
	 *            the delegate
	 * @throws EmfRuntimeException
	 *             on unexpected error during build of model - wrong users or model
	 */
	@Override
	public void sendEmail(MailNotificationContext delegate, String mailGroupId) throws EmfRuntimeException {
		LOGGER.debug("Sending mail: {}, and mailsId: {}", delegate, mailGroupId);
	}

}
