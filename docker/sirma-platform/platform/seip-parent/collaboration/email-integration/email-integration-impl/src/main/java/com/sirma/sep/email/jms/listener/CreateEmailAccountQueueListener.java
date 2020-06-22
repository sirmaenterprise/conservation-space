package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.email.ZimbraEmailIntegrationConstants;
import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.EmailSenderService;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;

/**
 * Message bean queue used to connect to local broker and create email account.
 *
 * @author S.Djulgerova
 */
@Singleton
public class CreateEmailAccountQueueListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateEmailAccountQueueListener.class);

	@Inject
	private ResourceService resourceService;

	@Inject
	private EmailAccountAdministrationService accountAdministrationService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private EmailSenderService mailMessageSender;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityContextManager contextManager;

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Inject
	private ApplicationConfigurationProvider applicationConfigurationProvider;

	@Inject
	private ShareFolderAdministrationService shareFolderAdministrationService;

	/**
	 * Listener for create email account messages. The account id is provided with the message. Accounts are created in
	 * a certain tenant which means a concrete domain in the mail server. If an error occurs during create account
	 * request, then an exception is thrown and the request is retried.
	 *
	 * @param msg
	 *            The payload message.
	 */
	@QueueListener(EmailIntegrationQueueNames.CREATE_EMAIL_ACCOUNT_QUEUE)
	public void onCreateEmailAccount(Message msg) {
		try {
			MapMessage message = (MapMessage) msg;
			String emailAddress = message.getString(EMAIL_ADDRESS);

			if (emailAddressResolver.getEmailAddress(emailAddress) == null) {
				String instanceId = message.getString(INSTANCE_ID);
				String accountPassword = RandomStringUtils.randomAlphanumeric(8);
				Resource resource = resourceService.findResource(instanceId);

				Map<String, String> attributes = new HashMap<>();
				attributes.put(GIVEN_NAME, Objects.toString(message.getString(GIVEN_NAME), ""));
				attributes.put(SN, Objects.toString(message.getString(SN), ""));
				attributes.put(DISPLAY_NAME, Objects.toString(message.getString(DISPLAY_NAME)));
				disablePreferencesForDomainObject(resource, attributes);

				LOGGER.debug("Creating email account for instance: [{}] and email address: [{}]", instanceId,
						emailAddress);
				accountAdministrationService.createAccount(emailAddress, accountPassword, attributes);
				shareFolderAdministrationService.shareFolderWithUser(emailAddress);
				emailAddressResolver.insertEmailAddress(instanceId, securityContext.getCurrentTenantId(), emailAddress,
						emailIntegrationConfiguration.getTenantDomainAddress().get());
				LOGGER.debug("Created account for instance: [{}] and email address: [{}]", instanceId, emailAddress);

				notifyIfUser(resource, emailAddress, accountPassword);
				grantRightsIfAdmin(resource, emailAddress);
			}
		} catch (EmailIntegrationException | JMSException e) {
			LOGGER.warn(
					"There's error during email account creation. Message will be redelivered and operation will be retried. "
							+ "If max delivery attemps limit is riched this message will be send to dead letters queue. "
							+ e.getMessage(),
					e);
			throw new RollbackedRuntimeException("Email account creation failed " + e.getMessage(), e);
		}
	}

	private static void disablePreferencesForDomainObject(Resource resource, Map<String, String> attributes) {
		if (resource == null) {
			attributes.put(ZimbraEmailIntegrationConstants.OPTIONS_ENABLED, "FALSE");
		}
	}

	private void notifyIfUser(Resource resource, String emailAddress, String accountPassword)
			throws EmailIntegrationException {
		if (resource != null) {
			sendWelcomeMail(emailAddress, accountPassword);
		}
	}

	private void grantRightsIfAdmin(Resource resource, String emailAddress) throws EmailIntegrationException {
		if (resource != null && resource.getName().equals(contextManager.getAdminUser().getIdentityId())) {
			// Grant access to admin views
			accountAdministrationService.modifyAdminAccount(emailAddress);
			// Grant admin rights for domain management
			accountAdministrationService.grantAdminDomainRights(emailAddress);
			// Grant admin rights for accounts management
			accountAdministrationService.grantAdminAccountRights(emailAddress);
		}
	}

	private void sendWelcomeMail(String recipient, String password) throws EmailIntegrationException {
		try {
			StringBuilder content = new StringBuilder(512);
			content.append(labelProvider.getValue("emailintegration.welcome.mail.message"));
			content.append("\n");
			content.append(labelProvider.getValue("emailintegration.welcome.mail.username"));
			content.append(recipient);
			content.append("\n");
			content.append(labelProvider.getValue("emailintegration.welcome.mail.password"));
			content.append(password);
			content.append("\n");
			content.append(labelProvider.getValue("emailintegration.welcome.mail.system.url"));
			content.append(applicationConfigurationProvider.getBaseURL());
			content.append("\n");
			content.append(labelProvider.getValue("emailintegration.welcome.mail.mailbox.url"));
			content.append(emailIntegrationConfiguration.getWebmailProtocol().get());
			content.append("://");
			content.append(emailIntegrationConfiguration.getWebmailUrl().get());

			mailMessageSender.sendMessage(buildFromEmail(), recipient,
					labelProvider.getValue("emailintegration.welcome.mail.subject"), content.toString(),
					emailIntegrationConfiguration.getTenantAdminAccount().get().getCredentials().toString());
		} catch (Exception e) {
			LOGGER.error("Could not send mail notification." + e.getMessage(), e);
			throw new EmailIntegrationException("Could not send mail notification." + e.getMessage(), e);
		}
	}

	private String buildFromEmail() {
		return EmailIntegrationHelper.generateEmailAddress(
				emailIntegrationConfiguration.getTenantAdminAccount().get().getName(),
				emailIntegrationConfiguration.getTenantDomainAddress().get(),
				emailIntegrationConfiguration.getTestEmailPrefix().get());
	}
}