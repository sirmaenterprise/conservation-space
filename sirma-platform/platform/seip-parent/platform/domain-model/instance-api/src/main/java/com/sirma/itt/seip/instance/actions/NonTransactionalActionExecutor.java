package com.sirma.itt.seip.instance.actions;

/**
 * Executes actions without starting transaction. It is mainly intended for long running actions, where the operation
 * should not fail due to transaction timeout.
 *
 * @author A. Kunchev
 */
class NonTransactionalActionExecutor extends ActionExecutor {}