package com.sirma.itt.pm.services.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.executors.DeleteInstanceExecutor;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Executor for deletion of a project
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DeleteProjectExecutor.TARGET_NAME, order = 220)
public class DeleteProjectExecutor extends DeleteInstanceExecutor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "deleteProjectInstance";
	}

	@Override
	public OperationResponse execute(SchedulerContext context) {
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		try {
			return super.execute(context);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		}
	}

}
