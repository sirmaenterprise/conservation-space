package com.sirmaenterprise.sep.bpm.camunda.service;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The {@link ActivityDetails} is bean holding the basic information for given activity - task. In addition provides
 * extra information
 *
 * @author bbanchev
 */
public class ActivityDetails {
	private boolean active;
	private boolean process;
	private boolean isCase;
	private String camundaActivityId;
	private String camundaProcessBusinessId;
	private Instance activity;

	/**
	 * Instantiates a new activity details.
	 *
	 * @param process
	 *            the process that this activity is part of
	 * @param activity
	 *            the activity that this bean is related to
	 * @param task
	 *            the Camunda task for this activity
	 */
	public ActivityDetails(ProcessInstance process, Instance activity, Task task) {
		this.camundaProcessBusinessId = process.getBusinessKey();
		setTaskData(activity, task);
	}

	/**
	 * Instantiates a new activity details for process.
	 *
	 * @param process
	 *            the process that this activity is part of
	 * @param activity
	 *            the activity that this bean is related to
	 */
	public ActivityDetails(ProcessInstance process, Instance activity) {
		this.camundaProcessBusinessId = process.getBusinessKey();
		this.camundaActivityId = activity.getId().toString();
		this.process = true;
		this.active = true;
	}

	/**
	 * Instantiates a new activity details for completed activity of active process.
	 *
	 * @param process
	 *            the process that this activity is part of
	 * @param activity
	 *            the activity that this bean is related to
	 * @param task
	 *            the Camunda historic task for this activity
	 */
	public ActivityDetails(ProcessInstance process, Instance activity, HistoricTaskInstance task) {
		this.camundaProcessBusinessId = process.getBusinessKey();
		setTaskData(activity, task);
	}

	/**
	 * Instantiates a new activity details for completed activity of completed process.
	 *
	 * @param process
	 *            the historic process that this activity was part of
	 * @param activity
	 *            the activity that this bean is related to
	 * @param task
	 *            the Camunda historic task for this activity
	 */
	public ActivityDetails(HistoricProcessInstance process, Instance activity, HistoricTaskInstance task) {
		this.camundaProcessBusinessId = process.getBusinessKey();
		setTaskData(activity, task);
	}

	/**
	 * Instantiates a new activity details for completed process.
	 *
	 * @param process
	 *            the historic process that this activity was part of
	 * @param activity
	 *            the activity that this bean is related to
	 */
	public ActivityDetails(HistoricProcessInstance process, Instance activity) {
		this.camundaProcessBusinessId = process.getBusinessKey();
		this.camundaActivityId = activity.getId().toString();
		this.process = true;
		this.active = false;
	}

	private void setTaskData(Instance activity, Task task) {
		this.activity = activity;
		this.camundaActivityId = task.getId();
		this.active = true;
	}

	private void setTaskData(Instance activity, HistoricTaskInstance task) {
		this.activity = activity;
		this.camundaActivityId = task.getId();
		this.active = false;
	}

	/**
	 * Gets the activity instance in SEP.
	 *
	 * @return the activity
	 */
	public Instance getActivity() {
		return activity;
	}

	/**
	 * Gets the activity id as the activity could be found in Camunda.
	 *
	 * @return the activity id
	 */
	public String getCamundaActivityId() {
		return camundaActivityId;
	}

	/**
	 * Gets the Camunda process business id.
	 *
	 * @return the Camunda process business id
	 */
	public String getCamundaProcessBusinessId() {
		return camundaProcessBusinessId;
	}

	/**
	 * Checks if this activity is considered active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Checks if the activity is process.
	 *
	 * @return true, if is process
	 */
	public boolean isProcess() {
		return process;
	}

	/**
	 * Checks if the activity is case.
	 * 
	 * @return true if case
	 */
	public boolean isCase() {
		return isCase;
	}

	@Override
	public String toString() {
		return "ActivityDetails [active=" + active + ", processId=" + camundaProcessBusinessId + ", camundaActivityIds="
				+ camundaActivityId + "]";
	}

}
