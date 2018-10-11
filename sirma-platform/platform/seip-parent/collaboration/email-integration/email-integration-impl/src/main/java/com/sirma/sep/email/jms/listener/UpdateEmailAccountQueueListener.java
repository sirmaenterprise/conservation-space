package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * Message bean queue used to connect to local broker and update email account. Attributes of the account that need to
 * be updated are passed as key-value pairs with the message. Except the emailAddress, all other properties passed with
 * the message are considered account attributes and are passed as is for update to the underlying email platform.
 *
 * @author S.Djulgerova
 */
@Singleton
public class UpdateEmailAccountQueueListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEmailAccountQueueListener.class);

	@Inject
	private EmailAccountAdministrationService accountAdministrationService;

	/**
	 * Listener for update email account messages.
	 *
	 * @param msg
	 *            The payload message.
	 */
	@QueueListener(EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE)
	public void onUpdateEmailAccount(Message msg) {
		try {
			MapMessage message = (MapMessage) msg;
			EmailAccountInformation accountInfo = accountAdministrationService
					.getAccount(message.getString(EMAIL_ADDRESS));

			@SuppressWarnings("unchecked")
			Enumeration<String> mapNames = message.getMapNames();
			List<GenericAttribute> accountAttributes = new LinkedList<>();
			while (mapNames.hasMoreElements()) {
				String name = mapNames.nextElement();
				// emailAddress property is passed always and is the email address of the account to be altered, so it's
				// handled separately and should not go into attributes map
				if (EMAIL_ADDRESS.equals(name)) {
					continue;
				}
				String value = message.getString(name);
				accountAttributes.add(new GenericAttribute(name, value));
			}

			LOGGER.debug("Updating email account: [{}]", message.getString(EMAIL_ADDRESS));
			accountAdministrationService.modifyAccount(accountInfo.getAccountId(),
					accountAttributes);

		} catch (EmailIntegrationException | JMSException e) {
			throw new RollbackedRuntimeException("Email account update operation failed. " + e.getMessage(), e);
		}
	}
}