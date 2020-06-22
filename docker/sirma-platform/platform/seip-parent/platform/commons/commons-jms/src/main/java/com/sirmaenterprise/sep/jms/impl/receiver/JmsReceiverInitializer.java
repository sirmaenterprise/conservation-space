package com.sirmaenterprise.sep.jms.impl.receiver;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirmaenterprise.sep.jms.impl.JmsDefinitionProvider;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * On application start register all receiver definitions
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class JmsReceiverInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private JmsDefinitionProvider destinationsProvider;
	@Inject
	private JmsReceiverManager receiverManager;

	@Startup(async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	void initializeListeners() {
		Set<ReceiverDefinition> destinationMethods = destinationsProvider.getDefinitions();
		LOGGER.info("Found {} JMS destination. Starting listeners", destinationMethods.size());

		int total = 0;
		for (ReceiverDefinition definition : destinationMethods) {
			total += receiverManager.registerJmsListener(definition);
		}
		LOGGER.info("Completed listeners initialization. Starting {} listeners", total);
		receiverManager.start();
	}
}
