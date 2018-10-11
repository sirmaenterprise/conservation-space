package com.sirma.itt.seip.mail;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Default mail notification wrapper
 *
 * @author Valeri Tishev
 */
public class DefaultNotificationContext implements MailNotificationContext {

	private String recipientEmail;

	private String subject;

	private String templateId;

	private ScriptInstance target;

	private ScriptInstance[] relevantNodes;

	private MailAttachment[] attachments;

	private User user;

	private Map<String, Object> properties;

	/**
	 * Instantiates a new default notification context.
	 */
	public DefaultNotificationContext() {
		// default
	}

	/**
	 * Instantiates a new default notification context.
	 *
	 * @param recipient
	 *            the email recipient
	 * @param user
	 *            user executed the operation (currently logged user)
	 * @param subject
	 *            the subject of the email message to be sent
	 * @param templateId
	 *            the freemarker template id to be processed
	 * @param target
	 *            the target {@link ScriptInstance}
	 * @param relevantNodes
	 *            array of all relevant script nodes to be referenced in the freemarker template
	 * @param properties
	 *            properties passed from the context in the definition to be used in the mail
	 * @param attachments
	 *            the attachments, which will be added to the mail
	 */
	@SuppressWarnings("squid:S00107")
	public DefaultNotificationContext(String recipient, User user, String subject, String templateId,
			ScriptInstance target, ScriptInstance[] relevantNodes, Map<String, Object> properties,
			MailAttachment... attachments) {
		recipientEmail = recipient;
		this.user = user;
		this.subject = subject;
		this.templateId = templateId;
		this.target = target;
		this.relevantNodes = relevantNodes;
		this.attachments = attachments;
		this.setProperties(properties);
	}

	@Override
	public Collection<String> getSendTo() {
		return Collections.singletonList(recipientEmail);
	}

	@Override
	public Resource getSendFrom() {
		return user;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = CollectionUtils.createHashMap(6);
		modelMap.put("recipient", recipientEmail);
		modelMap.put("user", user);
		modelMap.put("target", target);
		modelMap.put("relevantNodes", relevantNodes);
		modelMap.put("properties", properties);
		return modelMap;
	}

	@Override
	public String getTemplateId() {
		return templateId;
	}

	@Override
	public MailAttachment[] getAttachments() {
		return attachments != null && attachments.length > 0 ? attachments : EMPTY_ATTACHMENTS;
	}

	/**
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

}
