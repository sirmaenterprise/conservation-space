package com.sirma.itt.seip.instance;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides means of processing instance information in different phases, when instance save is executed. There are
 * phases that should be executed before instance save and phases that are executed after the save. In addition defines
 * a way of recovering, when problem occurs while the saving process is running.
 * <p>
 * Different steps(implementations) are executed in defined order. <br>
 * If any error occurs, rollback for every already executed step is invoked in reversed order.
 * <p>
 * There are different rollbacks for the different phases, because there are cases, where we need to execute specific
 * rollback only, if specific something had happened. Also there could be cases, where the rollback itself could throw
 * error, which potentially could prevent other rollbacks execution. In order to eliminate or at least minimise the
 * occurrence of those cases, every phase should implement, its own rollback, if any is needed.
 *
 * @author A. Kunchev
 */
public interface InstanceSaveStep extends Plugin, Named {

	String NAME = "instanceSave";

	/**
	 * Process instance information stored in {@link InstanceSaveContext} before the actual instance save.
	 *
	 * @param saveContext
	 *            {@link InstanceSaveContext} object that is used to store and share the instance information between
	 *            different steps
	 */
	default void beforeSave(InstanceSaveContext saveContext) {
		// nothing to do here
	}

	/**
	 * Process instance information stored in {@link InstanceSaveContext} after the actual instance save.
	 *
	 * @param saveContext
	 *            {@link InstanceSaveContext} object that is used to store and share the instance information between
	 *            different steps
	 */
	default void afterSave(InstanceSaveContext saveContext) {
		// nothing to do here
	}

	/**
	 * Handles error recovery process for before save phase of the current step. Should be invoked, when any error
	 * occurs in the saving process.
	 *  @param saveContext
	 *            {@link InstanceSaveContext} object that is used to store and share the instance information between
	 *            different steps
	 *
	 */
	default void rollbackBeforeSave(InstanceSaveContext saveContext) {
		// nothing to do here
	}

	/**
	 * Handles error recovery process for after save phase of the current step. Should be invoked, when any error occurs
	 * in the saving process and the after phase of the current step is executed.
	 *  @param saveContext
	 *            {@link InstanceSaveContext} object that is used to store and share the instance information between
	 *            different steps
	 *
	 */
	default void rollbackAfterSave(InstanceSaveContext saveContext) {
		// nothing to do here
	}

}
