package com.sirma.itt.cmf.notification;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;

import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * The RequestHelpNotificationContext is the wrapper logic for requested user help
 */
public class RequestHelpNotificationContext extends AbstractMailNotificationContext {
	/** The user. */
	private Resource user;

	private String subject;

	private byte[] body;

	/** dummy init because of validation */
	@Inject
	@Config(name = CmfConfigurationProperties.NOTIFICATIONS_ADMIN_USERNAME)
	private String sendtoUser;

	/**
	 * dummy init because of validation
	 */
	public RequestHelpNotificationContext() {
		super(null);
	}

	/**
	 * Creates new request help context
	 *
	 * @param helperService
	 *            is the helper service injected
	 * @param subject
	 *            is the message subject - plain text
	 * @param body
	 *            is the content of message - base64 encoded html
	 * @param user
	 *            is the user requested help
	 */
	public RequestHelpNotificationContext(MailNotificationHelperService helperService,
			String subject, String body, Resource user) {
		super(helperService);
		this.subject = subject;
		this.body = body.getBytes();
		this.user = user;
	}

	/**
	 * Creates new request help context
	 *
	 * @param helperService
	 *            is the helper service injected
	 * @param subject
	 *            is the message subject - plain text
	 * @param body
	 *            is the content of message - base64 encoded html as byte array
	 * @param user
	 *            is the user requested help
	 */
	public RequestHelpNotificationContext(MailNotificationHelperService helperService,
			String subject, byte[] body, Resource user) {
		super(helperService);
		this.subject = subject;
		this.body = body;
		this.user = user;
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getTemplateId()
	 */
	@Override
	public String getTemplateId() {
		return "request_help.ftl";
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getSubject()
	 */
	@Override
	public String getSubject() {
		String label = helperService.getLabelProvider()
				.getValue("notification.requesthelp.subject");
		if (subject == null) {
			return MessageFormat.format(label, "");
		}
		return MessageFormat.format(label, subject);
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getSendTo()
	 */
	@Override
	public Collection<Resource> getSendTo() {
		// get the user name
		String adminUserName = helperService
				.getConfigProperty(CmfConfigurationProperties.NOTIFICATIONS_ADMIN_USERNAME);
		return Collections.singletonList(helperService.getResourceService().getResource(
				adminUserName, ResourceType.USER));
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getModel()
	 */
	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<String, Object>(5);
		modelMap.put("content", new String(Base64.decodeBase64(body)));
		return modelMap;
	}

	@Override
	public Resource getSendFrom() {
		return user;
	}

}
