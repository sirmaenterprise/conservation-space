package com.sirma.itt.seip.eai.service.search;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.SearchResultInstances;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.SearchEngine;

/**
 * Extension of search engines that checks if this is search based on context. If external system with that id matches
 * the context, the search request is sent using the facilities for that system
 * 
 * @author bbanchev
 */
public abstract class ExternalSystemSearchEngine implements SearchEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	protected EAICommunicationService communicationService;
	@Inject
	protected EAIRequestProvider requestProvider;
	@Inject
	protected EAIResponseReader responseReader;
	@Inject
	protected EAIConfigurationService integrationService;

	@Override
	public <S extends SearchArguments<? extends Instance>> boolean isSupported(Class<?> target, S arguments) {
		return Instance.class.isAssignableFrom(target) && isExternalContext(arguments.getContext());
	}

	private static boolean isExternalContext(String context) {
		return context != null;
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		try {
			searchInExternalSystem(arguments);
		} catch (Exception e) {
			LOGGER.error("Generic failure during external search request!", e);
			if (arguments.getResult() == null) {
				arguments.setResult(Collections.emptyList());
			}
			arguments.setSearchError(e);
		}
	}

	@Override
	public <S extends SearchArguments<? extends Instance>> Stream<ResultItem> stream(S arguments) {
		throw new UnsupportedOperationException("External system search does not support result streaming");
	}

	/**
	 * Performs the actual search in external system that may produce an error during search process. Some of the
	 * {@link SearchArguments} fields might not be initialized at the end of execution due to an error.
	 * 
	 * @param arguments
	 *            to use during search
	 */
	protected <E extends Instance, S extends SearchArguments<E>> void searchInExternalSystem(S arguments) {
		String systemId = arguments.getContext();
		try {
			RequestInfo request = requestProvider.provideRequest(systemId, BaseEAIServices.SEARCH, arguments);
			ResponseInfo response = communicationService.invoke(request);
			SearchResultInstances<E> parsedResponse = responseReader.parseResponse(response);
			populateResponse(arguments, parsedResponse);
		} catch (EAIReportableException e) {
			logError(arguments, e);
		} catch (EAIException e) {
			throw new EmfRuntimeException("Failed to execute search request to " + systemId, e);
		}
	}

	/**
	 * Logs an {@link EAIReportableException} error in the remote system and rethrow the exception as
	 * {@link EmfRuntimeException} to processed.
	 * 
	 * @param arguments
	 *            the arguments to use
	 * @param eaiException
	 *            the reportable error
	 */
	protected <E extends Instance, S extends SearchArguments<E>> void logError(S arguments,
			EAIReportableException eaiException) {
		String systemId = arguments.getContext();
		try {
			communicationService
					.invoke(requestProvider.provideRequest(systemId, BaseEAIServices.LOGGING, eaiException));
		} catch (Exception e1) {// NOSONAR
			throw new EmfRuntimeException("Failed to process response of "
					+ integrationService.getIntegrationConfiguration(systemId).getSystemClientId().get()
					+ " and subsequent notification error: " + e1.getLocalizedMessage() + "! \nOriginal cause: "
					+ eaiException.getLocalizedMessage(), eaiException);
		}
		throw new EmfRuntimeException("Failed to process response of "
				+ integrationService.getIntegrationConfiguration(systemId).getSystemClientId().get()
				+ "! System is notified!\nOriginal cause: " + eaiException.getLocalizedMessage(), eaiException);
	}

	protected static <E extends Instance, S extends SearchArguments<E>> void populateResponse(final S arguments,
			final SearchResultInstances<E> parsedResponse) throws EAIException {
		// update the arguments wrapper and throw the error at the end
		arguments.setResult(parsedResponse.getInstances());
		arguments.setTotalItems(parsedResponse.getPaging().getTotal());
		arguments.setSkipCount(parsedResponse.getPaging().getSkip());
		arguments.setPageSize(parsedResponse.getPaging().getLimit());
		if (parsedResponse.getError() != null) {
			throw parsedResponse.getError();
		}
	}

	@Override
	public boolean prepareSearchArguments(SearchRequest request, SearchArguments<Instance> searchArguments) {
		// fill the context - might be null
		Optional<EAIConfigurationProvider> subSystem = integrationService
				.findIntegrationConfiguration(request.getFirst("context"));
		if (subSystem.isPresent()) {
			searchArguments.setContext(subSystem.get().getName());
			searchArguments.setCondition(request.getSearchTree());
			readSearchParameters(searchArguments, subSystem.get());
			return true;
		}
		return false;
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		return Function.identity();
	}

	private static void readSearchParameters(SearchArguments<Instance> searchArgs,
			EAIConfigurationProvider eaiConfigurationProvider) {
		searchArgs.setSkipCount(Math.max(searchArgs.getPageNumber() - 1, 0) * searchArgs.getPageSize());
		if (searchArgs.getSorters().isEmpty()) {
			SearchModelConfiguration searchConfiguration = eaiConfigurationProvider.getSearchConfiguration().get();
			if (searchConfiguration.getOrderData() != null && !searchConfiguration.getOrderData().isEmpty()) {
				searchArgs.setOrdered(true);
				searchArgs.addSorter(
						new Sorter(searchConfiguration.getOrderData().get(0).getPropertyId(), Sorter.SORT_DESCENDING));
			}

		}
	}

}
