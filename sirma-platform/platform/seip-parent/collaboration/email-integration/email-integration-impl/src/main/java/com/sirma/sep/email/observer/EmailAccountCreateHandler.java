package com.sirma.sep.email.observer;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static com.sirma.sep.email.EmailIntegrationConstants.USER_FULL_URI;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.event.ResourceAddedEvent;
import com.sirma.itt.seip.resources.event.ResourcePersistedEvent;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.entity.MailboxSupportable;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.exception.EmailAccountCreationException;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Observer for events after which email accounts should be created. When instance is about to be persisted is fired
 * {@link AfterInstancePersistEvent} or {@link ResourcePersistedEvent} in case where the instance is of type
 * {@link Resource} and email account is created or persisting is stopped. When existing instances need to have email
 * accounts created (patching existing tenants), a patch is executed where {@link CreateEmailAccountEvent} is fired and
 * the observer triggers the email account creation.
 *
 * @author S.Djulgerova
 */
public class EmailAccountCreateHandler implements Serializable {

	private static final long serialVersionUID = 3294827664615669369L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAccountCreateHandler.class);


	@Inject
	private DbDao dbDao;

	@Inject
	private SenderService senderService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private javax.enterprise.inject.Instance<EmailIntegrationConfiguration> emailIntegrationConfiguration;

	/**
	 * Email account should be created for every entity type that supports it (marked as mailboxSupportable). Account
	 * creation is asynchronous. {@link AfterInstancePersistEvent} is fired from the core module when persist operation
	 * is started. This method intercepts {@link AfterInstancePersistEvent} extracts instanceId and emailAddress from
	 * the instance and triggers the account creation. A JMS persistent queue is deployed and messages for every new
	 * instance that supports mailbox is put in the queue when {@link CreateEmailAccountEvent} is caught. Accounts
	 * generated for instances are unique and are formed by sequenceId and number + tennantId + domain address (e.g
	 * project-13-tenantId@domainAddress)
	 *
	 * @param event
	 *            the event observed
	 * @throws EmailAccountCreationException
	 *             if account creation fails
	 */
	public <I extends Instance, E extends TwoPhaseEvent> void onAfterInstancePersistedSuccessfully(
			@Observes AfterInstancePersistEvent<I, E> event) throws EmailAccountCreationException {

		// If instance type is "user" we have to skip the logic in this method because emailAddress is still not
		// available. Later for users is fired ResourcePersistedEvent and mailbox creation is handled correctly.
		if (!event.getInstance().type().isMailboxSupportable() || event.getInstance().type().is(ObjectTypes.USER)) {
			return;
		}

		String instanceId = Objects.toString(event.getInstance().getId(), null);
		String emailAddress = Objects.toString(event.getInstance().getProperties().get(EMAIL_ADDRESS), null);
		if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(instanceId)) {
			throw new EmailAccountCreationException("Missing email address or instance id");
		}

		String displayName = EmailIntegrationHelper.generateDisplayName(event.getInstance().getProperties().get(TITLE));
		Map<String, String> attributes = CollectionUtils.createHashMap(3);
		attributes.put(DISPLAY_NAME, displayName);
		triggerAccountCreation(emailAddress, instanceId, attributes);
	}

	/**
	 * When user is created from UI emailAddress property is missing and after persistence is not saved in semantic. To
	 * prevent this problem the property is set before instance persistence.
	 *
	 * @param event
	 *            the event observed
	 */
	public void onBeforeInstanceSaveEvent(@Observes BeforeInstanceSaveEvent event) {
		Instance instance = event.getInstanceToSave();
		if (instance.type().isMailboxSupportable() && instance.type().is(ObjectTypes.USER)) {
			String emailAddress = EmailIntegrationHelper.generateEmailAddress(
					instance.getString(ResourceProperties.USER_ID),
					emailIntegrationConfiguration.get().getTenantDomainAddress().get(),
					emailIntegrationConfiguration.get().getTestEmailPrefix().get());
			instance.add(EMAIL_ADDRESS, emailAddress);
		}
	}

	/**
	 * Intercepts {@link CreateEmailAccountEvent} and send message to JMS Queue
	 *
	 * @param event
	 *            {@link CreateEmailAccountEvent} which triggers send message to JMS Queue
	 */
	public void onCreateEmailAccountEvent(@Observes CreateEmailAccountEvent event) {
		triggerAccountCreation(event.getAccountName(), event.getInstanceId(), event.getAttributes());
	}

	/**
	 * Email account should be created for every new user registered in the system. Account creation is asynchronous.
	 * The user is added into the system during synchronization and {@link ResourcePersistedEvent} is fired. This method
	 * intercepts {@link ResourcePersistedEvent} extracts resource id from the instance, generates mailbox account and
	 * triggers account creation. A JMS persistent queue is deployed and messages for every new user is put in the queue
	 * when {@link CreateEmailAccountEvent} is caught. Queue listener is waiting for new messages and sends soap
	 * requests to the external mail client for account creation. Accounts generated for users are different from the
	 * other instances accounts and are formed by username and tennantId + domain address (e.g
	 * someName-tenantId@domainAddress).
	 *
	 * @param event
	 *            the event observed
	 * @throws EmailAccountCreationException
	 *             if account creation fails
	 */
	public void createUserEmailAccount(@Observes ResourceAddedEvent event) throws EmailAccountCreationException {
		Collection<String> existingClasses = dbDao.fetchWithNamed(MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY,
				Collections.emptyList());

		// If mailbox integration is not supported return
		if (existingClasses.isEmpty() || !existingClasses.contains(USER_FULL_URI)) {
			return;
		}

		if (event.getInstance() instanceof User) {
			String instanceId = Objects.toString(event.getInstance().getId(), null);
			User user = (User) event.getInstance();
			String emailAddress = EmailIntegrationHelper.generateEmailAddress(user.getName(),
					emailIntegrationConfiguration.get().getTenantDomainAddress().get(),
					emailIntegrationConfiguration.get().getTestEmailPrefix().get());

			if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(instanceId)) {
				throw new EmailAccountCreationException("Missing email address or instance id");
			}

			Map<String, String> attributes = CollectionUtils.createHashMap(5);
			attributes.put(GIVEN_NAME, Objects.toString(user.getAsString(FIRST_NAME), ""));
			attributes.put(SN, Objects.toString(user.getAsString(LAST_NAME), ""));
			attributes.put(DISPLAY_NAME, EmailIntegrationHelper.generateDisplayName(user.getAsString(FIRST_NAME),
					user.getAsString(LAST_NAME)));

			sendAccountCreattionMessage(emailAddress, user.getId().toString(), attributes);
			// the user is not persisted, yet. We can update it and the changes will be stored
			user.add(EMAIL_ADDRESS, emailAddress);
		}
	}

	private void triggerAccountCreation(String emailAddress, String instanceId, Map<String, String> attributes) {
		sendAccountCreattionMessage(emailAddress, instanceId, attributes);
		updateResource(instanceId, emailAddress);
	}

	private void sendAccountCreattionMessage(String emailAddress, String instanceId, Map<String, String> attributes) {
		LOGGER.debug("Schedule email account creation for instance: [{}] with email address: [{}]", instanceId,
				emailAddress);
		attributes.put(INSTANCE_ID, instanceId);
		attributes.put(EMAIL_ADDRESS, emailAddress);
		senderService.send(EmailIntegrationQueueNames.CREATE_EMAIL_ACCOUNT_QUEUE, attributes,
				SendOptions.create().asTenantAdmin());
	}

	private void updateResource(String instanceId, String emailAddress) {
		Resource resource = resourceService.getResource(instanceId);
		if (resource != null && resource.getProperties().get(EMAIL_ADDRESS) == null) {
			resource.getProperties().put(EMAIL_ADDRESS, emailAddress);
			resourceService.updateResource(resource, new Operation(ActionTypeConstants.EDIT_DETAILS));
		}
	}

}