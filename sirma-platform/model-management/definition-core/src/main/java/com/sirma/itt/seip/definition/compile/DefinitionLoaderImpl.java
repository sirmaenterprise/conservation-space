package com.sirma.itt.seip.definition.compile;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.definition.compile.DefinitionCompiler;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback;
import com.sirma.itt.seip.definition.compile.DefinitionLoader;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil.ValidationMessageHolder;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * Implementation class for loading definitions into the application.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionLoaderImpl implements DefinitionLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionLoader.class);

	/** The definitions. */
	@Inject
	@ExtensionPoint(DefinitionCompilerCallback.TARGET_NAME)
	private Iterable<DefinitionCompilerCallback<TopLevelDefinition>> definitions;

	/** The compiler. */
	@Inject
	private DefinitionCompiler compiler;

	/** Used to restrict multiple simultaneous definition loadings. */
	@Inject
	private ContextualLock contextualLock;

	@Override
	public List<Message> loadDefinitions() {
		if (!contextualLock.tryLock()) {
			// already running
			return logAlreadyRunning("Definition loading in progress: ignoring event requiest loadDefinitions()");
		}
		try {
			return executeInternalLoading(compiler, definitions);
		} finally {
			contextualLock.unlock();
		}
	}

	/**
	 * Log already running.
	 *
	 * @param message
	 *            the message
	 * @return the list
	 */
	private static List<Message> logAlreadyRunning(String message) {
		try {
			ValidationLoggingUtil.startErrorCollecting();
			LOGGER.warn(message);
			ValidationLoggingUtil.addWarningMessage(message);
			return ValidationLoggingUtil.getMessages();
		} finally {
			ValidationLoggingUtil.stopErrorCollecting();
		}
	}

	/**
	 * Execute internal loading for the given list of callbacks
	 *
	 * @param callbacks
	 *            the list of callbacks to execute.
	 * @return the list
	 */
	private static List<Message> executeInternalLoading(DefinitionCompiler compiler,
			Iterable<DefinitionCompilerCallback<TopLevelDefinition>> callbacks) {
		TimeTracker tracker = new TimeTracker().begin();
		List<Message> messages = new LinkedList<>();
		for (DefinitionCompilerCallback<TopLevelDefinition> callback : callbacks) {
			List<Message> list = doDefinitionLoading(compiler, callback,
					ValidationLoggingUtil.acquireMessageHolder());
			if (list != null) {
				messages.addAll(list);
			}
		}
		LOGGER.info(LoggingUtil
				.buildInfoMessage("Definitions loaded! The operation took " + tracker.stopInSeconds() + " s"));
		return messages;
	}

	private static List<Message> doDefinitionLoading(DefinitionCompiler compiler,
			DefinitionCompilerCallback<TopLevelDefinition> callback, ValidationMessageHolder holder) {
		ValidationLoggingUtil.initialize(holder);
		try {
			TimeTracker tracker = new TimeTracker().begin();

			LOGGER.info("Start loading {} definitions", callback.getCallbackName());

			List<TopLevelDefinition> list = compiler.compileDefinitions(callback, true);

			LOGGER.info("Loading of {} {} definitions took {} ms", list.size(), callback.getCallbackName(),
					tracker.stop());
			return ValidationLoggingUtil.getMessages();
		} finally {
			ValidationLoggingUtil.releaseMessageHolder(holder);
		}
	}

}
