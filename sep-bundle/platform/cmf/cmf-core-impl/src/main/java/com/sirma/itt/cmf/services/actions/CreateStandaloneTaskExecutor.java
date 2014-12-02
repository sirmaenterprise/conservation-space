package com.sirma.itt.cmf.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Executor for create task operation.
 *
 * @author dvladov
 */
@ApplicationScoped
@Extension(target = CreateStandaloneTaskExecutor.TARGET_NAME, order = 165)
public class CreateStandaloneTaskExecutor extends BaseInstanceExecutor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CreateStandaloneTaskExecutor.class);

	@Inject
	private StandaloneTaskService standaloneTaskService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return ActionTypeConstants.CREATE_TASK;
	}

	@Override
	protected Instance getOrCreateInstance(SchedulerContext context) {
		Instance standaloneTask = super.getOrCreateInstance(context);
		StandaloneTaskInstance started = standaloneTaskService.start(
				(StandaloneTaskInstance) standaloneTask, new Operation(ActionTypeConstants.START));
		return started;
	}

	// @Override
	// public boolean rollback(SchedulerContext data) {
	//
	// Instance instance = getOrCreateInstance(data);
	// if (instance instanceof StandaloneTaskInstance) {
	// StandaloneTaskInstance standaloneTaskInstance = (StandaloneTaskInstance) instance;
	// if (standaloneTaskInstance.getDmsId() != null) {
	// // delete all content from DMS about the task
	// try {
	// taskService.delete(standaloneTaskInstance, new Operation(
	// ActionTypeConstants.DELETE), true);
	// } catch (Exception e) {
	// LOGGER.warn("Fail to rollback task instance creation by deleting due to {} {}",
	// e.getMessage(), e);
	// return false;
	// }
	// }
	// // probably we didn't manage to create it so nothing to worry about
	// } else {
	// if (instance != null) {
	// LOGGER.warn("Invalid instance type passed for rollback. Expected {} but was {}",
	// StandaloneTaskInstance.class.getSimpleName(),
	// instance.getClass().getSimpleName());
	// } else {
	// return super.rollback(data);
	// }
	// }
	// return true;
	//
	// }
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJson(Instance instance) {
		JSONObject json = super.toJson(instance);
		return json;
	}

}
