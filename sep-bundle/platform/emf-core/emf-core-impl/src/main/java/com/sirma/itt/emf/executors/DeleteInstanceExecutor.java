package com.sirma.itt.emf.executors;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Executor that could handle generic delete operation of any instance.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DeleteInstanceExecutor.TARGET_NAME, order = 20)
public class DeleteInstanceExecutor extends BaseInstanceExecutor {

	/** The Constant OPERATION. */
	private static final Operation OPERATION = new Operation("delete");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return OPERATION.getOperation();
	}

	/**
	 * Execute.
	 * 
	 * @param context
	 *            the context
	 * @return the object
	 */
	@Override
	public OperationResponse execute(SchedulerContext context) {
		Instance instance = getOrCreateInstance(context);
		instanceService.delete(instance, OPERATION, false);
		return new OperationResponse(SchedulerEntryStatus.COMPLETED, null);
	}

	/**
	 * Rollback.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	@Override
	public boolean rollback(SchedulerContext data) {
		// we cannot rollback the operation because all sub elements are deleted also
		return true;
	}

}
