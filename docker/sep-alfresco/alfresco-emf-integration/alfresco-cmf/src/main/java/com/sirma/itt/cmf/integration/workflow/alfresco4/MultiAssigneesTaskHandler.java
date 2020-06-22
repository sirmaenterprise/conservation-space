package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.integration.workflow.ActivitiWizards;

/**
 * The MultiAssigneesTaskHandler handles creation notification for pool tasks,
 * so to copy needed data in execution context
 */
public class MultiAssigneesTaskHandler extends BaseTaskHandler {

	public static final String BPM_GROUP_ASSIGNEES = "bpm_groupAssignees";
	public static final String BPM_USERS_ASSIGNEES = "bpm_assignees";
	public static final String BPM_MULTI_ASSIGNEES = "cmfwf_multiAssignees";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(MultiAssigneesTaskHandler.class);
	// cache log levels
	private static final boolean traceEnabled = LOGGER.isDebugEnabled();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.
	 * delegate.DelegateTask)
	 */
	@Override
	public void notify(DelegateTask task) {
		super.notify(task);
		Map<String, Object> taskVariables = task.getVariables();
		if (traceEnabled) {
			trace("Task variables: " + taskVariables);
		}
		Map<String, Object> execVariables = task.getExecution().getVariables();

		// copy variable to task so to be accessible/returnable
		Object multiAssignees = execVariables.get(MultiAssigneesTaskHandler.BPM_MULTI_ASSIGNEES);
		task.setVariable(MultiAssigneesTaskHandler.BPM_MULTI_ASSIGNEES, multiAssignees);
		task.setVariableLocal(MultiAssigneesTaskHandler.BPM_MULTI_ASSIGNEES, multiAssignees);
		Object userAssignees = execVariables.get(BPM_USERS_ASSIGNEES);
		Object groupAssignees = execVariables.get(BPM_GROUP_ASSIGNEES);
		if (userAssignees == null && groupAssignees == null && (multiAssignees != null)) {
			Pair<List<String>, List<String>> extractMultiAssignees = ActivitiWizards
					.extractMultiAssignees(multiAssignees);
			userAssignees = extractMultiAssignees.getFirst();
			groupAssignees = extractMultiAssignees.getSecond();
		}
		attachCandidates(userAssignees, true, task);
		attachCandidates(groupAssignees, false, task);

	}

	/***
	 * Converts multi assignees (groups and users) and adds them as candidates
	 * for the task
	 *
	 * @param object
	 *            is the value to convert
	 * @param task
	 *            is the task to assigne resources to
	 * @param userMode
	 *            is whether this is users or group
	 * @return the , separated list of authorities if not set to the task
	 *         immediately
	 */
	private String attachCandidates(Object object, boolean userMode, DelegateTask task) {

		StringBuffer multiValue = new StringBuffer();
		if (object instanceof Collection) {
			Iterator<?> iterator = ((Collection<?>) object).iterator();
			while (iterator.hasNext()) {
				Object nextValue = iterator.next();

				if (task != null) {
					if (userMode) {
						task.addCandidateUser(nextValue.toString());
						if (traceEnabled) {
							trace("Added candidate user:" + nextValue + " for " + task.getId());
						}
					} else {
						task.addCandidateGroup(nextValue.toString());
						if (traceEnabled) {
							trace("Added candidate group:" + nextValue + " for " + task.getId());
						}
					}
				} else {
					// just append the data as , separated
					multiValue.append(nextValue);
					if (iterator.hasNext()) {
						multiValue.append(", ");
					}
				}
			}
			return multiValue.toString();
		}
		if (object instanceof String) {
			return object.toString();
		}
		if (object != null) {
			throw new RuntimeException("Not implemented " + object.getClass() + " " + object);
		}
		return null;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public Logger getLogger() {
		return LOGGER;
	}

}
