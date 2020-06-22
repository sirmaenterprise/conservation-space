package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * Message bean queue used to connect to local broker and delete email account.
 *
 * @author S.Djulgerova
 */
@Singleton
public class DeleteEmailAccountQueueListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteEmailAccountQueueListener.class);

	@Inject
	private EmailAccountAdministrationService accountAdministrationService;

	@Inject
	private ShareFolderAdministrationService shareFolderAdministrationService;

	/**
	 * Listener for delete email account messages. Account id is provided with the message. If an error occurs during
	 * create account request, then an exception is thrown and the request is retried.
	 *
	 * @param msg
	 *            The payload message.
	 */
	@QueueListener(EmailIntegrationQueueNames.DELETE_EMAIL_ACCOUNT_QUEUE)
	public void onDeleteAccount(Message msg) {
		try {
			MapMessage message = (MapMessage) msg;
			String emailaddress = message.getString(EMAIL_ADDRESS);
			if (emailaddress != null) {
				EmailAccountInformation account = accountAdministrationService.getAccount(emailaddress);
				LOGGER.debug("Deleting email account: [{}]", emailaddress);
				accountAdministrationService.deleteAccount(account.getAccountId());
				shareFolderAdministrationService.removeContactFromShareFolder(emailaddress);
			}
		} catch (EmailIntegrationException | JMSException e) {
			LOGGER.warn(
					"There's error during email account delete operation. Message will be redelivered and operation will be retried. "
							+ "If max delivery attemps limit is riched this message will be send to dead letters queue. "
							+ e.getMessage(),
					e);
			throw new RollbackedRuntimeException("Email account delete operation failed " + e.getMessage(), e);
		}
	}
}