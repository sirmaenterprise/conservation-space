package com.sirma.itt.seip.eai.service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.internal.DataIntegrationRequest;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The service is responsible to execute the actual resolve of instances and to link and persist them using the
 * {@link #importInstances(Collection)} method or {@link #importInstances(String, Collection)}
 *
 * @author bbanchev
 */
@ApplicationScoped
public class IntegrateExternalObjectsService {

	private static final String SYSTEM_IS_NOTIFIED = "! System is notified!";
	private static final String ORIGINAL_CAUSE = "! Original cause: ";
	private static final String FAILED_TO_PROCESS_RESPONSE_OF = "Failed to process response of ";
	private static final String MISSING_IMPORT_SERVICE_REGISTERED_TO = "Missing import service registered to: ";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private EAICommunicationService communicationService;
	@Inject
	private EAIRequestProvider requestProvider;
	@Inject
	@ExtensionPoint(value = IntegrateObjectsServiceAdapter.PLUGIN_ID)
	private Plugins<IntegrateObjectsServiceAdapter> integrationAdapters;

	/**
	 * Import instances based on a collection of instances containing emf:externalID as property. Returns the imported
	 * instances. The actual behavior depends on the {@link IntegrateObjectsServiceAdapter} used, based on the
	 * {@link IntegrateObjectsServiceAdapter#getName()} that matches the 'integrated' property of the instances.
	 *
	 * @param items
	 *            the items to extract externalId and to import
	 * @return the collection of imported instances.
	 */
	public Collection<Instance> importInstances(Collection<Instance> items) {
		if (items.isEmpty()) {
			LOGGER.warn("No instances are provided for import! Skipping import operation...");
			return Collections.emptyList();
		}
		Collection<ExternalInstanceIdentifier> externalId = new ArrayList<>(items.size());
		String context = null;
		for (Instance item : items) {
			try {
				Pair<String, ExternalInstanceIdentifier> extractExternalInstanceIdentifier = extractExternalInstanceIdentifier(
						item);
				// Only first system is used.
				if (context == null) {
					context = extractExternalInstanceIdentifier.getFirst();
				}
				externalId.add(extractExternalInstanceIdentifier.getSecond());
			} catch (Exception e) {
				LOGGER.error("Failed to import instance with id {}", item.getId(), e);
				throw new EAIRuntimeException("Failed to prepare import for the instance set: " + items
						+ ORIGINAL_CAUSE + e.getLocalizedMessage(), e);
			}
		}
		return importInstances(context, externalId);
	}

	/**
	 * Import instances based on a collection of emf:externalID. Returns the imported instances. The actual behavior
	 * depends on the {@link IntegrateObjectsServiceAdapter} used, based on the
	 * {@link IntegrateObjectsServiceAdapter#getName()} that matches the systemId parameter
	 *
	 * @param systemId
	 *            the system id to import from
	 * @param externalIds
	 *            the external ids to search for
	 * @return the collection of imported instances
	 */
	public Collection<Instance> importInstances(String systemId, Collection<ExternalInstanceIdentifier> externalIds) {
		Importer<Collection<Instance>> importer = () -> integrationAdapters
				.get(systemId)
					.orElseThrow(() -> new EAIException(MISSING_IMPORT_SERVICE_REGISTERED_TO + systemId))
					.importInstances(externalIds, true, true);
		return doImport(systemId, importer);
	}

	/**
	 * Import instances based on a collection of emf:externalID. Returns the imported instances. The actual behavior
	 * depends on the {@link IntegrateObjectsServiceAdapter} used, based on the
	 * {@link IntegrateObjectsServiceAdapter#getName()} that matches the systemId parameter
	 *
	 * @param request
	 *            is the request data to be imported
	 * @return the collection of imported instances
	 */
	public <T extends ExternalInstanceIdentifier, R> Collection<R> importInstances(DataIntegrationRequest<T> request) {
		String systemId = request.getSystemId();
		Importer<Collection<R>> importer = () -> integrationAdapters
				.get(systemId)
					.orElseThrow(() -> new EAIException(MISSING_IMPORT_SERVICE_REGISTERED_TO + systemId))
					.importInstances(request);
		return doImport(systemId, importer);
	}

	private <R> R doImport(String systemId, Importer<R> importProvider) {
		try {
			return importProvider.importInstances();
		} catch (EAIReportableException e) {
			LOGGER.error("Failed to execute import request to {}", systemId, e);
			try {
				communicationService.invoke(requestProvider.provideRequest(systemId, BaseEAIServices.LOGGING, e));
			} catch (Exception e1) {// NOSONAR
				throw new EAIRuntimeException(
						FAILED_TO_PROCESS_RESPONSE_OF + systemId + " and subsequent notification error: "
								+ e1.getLocalizedMessage() + ORIGINAL_CAUSE + e.getLocalizedMessage(),
						e);
			}
			throw new EAIRuntimeException(FAILED_TO_PROCESS_RESPONSE_OF + systemId + SYSTEM_IS_NOTIFIED, e);
		} catch (Exception e) {
			LOGGER.error("Failed to execute import request to {}", systemId, e);
			throw new EAIRuntimeException(
					"Error! The operation cannot be performed. See the details below:\r\n" + e.getMessage(), e);
		}
	}

	private Pair<String, ExternalInstanceIdentifier> extractExternalInstanceIdentifier(Instance item)
			throws EAIException {
		for (IntegrateObjectsServiceAdapter integrateObjectsServiceAdapter : integrationAdapters) {
			ExternalInstanceIdentifier id = integrateObjectsServiceAdapter.extractExternalInstanceIdentifier(item);
			if (id != null) {
				return new Pair<>(integrateObjectsServiceAdapter.getName(), id);
			}
		}
		throw new EmfRuntimeException("Failed to extract external id during request for instance: " + item);
	}

	/**
	 * Resolve instance by its identifying {@link ResolvableInstance}.The actual behavior depends on the
	 * {@link IntegrateObjectsServiceAdapter} used, based on the
	 * {@link IntegrateObjectsServiceAdapter#isResolveSupported(ResolvableInstance)} check
	 *
	 * @param resolvable
	 *            the resolvable instance to resolve
	 * @param persist
	 *            whether to persist the instance only after resolve
	 * @return the instance that is resolved - might be null
	 * @throws EAIException
	 *             on any error of the invoked service with detailed description
	 */
	public Instance resolveInstance(ResolvableInstance resolvable, boolean persist) {
		Optional<IntegrateObjectsServiceAdapter> adapterRef = integrationAdapters
				.stream()
					.filter(e -> e.isResolveSupported(resolvable))
					.findFirst();
		IntegrateObjectsServiceAdapter adapter;
		if (adapterRef.isPresent()) {
			adapter = adapterRef.get();
		} else {
			throw new EmfRuntimeException("Missing external instance resolver for: " + resolvable);
		}
		String systemId = adapter.getName();
		try {
			return adapter.resolveInstance(resolvable.getExternalIdentifier(), persist);
		} catch (EAIReportableException e) {
			try {
				communicationService.invoke(requestProvider.provideRequest(systemId, BaseEAIServices.LOGGING, e));
			} catch (Exception e1) {// NOSONAR
				throw new EmfRuntimeException(
						FAILED_TO_PROCESS_RESPONSE_OF + systemId + " and subsequent notification error: "
								+ e1.getLocalizedMessage() + ORIGINAL_CAUSE + e.getLocalizedMessage(),
						e);
			}
			throw new EmfRuntimeException(FAILED_TO_PROCESS_RESPONSE_OF + systemId + SYSTEM_IS_NOTIFIED, e);
		} catch (EAIException e) {
			throw new EmfRuntimeException("Failed to execute import request to " + systemId, e);
		}
	}

	/**
	 * Functional interface to represent a import operation
	 *
	 * @author BBonev
	 * @param <E>
	 *            result type
	 */
	private interface Importer<E> {

		/**
		 * Perform the import and return the result
		 *
		 * @return the created data after the import
		 * @throws EAIException
		 *             any error
		 */
		E importInstances() throws EAIException;
	}
}
