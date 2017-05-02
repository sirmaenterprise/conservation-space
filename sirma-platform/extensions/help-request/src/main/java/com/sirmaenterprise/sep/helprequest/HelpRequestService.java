package com.sirmaenterprise.sep.helprequest;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.mail.MailMessage;
import com.sirma.itt.seip.mail.MailService;
import com.sirma.itt.seip.mail.MessageSender;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Service for help request functionality. Service will fetch:
 * 1. Configuration "help.support.email" and will use it as recipient of mail.
 * 2. Email of current user and will use it as  "Cc" (carbon copy) recipients.
 * 
 * @author Boyan Tonchev
 */
@ApplicationScoped
public class HelpRequestService {

	public static final String PROPERTY_USER_EMAIL = "email";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "help.support.email", type = String.class, label = "The e-mail address of the help support.")
	private ConfigurationProperty<String> helpSuportEmail;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "help.support.codelist.mail.type", type = Integer.class, defaultValue = "15", label = "Code list of mail types.")
	private ConfigurationProperty<Integer> codelistMailType;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private MessageSender messageSender;

	@Inject
	private CodelistService codelistService;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private MailService mailService;

	/**
	 * Send help request to desk support.
	 *
	 * @param message
	 */
	public void sendHelpRequest(HelpRequestMessage message) {
		mailService.enqueueMessage(convertToMailMessage(message));
	}

	/**
	 * Convert {@link HelpRequestMessage} object to {@link MailMessage}.
	 * 
	 * @param message the {@link HelpRequestMessage} object.
	 * @return converted {@link MailMessage} object.
	 */
	private MailMessage convertToMailMessage(HelpRequestMessage message) {
		return messageSender.constructMessage(message.getDescription(), fetchSubject(message), null,
				getRecipientEmail(), getCcRecipientEmail(), null);
	}

	/**
	 * Fetch subject from <code>message</code> and type of mail from code list server.
	 * 
	 * @param message the {@link HelpRequestMessage} object.
	 * @return generated subject of mail.
	 */
	private String fetchSubject(HelpRequestMessage message) {
		CodeValue codeValue = codelistService.getCodeValue(codelistMailType.get(), message.getType());
		StringBuilder subject = new StringBuilder(
				codeValue.getDescription(new Locale(systemConfiguration.getSystemLanguage())));
		subject.append(" : ").append(message.getSubject());
		return subject.toString();
	}

	/**
	 * Fetch current user mail and add it to array with cc recipients.
	 * 
	 * @return the array with current user mail which will be used as "Cc" (carbon copy) recipients.
	 */
	private String[] getCcRecipientEmail() {
		User currentUser = securityContext.getAuthenticated();
		Map<String, Serializable> properties = currentUser.getProperties();
		Serializable currentUserEmail = properties.get(PROPERTY_USER_EMAIL);
		if (currentUserEmail instanceof String) {
			return new String[] { (String) currentUserEmail };
		}
		return new String[0];
	}

	/**
	 * Fetch email of desk support configured by property "help.suport.email".
	 * 
	 * @return recipients for mail.
	 */
	private String[] getRecipientEmail() {
		return new String[] { helpSuportEmail.requireConfigured().get() };
	}
}
