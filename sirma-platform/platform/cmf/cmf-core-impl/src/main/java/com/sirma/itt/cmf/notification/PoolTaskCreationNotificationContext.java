package com.sirma.itt.cmf.notification;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.Resource;

/**
 * The PoolTaskCreationNotificationContext is the wrapper logic for task pool task creation notifications.
 */
public class PoolTaskCreationNotificationContext implements MailNotificationContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(PoolTaskCreationNotificationContext.class);

	private Instance task;

	private Resource user;

	private MailResourceProvider mailResourceProvider;

	/**
	 * Instantiates a new pool task creation mail delegate.
	 *
	 * @param task
	 *            the task
	 * @param user
	 *            the user
	 * @param mailResourceProvider
	 *            the mail resource provider
	 */
	public PoolTaskCreationNotificationContext(Instance task, Resource user,
			MailResourceProvider mailResourceProvider) {
		this.task = task;
		this.user = user;
		this.mailResourceProvider = mailResourceProvider;
	}

	@Override
	public String getTemplateId() {
		return "email_task_pooled";
	}

	@Override
	public String getSubject() {
		String label = mailResourceProvider.getLabel("notification.assignment.task.subject");
		String type = DefaultProperties.TYPE;
		String taskType = mailResourceProvider.getDisplayableProperty(task, user, type);

		if (StringUtils.isNotNullOrEmpty(taskType)) {
			return MessageFormat.format(label, taskType);
		}

		LOGGER.warn("Task with identifier [{}] does not have a [{}] field definition", task.getId(), type);
		return MessageFormat.format(label, "");
	}

	@Override
	public Collection<String> getSendTo() {
		if (user != null) {
			String userMail = user.getUserMail();
			if (StringUtils.isNotNullOrEmpty(userMail)) {
				return Collections.singletonList(userMail);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<>(5);
		modelMap.put("user", user);
		modelMap.put("currentTask", task);
		Instance root = InstanceUtil.getRootInstance(task);
		modelMap.put("rootContext", root);
		return modelMap;
	}

}
