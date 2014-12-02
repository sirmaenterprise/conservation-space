package com.sirma.itt.cmf.workflow.observers;

import java.io.Serializable;
import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskCreateEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowStartEvent;
import com.sirma.itt.cmf.event.workflow.WorkflowCreateEvent;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Calculates the {@link WorkflowProperties#PLANNED_END_DATE} for the given workflow.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DueDateCalculator {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DueDateCalculator.class);

	/** The priority normal. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_PRIORITY_NORMAL)
	private String priorityNormal;

	/** The priority low. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_PRIORITY_LOW)
	private String priorityLow;

	/** The priority high. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_PRIORITY_HIGH)
	private String priorityHigh;

	/**
	 * Calculate due date for E-GOV.
	 *
	 * @param event
	 *            the event
	 */
	public void calculateDueDateForStandaloneTask(@Observes StandaloneTaskCreateEvent event) {
		StandaloneTaskInstance context = event.getInstance();

		if (context.getProperties().get(WorkflowProperties.PLANNED_END_DATE) == null) {
			// if the due date is not passed via the service we should fill it
			// with something
			calculateDueDateAndPriorityInternal(context);
		}

		LOGGER.debug("For workflow {} with priority {} set due date {}", context
				.getTaskInstanceId(), context.getProperties().get(WorkflowProperties.PRIORITY),
				context.getProperties().get(WorkflowProperties.PLANNED_END_DATE));
	}

	/**
	 * Calculate due date for E-GOV.
	 *
	 * @param event
	 *            the event
	 */
	public void calculateDueDateForEgov(@Observes WorkflowCreateEvent event) {
		WorkflowInstanceContext context = event.getInstance();

		if (context.getProperties().get(WorkflowProperties.PLANNED_END_DATE) == null) {
			// if the due date is not passed via the service we should fill it
			// with something
			calculateDueDateAndPriorityInternal(context);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("For workflow {} with priority {} set due date {}", context
					.getIdentifier(), context.getProperties().get(WorkflowProperties.PRIORITY),
					context.getProperties().get(WorkflowProperties.PLANNED_END_DATE));
		}
	}

	/**
	 * CMF ONLY. Calculate due date based on that the normal execution time is 7 days.
	 *
	 * @param event
	 *            the event
	 */
	public void calculateDueDate(@Observes BeforeWorkflowStartEvent event) {
		WorkflowInstanceContext context = event.getInstance();
		calculateDueDateAndPriorityInternal(context);
	}

	/**
	 * Calculate due date internal and set it. Priority is also set if not already
	 *
	 * @param context
	 *            the context
	 */
	private void calculateDueDateAndPriorityInternal(Instance context) {
		Serializable plannedEndTime = context.getProperties().get(WorkflowProperties.PLANNED_END_DATE);
		if (plannedEndTime != null) {
			return;
		}
		Serializable serializable = context.getProperties().get(WorkflowProperties.PRIORITY);
		// if priority is not set then we set it to normal execution
		String priority = priorityNormal;
		if (serializable instanceof String) {
			priority = (String) serializable;
		} else if (serializable == null) {
			// the check is not necessary but
			// just in case if priority is not set
			context.getProperties().put(WorkflowProperties.PRIORITY, priority);
		}
		int normalService = 7;
		int plusDays = 0;
		int div = 1;
		if (EqualsHelper.nullSafeEquals(priority, priorityLow, true)
				|| (EqualsHelper.nullSafeEquals(priority, priorityNormal, true))) {
			if (EqualsHelper.nullSafeEquals(priority, priorityLow, true)) {
				div = 2;
			}
			plusDays = normalService / div;
		} else if (EqualsHelper.nullSafeEquals(priority, priorityHigh, true)) {
			// express
			plusDays = 1;
		} else {
			plusDays = normalService;
		}
		Calendar calendar = Calendar.getInstance();
		// clear some precision
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_YEAR, plusDays);

		context.getProperties().put(WorkflowProperties.PLANNED_END_DATE, calendar.getTime());
		LOGGER.debug("Calculated DueDate for workflow id: {} with priority {} for {}",
				context.getId(), priority,
				context.getProperties().get(WorkflowProperties.PLANNED_END_DATE));

	}

}
