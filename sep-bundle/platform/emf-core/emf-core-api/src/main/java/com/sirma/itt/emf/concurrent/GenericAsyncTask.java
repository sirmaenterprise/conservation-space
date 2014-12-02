package com.sirma.itt.emf.concurrent;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfiguration.CurrentRuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Generic fork/join task for executing asynchronous operations. The class offers 3 execution
 * methods. {@link GenericAsyncTask#executeTask()} method will be executed in a own thread separate
 * thread but the methods {@link #executeOnSuccess()} and {@link #executeOnFail()} will be executed
 * on the original thread that was used to call the task on the first place.<br>
 * The class will initialize the security context (if present) and the runtime configuration
 * properties before calling the {@link GenericAsyncTask#executeTask()} method.
 * 
 * @author BBonev
 */
public abstract class GenericAsyncTask extends RecursiveTask<Boolean> implements Callable<Boolean> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericAsyncTask.class);

	/** The trace. */
	private static boolean trace = LOGGER.isTraceEnabled();

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2493861809509789668L;

	/** The tracker. */
	private TimeTracker tracker;

	/** The runtime configuration. */
	private final CurrentRuntimeConfiguration runtimeConfiguration;

	/** The security context. */
	private final SecurityContext securityContext;

	/**
	 * Instantiates a new generic async task.
	 */
	public GenericAsyncTask() {
		tracker = new TimeTracker();
		// get the current security context
		securityContext = SecurityContextManager.getCurrentSecurityContext();
		// transfer the current configuration to the executing thread
		runtimeConfiguration = RuntimeConfiguration.getCurrentConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean compute() {
		if (securityContext != null) {
			return SecurityContextManager.callAs(securityContext.getAuthentication(),
					securityContext.getEffectiveAuthentication(), this);
		}
		// else just invoke the call method
		try {
			return call();
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean call() throws Exception {
		tracker.begin();
		// set the new configurations
		migrateConfigurationContext();

		boolean resultStatus = executeTask();

		double stopInSeconds = tracker.stopInSeconds();
		if (trace) {
			LOGGER.trace("Generic async task: {} took: {} sec", this.getClass().getSimpleName(),
					stopInSeconds);
		}
		return resultStatus;
	}

	/**
	 * Migrate configuration context to the new calling thread while preserving a single
	 * serialization engine for the executing thread if needed.
	 */
	protected void migrateConfigurationContext() {
		// backup existing engine
		Serializable serializationEngine = RuntimeConfiguration
				.getConfiguration(RuntimeConfigurationProperties.SERIALIZATION_ENGINE);
		// clear all previous configuration from other configurations copy
		RuntimeConfiguration.resetConfiguration();
		// set all provided configurations from the calling thread
		RuntimeConfiguration.setCurrentConfiguration(runtimeConfiguration);
		if (trace) {
			Serializable oldConfiguredEngine = RuntimeConfiguration
					.getConfiguration(RuntimeConfigurationProperties.SERIALIZATION_ENGINE);
			LOGGER.trace("Coping configuration. Current SE={} and old SE={}", serializationEngine,
					oldConfiguredEngine);
		}
		// if the new engine is the same as the old one (if equal this means the method is not
		// called on other thread but on the original so no need to clear the engine instance)

		// clear and override the engine with the local instance, because of the internal
		// implementation a new set will push the old engine in the stack so we explicitly
		// clear it first before setting the new one!
		// this will ensure that the thread that runs the task has only one engine and not a
		// new one every call
		RuntimeConfiguration
				.clearConfiguration(RuntimeConfigurationProperties.SERIALIZATION_ENGINE);
		if (serializationEngine == null) {
			// no need to set null engine
			return;
		}
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.SERIALIZATION_ENGINE,
				serializationEngine);
	}

	/**
	 * Execute task.
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean executeTask() throws Exception;

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
	 * The method will be called if any of the batch tasks failed and the execution was required to
	 * fail if any fails. The method will not be called if it's not required to fail on error. The
	 * method will be called only for tasks that succeeded in their execution. The method is called
	 * on the original execution thread.
	 */
	public void onRollback() {
		// nothing to do
	}

	/**
	 * Gets the current execution time of the task in milliseconds. If task has not been started -1
	 * is returned if already finished 0 is returned.
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
			return 0;
		}
	}
}