package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DO_NO_CALL_DMS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.executors.EditDetailsExecutor;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Operation that does not call DMS when saving instances.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 11)
public class EditDetailsImportedInstanceExecutor extends EditDetailsExecutor {

	/** The Constant EDIT_DETAILS. */
	private static final Operation EDIT_DETAILS = new Operation(ActionTypeConstants.EDIT_DETAILS);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "saveImported";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(SchedulerContext context) {
		RuntimeConfiguration.enable(DO_NO_CALL_DMS);
		RuntimeConfiguration.enable(DISABLE_STALE_DATA_CHECKS);
		RuntimeConfiguration.enable(OVERRIDE_MODIFIER_INFO);
		try {
			return super.execute(context);
		} finally {
			RuntimeConfiguration.disable(DO_NO_CALL_DMS);
			RuntimeConfiguration.disable(DISABLE_STALE_DATA_CHECKS);
			RuntimeConfiguration.disable(OVERRIDE_MODIFIER_INFO);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Operation createOperation() {
		return EDIT_DETAILS;
	}
}
