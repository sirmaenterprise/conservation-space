package com.sirma.itt.cmf.notification;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * The RequestHelpNotificationContext is the wrapper logic for requested user help
 */
public class RequestHelpNotificationContext implements MailNotificationContext {

	private Resource user;

	private String subject;

	private byte[] body;

	@Inject
	private MailResourceProvider mailResourceProvider;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "notifications.admin.username", label = "Who is the responsible user for requested user help?")
	private ConfigurationProperty<String> sendtoUser;

	/**
	 * Dummy init because of validation.
	 */
	public RequestHelpNotificationContext() {
		// default
	}

	@Override
	public String getTemplateId() {
		return "email_help_request";
	}

	@Override
	public String getSubject() {
		String label = mailResourceProvider.getLabel("notification.requesthelp.subject");
		if (subject == null) {
			return MessageFormat.format(label, "");
		}
		return MessageFormat.format(label, subject);
	}

	@Override
	public Collection<String> getSendTo() {
		Resource resource = mailResourceProvider.getResource(sendtoUser.get(), ResourceType.USER);
		return resource == null ? Collections.emptyList() : Collections.singletonList(resource.getUserMail());
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<>(5);
		modelMap.put("content", new String(Base64.decodeBase64(body), StandardCharsets.UTF_8));
		return modelMap;
	}

	@Override
	public Resource getSendFrom() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(Resource user) {
		this.user = user;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Sets the body.
	 *
	 * @param body
	 *            the body to set
	 */
	public void setBody(byte[] body) {
		this.body = body;
	}

	/**
	 * Sets the body.
	 *
	 * @param body
	 *            the new body
	 */
	public void setBody(String body) {
		this.body = body.getBytes(StandardCharsets.UTF_8);
	}

}
