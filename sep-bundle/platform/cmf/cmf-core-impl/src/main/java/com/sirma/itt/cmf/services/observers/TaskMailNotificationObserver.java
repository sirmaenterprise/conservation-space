package com.sirma.itt.cmf.services.observers;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.task.standalone.AfterStandaloneTaskPersistEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskReassignEvent;
import com.sirma.itt.cmf.event.task.workflow.AfterTaskPersistEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskReassignEvent;
import com.sirma.itt.cmf.notification.PoolTaskCreationNotificationContext;
import com.sirma.itt.cmf.notification.TaskAssignmentNotificationContext;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.mail.notification.MailNotificationService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;

/**
 * An observer that sends the notifications on task operations
 */
@ApplicationScoped
public class TaskMailNotificationObserver {

	/** The helper. */
	@Inject
	private MailNotificationHelperService helper;

	/** The notification service. */
	@Inject
	private MailNotificationService notificationService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(TaskMailNotificationObserver.class);

	/**
	 * Event on standalone task created. Pool task or simple task creation and assignment
	 * 
	 * @param event
	 *            the event
	 */
	public void onStandaloneTaskCreated(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) AfterStandaloneTaskPersistEvent event) {
		StandaloneTaskInstance instance = event.getInstance();
		sendMailOnAssign(instance);
	}

	/**
	 * Event on task created. Pool task or simple task creation and assignment
	 * 
	 * @param event
	 *            the event
	 */
	public void onTaskCreated(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) AfterTaskPersistEvent event) {
		TaskInstance instance = event.getInstance();
		sendMailOnAssign(instance);
	}

	/**
	 * Internal send mail on task created. If it is pool task, to all pool user is sent mail if
	 * possible.
	 * 
	 * @param instance
	 *            the task created
	 */
	private void sendMailOnAssign(AbstractTaskInstance instance) {
		try {
			if (TaskState.COMPLETED.equals(instance.getState())) {
				// already completed
				return;
			}
			Collection<String> poolUsers = taskService.getPoolUsers(instance);
			if (poolUsers != null) {
				for (String userId : poolUsers) {
					PoolTaskCreationNotificationContext taskAssignmentMailDelegate = new PoolTaskCreationNotificationContext(
							helper, instance,
							resourceService.getResource(userId, ResourceType.USER));
					notificationService.sendEmail(taskAssignmentMailDelegate);
				}
			} else if (instance.getProperties().get(TaskProperties.TASK_OWNER) != null) {
				TaskAssignmentNotificationContext taskAssignmentMailDelegate = new TaskAssignmentNotificationContext(
						helper, instance, resourceService.getResource(
								instance.getProperties().get(TaskProperties.TASK_OWNER).toString(),
								ResourceType.USER));
				notificationService.sendEmail(taskAssignmentMailDelegate);
			}
		} catch (Exception e) {
			logger.error("Error during message sending", e);
		}
	}

	/**
	 * Event on standalone task reassigned. Notification to the new owner is sent
	 * 
	 * @param event
	 *            the event
	 */
	public void onTaskReassigned(@Observes StandaloneTaskReassignEvent event) {
		StandaloneTaskInstance instance = event.getInstance();
		onReassign(instance);
	}

	/**
	 * Event on task reassigned. Notification to the new owner is sent
	 * 
	 * @param event
	 *            the event
	 */
	public void onTaskReassigned(@Observes TaskReassignEvent event) {
		TaskInstance instance = event.getInstance();
		onReassign(instance);
	}

	/**
	 * Internal send mail on task reassign.
	 * 
	 * @param instance
	 *            the task reassigned
	 */
	private void onReassign(AbstractTaskInstance instance) {
		if (instance.getProperties().get(TaskProperties.TASK_OWNER) != null) {
			try {
				TaskAssignmentNotificationContext taskAssignmentMailDelegate = new TaskAssignmentNotificationContext(
						helper, instance, resourceService.getResource(
								instance.getProperties().get(TaskProperties.TASK_OWNER).toString(),
								ResourceType.USER));
				notificationService.sendEmail(taskAssignmentMailDelegate);
			} catch (Exception e) {
				logger.error("Error during message sending", e);
			}
		} else {
			logger.warn("Message could not be send, as user could not be found for task "
					+ instance.getIdentifier());
		}
	}
}
