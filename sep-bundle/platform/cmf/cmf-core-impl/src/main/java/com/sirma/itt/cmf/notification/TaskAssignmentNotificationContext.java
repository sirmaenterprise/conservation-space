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
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * The TaskAssignmentMailDelegate is the wrapper logic for task assignment notifications.
 */
public class TaskAssignmentNotificationContext extends AbstractMailNotificationContext {

	/** The task. */
	private Instance task;

	/** The user. */
	private Resource user;

	/** The task type. */
	private Serializable taskType;

	/**
	 * Instantiates a new task assignment mail delegate.
	 *
	 * @param helperService
	 *            the helper service
	 * @param task
	 *            the task
	 * @param user
	 *            the user
	 */
	public TaskAssignmentNotificationContext(MailNotificationHelperService helperService,
			AbstractTaskInstance task, Resource user) {
		super(helperService);
		this.task = task;
		this.user = user;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTemplateId() {
		return "task_assign.ftl";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSubject() {
		String label = helperService.getLabelProvider().getValue(
				"notification.assignment.task.subject");
		Serializable localType = getTaskType();
		if (localType != null) {
			return MessageFormat.format(label, localType);
		}
		return MessageFormat.format(label, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Resource> getSendTo() {
		return Collections.singletonList(user);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<String, Object>(5);
		modelMap.put("user", user);
		modelMap.put("currentTask", task);
		modelMap.put("title", calculateTitle());
		modelMap.put("type", getTaskType());
		Instance context = InstanceUtil.getRootInstance(task, true);
		modelMap.put("rootContext", context);
		return modelMap;
	}

	/**
	 * Calculate title as the property title might not exist in the current task, use the name as
	 * fallback and the task type.
	 * 
	 * @return the title
	 */
	private String calculateTitle() {
		if (task.getProperties().get(DefaultProperties.TITLE) != null) {
			return task.getProperties().get(DefaultProperties.TITLE).toString();
		}
		if (task.getProperties().get(DefaultProperties.NAME) != null) {
			return task.getProperties().get(DefaultProperties.NAME).toString();
		}
		Serializable localType = getTaskType();
		if (localType != null) {
			return localType.toString();
		}
		return "";
	}

	/**
	 * Gets the task type based on the codelist invocation.
	 * 
	 * @return the task type
	 */
	private Serializable getTaskType() {
		PropertyDefinition property = helperService.getDictionaryService().getProperty(
				TaskProperties.TYPE, task.getRevision(), task);
		Serializable taskTypeCode = task.getProperties().get(TaskProperties.TYPE);
		if (taskTypeCode != null) {
			CodeValue codeValue = helperService.getCodelistService().getCodeValue(
					property.getCodelist(), taskTypeCode.toString());
			if (codeValue != null) {
				taskType = codeValue.getProperties().get(
						SecurityContextManager.getUserLanguage(user));
			} else {
				taskType = taskTypeCode;
			}
		}
		return taskType;
	}
}
