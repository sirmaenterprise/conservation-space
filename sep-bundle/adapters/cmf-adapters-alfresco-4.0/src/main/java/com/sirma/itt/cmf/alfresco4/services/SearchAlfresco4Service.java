/*
 * Copyright (c) 2012 14.09.2012 , Sirma ITT.
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessor;
import com.sirma.itt.cmf.alfresco4.services.convert.FieldProcessorSearchArguments;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.DateRange;

/**
 * The Class SearchAlfresco4Service is search adapter in alfresco.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class SearchAlfresco4Service implements CMFSearchAdapterService,
		AlfrescoCommunicationConstants {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -9120060781052022245L;
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(SearchAlfresco4Service.class);
	/** cache the check. */
	private boolean debugEnabled = LOGGER.isDebugEnabled();

	/** The rest client. */
	@Inject
	private RESTClient restClient;
	@Inject
	private DMSConverterFactory convertorFactory;

	private static final FieldProcessor SEARCH_LEVEL = new FieldProcessorSearchArguments();
	@Inject
	private CMFPermissionAdapterService permissionAdapterService;

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

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends FileDescriptor> SearchArguments<E> search(SearchArguments<E> args,
			Class<? extends Instance> model) throws DMSException {
		try {
			JSONObject request = new JSONObject();
			List<E> results = null;

			QueryVistor visitor = new QueryVistor(convertorFactory.getConverter(model));
			args.getQuery().visit(visitor);

			request.put(KEY_PAGING, getPaging(args));
			request.put(KEY_SORT, getSorting(args, model));
			if (StringUtils.isNotNullOrEmpty(args.getContext())) {
				request.put(KEY_CONTEXT, args.getContext());
			}
			request.put(KEY_QUERY, visitor.getQuery().toString());

			debug("QUERY: ", visitor.getQuery().toString());
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String response = restClient.request(ServiceURIRegistry.CMF_SEARCH_SERVICE,
					createMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					results = new ArrayList<E>(nodes.length());
					for (int i = 0; i < nodes.length(); i++) {
						JSONObject jsonObject = (JSONObject) nodes.get(i);
						String id = jsonObject.getString(KEY_NODEREF);
						// TODO remove it
						if (jsonObject.has(KEY_SITE_ID)) {
							String containerId = jsonObject.getString(KEY_SITE_ID);
							results.add((E) new AlfrescoFileDescriptor(id, containerId, restClient));
						}
					}
					args.setResult(results);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Result " + args.getResult());
					}
					AlfrescoUtils.populatePaging(result, args);
					return args;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Search in DMS failed for " + args, e);
		} catch (Exception e) {
			throw new DMSException("Search in DMS failed!", e);
		}
		throw new DMSException("DMS system does not respond to search!");
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
	@SuppressWarnings("rawtypes")
	private boolean appendByValueType(StringBuffer finalQuery, Query query,
			DMSTypeConverter modelConvertor) throws DMSException {
		Pair<String, Serializable> convertedProperty = modelConvertor.convertCMFtoDMSProperty(
				query.getKey(), query.getValue(), SEARCH_LEVEL);
		if (convertedProperty == null) {
			return false;
		}
		String key = convertedProperty.getFirst();
		Serializable value = convertedProperty.getSecond();
		if ((key == null) || (value == null) || value.toString().isEmpty()) {
			return false;
		}

		if (CMFPermissionAdapterService.LIST_OF_ACTIVE_USERS.equals(query.getKey())
				|| CMFPermissionAdapterService.LIST_OF_ALLOWED_USERS.equals(query.getKey())) {
			value = permissionAdapterService.searchableUserId(value);
		}

		StringBuilder queryBuilder = new StringBuilder();
		// prepare query
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
						queryBuilder.append(query.getBoost().toString());
					}
				}
				queryBuilder.append(AlfrescoUtils.SEARCH_START);
			}
		}
		boolean appended = false;
		// check what is provided and append specifically
		if (key.endsWith("TYPE") || key.endsWith("ASPECT")) {
			Collection aspects = null;
			if (value instanceof Collection) {
				aspects = (Collection) value;
			} else {
				aspects = Collections.singletonList(value);
			}
			int index = aspects.size();
			if (index > 0) {
				// queryBuilder.append(AlfrescoUtils.SEARCH_START);
				for (Object val : aspects) {
					convertedProperty = modelConvertor.convertCMFtoDMSProperty(val.toString(), "",
							DMSTypeConverter.PROPERTIES_MAPPING);
					queryBuilder.append(KEY_ASPECT).append(AlfrescoUtils.SEARCH_START_VALUE)
							.append(convertedProperty.getFirst().toString())
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
				// queryBuilder.append(AlfrescoUtils.SEARCH_END);
				appended = true;
			}
		} else if (key.endsWith("PARENT")) {
			String dmsId = null;
			if (value instanceof String) {
				dmsId = value.toString();
			} else if (value instanceof DMSInstance) {
				dmsId = ((DMSInstance) value).getDmsId();
			}
			if (dmsId != null) {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(dmsId)
						.append(AlfrescoUtils.SEARCH_END_VALUE);
				appended = true;
			}
		} else if (query.getValue() instanceof DateRange) {
			// daterange is converted to string
			if (value != null) {
				queryBuilder.append(key).append(":")
						.append(TypeConverterUtil.getConverter().convert(String.class, value));
				appended = true;
			}
		} else if (query.getKey().endsWith(CommonProperties.PROPERTY_NOTNULL)
				|| query.getKey().endsWith(CommonProperties.PROPERTY_ISNULL)) {
			Collection aspects = null;
			if (value instanceof Collection) {
				aspects = (Collection) value;
			} else {
				aspects = Collections.singletonList(value);
			}
			int index = aspects.size();
			if (index > 0) {
				// queryBuilder.append(AlfrescoUtils.SEARCH_START);
				for (Object val : aspects) {
					convertedProperty = modelConvertor.convertCMFtoDMSProperty(val.toString(), "",
							DMSTypeConverter.PROPERTIES_MAPPING);
					queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE)
							.append(convertedProperty.getFirst().toString())
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
				// queryBuilder.append(AlfrescoUtils.SEARCH_END);
				appended = true;
			}
		} else if (value instanceof String) {
			// if during dms convert Collections are converted to string.
			if (query.getValue() instanceof Collection) {
				appended = iterateCollection(query, key, query.getValue(), queryBuilder);
			} else {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(value)
						.append(AlfrescoUtils.SEARCH_END_VALUE);
				appended = true;
			}
		} else if (value instanceof Date) {
			queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE)
					.append(AlfrescoUtils.formatDate((Date) value))
					.append(AlfrescoUtils.DATE_FROM_SUFFIX).append(AlfrescoUtils.SEARCH_END_VALUE);
			appended = true;
		} else if (value instanceof Number) {
			queryBuilder.append(key).append(":").append(value);
			appended = true;
		} else if (query.getValue() instanceof List) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (query.getValue() instanceof Set) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (value instanceof Collection) {
			appended = iterateCollection(query, key, value, queryBuilder);
		}
		if (appended) {
			finalQuery.append(queryBuilder).append(AlfrescoUtils.SEARCH_END);
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
	private boolean iterateCollection(Query query, String key, Serializable value,
			StringBuilder queryBuilder) {
		Collection collection = null;
		if (value instanceof Collection) {
			collection = (Collection) value;
		} else {
			collection = Collections.singletonList(value);
		}
		int index = collection.size();
		boolean appended = false;
		if (index > 0) {
			// queryBuilder.append(AlfrescoUtils.SEARCH_START);
			for (Object val : collection) {
				queryBuilder.append(key).append(AlfrescoUtils.SEARCH_START_VALUE).append(val)
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
			// queryBuilder.append(AlfrescoUtils.SEARCH_END);
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
	private JSONObject getPaging(SearchArguments<?> args) throws JSONException {
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
		JSONArray sorting = null;
		Sorter sorter = args.getSorter();
		Map<String, Serializable> sortingArgs = new LinkedHashMap<>();
		if (sorter != null) {
			Map<String, Serializable> sortArgs = new HashMap<String, Serializable>();
			sortArgs.put(sorter.getSortField(), sorter.isAscendingOrder());
			sortingArgs.putAll(convertorFactory.getConverter(model)
					.convertCMFtoDMSPropertiesByValue(sortArgs, DMSTypeConverter.ALLOW_ALL));
			// add second sorter
			if (!sortingArgs.containsKey("cm:modified")) {
				sortingArgs.put("cm:modified", false);
			}

		} else {
			sortingArgs.put("cm:modified", false);
		}
		Set<Entry<String, Serializable>> keySet = sortingArgs.entrySet();
		sorting = new JSONArray();
		for (Entry<String, Serializable> string : keySet) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(string.getKey(), string.getValue());
			sorting.put(jsonObject);
		}
		sortingArgs = null;
		return sorting;

	}

	/**
	 * Prints debug message.
	 *
	 * @param message
	 *            is the message to print
	 */
	private void debug(String... message) {
		if (debugEnabled) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string.toString());
			}
			LOGGER.debug(builder.toString());
		}
	}

}
