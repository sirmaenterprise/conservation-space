package com.sirma.itt.seip.eai.cs.service.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.eai.cs.model.CSResultItem;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.request.CSRetrieveRequest;
import com.sirma.itt.seip.eai.cs.model.request.CSSearchRequest;
import com.sirma.itt.seip.eai.cs.model.response.CSItemsSetResponse;
import com.sirma.itt.seip.eai.cs.model.response.CSRetrieveItemsResponse;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.URIServiceRequest;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.error.LoggingDTO;
import com.sirma.itt.seip.eai.model.request.ResultOrdering;
import com.sirma.itt.seip.eai.model.request.query.QueryEntry;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;
import com.sirma.itt.seip.eai.model.request.query.RawQueryEntry;
import com.sirma.itt.seip.eai.model.response.SimpleHttpResponse;
import com.sirma.itt.seip.eai.model.response.StreamingResponse;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.impl.HttpClientCommunicationServiceAdapter;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Abstract CS communication adapter holding the base service requests and base mapping function from response to java
 * objects.
 * 
 * @author bbanchev
 */
public abstract class CSClientCommunicationAdapter extends HttpClientCommunicationServiceAdapter {
	private static final String MSG_INVALID_REQUEST = "Generated invalid request!";
	private static final ContentType MSG_CONTENT = ContentType.create("text/plain", Consts.UTF_8);

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	protected ObjectMapper mapper;
	@Inject
	private SecurityContextManager securityManager;

	@Override
	public ServiceResponse invoke(RequestInfo request) throws EAIException {
		if (BaseEAIServices.SEARCH.equals(request.getServiceId())) {
			return invokeSearch(request);
		}
		if (BaseEAIServices.RETRIEVE.equals(request.getServiceId())) {
			return invokeRetrieve(request);
		}
		if (BaseEAIServices.LOGGING.equals(request.getServiceId())) {
			return invokeLogging(request);
		}
		if (BaseEAIServices.DIRECT.equals(request.getServiceId())) {
			return invokeByURI(request);
		}
		throw new EAIException("Unrecognized service: " + request.getServiceId());
	}

	/**
	 * Invoke a logging service with the information provided as a request. Expected {@link LoggingDTO} on invoke of
	 * {@link RequestInfo#getRequest()}
	 *
	 * @param request
	 *            the logging information to sent
	 * @return a {@link SimpleHttpResponse} with the status code for the response
	 * @throws EAIReportableException
	 *             on error during communication or during building the request method
	 */
	protected ServiceResponse invokeLogging(RequestInfo request) throws EAIReportableException {
		HttpPost errorLog = null;
		try {
			LoggingDTO loggedInfo = (LoggingDTO) request.getRequest();
			LOGGER.debug("Error '{}' is going to be recorded in the remote system {}.", loggedInfo, getName());
			errorLog = createPostMethod(request.getServiceId());
			// by ICD spec
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addTextBody("severity", loggedInfo.getSeverity().name(), MSG_CONTENT);
			entityBuilder.addTextBody("summary", loggedInfo.getSummary(), MSG_CONTENT);
			entityBuilder.addTextBody("details", EqualsHelper.getOrDefault(loggedInfo.getDetails(), ""), MSG_CONTENT);
			entityBuilder.addTextBody("origin", String.valueOf(EqualsHelper.getOrDefault(loggedInfo.getOrigin(), "")),
					MSG_CONTENT);
			entityBuilder.setCharset(Consts.UTF_8);
			errorLog.setEntity(entityBuilder.build());
			LoggingDTO logResponse = executeMethodWithResponse(errorLog,
					response -> readObjectFromResponse(response, LoggingDTO.class));
			LOGGER.debug("Error is sent to the remote system {} with response '{}'.", getName(), logResponse);
			return logResponse;
		} catch (EAIReportableException e) {// NOSONAR
			throw new EAIReportableException(e.getMessage(), e.getCause(),
					Objects.toString(request, MSG_INVALID_REQUEST));
		} catch (Exception e) {
			throw new EAIReportableException("Failed to build and execute a logging message!", e,
					Objects.toString(request, MSG_INVALID_REQUEST));
		}
	}

	protected CSItemsSetResponse invokeSearch(RequestInfo wrappedRequest) throws EAIException {
		HttpGet method = null;
		try {
			CSSearchRequest request = (CSSearchRequest) wrappedRequest.getRequest();
			// only one level - no recursive iteration
			List<QueryEntry> entries = request.getQuery().getEntries();
			List<NameValuePair> params = new LinkedList<>();
			if (entries.size() == 1 && entries.get(0) instanceof RawQuery) {
				entries = ((RawQuery) entries.get(0)).getEntries();
			}
			for (QueryEntry queryEntry : entries) {
				if (queryEntry instanceof RawQueryEntry && ((RawQueryEntry) queryEntry).getProperty() != null) {
					processQueryParam(params, queryEntry);
				}
			}
			ResultPaging paging = request.getPaging();
			List<ResultOrdering> ordering = request.getOrdering();
			appendQueryParam(params, "skip", String.valueOf(paging.getSkip()));
			appendQueryParam(params, "limit", String.valueOf(paging.getLimit()));
			appendQueryParam(params, "references", String.valueOf(request.isIncludeReferences()));
			appendQueryParam(params, "thumbnails", String.valueOf(request.isIncludeThumbnails()));
			for (ResultOrdering resultOrdering : ordering) {
				appendQueryParam(params, "order", (resultOrdering.isAsc() ? "" : "-") + resultOrdering.getOrderBy());
			}
			method = createGetMethod(wrappedRequest.getServiceId(), params);
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Failed to build a request. ", e);
		}

		return executeMethodWithResponse(method,
				response -> readObjectFromResponse(response, CSItemsSetResponse.class));
	}

	protected ServiceResponse invokeRetrieve(RequestInfo request) throws EAIException {
		CSRetrieveItemsResponse result = new CSRetrieveItemsResponse();
		CSRetrieveRequest importRequest = (CSRetrieveRequest) request.getRequest();
		invokeParallelImport(request, result, importRequest);
		return result;

	}

	private void invokeParallelImport(final RequestInfo request, final CSRetrieveItemsResponse result,
			final CSRetrieveRequest importRequest) {
		StringBuilder errorBuilder = new StringBuilder();
		final SecurityContext currentContext = securityManager.createTransferableContext();
		final String systemClientId = integrationService
				.getIntegrationConfiguration(getName())
					.getSystemClientId()
					.get();
		importRequest.getExternalIds().parallelStream().forEach(externalId -> {
			try {
				securityManager.initializeFromContext(currentContext);
				executeSingleImport(request, result, externalId, systemClientId);
			} catch (Exception e) {
				synchronized (errorBuilder) {
					errorBuilder
							.append("Error on instance: ")
								.append(externalId)
								.append(". Details:")
								.append(e.getMessage())
								.append("\r\n");
				}
				LOGGER.warn("Failed to execute service {} on instance {}!", request.getServiceId(), externalId, e);
			} finally {
				securityManager.endContextExecution();
			}
		});
		if (errorBuilder.length() > 0) {
			String error = errorBuilder.toString();
			result.setError(
					new EAIReportableException("The operation cannot be performed. See details below:\r\n" + error,
							new EAIException(error), Objects.toString(request, MSG_INVALID_REQUEST)));
		}
	}

	protected CSResultItem executeSingleImport(final RequestInfo request, final CSRetrieveItemsResponse result,
			final CSExternalInstanceId externalId, String systemClientId) throws EAIException {
		String currentClientId = integrationService.getIntegrationConfiguration(getName()).getSystemClientId().get();
		if (!systemClientId.equals(currentClientId)) {
			throw new EAIException("Expected client: " + systemClientId + ", however is provided: " + currentClientId);
		}
		// process each item and skip failed
		HttpGet createGetMethod = createGetMethod(request.getServiceId(), externalId.getSourceSystemId(),
				externalId.getExternalId());
		CSResultItem record = executeMethodWithResponse(createGetMethod,
				response -> readObjectFromResponse(response, CSResultItem.class));
		result.addRetrieved(externalId, record.getRecord());
		return record;
	}

	protected StreamingResponse invokeByURI(RequestInfo request) throws EAIReportableException {
		URIServiceRequest uriRequest = null;
		try {
			uriRequest = (URIServiceRequest) request.getRequest();
			HttpGet directURIService = new HttpGet(uriRequest.getUri());
			return executeMethodWithResponse(directURIService, CSClientCommunicationAdapter::readStreamFromResponse);
		} catch (EAIReportableException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIReportableException("Failed to download the requested content: " + uriRequest, e,
					request.toString());
		}
	}

	protected <T> T executeMethodWithResponse(HttpRequestBase method, ResponseParser<HttpResponse, T> postProcessor)
			throws EAIException {
		try {
			HttpResponse response = provideHttpClient().execute(method);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == 200) {
				return postProcessor.parse(response);
			} else if (statusLine.getStatusCode() == 400 || statusLine.getStatusCode() == 500) {
				// part of ICD - just report error in the log
				StringPair summaryAndOrigin = extractSummaryAndOrigin(response, statusLine);
				throw new EAIException("Remote server reported an error on the request: " + method.getURI() + ". "
						+ summaryAndOrigin.getFirst());
			}
			StringPair summaryAndOrigin = extractSummaryAndOrigin(response, statusLine);
			// report the user friendly message + add cause origin - the http request
			throw new EAIReportableException("Failure during service request. " + summaryAndOrigin.getFirst()
					+ "! Request URI: " + method.getURI(), Objects.toString(method.getURI(), MSG_INVALID_REQUEST));
		} catch (EAIException e) {// EAIException or EAIReportableException
			throw e;
		} catch (IOException e) {
			String errorMsg = "Communication error during request on URI: " + method.getURI() + " with details: "
					+ e.getMessage();
			LOGGER.error(errorMsg, e);
			throw new EAIReportableException(errorMsg, e, Objects.toString(method.getURI(), MSG_INVALID_REQUEST));
		} catch (Exception e) {
			String errorMsg = "Failed to execute a request: " + method + " with details: " + e.getMessage();
			LOGGER.error(errorMsg, e);
			throw new EAIException(errorMsg, e);
		}
	}

	private StringPair extractSummaryAndOrigin(HttpResponse response, StatusLine statusLine) {
		LoggingDTO extractErrorDetails = null;
		if (statusLine.getStatusCode() == 200 || statusLine.getStatusCode() == 500
				|| statusLine.getStatusCode() == 400) {
			try {
				extractErrorDetails = readObjectFromResponse(response, LoggingDTO.class);
				LOGGER.error("Reported error '{}' with origin {} and details: {}", extractErrorDetails.getSummary(),
						extractErrorDetails.getOrigin(), extractErrorDetails.getDetails());
			} catch (Exception e) {// NOSONAR
				// skip the error since remote server does not provide information
				LOGGER.trace("Remote server did not respond to logging request!", e);
			}
		}
		String status;
		String origin = null;
		if (extractErrorDetails != null) {
			status = "Summary: " + extractErrorDetails.getSummary();
			origin = Objects.toString(extractErrorDetails.getOrigin(), null);
		} else {
			status = "Status: " + statusLine.toString();
		}
		return new StringPair(status, origin);
	}

	/***
	 * Process appending {@link QueryEntry} as {@link NameValuePair} in the params list.
	 * 
	 * @param params
	 *            to append to
	 * @param queryEntry
	 *            to use as source
	 */
	protected void processQueryParam(List<NameValuePair> params, QueryEntry queryEntry) {
		RawQueryEntry rawQueryEntry = (RawQueryEntry) queryEntry;
		Collection<Object> values = rawQueryEntry.getValues();
		if (values.isEmpty()) {
			return;
		}
		// between is supported only for dates
		if (EqualsHelper.nullSafeEquals(rawQueryEntry.getOperator(), "between", true)) {
			// always with both params or at least one
			if (values.size() != 2) {
				LOGGER.warn("Uncompatible between operator parameters: {}", values);
				return;
			}

			Iterator<Object> arguments = values.iterator();
			String dateFrom = getISODate(arguments.next());
			String dateTo = getISODate(arguments.next());
			if (dateFrom != null || dateTo != null) {
				// add at least ""
				appendQueryParam(params, rawQueryEntry.getProperty(), dateFrom == null ? "" : dateFrom);
				appendQueryParam(params, rawQueryEntry.getProperty(), dateTo == null ? "" : dateTo);
			}
		} else if (values.size() > 1) {// by ICD spec append as \n splitted
			Object[] arguments = values.toArray(new Object[values.size()]);
			StringBuilder criteria = new StringBuilder(arguments.length * 20);
			for (int i = 0; i < arguments.length; i++) {
				criteria.append(arguments[i]);
				if (i + 1 < arguments.length) {
					criteria.append('\n');
				}
			}
			appendQueryParam(params, rawQueryEntry.getProperty(), criteria.toString());
		} else {
			Object criteriaValue = values.iterator().next();
			String criteriaValueAsString;
			if (criteriaValue == null || (criteriaValueAsString = String.valueOf(criteriaValue)).isEmpty()) {
				return;
			}
			appendQueryParam(params, rawQueryEntry.getProperty(), criteriaValueAsString);
		}
	}

	private static JsonObject readJsonObject(String jsonValue) {
		try (StringReader reader = new StringReader(jsonValue)) {
			return Json.createReader(reader).readObject();
		}
	}

	protected static void appendQueryParam(List<NameValuePair> params, String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	/**
	 * Process string/date, checks for validity and return the iso date if valid, and null if value is invalid or
	 * missing
	 * 
	 * @param object
	 *            the source
	 * @return the ISO date or null
	 */
	protected static String getISODate(Object object) {
		if (object == null) {
			return null;
		}
		try {
			if (object instanceof String) {
				// do convert to check validness - empty value is allowed
				Date parsed = ISO8601DateFormat.parse((String) object);
				if (parsed != null) {
					return (String) object;
				}
				return null;
			} else if (object instanceof Date) {
				return ISO8601DateFormat.format((Date) object);
			} else {
				throw new EAIRuntimeException("Unrecognized date format: " + object);
			}
		} catch (Exception e) {
			throw new EAIRuntimeException("Invalid date format: " + object, e);
		}
	}

	/**
	 * Read http response as mapped java object using {@link ObjectMapper}.
	 *
	 * @param <T>
	 *            the generic type of clz as the read response
	 * @param response
	 *            the http response to read as clz
	 * @param clz
	 *            the clz to produce
	 * @return the mapped json as java object
	 * @throws EAIReportableException
	 *             if message is not parsable or not expected format
	 */
	protected <T> T readObjectFromResponse(HttpResponse response, Class<T> clz) throws EAIReportableException {
		try (InputStream content = response.getEntity().getContent()) {
			T readValue = mapper.readValue(content, clz);
			LOGGER.trace("Object information {} from response {} ", readValue, response);
			return readValue;
		} catch (Exception e) {
			throw new EAIReportableException("Could not read response from " + response.getEntity(), e);
		}
	}

	protected static StreamingResponse readStreamFromResponse(HttpResponse response) throws EAIReportableException {
		try {
			HttpEntity entity = response.getEntity();
			StreamingResponse streamingResponse = new StreamingResponse(entity.getContent(),
					getHeaderValue(entity.getContentType(), "application/octet-stream"),
					getHeaderValue(entity.getContentEncoding(), StandardCharsets.UTF_8.name()),
					entity.getContentLength());
			LOGGER.trace("Stream information {} from response {} ", streamingResponse, response);
			return streamingResponse;
		} catch (Exception e) {
			throw new EAIReportableException("Failed to invoke service!", e);
		}
	}

	protected static String getHeaderValue(Header header, String defaultValue) {
		if (header != null) {
			return header.getValue();
		}
		return defaultValue;
	}
}