package com.sirma.itt.seip.annotations.mention;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.mail.MailNotificationContext;

/**
 * User mention mail notification wrapper
 *
 * @author tdossev
 */
public class AnnotationMentionNotificationContext implements MailNotificationContext {
	private Instance userInstance;
	private Instance commentedInstance;
	private Instance commenterInstance;
	private String commentsOn;
	private String applicationName;
	private String ui2Url;

	/**
	 * Constructor.
	 *
	 * @param userInstance
	 *            user instance
	 * @param commentedInstance
	 *            commented document instance
	 * @param commenterInstance
	 *            commented user instance
	 * @param commentsOn
	 *            commented tab
	 * @param applicationName
	 *            brand application name
	 * @param ui2Url
	 *            ui2 url
	 */
	public AnnotationMentionNotificationContext(Instance userInstance, Instance commentedInstance,
			Instance commenterInstance, String commentsOn, String applicationName, String ui2Url) {
		this.userInstance = userInstance;
		this.commentedInstance = commentedInstance;
		this.commenterInstance = commenterInstance;
		this.commentsOn = commentsOn;
		this.applicationName = applicationName;
		this.ui2Url = ui2Url;
	}

	@Override
	public Collection<String> getSendTo() {
		return Collections.singletonList(userInstance.getAsString("email"));
	}

	@Override
	public String getSubject() {
		return "You are mentioned in a comment";
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<>(7);
		modelMap.put("user", userInstance);
		modelMap.put("commented", commentedInstance);
		modelMap.put("commenter", commenterInstance);
		modelMap.put("commentsOn", commentsOn);
		modelMap.put("applicationName", applicationName);
		modelMap.put("url", ui2Url);
		return modelMap;
	}

	@Override
	public String getTemplateId() {
		return "email_mention";
	}
}
