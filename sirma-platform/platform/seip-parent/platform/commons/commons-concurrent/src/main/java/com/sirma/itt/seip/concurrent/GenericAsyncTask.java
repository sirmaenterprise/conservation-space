package com.sirma.itt.seip.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.RuntimeContext;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Generic fork/join task for executing asynchronous operations. The class offers 3 execution methods.
 * {@link GenericAsyncTask#executeTask()} method will be executed in a own thread separate thread but the methods
 * {@link #executeOnSuccess()} and {@link #executeOnFail()} will be executed on the original thread that was used to
 * call the task on the first place.<br>
 * The class will initialize the security context (if present) and the runtime configuration properties before calling
 * the {@link GenericAsyncTask#executeTask()} method.
 *
 * @author BBonev
 */
public abstract class GenericAsyncTask extends RecursiveTask<Boolean>implements Callable<Boolean> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericAsyncTask.class);

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2493861809509789668L;

	/** The tracker. */
	private final TimeTracker tracker;

	/** The runtime configuration. */
	private final CurrentRuntimeConfiguration runtimeConfiguration;

	private CurrentRuntimeConfiguration oldConfiguration;

	private transient SecurityContextManager securityContextManager;
	private SecurityContext securityContext;

	/**
	 * Instantiates a new generic async task.
	 */
	public GenericAsyncTask() {
		tracker = new TimeTracker();
		// transfer the current configuration to the executing thread
		runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
	}

	/**
	 * Initialize security context before execution
	 *
	 * @param contextManager
	 *            the security context manager
	 */
	void initializeSecurity(SecurityContextManager contextManager) {
		securityContextManager = contextManager;
		securityContext = contextManager.createTransferableContext();
	}

	@Override
	protected Boolean compute() {
		// else just invoke the call method
		try {
			return call();
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("boxing")
	public Boolean call() throws Exception {
		tracker.begin();
		beforeCall();
		try {
			return executeTask();
		} finally {
			// clear configurations at the end not to leave anything
			afterCall();
			LOGGER.trace("Generic async task: {} took: {} ms", this.getClass().getSimpleName(), tracker.stop());
		}
	}

	private void afterCall() {
		if (isSecurityInitialized()) {
			securityContextManager.endContextExecution();
		}
		RuntimeContext.replaceConfiguration(oldConfiguration);
	}

	private boolean isSecurityInitialized() {
		return securityContextManager != null && securityContext != null;
	}

	protected void beforeCall() {
		// set the new configurations
		oldConfiguration = RuntimeContext.replaceConfiguration(runtimeConfiguration);
		if (isSecurityInitialized()) {
			securityContextManager.initializeFromContext(securityContext);
		}
	}

	/**
	 * Execute task.
	 *
	 * @return true, if successful
	 * @throws Exception
	 *             on error
	 */
	protected abstract boolean executeTask() throws Exception; // NOSONAR

	/**
	 * Executes the method on success on the original calling thread.
	 */
	public void executeOnSuccess() {
		// nothing to do
	}

	/**
	 * Executes the method on failure on the original calling thread.
	 */
	public void executeOnFail() {
		// nothing to do
	}

	/**
	 * The method will be called if any of the batch tasks failed and the execution was required to fail if any fails.
	 * The method will not be called if it's not required to fail on error. The method will be called only for tasks
	 * that succeeded in their execution. The method is called on the original execution thread.
	 */
	public void onRollback() {
		// nothing to do
	}

	/**
	 * Gets the current execution time of the task in milliseconds. If task has not been started -1 is returned if
	 * already finished 0 is returned.
	 *
	 * @return the long
	 */
	public long executionTime() {
		if (tracker == null) {
			return -1;
		}
		try {
			return tracker.elapsedTime();
		} catch (RuntimeException e) {
			LOGGER.trace("Problem when stopping time tracking: ", e);
			return 0;
		}
	}
}
