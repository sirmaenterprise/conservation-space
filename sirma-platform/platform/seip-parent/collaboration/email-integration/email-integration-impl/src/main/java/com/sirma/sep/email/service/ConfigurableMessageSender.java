package com.sirma.sep.email.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.mail.MailConfiguration;
import com.sirma.itt.seip.mail.MailMessage;
import com.sirma.itt.seip.mail.MailSender;
import com.sirma.itt.seip.mail.SMTPAuthenticator;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;

/**
 * Constructs email configuration from subsystem parameters and sends email messages
 *
 *
 * @author g.tsankov
 */
@ApplicationScoped
public class ConfigurableMessageSender {

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Inject
	private Contextual<MailSender> mailMessageSender;

	@PostConstruct
	void initialize() {
		mailMessageSender.initializeWith(this::buildMailSender);
		// reset sender on configuration change
		emailIntegrationConfiguration.getWebmailUrl()
				.addConfigurationChangeListener(cfg -> mailMessageSender.clearContextValue());
		emailIntegrationConfiguration.getWebmailPort()
				.addConfigurationChangeListener(cfg -> mailMessageSender.clearContextValue());
		emailIntegrationConfiguration.getTenantAdminAccount()
				.addConfigurationChangeListener(cfg -> mailMessageSender.clearContextValue());
		emailIntegrationConfiguration.getTenantDomainAddress()
				.addConfigurationChangeListener(cfg -> mailMessageSender.clearContextValue());
	}

	/**
	 * Initializes the {@link MailSender} with the proper email server configuration, since it is deployed locally.
	 *
	 * @return
	 */
	private MailSender buildMailSender() {
		String fromEmail = EmailIntegrationHelper.generateEmailAddress(
				emailIntegrationConfiguration.getTenantAdminAccount().get().getName(),
				emailIntegrationConfiguration.getTenantDomainAddress().get(),
				emailIntegrationConfiguration.getTestEmailPrefix().get());

		MailConfiguration mailConfig = MailConfiguration.createSMTPConfiguration();
		mailConfig.setServerHost(emailIntegrationConfiguration.getWebmailUrl().get());
		mailConfig.setServerFrom(fromEmail);
		mailConfig.setProperty(MailConfiguration.HOST, emailIntegrationConfiguration.getWebmailUrl().get());
		mailConfig.setProperty(MailConfiguration.PORT, emailIntegrationConfiguration.getWebmailPort().get());
		SMTPAuthenticator authenticator = new SMTPAuthenticator(fromEmail,
				emailIntegrationConfiguration.getTenantAdminAccount().get().getCredentials().toString());

		MailSender messageSender = new MailSender(mailConfig, authenticator);
		messageSender.setUseAuthentication(true);
		return messageSender;
	}

	/**
	 * Sends email message with using the configured mail sender.
	 *
	 * @param msg
	 *            mail message to be sent.
	 */
	public void sendMessage(MailMessage msg) {
		mailMessageSender.getContextValue().postMail(msg);
	}
}
