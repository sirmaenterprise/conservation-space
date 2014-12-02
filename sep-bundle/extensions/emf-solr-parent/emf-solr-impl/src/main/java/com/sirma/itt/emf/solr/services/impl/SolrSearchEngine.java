package com.sirma.itt.emf.solr.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchEngine;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * The Class SolrSearchEngine is responsible for search request through solr.
 */
@Extension(target = SearchEngine.TARGET_NAME, order = 15)
@ApplicationScoped
public class SolrSearchEngine implements SearchEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSearchEngine.class);
	/** The solr connector. */
	@Inject
	private SolrConnector solrConnector;
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_CONFIG_DASHLETS_ALL_FL, defaultValue = "uri,instanceType")
	private String defaultFields;
	@Inject
	@Config(name = SolrConfigurationProperties.SOLR_CONFIG_FQ_STATUS, defaultValue = "-status:(DELETED) AND isDeleted:false")
	private String fqStatus;
	@Inject
	private DictionaryService dictionaryService;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	@Inject
	private TypeConverter typeConverter;

	private final Map<String, Class<? extends Instance>> typeMapping = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Instance, S extends SearchArguments<E>> boolean isSupported(Class<?> target,
			S arguments) {
		return (arguments.getStringQuery() != null)
				&& EqualsHelper.nullSafeEquals(SearchDialects.SOLR, arguments.getDialect(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target,
			S arguments) {
		try {
			SolrQuery parameters = buildSolrQuery(arguments);

			QueryResponse queryResponse = solrConnector.queryWithPost(parameters);

			parseResponse(arguments, queryResponse);
		} catch (Exception e) {
			// TODO: implement proper exception handling
			// to prevent NPE when the result is not set for list value
			arguments.setResult(CollectionUtils.<E> emptyList());
			LOGGER.error("Error during execution of solr query: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses the response.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param arguments
	 *            the arguments
	 * @param queryResponse
	 *            the query response
	 */
	private <S extends SearchArguments<E>, E extends Instance> void parseResponse(S arguments,
			QueryResponse queryResponse) {
		if (queryResponse != null) {
			SolrDocumentList solrDocumentList = queryResponse.getResults();

			// update the total count
			arguments.setTotalItems(new Long(solrDocumentList.getNumFound()).intValue());

			// we only care for the count no need to create all results
			if (arguments.isCountOnly()) {
				arguments.setResult(Collections.<E> emptyList());
				return;
			}

			List<E> resultList = new ArrayList<>(solrDocumentList.size());

			Iterator<SolrDocument> iterator = solrDocumentList.iterator();
			while (iterator.hasNext()) {
				SolrDocument nextDocument = iterator.next();
				E instance = parseResponseItem(nextDocument);
				if (instance != null) {
					iterator.remove();
					resultList.add(instance);
				}
			}
			arguments.setResult(resultList);
		}
	}

	/**
	 * Parses the response item.
	 *
	 * @param <E>
	 *            the element type
	 * @param nextDocument
	 *            the next document
	 * @return the created instance or <code>null</code> if elements has missing data.
	 */
	private <E extends Instance> E parseResponseItem(SolrDocument nextDocument) {
		Map<String, Object> fieldValueMap = nextDocument.getFieldValueMap();
		String instanceId = (String) fieldValueMap.get(SolrQueryConstants.FIELD_NAME_INSTANCE_ID);
		if (instanceId == null) {
			LOGGER.warn("Invalid object id for: {}", fieldValueMap);
			return null;
		}

		String instanceType = (String) fieldValueMap
				.get(SolrQueryConstants.FIELD_NAME_INSTANCE_TYPE);
		if (instanceType == null) {
			LOGGER.warn("Type not found for entity with id: {}", instanceId);
			return null;
		}
		Class<? extends Instance> typeClass = getType(instanceType);
		if ((typeClass == null) || !Instance.class.isAssignableFrom(typeClass)) {
			LOGGER.warn("Invalid type {} for id {}", instanceType, instanceId);
			return null;
		}
		@SuppressWarnings("unchecked")
		E instance = (E) ReflectionUtils.newInstance(typeClass);
		instance.setId(namespaceRegistryService.getShortUri(instanceId));

		Set<String> keys = fieldValueMap.keySet();
		// clean the duplicate and not actual properties
		sanitizeProperties(keys);
		if (instance.getProperties() == null) {
			instance.setProperties(CollectionUtils.<String, Serializable> createHashMap(keys.size()));
		}
		setInstanceVariables(instance, keys, fieldValueMap);
		for (String entry : keys) {
			Object object = fieldValueMap.get(entry);
			if (object instanceof Serializable) {
				instance.getProperties().put(entry, (Serializable) object);
			}
		}
		return instance;
	}

	/**
	 * Builds the solr query from the given arguments
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @return the solr query
	 * @throws Exception
	 *             the exception
	 */
	private <E extends Instance, S extends SearchArguments<E>> SolrQuery buildSolrQuery(S arguments)
			throws Exception {
		SolrQuery query = new SolrQuery();

		parseQuery(query, arguments.getStringQuery());

		if (arguments.getSorter() != null) {
			query.setSort(arguments.getSorter().getSortField(), arguments.getSorter()
					.isAscendingOrder() ? ORDER.asc : ORDER.desc);
		}

		query.setStart(arguments.getSkipCount() > 0 ? arguments.getSkipCount() : null);
		query.set(CommonParams.FL,
				arguments.getProjection() == null ? defaultFields
				: arguments.getProjection());
		if (arguments.getArguments().containsKey(CommonParams.FQ)) {
			Serializable externalFQ = arguments.getArguments().get(CommonParams.FQ);
			if (externalFQ instanceof Collection) {
				@SuppressWarnings("unchecked")
				String[] fq = ((Collection<String>) externalFQ)
						.toArray(new String[((Collection<String>) externalFQ).size()]);
				query.add(CommonParams.FQ, fq);
			} else if (externalFQ instanceof String) {
				query.add(CommonParams.FQ, externalFQ.toString());
			}
		}
		query.add(CommonParams.FQ, fqStatus);
		query.set(CommonParams.TZ, TimeZone.getDefault().getID());

		int returnCount = arguments.isCountOnly() ? 1 : arguments.getMaxSize();
		Integer skipSize = Math.max(0, arguments.getPageSize() * (arguments.getPageNumber() - 1));
		query.set(CommonParams.START, skipSize);
		query.set(CommonParams.ROWS, returnCount);

		if (arguments.getQueryTimeout() > 0) {
			query.setTimeAllowed(arguments.getQueryTimeout());
			// good idea to set some default timeout
		}
		return query;
	}

	/**
	 * Sets the instance variables.
	 *
	 * @param instance
	 *            the instance
	 * @param keys
	 *            the keys
	 * @param fieldValueMap
	 *            the field value map
	 */
	private void setInstanceVariables(Instance instance, Set<String> keys,
			Map<String, Object> fieldValueMap) {
		if ((instance instanceof Purposable) && keys.contains(DefaultProperties.PURPOSE)) {
			((Purposable) instance).setPurpose((String) fieldValueMap
					.get(DefaultProperties.PURPOSE));
			keys.remove(DefaultProperties.PURPOSE);
		}
	}

	/**
	 * Parses the single string query in manner to enrich the solr parameters with some nested
	 * subqueries in the main query.
	 *
	 * @param parameters
	 *            are the current solr params
	 * @param stringQuery
	 *            is the single query to be parsed.
	 * @throws Exception
	 *             on any error during parse
	 */
	private void parseQuery(SolrQuery parameters, String stringQuery) throws Exception {
		// some default query - instead of throwing exception from Solr
		String q = SolrQueryConstants.QUERY_DEFAULT_ALL;

		if (StringUtils.isNotNullOrEmpty(stringQuery)) {
			if (stringQuery.charAt(0) == '|') {
				int endOfNested = stringQuery.indexOf('|', 1);
				String nestedQueries = stringQuery.substring(1, endOfNested);
				q = stringQuery.substring(endOfNested + 1);
				JSONObject additionalParams = JsonUtil.toJsonObject(nestedQueries);
				@SuppressWarnings("rawtypes")
				Iterator keys = additionalParams.keys();
				while (keys.hasNext()) {
					String key = keys.next().toString();
					parameters.add(key, additionalParams.getString(key));
				}
			} else {
				q = stringQuery;
			}
		}
		parameters.add(CommonParams.Q, q);
	}

	/**
	 * Clean up properties that should not be in properties map of instance
	 *
	 * @param set
	 *            the key set to clean up
	 */
	private void sanitizeProperties(Set<String> set) {
		// remove the duplicate
		// TODO might be from config
		set.remove(SolrQueryConstants.FIELD_NAME_INSTANCE_ID);
		set.remove(SolrQueryConstants.FIELD_NAME_INSTANCE_TYPE);
		set.remove(SolrQueryConstants.FIELD_NAME_INSTANCE_SEMANTIC_TYPE);
	}

	/**
	 * Gets the actual type by name.
	 *
	 * @param typeName
	 *            the type name
	 * @return the type from the registry
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Instance> getType(String typeName) {
		// use localized mapping for high speed
		Class<? extends Instance> result = typeMapping.get(typeName);
		if (result == null) {
			DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(typeName);
			if (definition != null) {
				result = typeConverter.convert(Class.class, definition.getJavaClassName());
			}
			typeMapping.put(typeName, result);
		}
		return result;
	}

}
