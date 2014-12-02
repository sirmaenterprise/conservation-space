package com.sirma.itt.cmf.notification;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * The PoolTaskCreationNotificationContext is the wrapper logic for task pool task creation notifications.
 */
public class PoolTaskCreationNotificationContext extends AbstractMailNotificationContext {

	/** The task. */
	private Instance task;

	/** The user. */
	private Resource user;

	/**
	 * Instantiates a new pool task creation mail delegate.
	 *
	 * @param helperService
	 *            the helper service
	 * @param task
	 *            the task
	 * @param user
	 *            the user
	 */
	public PoolTaskCreationNotificationContext(MailNotificationHelperService helperService,
			AbstractTaskInstance task, Resource user) {
		super(helperService);
		this.task = task;
		this.user = user;
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getTemplateId()
	 */
	@Override
	public String getTemplateId() {
		return "task_pooled.ftl";
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getSubject()
	 */
	@Override
	public String getSubject() {
		String label = helperService.getLabelProvider().getValue(
				"notification.assignment.task.subject");

		PropertyDefinition property = helperService.getDictionaryService().getProperty(
				TaskProperties.TYPE, task.getRevision(), task);
		Serializable taskTypeCode = task.getProperties().get(TaskProperties.TYPE);
		if (taskTypeCode != null) {
			Serializable taskType = helperService.getCodelistService()
					.getCodeValue(property.getCodelist(), taskTypeCode.toString()).getProperties()
					.get(SecurityContextManager.getUserLanguage(user));
			return MessageFormat.format(label, taskType);
		}
		return MessageFormat.format(label, "");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getSendTo()
	 */
	@Override
	public Collection<Resource> getSendTo() {
		return Collections.singletonList(user);
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.emf.mail.notification.MailSenderDelegate#getModel()
	 */
	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<String, Object>(5);
		modelMap.put("user", user);
		modelMap.put("currentTask", task);
		Instance root = InstanceUtil.getRootInstance(task, true);
		// Instance context = InstanceUtil.getContext(task, true);
		// modelMap.put("context", context);
		modelMap.put("rootContext", root);
		return modelMap;
	}

}
