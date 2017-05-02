package com.sirma.sep.bpm.camunda.bpmn.email;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.mail.MailNotificationContext;

/**
 * Notification context for sending WF e-mails.
 * 
 * @author simeon iliev
 */
public class WorkflowNotificationContext implements MailNotificationContext {

	private Instance target;
	private String recipientEmail;
	private String subject;
	private String templateID;

	/**
	 * Notification context that is used in workflows.
	 * 
	 * @param target
	 *            the target instance of the mail.
	 * @param recipientEmail
	 *            the recipient of the mail.
	 * @param subject
	 *            the subject of the mail.
	 * @param templateID
	 *            the template that will be used in the email.
	 */
	public WorkflowNotificationContext(Instance target, String recipientEmail, String subject, String templateID) {
		this.target = target;
		this.recipientEmail = recipientEmail;
		this.subject = subject;
		this.templateID = templateID;
	}

	@Override
	public Collection<String> getSendTo() {
		return Collections.singletonList(recipientEmail);
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = CollectionUtils.createHashMap(4);
		modelMap.put("recipient", recipientEmail);
		modelMap.put("target", target);
		return modelMap;
	}

	@Override
	public String getTemplateId() {
		return templateID;
	}

}
