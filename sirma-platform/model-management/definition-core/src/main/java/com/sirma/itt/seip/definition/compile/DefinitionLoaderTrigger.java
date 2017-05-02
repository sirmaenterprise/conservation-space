package com.sirma.itt.seip.definition.compile;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.definition.event.LoadAllDefinitions;
import com.sirma.itt.seip.definition.event.LoadAllDefinitionsSynchronious;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.runtime.RuntimeInfo;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Asynchronous event handler for definition loading.
 *
 * @author BBonev
 */
@Stateless
public class DefinitionLoaderTrigger {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT = "Completed definition loading in {} ms";
	private static final String CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED = "Called definition compilation on disabled compiler. Nothing is updated.";

	@Inject
	private DefinitionLoader definitionLoader;

	@Inject
	private EventService eventService;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "compiler.definition.disable", system = true, sensitive = true, type = Boolean.class, defaultValue = "false", label = "Configuration that disables the definition synchronization. Turning this value to true will speed the startup which is very useful for development purposes.")
	private ConfigurationProperty<Boolean> disableCompiler;

	/**
	 * Server startup definition loading for all tenants
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(async = true, phase = StartupPhase.BEFORE_APP_START, order = Double.MAX_VALUE - 1)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void serverStartupDefinitionLoading() {
		if (disableCompiler.get().booleanValue() && !RuntimeInfo.isStarted()) {
			// if the definition compilation is disabled but was called with
			// forced event then we should perform it.

			// compilation is disabled
			LOGGER.info(CALLED_DEFINITION_COMPILATION_ON_DISABLED_COMPILER_NOTHING_IS_UPDATED);
			eventService.fire(new AllDefinitionsLoaded());
			return;
		}
		loadDefinitionsInternal();
	}

	/**
	 * Load definitions asynchronously based on the given event.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SecureObserver
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadAllDefinitions(@Observes LoadAllDefinitions event) {
		loadDefinitionsInternal();
	}

	/**
	 * Load definitions synchronously based on the given event.
	 *
	 * @param event
	 *            the trigger event
	 */
	@SecureObserver
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadSynchroniousAllDefinitions(@Observes LoadAllDefinitionsSynchronious event) {
		loadDefinitionsInternal();
	}

	private void loadDefinitionsInternal() {
		performLoading(() -> {
			definitionLoader.loadDefinitions();
			return null;
		});
		eventService.fire(new AllDefinitionsLoaded());
	}

	/**
	 * Perform loading by calling the given callables.
	 *
	 * @param callables
	 *            the callables
	 */
	private static void performLoading(Callable<?>... callables) {
		if (callables == null) {
			return;
		}
		TimeTracker tracker = new TimeTracker().begin();
		LOGGER.info("Starting definition loading...");
		ValidationLoggingUtil.startErrorCollecting();
		try {
			for (int i = 0; i < callables.length; i++) {
				Callable<?> callable = callables[i];
				try {
					callable.call();
				} catch (Exception e) {
					LOGGER.error("Failed execution of {}", callable, e);
				}
			}
			printErrorMessages();
		} finally {
			ValidationLoggingUtil.stopErrorCollecting();
		}
		LOGGER.info(COMPLETED_DEFINITION_LOADING_IN_S_FOR_EVENT, tracker.stop());
	}

	/**
	 * Prints the error message.
	 *
	 * @param event
	 *            the event
	 * @param string
	 *            the string
	 */
	private static void printErrorMessage(String string) {
		LOGGER.error(
				"\n=======================================================================\nFound problems loading definitions "
						+ string + "\n=======================================================================\n");
	}

	private static void printErrorMessages() {
		List<Message> errors = new LinkedList<>();
		ValidationLoggingUtil.waitForLeasedMessageHolder();
		ValidationLoggingUtil.copyMessages(ValidationLoggingUtil.getMessages(), errors, MessageType.ERROR,
				MessageType.WARNING);
		if (!errors.isEmpty()) {
			String string = ValidationLoggingUtil.printMessages(errors);
			printErrorMessage(string);
		}
	}
}
