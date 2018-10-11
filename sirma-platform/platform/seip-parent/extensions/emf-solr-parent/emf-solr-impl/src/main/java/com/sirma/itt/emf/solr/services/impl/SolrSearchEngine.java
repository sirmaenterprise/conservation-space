package com.sirma.itt.emf.solr.services.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.solr.services.impl.facet.FacetResultTransformer;
import com.sirma.itt.emf.solr.services.impl.facet.FacetSolrHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.SearchEngine;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * The Class SolrSearchEngine is responsible for search request through solr. The engine also supports search arguments
 * preparation for basic/advanced search
 */
@Extension(target = SearchEngine.TARGET_NAME, order = 15)
@ApplicationScoped
public class SolrSearchEngine implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSearchEngine.class);

	private static final String TDATES = "tdates";

	private final Map<String, Class<? extends Instance>> typeMapping = new HashMap<>();

	@Inject
	private SolrConnector solrConnector;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	@Inject
	private FTSQueryParser parser;
	@Inject
	private DateConverter dateConverter;
	@Inject
	private FacetSolrHelper facetSolrHelper;
	@Inject
	private FacetResultTransformer facetResultTransformer;
	@Inject
	private SolrSearchConfiguration searchConfiguration;

	private String basicQueryDF;

	/**
	 * On startup prepare the field list to be populated on the result
	 */
	@PostConstruct
	public void onStartup() {
		searchConfiguration
				.getDashletsRequestFields()
					.addConfigurationChangeListener(changedProperty -> setBasicFilterDF());
		setBasicFilterDF();
	}

	/**
	 * Sets the basic filter df.
	 */
	private void setBasicFilterDF() {
		String defaultFields = searchConfiguration.getDashletsRequestFields().get();
		if (defaultFields.contains(DefaultProperties.HEADER_DEFAULT)) {
			basicQueryDF = defaultFields;
			return;
		}
		if (defaultFields.contains(DefaultProperties.HEADER_COMPACT)) {
			basicQueryDF = defaultFields.replace(DefaultProperties.HEADER_COMPACT, DefaultProperties.HEADER_DEFAULT);
		} else {
			basicQueryDF = defaultFields + "," + DefaultProperties.HEADER_DEFAULT;
		}
	}

	@Override
	public <S extends SearchArguments<? extends Instance>> boolean isSupported(Class<?> target, S arguments) {
		return (arguments.getStringQuery() != null || arguments.getQuery() != null)
				&& EqualsHelper.nullSafeEquals(SearchDialects.SOLR, arguments.getDialect(), true);
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		try {
			SolrQuery parameters = buildSolrQuery(arguments);

			QueryResponse queryResponse = solrConnector.queryWithPost(parameters);

			parseResponse(arguments, queryResponse);
		} catch (Exception e) {
			// TODO: implement proper exception handling
			// to prevent NPE when the result is not set for list value
			arguments.setResult(CollectionUtils.emptyList());
			LOGGER.error("Error during execution of solr query: " + e.getMessage(), e);
		}
	}

	@Override
	public <S extends SearchArguments<? extends Instance>> Stream<ResultItem> stream(S arguments) {
		throw new UnsupportedOperationException("Solr engine does not support result streaming right now");
	}

	private <S extends SearchArguments<E>, E extends Instance> void parseResponse(S arguments,
			QueryResponse queryResponse) {
		if (queryResponse != null) {
			SolrDocumentList solrDocumentList = queryResponse.getResults();

			// update the total count
			arguments.setTotalItems(Long.valueOf(solrDocumentList.getNumFound()).intValue());

			// we only care for the count no need to create all results
			if (arguments.isCountOnly()) {
				arguments.setResult(Collections.emptyList());
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

			// Facets
			if (arguments.isFaceted()) {
				facetResultTransformer.extractFacetsFromResponse(arguments, queryResponse);
			}
		}
	}

	/**
	 * @return the created instance or <code>null</code> if elements has missing data
	 */
	// entry set is not implemented by solrDocument
	@SuppressWarnings("findbugs:WMI_WRONG_MAP_ITERATOR")
	private <E extends Instance> E parseResponseItem(SolrDocument nextDocument) {
		Map<String, Object> fieldValueMap = nextDocument.getFieldValueMap();
		String instanceId = (String) fieldValueMap.get(DefaultProperties.URI);
		if (instanceId == null) {
			LOGGER.warn("Invalid object id for: {}", fieldValueMap);
			return null;
		}

		Class<? extends Instance> typeClass = getInstanceClass(fieldValueMap, instanceId);
		if (typeClass == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		E instance = (E) ReflectionUtils.newInstance(typeClass);
		instance.setId(namespaceRegistryService.getShortUri(instanceId));

		Set<String> keys = fieldValueMap.keySet();
		// clean the duplicate and not actual properties
		sanitizeProperties(keys);
		if (instance.getProperties() == null) {
			instance.setProperties(CollectionUtils.createHashMap(keys.size()));
		}
		setInstanceVariables(instance, keys, fieldValueMap);
		readProperties(nextDocument, instance, keys);
		return instance;
	}

	private static <E extends Instance> void readProperties(SolrDocument nextDocument, E instance, Set<String> keys) {
		for (String keyName : keys) {
			// entry set is not implemented by solrDocument
			Serializable value = readValue(nextDocument.getFieldValue(keyName));
			instance.addIfNotNull(keyName, value);
		}
	}

	private static Serializable readValue(Object object) {
		Object value = object;
		if (object instanceof Collection) {
			Collection<?> collection = (Collection<?>) object;
			if (collection.isEmpty()) {
				// does not return empty collections
				return null;
			}
			if (collection.size() == 1) {
				// if single value get it
				value = collection.iterator().next();
			}
		}
		if (value instanceof Serializable) {
			return (Serializable) value;
		}
		return null;
	}

	private Class<? extends Instance> getInstanceClass(Map<String, Object> fieldValueMap, String instanceId) {
		String instanceType = (String) fieldValueMap.get(DefaultProperties.INSTANCE_TYPE);
		if (instanceType == null) {
			LOGGER.warn("Type not found for entity with id: {}", instanceId);
			return null;
		}
		Class<? extends Instance> typeClass = getType(instanceType);
		if (typeClass == null || !Instance.class.isAssignableFrom(typeClass)) {
			LOGGER.warn("Invalid type {} for id {}", instanceType, instanceId);
			return null;
		}
		return typeClass;
	}

	/**
	 * Builds the solr query from the given arguments
	 */
	private <E extends Instance, S extends SearchArguments<E>> SolrQuery buildSolrQuery(S arguments) throws Exception {
		SolrQuery query = new SolrQuery();

		if (arguments.getStringQuery() != null) {
			parseQuery(query, arguments.getStringQuery());
		} else {
			buildQuery(query, arguments.getQuery());
		}
		setSorter(arguments, query);

		setQueryProjection(arguments, query);

		setFilterQuery(arguments, query);

		setSkip(arguments, query);

		setRows(arguments, query);

		int queryTimeout = arguments.getQueryTimeout(TimeUnit.MILLISECONDS);
		if (queryTimeout > 0) {
			query.setTimeAllowed(queryTimeout);
			// good idea to set some default timeout
		}

		buildFacetQuery(query, arguments);
		return query;
	}

	/**
	 * Sets the query projection.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param query
	 *            the query
	 */
	protected <E, S extends SearchArguments<E>> void setQueryProjection(S arguments, SolrQuery query) {
		query.set(CommonParams.FL, arguments.getProjection() == null
				? searchConfiguration.getDashletsRequestFields().get() : arguments.getProjection());
	}

	/**
	 * Sets the sorter.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param query
	 *            the query
	 */
	protected static <E, S extends SearchArguments<E>> void setSorter(S arguments, SolrQuery query) {
		arguments.getSorters().forEach(sorter -> query.addSort(sorter.getSortField(),
				arguments.getFirstSorter().isAscendingOrder() ? ORDER.asc : ORDER.desc));
	}

	/**
	 * Sets the filter query.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param query
	 *            the query
	 */
	protected static <E, S extends SearchArguments<E>> void setFilterQuery(S arguments, SolrQuery query) {
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
	}

	/**
	 * Sets the rows.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param query
	 *            the query
	 */
	protected static <E, S extends SearchArguments<E>> void setRows(S arguments, SolrQuery query) {
		int returnCount = arguments.isCountOnly() ? 1 : arguments.getPageSize();
		if (returnCount > 0) {
			query.setRows(returnCount);
		} else {
			query.setRows(0);
		}
	}

	/**
	 * Sets the skip.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param query
	 *            the query
	 */
	protected static <E, S extends SearchArguments<E>> void setSkip(S arguments, SolrQuery query) {
		int skipSize = arguments.getSkipCount();
		if (skipSize <= 0) {
			skipSize = Math.max(0, Math.max(arguments.getPageSize(), 0) * (arguments.getPageNumber() - 1));
		}
		if (skipSize > 0) {
			query.setStart(skipSize);
		}
	}

	/**
	 * Build a query.
	 *
	 * @param parameters
	 *            query params
	 * @param query
	 *            Query
	 */
	protected static void buildQuery(SolrQuery parameters, Query query) {
		SolrQueryVistor solrQueryVistor = new SolrQueryVistor();
		try {
			query.getRoot().visit(solrQueryVistor);
		} catch (Exception e) {
			LOGGER.error("Error during dynamic query build in solr: " + e.getMessage(), e);
			throw new EmfRuntimeException(e);
		}
		parameters.setQuery(solrQueryVistor.getQuery().toString());
	}

	/**
	 * Enables faceting and adds facet fields & arguments if any.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param query
	 *            current solr parameters
	 * @param arguments
	 *            search arguments
	 */
	private <E extends Instance, S extends SearchArguments<E>> void buildFacetQuery(SolrQuery query, S arguments) {
		Map<String, Serializable> facetArguments = arguments.getFacetArguments();
		if (arguments.isFaceted() || CollectionUtils.isNotEmpty(facetArguments)) {

			if (arguments.isFaceted()) {
				facetSolrHelper.addDefaultFacetParameters(query);
			}

			// Transfers any additional arguments from selecting facets for
			// example,
			// to the main query.
			facetSolrHelper.assignFacetArgumentsToSolrQuery(arguments, query);

			Map<String, Facet> facets = arguments.getFacets();
			if (!CollectionUtils.isNotEmpty(facets)) {
				return;
			}

			// TODO: Allocate some size..
			StringBuilder facetQuery = new StringBuilder();
			boolean appendAnd = false;

			for (Facet facet : facets.values()) {

				if (TDATES.equals(facet.getSolrType())) {
					query.add(FacetParams.FACET_DATE, facet.getSolrFieldName());
				} else {
					query.add(FacetParams.FACET_FIELD, facet.getSolrFieldName());
				}

				Set<String> selectedValues = facet.getSelectedValues();
				if (CollectionUtils.isNotEmpty(selectedValues)) {
					boolean hasEmptyFacetValue = selectedValues.remove(FacetQueryParameters.NO_VALUE);

					// TODO: Avoid this new builder?
					StringBuilder valuesQuery = new StringBuilder();
					if (!selectedValues.isEmpty()) {
						if (appendAnd) {
							facetQuery.append(" AND ");
						} else {
							appendAnd = true;
						}

						if (TDATES.equals(facet.getSolrType())) {
							facetSolrHelper.generateSolrDateQuery(valuesQuery, facet);
						} else {
							facetSolrHelper.generateSolrTextQuery(valuesQuery, facet);
						}
					}

					if (hasEmptyFacetValue) {
						if (appendAnd) {
							facetQuery.append(" AND ");
						} else {
							appendAnd = true;
						}

						if (valuesQuery.length() > 0) {
							facetQuery
									.append("(" + valuesQuery.toString() + " OR -" + facet.getSolrFieldName() + ":*)");
						} else {
							facetQuery.append("-" + facet.getSolrFieldName() + ":*");
						}
						selectedValues.add(FacetQueryParameters.NO_VALUE);
					} else {
						facetQuery.append(valuesQuery);
					}
				}
			}

			if (facetQuery.length() > 0) {
				query.addFilterQuery(facetQuery.toString());
			}
		}
	}

	private static void setInstanceVariables(Instance instance, Set<String> keys, Map<String, Object> fieldValueMap) {
		if (instance instanceof Purposable && keys.contains(DefaultProperties.PURPOSE)) {
			((Purposable) instance).setPurpose((String) fieldValueMap.get(DefaultProperties.PURPOSE));
			keys.remove(DefaultProperties.PURPOSE);
		}
	}

	/**
	 * Parses the single string query in manner to enrich the solr parameters with some nested subqueries in the main
	 * query.
	 *
	 * @param parameters
	 *            are the current solr params
	 * @param stringQuery
	 *            is the single query to be parsed.
	 * @throws Exception
	 *             on any error during parse
	 */
	private static void parseQuery(SolrQuery parameters, String stringQuery) throws Exception {
		// some default query - instead of throwing exception from Solr
		String q = SolrQueryConstants.QUERY_DEFAULT_ALL;

		if (StringUtils.isNotBlank(stringQuery)) {
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
	 * Clean up properties that should not be in properties map of instance.
	 *
	 * @param set
	 *            the key set to clean up
	 */
	private static void sanitizeProperties(Set<String> set) {
		// remove the duplicate
		// TODO might be from config
		set.remove(DefaultProperties.URI);
		set.remove(DefaultProperties.INSTANCE_TYPE);
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
		return typeMapping.computeIfAbsent(typeName, name -> {
			Class<? extends Instance> result = null;
			DataTypeDefinition definition = definitionService.getDataTypeDefinition(name);
			if (definition != null) {
				result = (Class<? extends Instance>) definition.getJavaClass();
			}
			return result;
		});
	}

	@Override
	public boolean prepareSearchArguments(SearchRequest request, SearchArguments<Instance> searchArgs) {
		if (!isApplicable(request)) {
			return false;
		}

		if (request.getFirst(SearchQueryParameters.QUERY_FROM_DEFINITION) != null
				&& request.getFirst(FacetQueryParameters.REQUEST_FACET_ARGUMENTS) != null) {
			// XXX: Implement method prepareFacetArguments(request, searchArgs)
			// if later solr search is used in Case/Task filter page and set
			// rdf:type and context
		}

		if (request.getFirst(SearchQueryParameters.FILTER_BY_PERMISSION) != null) {
			searchArgs.getArguments().put(SearchQueryParameters.CURRENT_USER,
					request.getFirst(SearchQueryParameters.USER_URI));
			searchArgs.setPermissionsType(QueryResultPermissionFilter.WRITE);
		}

		searchArgs.setQuery(Query.getEmpty());
		searchArgs.setDialect(SearchDialects.SOLR);
		searchArgs.setProjection(basicQueryDF);

		if (searchArgs.getStringQuery() == null) {
			searchArgs.setStringQuery(parseQueryArguments(request, searchArgs).toString());
		}
		// fix order by data
		if (request.getRequest().get(SearchQueryParameters.ORDER_BY) != null) {
			List<String> orderByUpdated = request
					.getRequest()
						.remove(SearchQueryParameters.ORDER_BY)
						.stream()
						.map(orderByValue -> orderByValue.substring(orderByValue.indexOf(':') + 1))
						.collect(Collectors.toList());
			request.getRequest().put(SearchQueryParameters.ORDER_BY, orderByUpdated);
		}
		searchArgs.setMaxSize(searchArgs.getPageSize());
		if (searchArgs.getSorters().isEmpty()) {
			searchArgs.setOrdered(true);
			searchArgs.addSorter(new Sorter(DefaultProperties.MODIFIED_ON, Sorter.SORT_DESCENDING));
		}
		return true;
	}

	/**
	 * Parses the query arguments. All the supported query arguments are contacted in a single query. If no arguments
	 * are provided an empty {@link SolrQueryConstants#QUERY_DEFAULT_EMPTY} is returned. <br>
	 * FQ argument is also set in the arguments map, since there might have valid instances for the query, but currently
	 * are supported only that contains compact_header
	 *
	 * @param searchRequest
	 *            the search request
	 * @param searchArgs
	 *            the search args to update
	 * @return the string containg the single processed query
	 */
	private StringBuilder parseQueryArguments(SearchRequest searchRequest, SearchArguments<Instance> searchArgs) {
		// TODO do we need to configure this set of solr properties?
		StringBuilder resultQuery = new StringBuilder();
		List<String> location = searchRequest.get(SearchQueryParameters.LOCATION);
		if (location != null && !location.isEmpty()) {
			resultQuery.append("partOfRelation:").append(joinArgumentList(location, " OR ", true, true));
		}
		String metaText = searchRequest.getFirst(SearchQueryParameters.META_TEXT);
		if (StringUtils.isNotBlank(metaText)) {
			startNextClause(resultQuery);
			resultQuery.append("(" + parser.prepare(metaText) + ")");
		}

		appendTypesQuery(searchRequest, resultQuery);

		String mimeType = searchRequest.getFirst(SearchQueryParameters.MIMETYPE);
		if (StringUtils.isNotBlank(mimeType)) {
			startNextClause(resultQuery);
			// replace the regex otherwise consider it is full mimetype id
			if (mimeType.indexOf('^') == 0) {
				mimeType = "*" + mimeType.substring(1) + "*";
			}
			resultQuery.append("mimetype:\"").append(mimeType).append("\"");
		}

		String identifier = searchRequest.getFirst(SearchQueryParameters.IDENTIFIER);
		if (StringUtils.isNotBlank(identifier)) {
			startNextClause(resultQuery);
			resultQuery.append("identifier:\"").append(namespaceRegistryService.buildFullUri(identifier)).append("\"");
		}

		// create the range query
		try {
			appendDateRangeQuery(searchRequest, resultQuery);
		} catch (ParseException e) {
			throw new EmfRuntimeException(
					"Failed to create date range query arguments for date format: " + dateConverter.getDateFormat(), e);
		}

		List<String> createdByList = searchRequest.get("createdBy[]");
		if (createdByList != null && !createdByList.isEmpty()) {
			startNextClause(resultQuery);
			resultQuery.append("createdBy:").append(joinArgumentList(createdByList, " OR ", true, true));
		}
		if (resultQuery.length() == 0) {
			resultQuery.append(SolrQueryConstants.QUERY_DEFAULT_ALL);
		}

		String fqBasicSearch = searchConfiguration.getFullTextSearchFilterQuery().get();
		String existingFilterQuery = searchRequest.getFirst(CommonParams.FQ);
		if (StringUtils.isNotBlank(existingFilterQuery)) {
			fqBasicSearch += " AND " + existingFilterQuery;
		}

		searchArgs.getArguments().put(CommonParams.FQ, fqBasicSearch);
		return resultQuery;
	}

	private void appendTypesQuery(SearchRequest searchRequest, StringBuilder resultQuery) {
		Set<String> rdfTypes = new HashSet<>();
		List<String> objectTypes = searchRequest.get(SearchQueryParameters.OBJECT_TYPE);
		if (objectTypes != null && !objectTypes.isEmpty()) {
			rdfTypes.addAll(objectTypes);
		}

		List<String> subTypeArg = searchRequest.get(SearchQueryParameters.SUB_TYPE);
		Set<String> subTypes = new HashSet<>(subTypeArg.size());
		if (!subTypeArg.isEmpty()) {
			for (String subTypeString : subTypeArg) {
				// check the subType if it is URI
				if (subTypeString.indexOf(':') > 0) {
					rdfTypes.add(subTypeString);
				} else {
					subTypes.add(subTypeString);
				}
			}
		}
		boolean typesClausePresent = !(subTypes.isEmpty() && rdfTypes.isEmpty());
		if (typesClausePresent) {
			startNextClause(resultQuery);
			resultQuery.append("( ");
		}
		if (!subTypes.isEmpty()) {
			resultQuery.append("type:").append(joinArgumentList(new ArrayList<>(subTypes), " OR ", false, true));
		}
		// when there is an URI in the subType list
		if (!rdfTypes.isEmpty()) {
			if (!subTypes.isEmpty()) {
				resultQuery.append(" OR ");
			}
			resultQuery.append("rdfType:").append(joinArgumentList(new ArrayList<>(rdfTypes), " OR ", true, true));
		}
		if (typesClausePresent) {
			resultQuery.append(" )");
		}
	}

	private void appendDateRangeQuery(SearchRequest searchRequest, StringBuilder resultQuery) throws ParseException {
		String createdFrom = searchRequest.getFirst(SearchQueryParameters.CREATED_FROM_DATE);
		StringBuilder createdOnRange = null;
		if (StringUtils.isNotBlank(createdFrom)) {
			Date start = dateConverter.parseDate(createdFrom);
			Calendar calendar = Calendar.getInstance();
			// solr works only with UTC format - use TZ query param to fix the
			// offset
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			calendar.setTime(start);
			createdOnRange = new StringBuilder("[");
			String formated = ISO8601DateFormat.format(calendar);
			createdOnRange.append(formated);
		}
		String createdTo = searchRequest.getFirst(SearchQueryParameters.CREATED_TO_DATE);
		if (StringUtils.isNotBlank(createdTo)) {
			if (createdOnRange != null) {
				createdOnRange.append(" TO ");
			} else {
				createdOnRange = new StringBuilder("[* TO ");
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateConverter.parseDate(createdTo));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			long paramTime = calendar.getTimeInMillis();
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			calendar.setTimeInMillis(paramTime);
			String formated = ISO8601DateFormat.format(calendar);
			createdOnRange.append(formated).append("]");
		} else {
			if (createdOnRange != null) {
				createdOnRange.append(" TO *]");
			}
		}

		if (createdOnRange != null) {
			startNextClause(resultQuery);
			resultQuery.append("createdOn:").append(createdOnRange);
		}
	}

	/**
	 * Start next clause. Check whether to put AND clause in the query
	 *
	 * @param resultQuery
	 *            the result query to update
	 */
	private static void startNextClause(StringBuilder resultQuery) {
		if (resultQuery.length() > 0) {
			resultQuery.append(" AND ");
		}
	}

	private static boolean isApplicable(SearchRequest request) {
		if (request == null || request.getRequest() == null) {
			return false;
		}
		return SearchDialects.SOLR.equals(request.getDialect());
	}

	/**
	 * Join argument list and process each argument - make the namespace in full format and surround the whole argument
	 * in "".
	 *
	 * @param params
	 *            the params to join
	 * @param joinClause
	 *            the join clause to concat elements with
	 * @param updateNamespace
	 *            the update namespace if the argument list contains semantic namespace prefixes
	 * @param makePhrase
	 *            to surround each argument with ""
	 * @return the join arguments surrounded in ( )
	 */
	private StringBuilder joinArgumentList(List<String> params, String joinClause, boolean updateNamespace,
			boolean makePhrase) {
		StringBuilder result = new StringBuilder("( ");
		int size = params.size();
		for (int i = 0; i < size; i++) {
			if (makePhrase) {
				result.append("\"");
			}
			String nextArg = params.get(i);
			if (updateNamespace) {
				result.append(namespaceRegistryService.buildFullUri(nextArg));
			} else {
				result.append(nextArg);
			}
			if (makePhrase) {
				result.append("\"");
			}
			if (i + 1 < size) {
				result.append(joinClause);
			}
		}
		result.append(" )");
		return result;
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		if (SearchDialects.SOLR.equals(dialect)) {
			return ClientUtils::escapeQueryChars;
		}
		return null;
	}
}
