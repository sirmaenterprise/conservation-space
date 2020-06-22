package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.TransactionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.security.SecurityMode;

/**
 * Observer that listens for a {@link InstanceChangeEvent} event and update instance email account
 *
 * @author S.Djulgerova
 */
@TransactionScoped
public class EmailAccountUpdateHandler implements Serializable {

	private static final long serialVersionUID = 5419809613776509838L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAccountUpdateHandler.class);

	private static final String NO_ID = "NO_ID";

	@Inject
	@JmsSender(destination = EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE, security = SecurityMode.TENANT_ADMIN)
	private MessageSender updateEmailAccountQueue;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	/**
	 * Update existing email account details every time when the instance is successfully updated
	 *
	 * @param event
	 *            the event observed
	 */
	public void onInstanceChanged(@Observes InstanceChangeEvent<?> event) {
		Instance instance = event.getInstance();
		if (!event.getInstance().type().isMailboxSupportable()
				|| emailAddressResolver.getEmailAddress(instance.getString(EMAIL_ADDRESS)) == null) {
			LOGGER.debug(
					"Can't update email account for instance which doesn't have one or doesn't support mailboxes: [{}]",
					instance.getId());
			return;
		}

		if (instance.getAsString(DefaultProperties.UNIQUE_IDENTIFIER) != NO_ID) {
			Map<Object, Object> attributes = CollectionUtils.createHashMap(2);
			attributes.put(DISPLAY_NAME, instance.getAsString(DefaultProperties.TITLE));
			attributes.put(EMAIL_ADDRESS, instance.getAsString(EMAIL_ADDRESS));
			LOGGER.debug("Schedule email account update for instance: [{}] with email address: [{}]", instance.getId(),
					instance.getAsString(EMAIL_ADDRESS));
			updateEmailAccountQueue.send(attributes);
		}
	}

}
