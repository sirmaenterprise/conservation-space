package com.sirma.itt.pm.services.actions;

import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.services.actions.BaseImportInstanceExecutor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Action for importing a project instance from external system
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ImportProjectExecutor.TARGET_NAME, order = 210)
public class ImportProjectExecutor extends BaseImportInstanceExecutor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "importProjectInstance";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(SchedulerContext context) {
		RuntimeConfiguration.enable(DISABLE_STALE_DATA_CHECKS);
		RuntimeConfiguration.enable(OVERRIDE_MODIFIER_INFO);
		try {
			return super.execute(context);
		} finally {
			RuntimeConfiguration.disable(DISABLE_STALE_DATA_CHECKS);
			RuntimeConfiguration.disable(OVERRIDE_MODIFIER_INFO);
		}
	}
}
