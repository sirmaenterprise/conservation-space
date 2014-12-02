package com.sirma.itt.emf.definition.compile;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.emf.definition.event.LoadAllDefinitions;
import com.sirma.itt.emf.definition.event.LoadTemplateDefinitions;
import com.sirma.itt.emf.definition.event.LoadTopLevelDefinitions;
import com.sirma.itt.emf.definition.event.TemplateDefinitionsLoaded;
import com.sirma.itt.emf.definition.event.TopLevelDefinitionsLoaded;
import com.sirma.itt.emf.definition.load.DefinitionLoader;
import com.sirma.itt.emf.domain.MessageType;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Asynchronous event handler for definition loading.
 *
 * @author BBonev
 */
@Stateless
@Secure(runAsSystem = true)
public class DefinitionLoaderEventHandler {
	private static final String COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT = "Completed definition loading in {} s for event: {}";
	private static final String RECEIVED_EVENT = "Received event: {}";
	private static final String CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED = "Called definition compilation on disabled compiler. Nothing is updated.";

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefinitionLoaderEventHandler.class);

	/** The definition loader. */
	@Inject
	private DefinitionLoader definitionLoader;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The disable compiler. */
	@Inject
	@Config(name = EmfConfigurationProperties.DISABLE_DEFINITION_COMPILER, defaultValue = "false")
	private Instance<Boolean> disableCompiler;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if (disableCompiler.get()) {
			LOGGER.warn("WARNING: Definition compiler has been disabled. Until enabled no definitions will be updated!");
		}
	}

	/**
	 * Load definitions asynchronously based on the given event.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadAllDefinitions(@Observes LoadAllDefinitions event) {
		if (disableCompiler.get() && !event.isForced()) {
			// if the definition compilation is disabled but was called with forced event then we
			// should perform it.

				// compilation is disabled
			LOGGER.info(CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED);
				return;
		}
		TimeTracker tracker = new TimeTracker().begin();
		LOGGER.info(RECEIVED_EVENT, event);
		List<VerificationMessage> errors = new LinkedList<VerificationMessage>();
		List<VerificationMessage> messages = definitionLoader.loadTemplateDefinitions();
		ValidationLoggingUtil.copyMessages(messages, errors, MessageType.ERROR, MessageType.WARNING);
		messages = definitionLoader.loadDefinitions();
		ValidationLoggingUtil.copyMessages(messages, errors, MessageType.ERROR, MessageType.WARNING);
		if (!errors.isEmpty()) {
			String string = ValidationLoggingUtil.printMessages(errors);
			printErrorMessage(event, string);
		}
		LOGGER.info(COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT, tracker.stopInSeconds(),
				event);
		eventService.fire(new AllDefinitionsLoaded());
	}

	/**
	 * Load definitions asynchronously based on the given event.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadDefinitions(@Observes LoadTopLevelDefinitions event) {
		if (disableCompiler.get() && !event.isForced()) {
			// if the definition compilation is disabled but was called with forced event then we
			// should perform it.
				// compilation is disabled
				LOGGER.info(CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED);
				return;
		}

		TimeTracker tracker = new TimeTracker().begin();
		LOGGER.info(RECEIVED_EVENT + event);
		List<VerificationMessage> errors = new LinkedList<VerificationMessage>();
		List<VerificationMessage> messages = definitionLoader.loadDefinitions();
		ValidationLoggingUtil
				.copyMessages(messages, errors, MessageType.ERROR, MessageType.WARNING);
		if (!errors.isEmpty()) {
			String string = ValidationLoggingUtil.printMessages(errors);
			printErrorMessage(event, string);
		}
		LOGGER.info(COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT, tracker.stopInSeconds(),
				event);
		eventService.fire(new TopLevelDefinitionsLoaded());
	}

	/**
	 * Load definitions asynchronously based on the given event.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadTemplateDefinitions(@Observes LoadTemplateDefinitions event) {
		if (disableCompiler.get()) {
			// if the definition compilation is disabled but was called with forced event then we
			// should perform it.
			if (!event.isForced()) {
				// compilation is disabled
				LOGGER.info(CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED);
				return;
			}
		}
		TimeTracker tracker = new TimeTracker().begin();
		LOGGER.info(RECEIVED_EVENT + event);
		List<VerificationMessage> errors = new LinkedList<VerificationMessage>();
		List<VerificationMessage> messages = definitionLoader.loadTemplateDefinitions();
		ValidationLoggingUtil
				.copyMessages(messages, errors, MessageType.ERROR, MessageType.WARNING);
		if (!errors.isEmpty()) {
			String string = ValidationLoggingUtil.printMessages(errors);
			printErrorMessage(event, string);
		}
		LOGGER.info(COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT, tracker.stopInSeconds(),
				event);
		eventService.fire(new TemplateDefinitionsLoaded());
	}

	/**
	 * Prints the error message.
	 * 
	 * @param event
	 *            the event
	 * @param string
	 *            the string
	 */
	private void printErrorMessage(EmfEvent event, String string) {
		LOGGER.error("\n=======================================================================\nFound problems executing event "
				+ event
				+ string
				+ "\n=======================================================================\n");
	}
}
