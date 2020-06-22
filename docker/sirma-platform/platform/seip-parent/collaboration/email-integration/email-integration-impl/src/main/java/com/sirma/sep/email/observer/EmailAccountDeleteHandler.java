package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;
import com.sirma.sep.email.EmailIntegrationConstants.EmailAccountStatus;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Observer that listens for a {@link BeforeInstanceDeleteEvent} event and send message to JMS queue
 *
 * @author S.Djulgerova
 */
@TransactionScoped
public class EmailAccountDeleteHandler implements Serializable {

	private static final long serialVersionUID = -3648286919686979540L;

	@Inject
	@JmsSender(destination = EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE)
	private MessageSender updateEmailAccountSender;

	/**
	 * Email account should be deleted when instance is deleted. Account delete is asynchronous.
	 * {@link BeforeInstanceDeleteEvent} is fired from the core module when delete operation is started. This method
	 * intercepts {@link BeforeInstanceDeleteEvent} extracts emailAddress and put it in a JMS persistent queue.
	 *
	 * @param event
	 *            the event observed
	 */
	public void onAfterInstanceDeleteEvent(@Observes BeforeInstanceDeleteEvent<?, ?> event) {
		String account = event.getInstance().getString(EMAIL_ADDRESS);
		if (account == null) {
			return;
		}
		try {
			deleteAccount(account);
		} catch (JMSException e) {
			throw new RollbackedRuntimeException("Email account delete operation failed for account:" + account, e);
		}
	}

	/**
	 * Send message to JMS queue with information for email account to be deleted.
	 *
	 * @param emailAddress
	 *            the email address which will be deleted
	 */
	private void deleteAccount(String emailAddress) throws JMSException {
		Map<String, String> attributes = CollectionUtils.createHashMap(2);
		attributes.put(ZIMBRA_ACCOUNT_STATUS, EmailAccountStatus.CLOSED.getStatus());
		attributes.put(EMAIL_ADDRESS, emailAddress);
		updateEmailAccountSender.send(attributes);
	}

}
