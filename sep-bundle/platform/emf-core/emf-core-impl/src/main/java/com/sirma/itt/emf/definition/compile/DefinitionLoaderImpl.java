package com.sirma.itt.emf.definition.compile;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionLoader;
import com.sirma.itt.emf.definition.load.TemplateDefinition;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Implementation class for loading definitions into the application.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Secure(runAsSystem = true)
public class DefinitionLoaderImpl implements DefinitionLoader {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionLoader.class);

	/** The definitions. */
	@Inject
	@Any
	@Definition
	private Instance<DefinitionCompilerCallback<TopLevelDefinition>> definitions;

	/** The template definitions. */
	@Inject
	@Any
	@TemplateDefinition
	private Instance<DefinitionCompilerCallback<TopLevelDefinition>> templateDefinitions;

	/** The compiler. */
	@Inject
	private DefinitionCompiler compiler;

	/** Used to restrict multiple simultaneous definition loadings. */
	private ReentrantLock lock = new ReentrantLock();

	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	/** The enable parallel loading. */
	@Inject
	@Config(name = EmfConfigurationProperties.PARALLEL_DEFINITION_COMPILING, defaultValue = "false")
	private Boolean enableParallelLoading;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<VerificationMessage> loadTemplateDefinitions() {
		// add warming up of the cache
		boolean unlock = false;
		try {
			// here we check if someone called a synchronization during
			// asynchronous call
			if (lock.isLocked()) {
				// if the method is called from the asynchronous method then we
				// own the lock
				if (!lock.isHeldByCurrentThread()) {
					try {
						ValidationLoggingUtil.startErrorCollecting();
						String message = "Definition loading in progress: ignoring event requiest loadTemplateDefinitions()";
						LOGGER.warn(message);
						ValidationLoggingUtil.addWarningMessage(message);
						return ValidationLoggingUtil.getMessages();
					} finally {
						ValidationLoggingUtil.stopErrorCollecting();
					}
				}
			} else {
				lock.lock();
				unlock = true;
			}

			List<VerificationMessage> messages = executeInternalLoading("template definitions",
					templateDefinitions);

			return messages;
		} finally {
			ValidationLoggingUtil.stopErrorCollecting();
			if (unlock) {
				lock.unlock();
			}
		}
	}

	/**
	 * Execute internal loading for the given list of callbacks
	 * 
	 * @param name
	 *            the name of the executed callback types.
	 * @param callbacks
	 *            the list of callbacks to execute.
	 * @return the list
	 */
	private List<VerificationMessage> executeInternalLoading(String name,
			Instance<DefinitionCompilerCallback<TopLevelDefinition>> callbacks) {
		TimeTracker tracker = new TimeTracker().begin();
		List<DefinitionLoaderTask> tasks = new LinkedList<>();
		List<VerificationMessage> messages = new LinkedList<>();
		for (DefinitionCompilerCallback<TopLevelDefinition> callback : callbacks) {
			DefinitionLoaderTask task = new DefinitionLoaderTask(callback);
			if (enableParallelLoading.booleanValue()) {
				tasks.add(task);
			} else {
				task.executeTask();
				List<VerificationMessage> list = task.getMessages();
				if (list != null) {
					messages.addAll(list);
				}
			}
		}
		if (!tasks.isEmpty()) {
			taskExecutor.execute(tasks);

			for (DefinitionLoaderTask task : tasks) {
				List<VerificationMessage> list = task.getMessages();
				if (list != null) {
					messages.addAll(list);
				}
			}
		}
		LOGGER.debug("All " + name + " loaded in " + tracker.stopInSeconds() + " s");

		return messages;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<VerificationMessage> loadDefinitions() {
		boolean unlock = false;
		try {
			// here we check if someone called a synchronization during
			// asynchronous call
			if (lock.isLocked()) {
				// if the method is called from the asynchronous method then we
				// own the lock
				if (!lock.isHeldByCurrentThread()) {
					try {
						ValidationLoggingUtil.startErrorCollecting();
						String message = "Definition loading in progress: ignoring event requiest loadDefinitions()";
						LOGGER.warn(message);
						ValidationLoggingUtil.addWarningMessage(message);
						List<VerificationMessage> messages = ValidationLoggingUtil.getMessages();
						return messages;
					} finally {
						ValidationLoggingUtil.stopErrorCollecting();
					}
				}
			} else {
				lock.lock();
				unlock = true;
			}
			List<VerificationMessage> messages = executeInternalLoading("definitions", definitions);
			return messages;
		} finally {
			if (unlock) {
				lock.unlock();
			}
		}
	}

	/**
	 * Loader definitions task.
	 * 
	 * @author BBonev
	 */
	class DefinitionLoaderTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -3874265379615136318L;
		/** The callback. */
		private DefinitionCompilerCallback<TopLevelDefinition> callback;
		/** The messages. */
		private List<VerificationMessage> messages;

		/**
		 * Instantiates a new definition loader task.
		 * 
		 * @param callback
		 *            the callback
		 */
		public DefinitionLoaderTask(DefinitionCompilerCallback<TopLevelDefinition> callback) {
			super();
			this.callback = callback;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean executeTask() {
			ValidationLoggingUtil.startErrorCollecting();
			try {
				TimeTracker tracker = new TimeTracker().begin();

				LOGGER.info("Start loading " + callback.getCallbackName() + " definitions");

				List<TopLevelDefinition> list = compiler.compileDefinitions(callback, true);

				LOGGER.info("Loaded " + list.size() + " " + callback.getCallbackName()
						+ " definitions in " + tracker.stopInSeconds() + " s");
				messages = ValidationLoggingUtil.getMessages();
			} finally {
				ValidationLoggingUtil.stopErrorCollecting();
			}
			return true;
		}

		/**
		 * Gets the messages.
		 * 
		 * @return the messages
		 */
		public List<VerificationMessage> getMessages() {
			return messages;
		}

	}

}
