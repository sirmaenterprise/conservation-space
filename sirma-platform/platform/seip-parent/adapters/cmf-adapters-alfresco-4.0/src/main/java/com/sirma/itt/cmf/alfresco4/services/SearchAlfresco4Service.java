/*
 * Copyright (c) 2012 14.09.2012 , Sirma ITT.
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessor;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessorSearchArguments;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.time.DateRange;

/**
 * The Class SearchAlfresco4Service is search adapter in alfresco.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class SearchAlfresco4Service implements CMFSearchAdapterService, AlfrescoCommunicationConstants {

	private static final String CM_MODIFIED = "cm:modified";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The rest client. */
	@Inject
	private RESTClient restClient;
	@Inject
	private DMSConverterFactory convertorFactory;

	private static final FieldProcessor SEARCH_LEVEL = new FieldProcessorSearchArguments();

	/**
	 * Visitor implementation for queries.
	 *
	 * @author Borislav Banchev
	 */
	private class QueryVistor extends DefaultQueryVisitor {

		/** The specfic converter. */
		DMSTypeConverter specficConverter = null;

		/**
		 * Instantiates a new query vistor.
		 *
		 * @param specficConverter
		 *            the specfic converter
		 */
		public QueryVistor(DMSTypeConverter specficConverter) {
			super();
			this.specficConverter = specficConverter;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.itt.cmf.search.Query.Visitor#visit(com.sirma.itt.cmf.search .Query)
		 */
		@Override
		public void visit(Query query) throws Exception {
			appendByValueType(builder, query, specficConverter);
		}

	}

	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	public <E extends FileDescriptor> SearchArguments<E> search(SearchArguments<E> args,
			Class<? extends Instance> model) throws DMSException {
		try {
			JSONObject request = new JSONObject();
			QueryVistor visitor = new QueryVistor(convertorFactory.getConverter(model));
			args.getQuery().visit(visitor);

			request.put(KEY_PAGING, getPaging(args));
			request.put(KEY_SORT, getSorting(args, model));
			if (StringUtils.isNotBlank(args.getContext())) {
				request.put(KEY_CONTEXT, args.getContext());
			}
			request.put(KEY_QUERY, visitor.getQuery().toString());

			debug("QUERY: ", visitor.getQuery().toString());
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.CMF_SEARCH_SERVICE, createMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					return parseSearchResponse(args, result);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Search in DMS failed for " + args, e);
		} catch (Exception e) {
			throw new DMSException("Search in DMS failed!", e);
		}
		throw new DMSException("DMS system does not respond to search!");
	}

	@SuppressWarnings("unchecked")
	private <E extends FileDescriptor> SearchArguments<E> parseSearchResponse(SearchArguments<E> args,
			JSONObject result) throws JSONException {
		List<E> results;
		JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
		results = new ArrayList<>(nodes.length());
		for (int i = 0; i < nodes.length(); i++) {
			JSONObject jsonObject = (JSONObject) nodes.get(i);
			String id = jsonObject.getString(KEY_NODEREF);
			// TODO remove it
			if (jsonObject.has(KEY_SITE_ID)) {
				String containerId = jsonObject.getString(KEY_SITE_ID);
				results.add((E) new AlfrescoFileDescriptor(id, containerId, null, restClient));
			}
		}
		args.setResult(results);
		LOGGER.debug("Result {}", args.getResult());
		AlfrescoUtils.populatePaging(result, args);
		return args;
	}

	/**
	 * Append by value type to base query.
	 *
	 * @param finalQuery
	 *            is the query buffer that is the final result
	 * @param query
	 *            is the query to process
	 * @param modelConvertor
	 *            is the specific data converter for the model
	 * @return true, if query is appended
	 * @throws DMSException
	 *             the dMS exception
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private boolean appendByValueType(StringBuilder finalQuery, Query query, DMSTypeConverter modelConvertor)
			throws DMSException {
		Pair<String, Serializable> convertedProperty = modelConvertor.convertCMFtoDMSProperty(query.getKey(),
				query.getValue(), SEARCH_LEVEL);
		if (convertedProperty == null) {
			return false;
		}
		String key = convertedProperty.getFirst();
		Serializable value = convertedProperty.getSecond();
		if (key == null || value == null || value.toString().isEmpty()) {
			return false;
		}

		StringBuilder queryBuilder = new StringBuilder();
		// prepare query
		parepareQuery(finalQuery, query, queryBuilder);

		boolean appended = false;
		// check what is provided and append specifically
		if (key.endsWith("TYPE") || key.endsWith("ASPECT")) {
			appended = handleType(query, modelConvertor, value, queryBuilder, appended);
		} else if (key.endsWith("PARENT")) {
			appended = handleParentKey(key, value, queryBuilder, appended);
		} else if (query.getValue() instanceof DateRange) {
			appended = handleDateRange(key, value, queryBuilder, appended);
		} else if (query.getKey().endsWith(CommonProperties.PROPERTY_NOTNULL)
				|| query.getKey().endsWith(CommonProperties.PROPERTY_ISNULL)) {
			appended = handleNonNull(query, modelConvertor, key, value, queryBuilder, appended);
		} else if (value instanceof String) {
			appended = handleString(query, key, value, queryBuilder);
		} else if (value instanceof Date) {
			queryBuilder
					.append(key)
						.append(AlfrescoUtils.SEARCH_START_VALUE)
						.append(AlfrescoUtils.formatDate((Date) value))
						.append(AlfrescoUtils.DATE_FROM_SUFFIX)
						.append(AlfrescoUtils.SEARCH_END_VALUE);
			appended = true;
		} else if (value instanceof Number) {
			queryBuilder.append(key).append(":").append(value);
			appended = true;
		} else if (query.getValue() instanceof Set) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (query.getValue() instanceof List) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (value instanceof Collection) {
			appended = iterateCollection(query, key, value, queryBuilder);
		}

		if (appended) {
			finalQuery.append(queryBuilder).append(AlfrescoUtils.SEARCH_END);
		}
		return appended;
	}

	private static void parepareQuery(StringBuilder finalQuery, Query query, StringBuilder queryBuilder) {
		if (query.getBoost() == QueryBoost.EXCLUDE) {
			queryBuilder.append(AlfrescoUtils.SEARCH_START).append(query.getBoost());
		} else {

			if (finalQuery.toString().trim().length() == 0) {
				queryBuilder.append(AlfrescoUtils.SEARCH_START);
			} else {
				int lastIndexOf = finalQuery.lastIndexOf("(");
				if (lastIndexOf > 0) {
					String substring = finalQuery.substring(lastIndexOf + 1);
					if (substring.trim().length() > 0) {
						queryBuilder.append(query.getBoost());
					}
				}
				queryBuilder.append(AlfrescoUtils.SEARCH_START);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean handleType(Query query, DMSTypeConverter modelConvertor, Serializable value,
			StringBuilder queryBuilder, boolean appended) {
		Pair<String, Serializable> convertedProperty;

		Collection<?> aspects = null;
		if (value instanceof Collection) {
			aspects = (Collection<?>) value;
		} else {
			aspects = Collections.singletonList(value);
		}

		int index = aspects.size();

		if (index > 0) {

			for (Object val : aspects) {
				convertedProperty = modelConvertor.convertCMFtoDMSProperty(val.toString(), "",
						DMSTypeConverter.PROPERTIES_MAPPING);
				queryBuilder
						.append(KEY_ASPECT)
							.append(AlfrescoUtils.SEARCH_START_VALUE)
							.append(convertedProperty.getFirst())
							.append(AlfrescoUtils.SEARCH_END_VALUE);
				index--;
				if (index > 0) {
					if (query.getBoost() == QueryBoost.EXCLUDE) {
						queryBuilder.append(AlfrescoUtils.SEARCH_AND);
					} else {
						queryBuilder.append(AlfrescoUtils.SEARCH_OR);
					}
				}
			}
			return true;
		}
		return appended;
	}

	private static boolean handleDateRange(String key, Serializable value, StringBuilder queryBuilder,
			boolean appended) {
		// daterange is converted to string
		if (value != null) {
			queryBuilder.append(key).append(":").append(TypeConverterUtil.getConverter().convert(String.class, value));
			return true;
		}
		return appended;
	}

	private static boolean handleParentKey(String key, Serializable value, StringBuilder queryBuilder,
			boolean appended) {
		String dmsId = null;
		if (value instanceof String) {
			dmsId = value.toString();
		} else if (value instanceof DMSInstance) {
			dmsId = ((DMSInstance) value).getDmsId();
		}
		if (dmsId != null) {
			queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(dmsId).append(
					AlfrescoUtils.SEARCH_END_VALUE);
			return true;
		}
		return appended;
	}

	private static boolean handleString(Query query, String key, Serializable value, StringBuilder queryBuilder) {
		boolean appended;
		// if during dms convert Collections are converted to string.
		if (query.getValue() instanceof Collection) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else {
			queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(value).append(
					AlfrescoUtils.SEARCH_END_VALUE);
			appended = true;
		}
		return appended;
	}

	@SuppressWarnings("rawtypes")
	private static boolean handleNonNull(Query query, DMSTypeConverter modelConvertor, String key, Serializable value,
			StringBuilder queryBuilder, boolean appended) {
		Pair<String, Serializable> convertedProperty;
		Collection aspects = null;
		if (value instanceof Collection) {
			aspects = (Collection) value;
		} else {
			aspects = Collections.singletonList(value);
		}
		int index = aspects.size();
		if (index > 0) {
			for (Object val : aspects) {
				convertedProperty = modelConvertor.convertCMFtoDMSProperty(val.toString(), "",
						DMSTypeConverter.PROPERTIES_MAPPING);
				queryBuilder
						.append(key)
							.append(AlfrescoUtils.SEARCH_START_VALUE)
							.append(convertedProperty.getFirst())
							.append(AlfrescoUtils.SEARCH_END_VALUE);
				index--;
				if (index > 0) {
					if (query.getBoost() == QueryBoost.EXCLUDE) {
						queryBuilder.append(AlfrescoUtils.SEARCH_AND);
					} else {
						queryBuilder.append(AlfrescoUtils.SEARCH_OR);
					}
				}
			}
			return true;
		}
		return appended;
	}

	/**
	 * Iterate collection of values and append them with the specified key to the final query.
	 *
	 * @param query
	 *            the query to get values from
	 * @param key
	 *            the updated key
	 * @param value
	 *            the expected collection.
	 * @param queryBuilder
	 *            the final query
	 * @return true, if appended
	 */
	@SuppressWarnings("rawtypes")
	private static boolean iterateCollection(Query query, String key, Serializable value, StringBuilder queryBuilder) {
		Collection<?> collection = null;
		if (value instanceof Collection) {
			collection = (Collection<?>) value;
		} else {
			collection = Collections.singletonList(value);
		}

		int index = collection.size();
		boolean appended = false;
		if (index > 0) {
			for (Object val : collection) {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(val).append(
						AlfrescoUtils.SEARCH_END_VALUE);
				index--;
				if (index > 0) {
					if (query.getBoost() == QueryBoost.EXCLUDE) {
						queryBuilder.append(AlfrescoUtils.SEARCH_AND);
					} else {
						queryBuilder.append(AlfrescoUtils.SEARCH_OR);
					}
				}

			}
			appended = true;
		}
		return appended;
	}

	/**
	 * Gets the paging arguments from request as json object.
	 *
	 * @param args
	 *            the args
	 * @return the pagging
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static JSONObject getPaging(SearchArguments<?> args) throws JSONException {
		JSONObject paging = new JSONObject();
		paging.put(KEY_PAGING_TOTAL, args.getTotalItems());
		paging.put(KEY_PAGING_SIZE, args.getPageSize());
		paging.put(KEY_PAGING_SKIP, args.getSkipCount());
		paging.put(KEY_PAGING_MAX, args.getMaxSize());
		return paging;

	}

	/**
	 * Gets the sorting.
	 *
	 * @param args
	 *            the args
	 * @param model
	 *            is the model to get sorting for
	 * @return the sorting array of json objects
	 * @throws JSONException
	 *             the jSON exception
	 * @throws DMSException
	 *             the dMS exception
	 */
	private JSONArray getSorting(SearchArguments<?> args, Class<? extends Instance> model)
			throws JSONException, DMSException {
		Sorter sorter = args.getFirstSorter();
		Map<String, Serializable> sortingArgs = new LinkedHashMap<>();
		if (sorter != null) {
			Map<String, Serializable> sortArgs = new HashMap<>();
			sortArgs.put(sorter.getSortField(), sorter.isAscendingOrder());
			sortingArgs.putAll(convertorFactory.getConverter(model).convertCMFtoDMSPropertiesByValue(sortArgs,
					DMSTypeConverter.ALLOW_ALL));
			// add second sorter
			if (!sortingArgs.containsKey(CM_MODIFIED)) {
				sortingArgs.put(CM_MODIFIED, false);
			}

		} else {
			sortingArgs.put(CM_MODIFIED, false);
		}
		Set<Entry<String, Serializable>> keySet = sortingArgs.entrySet();
		JSONArray sorting = new JSONArray();
		for (Entry<String, Serializable> string : keySet) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(string.getKey(), string.getValue());
			sorting.put(jsonObject);
		}
		return sorting;

	}

	/**
	 * Prints debug message.
	 *
	 * @param message
	 *            is the message to print
	 */
	private static void debug(String... message) {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string);
			}
			LOGGER.debug(builder.toString());
		}
	}

}
