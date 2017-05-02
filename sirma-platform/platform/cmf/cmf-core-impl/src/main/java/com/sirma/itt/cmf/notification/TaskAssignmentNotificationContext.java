package com.sirma.itt.cmf.notification;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.Resource;

/**
 * The TaskAssignmentMailDelegate is the wrapper logic for task assignment notifications.
 */
public class TaskAssignmentNotificationContext implements MailNotificationContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssignmentNotificationContext.class);

	private Instance task;

	/** The resource, containing the user from which we extract the email. */
	private Resource user;

	private MailResourceProvider mailResourceProvider;

	/**
	 * Instantiates a new task assignment mail delegate.
	 *
	 * @param task
	 *            the task
	 * @param user
	 *            the user
	 * @param mailResourceProvider
	 *            the mail resource provider
	 */
	public TaskAssignmentNotificationContext(Instance task, Resource user, MailResourceProvider mailResourceProvider) {
		this.task = task;
		this.user = user;
		this.mailResourceProvider = mailResourceProvider;
	}

	@Override
	public String getTemplateId() {
		return "email_task_assign";
	}

	@Override
	public String getSubject() {
		String label = mailResourceProvider.getLabel("notification.assignment.task.subject");
		String localType = getTaskType();
		if (localType != null) {
			return MessageFormat.format(label, localType);
		}
		return MessageFormat.format(label, "");
	}

	@Override
	public Collection<String> getSendTo() {
		if (user == null) {
			LOGGER.warn("There is no user from which can be get email.");
			return Collections.emptyList();
		}

		String email = user.getUserMail();
		if (StringUtils.isNullOrEmpty(email)) {
			LOGGER.warn("The user {} doesn't have email!", user.getDisplayName());
			return Collections.emptyList();
		}

		return Collections.singletonList(email);
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = CollectionUtils.createHashMap(5);
		modelMap.put("user", user);
		modelMap.put("currentTask", task);
		modelMap.put("title", calculateTitle());
		modelMap.put("type", getTaskType());
		Instance context = InstanceUtil.getRootInstance(task);
		modelMap.put("rootContext", context);
		return modelMap;
	}

	/**
	 * Calculate title as the property title might not exist in the current task, use the name as fallback and the task
	 * type.
	 *
	 * @return the title
	 */
	private String calculateTitle() {
		String title = task.getString(DefaultProperties.TITLE);
		if (title != null) {
			return title;
		}

		String name = task.getString(DefaultProperties.NAME);
		if (name != null) {
			return name;
		}

		String localType = getTaskType();
		if (localType != null) {
			return localType;
		}

		return "";
	}

	private String getTaskType() {
		return mailResourceProvider.getDisplayableProperty(task, user, DefaultProperties.TYPE);
	}

}
