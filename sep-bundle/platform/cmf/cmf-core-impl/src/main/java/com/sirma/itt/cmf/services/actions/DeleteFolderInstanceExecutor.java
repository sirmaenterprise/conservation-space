package com.sirma.itt.cmf.services.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.executors.DeleteInstanceExecutor;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Operation executor for folder/section deletion.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DeleteFolderInstanceExecutor.TARGET_NAME, order = 127)
public class DeleteFolderInstanceExecutor extends DeleteInstanceExecutor {

	@Override
	public String getOperation() {
		return "deleteFolderInstance";
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
