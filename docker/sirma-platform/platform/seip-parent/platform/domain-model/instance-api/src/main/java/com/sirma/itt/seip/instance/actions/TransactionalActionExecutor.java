package com.sirma.itt.seip.instance.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * Executes action inside transaction context. This executor will manage the transaction in which the action is executed
 * and it is intended for actions which perform data manipulations over resources.
 *
 * @author A. Kunchev
 */
@Transactional
@ApplicationScoped
class TransactionalActionExecutor extends ActionExecutor {}