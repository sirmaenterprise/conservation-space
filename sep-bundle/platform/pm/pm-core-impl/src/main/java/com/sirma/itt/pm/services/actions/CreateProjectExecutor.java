package com.sirma.itt.pm.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Executor for project creation. The revert operation here is permanent delete of the project
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 200)
public class CreateProjectExecutor extends BaseInstanceExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateProjectExecutor.class);

	@Inject
	private ProjectService projectService;

	@Override
	public String getOperation() {
		return PmActionTypeConstants.CREATE_PROJECT;
	}

	@Override
	public boolean rollback(SchedulerContext data) {
		Instance instance = getOrCreateInstance(data);
		if (instance instanceof ProjectInstance) {
			ProjectInstance projectInstance = (ProjectInstance) instance;
			if (projectInstance.getDmsId() != null) {
				// delete all content from DMS about the case
				try {
					projectService.delete(projectInstance,
							new Operation(ActionTypeConstants.DELETE), true);
				} catch (Exception e) {
					LOGGER.warn("Failed to rollback project instance creation by deleting"
							+ " due to {}", e.getMessage(), e);
					return false;
				}
			}
			// probably we didn't manage to create it so nothing to worry about
		} else {
			if (instance != null) {
				LOGGER.warn("Invalid instance type passed for rollback. Expected {} but was {}.",
						ProjectInstance.class.getSimpleName(), instance.getClass().getSimpleName());
			} else {
				LOGGER.debug("Nothing to rollback.");
			}
		}

		return true;
	}

}
