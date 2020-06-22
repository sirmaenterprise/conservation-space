package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAddressGeneratorService;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * When email integration is activated for certain object type, model change is observed and for any existing instance
 * in the database which doesn't have email address is triggered email address generation through a special queue. This
 * handler loads the instance, generates the email address, triggers {@link CreateEmailAccountEvent} for email account
 * generation and saves the instance with the newly generated email address. The instance is saved without revision to
 * be made.
 * 
 * @author svelikov
 */
@Singleton
public class GenerateEmailAddressListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateEmailAddressListener.class);

	@Inject
	private EventService eventService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private EmailAddressGeneratorService emailAddressGeneratorService;

	/**
	 * Handles the email generation.
	 * 
	 * @param msg
	 *            The payload message.
	 */
	@QueueListener(EmailIntegrationQueueNames.GENERATE_EMAIL_ADDRESS_QUEUE)
	public void onGenerateEmailAddress(Message msg) {
		try {
			MapMessage message = (MapMessage) msg;
			String instanceId = message.getString(INSTANCE_ID);

			Instance instance = domainInstanceService.loadInstance(instanceId);
			String emailAddress = emailAddressGeneratorService.generateEmailAddress(instance);
			instance.add(EMAIL_ADDRESS, emailAddress);

			String title = instance.getString(DefaultProperties.TITLE);
			Map<String, String> attributes = new HashMap<>();
			attributes.put(DISPLAY_NAME, EmailIntegrationHelper.generateDisplayName(title));

			InstanceSaveContext saveContext = InstanceSaveContext.create(instance,
					new Operation(ActionTypeConstants.EDIT_DETAILS));
			saveContext.getVersionContext().setVersionMode(VersionMode.NONE);
			
			LOGGER.debug("Email address generated: [{}]. Going to update instance: [{}]", emailAddress, instanceId);
			domainInstanceService.save(saveContext);
			LOGGER.debug("Instance [{}] updated. Trigger account creation.", instanceId);
			
			eventService.fire(new CreateEmailAccountEvent(instanceId, emailAddress, attributes));
			
		} catch (EmailIntegrationException | JMSException e) {
			LOGGER.warn(
					"There's error during email account create operation. Message will be redelivered and operation will be retried. "
							+ "If max delivery attemps limit is riched this message will be send to dead letters queue. "
							+ e.getMessage(),
					e);
			throw new RollbackedRuntimeException("Email account create operation failed " + e.getMessage(), e);
		}
	}
}
