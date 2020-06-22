package com.sirma.itt.seip.tenant.wizard;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The TenantStep represents a configuration step. Order of extension denote the execution order
 *
 * @author bbanchev
 */
public interface TenantStep extends Plugin, Identity {

	/** The name. */
	String CREATION_STEP_NAME = "TenantCreationStep";
	
	String DELETION_STEP_NAME = "TenantDeletionStep";
	
	/** The name. */
	String UPDATE_STEP_NAME = "TenantUpdateStep";

	/**
	 * Gets a new blank model - all needed fields and controls.
	 *
	 * @return the model wrapped as step
	 */
	TenantStepData provide();

	/**
	 * Execute the step creation. The call should be successful to continue with the next step.
	 *
	 * @param data
	 *            the data to use during step
	 * @param context
	 *            is the contextual information that could be accessed/updated during step execution
	 * @return true, if successful
	 */
	boolean execute(TenantStepData data, TenantInitializationContext context);

	/**
	 * Rollback the step creation
	 *
	 * @param data
	 *            the data to use during step
	 * @param context
	 *            contextual information that could be accessed/updated during step execution
	 * @return true, if successful
	 */
	default boolean delete(TenantStepData data, TenantDeletionContext context) {
		return true;
	}

	@Override
	default void setIdentifier(String identifier) {
		// nothing to do
	}
}