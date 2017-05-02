package com.sirma.itt.emf.semantic.search;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.APPLY_INSTANCES_BY_TYPE_FILTER_FLAG;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONNECTOR_NAME_CONSTANT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.COUNT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURRENT_USER;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.EMF_TYPE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FACET_PREFIX;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.HAS_PERMISSION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.NO_VALUE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_TYPE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.PERMISSIONS_BLOCK_CONSTANT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.QUERY_EXISTING_BODY;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RDF_TYPE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.SELECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.SORT_VARIABLE_SUFFIX;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.wrapQueryWithGroupBy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.TupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchEngine;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Search engine for semantic database.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchEngine.TARGET_NAME, order = 20)
public class SemanticSearchEngine implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticSearchEngine.class);

	private static final String TASK = "Task";
	private static final String CASE = "Case";
	private static final String CONTEXT = "context";

	private static final String ARGUMENT_MIMETYPE = EMF.PREFIX + ":" + DefaultProperties.MIMETYPE;
	private static final String ARGUMENT_IDENTIFIER = DCTERMS.PREFIX + ":" + SearchQueryParameters.IDENTIFIER;
	private static final String ARGUMENT_CREATED_ON = EMF.PREFIX + ":" + DefaultProperties.CREATED_ON;
	private static final String ARGUMENT_CREATED_BY = EMF.PREFIX + ":" + DefaultProperties.CREATED_BY;
	private static final String ARGUMENT_FTS = "fts";
	private static final String ARGUMENT_FQ = "fq";
	private static final String ARGUMENT_RELATIONS = "relations";

	@Inject
	private DictionaryService dictionaryService;

	private final Map<String, Class<? extends Instance>> typeMapping = new HashMap<>(32);

	/**
	 * The connection will be forced to get new on every call
	 */
	@Inject
	private javax.enterprise.inject.Instance<TransactionalRepositoryConnection> connection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private FTSQueryParser parser;

	@Inject
	private DateConverter dateConverter;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private Statistics statistics;

	@Inject
	private SemanticConfiguration semanticConfiguration;

	@Inject
	private SemanticSearchConfigurations configurations;

	@Inject
	private SearchQueryBuilder searchQueryBuilder;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	/**
	 * Initializes typeMapping for OWL classes that aren't part of the EMF project
	 */
	@PostConstruct
	public void init() {
		typeMapping.put(EMF.CLASS_DESCRIPTION.stringValue(), ClassInstance.class);
		typeMapping.put(OWL.DATATYPEPROPERTY.stringValue(), CommonInstance.class);
		typeMapping.put(EMF.RELATION.stringValue(), CommonInstance.class);
		typeMapping.put(Security.ROLE.stringValue(), CommonInstance.class);
		typeMapping.put(SKOS.CONCEPT.stringValue(), CommonInstance.class);
		SPARQLQueryHelper.setCaseInsenitiveOrderByList(configurations.getListOfCaseInsensitiveProperties().get());

		configurations.getListOfCaseInsensitiveProperties().addConfigurationChangeListener(
				c -> SPARQLQueryHelper.setCaseInsenitiveOrderByList(c.get()));
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> boolean isSupported(Class<?> target, S arguments) {
		return SearchDialects.SPARQL.equals(arguments.getDialect()) || Instance.class.isAssignableFrom(target);
	}

	@Override
	@SuppressWarnings("boxing")
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		printSearchArguments(arguments);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "semanticSearch").begin();

		Map<String, Serializable> bindings = new HashMap<>();
		final int maxSize = arguments.getMaxSize();

		String query = prepareQuery(arguments, bindings, calculateOffset(arguments), maxSize);

		if (arguments.shouldGroupBy()) {
			aggregatedSearch(arguments, bindings, query);
			LOGGER.debug("Semantic DB aggregation took {} seconds for {} property/properties", tracker.stopInSeconds(),
					arguments.getAggregatedData().size());
		} else {
			search(arguments, bindings, query);
			LOGGER.debug("Semantic DB search took {} seconds and returning {} entries out of {} in total",
					tracker.stopInSeconds(), arguments.getResult().size(), arguments.getTotalItems());
		}
	}

	<E extends Instance, S extends SearchArguments<E>> void aggregatedSearch(S arguments,
			Map<String, Serializable> bindings, String preparedQuery) {
		if (arguments.getGroupBy() != null) {
			arguments.setAggregated(CollectionUtils.createHashMap(arguments.getGroupBy().size()));
			for (String groupByProperty : arguments.getGroupBy()) {
				if (org.apache.commons.lang3.StringUtils.isNotBlank(groupByProperty)) {
					String wrappedQuery = wrapQueryWithGroupBy(preparedQuery, groupByProperty,
							semanticDefinitionService.getRelation(groupByProperty) != null);
					executeTupleQuery(arguments, wrappedQuery, bindings, groupByProperty);
				}
			}
		}
		arguments.setResult(CollectionUtils.emptyList());
	}

	<E extends Instance, S extends SearchArguments<E>> void search(S arguments, Map<String, Serializable> bindings,
			String preparedQuery) {
		LOGGER.trace("Query for execution: {}", preparedQuery);
		List<E> queryResult = executeTupleQuery(arguments, preparedQuery, bindings);
		collectAllInstanceIds(arguments, queryResult);

		int offset = arguments.getSkipCount();
		int toIndex = queryResult.size();
		final int pageSize = arguments.getPageSize();
		if (arguments.getMaxSize() > 0 && pageSize > 0) {
			toIndex = Math.min(offset + pageSize, queryResult.size());
		}

		if (pageSize << 2 < queryResult.size()) {
			// if items are much more (4 times) than the requested we will copy the list so we can free the memory now
			arguments.setResult(new ArrayList<>(queryResult.subList(offset, toIndex)));
			queryResult.clear();
		} else {
			arguments.setResult(queryResult.subList(offset, toIndex));
		}
	}

	private <S extends SearchArguments<E>, E extends Instance> void collectAllInstanceIds(S arguments,
			List<E> queryResult) {
		if (arguments.shouldReturnAllUries()) {
			// Get result URIs for external faceting
			Set<Serializable> ids = CollectionUtils.createLinkedHashSet(queryResult.size());
			for (E instance : queryResult) {
				ids.add(namespaceRegistryService.buildFullUri(instance.getId().toString()));
			}
			arguments.setUries(ids);
		}
	}

	static <E extends Instance, S extends SearchArguments<E>> void printSearchArguments(S arguments) {
		StringBuilder printMessage = new StringBuilder("Executing query ");
		if (StringUtils.isNotNullOrEmpty(arguments.getQueryName())) {
			printMessage.append("Name:").append(arguments.getQueryName()).append(" ");
		}
		printMessage.append("isFaceted:").append(arguments.isFaceted()).append(" ");
		printMessage.append("PageNumber:").append(arguments.getPageNumber()).append(" ");
		printMessage.append("PermissionsType:").append(arguments.getPermissionsType()).append(" ");
		printMessage.append(printMap("Arguments: ", arguments.getArguments()));
		printMessage.append(printMap("Configurations: ", arguments.getQueryConfigurations()));

		Sorter sorter = arguments.getFirstSorter();
		if (sorter != null) {
			printMessage.append("Order by:").append(sorter.getSortField()).append(" ").append(
					sorter.isAscendingOrder() ? Sorter.SORT_ASCENDING : Sorter.SORT_DESCENDING);
		}
		LOGGER.trace(printMessage.toString());
	}

	private static String printMap(String header, Map<String, Serializable> searchArguments) {
		StringBuilder printMessage = new StringBuilder(header);
		if (!searchArguments.isEmpty()) {
			searchArguments.forEach((key, value) -> printMessage.append(key).append(":").append(value).append(" "));
			return printMessage.toString();
		}
		return "";
	}

	/**
	 * Prepares SPARQL query
	 *
	 * @param arguments
	 *            Search arguments
	 * @param bindings
	 *            Binding parameters
	 * @param offset
	 *            Skip count
	 * @param limit
	 *            Results limit count
	 * @return built SPARQL query that is ready for execution
	 */
	<E extends Instance, S extends SearchArguments<E>> String prepareQuery(S arguments,
			Map<String, Serializable> bindings, int offset, int limit) {

		String query = arguments.getStringQuery();
		Map<String, Serializable> argumentsMap = arguments.getArguments();
		boolean skipVisitingArguments = false;
		Map<String, Serializable> queryArguments = CollectionUtils.createHashMap(argumentsMap.size());

		if (query != null) {
			if (!query.toUpperCase().startsWith(SELECT)) {
				queryArguments.put(QUERY_EXISTING_BODY, query);
			} else {
				skipVisitingArguments = true;
				bindings.putAll(argumentsMap);
			}
		}

		if (!skipVisitingArguments) {
			Map<String, Serializable> facetValues = readFacetValues(arguments);
			queryArguments.putAll(argumentsMap);
			queryArguments.putAll(facetValues);
			queryArguments.putAll(mergeDates(argumentsMap, facetValues));
			Query searchQuery = Query.getEmpty();
			searchQuery.and(Query.fromMap(queryArguments, QueryBoost.INCLUDE_AND));

			SemanticQueryVisitor visitor = prepareQueryVisitor(arguments);
			try {
				searchQuery.visit(visitor);
			} catch (Exception e) {
				LOGGER.error("Error parsing the search query", e);
			}

			query = visitor.getQuery().toString();
			bindings.putAll(visitor.getBindings());
		}

		query = setSolrConnectorName(query);
		query = appendPermissionsToQuery(arguments, query);
		query = SPARQLQueryHelper.appendOrderByToQuery(query, arguments.getSorters(),
				configurations.getSortResultsInGdb().get().booleanValue());
		query = SPARQLQueryHelper.appendOffsetToQuery(query, offset);
		query = SPARQLQueryHelper.appendLimitToQuery(query, limit);

		return query;
	}

	/**
	 * Merge the dates from the search form and the facets, e.g. when a created on date is selected both in the search
	 * form and the created on facet.
	 *
	 * @param arguments
	 *            the search arguments
	 * @param matchAgainst
	 *            the facet search arguments
	 * @return the merged dates from both maps
	 */
	private static Map<String, Serializable> mergeDates(Map<String, Serializable> arguments,
			Map<String, Serializable> matchAgainst) {
		Map<String, Serializable> mergedDates = new HashMap<>(arguments.size());
		for (Entry<String, Serializable> entry : arguments.entrySet()) {
			Serializable value = matchAgainst.get(entry.getKey());

			if (value != null && value instanceof List) {
				List<?> valueList = (List<?>) value;
				if (valueList.get(0) != null && valueList.get(0) instanceof DateRange) {
					DateRange facetDateRange = (DateRange) valueList.get(0);
					DateRange basicSearchDateRange = (DateRange) entry.getValue();

					mergedDates.put(entry.getKey(), mergeRanges(basicSearchDateRange, facetDateRange));
				}
			}
		}
		return mergedDates;
	}

	/**
	 * Merge two ranges into one by getting the latest start and earliest end dates.
	 *
	 * @param firstRange
	 *            the first range
	 * @param secondRange
	 *            the second range
	 * @return a new range containing the latest start and earliest end dates.
	 */
	private static DateRange mergeRanges(DateRange firstRange, DateRange secondRange) {
		Date firstDate;
		Date secondDate;
		if (firstRange.getFirst() == null
				|| secondRange.getFirst() != null && secondRange.getFirst().after(firstRange.getFirst())) {
			firstDate = secondRange.getFirst();
		} else {
			firstDate = firstRange.getFirst();
		}
		if (firstRange.getSecond() == null
				|| secondRange.getSecond() != null && secondRange.getSecond().before(firstRange.getSecond())) {
			secondDate = secondRange.getSecond();
		} else {
			secondDate = firstRange.getSecond();
		}
		return new DateRange(firstDate, secondDate);
	}

	/**
	 * Replaces connector name constant with the real name of the connector that is provided by the configurations. The
	 * query must contain the connector block and only the name to be replaced
	 *
	 * @param query
	 *            Builded query
	 * @return Query with replaced connector name
	 */
	private String setSolrConnectorName(String query) {
		String localQuery = query;
		if (localQuery.contains(CONNECTOR_NAME_CONSTANT)) {
			String connectorName = semanticConfiguration.getFtsIndexName().get();
			localQuery = localQuery.replace(CONNECTOR_NAME_CONSTANT, connectorName);
		}
		return localQuery;
	}

	/**
	 * Builds SemanticQueryVisitor and initializes it
	 *
	 * @param arguments
	 *            Search arguments
	 * @return Initialized SemanticQueryVisitor
	 */
	private <S extends SearchArguments<?>> SemanticQueryVisitor prepareQueryVisitor(S arguments) {
		SemanticQueryVisitor visitor = new SemanticQueryVisitor();
		visitor.setMaxResultLimit(arguments.getMaxSize());

		Serializable applyTypeFilter = arguments.getQueryConfigurations().get(APPLY_INSTANCES_BY_TYPE_FILTER_FLAG);
		boolean applyTypeFilterFlag = true;
		if (applyTypeFilter instanceof Boolean) {
			applyTypeFilterFlag = ((Boolean) applyTypeFilter).booleanValue();
		}
		visitor.setApplyFilterForType(applyTypeFilterFlag);
		visitor.setIgnoreInstancesForType(configurations.getIgnoreInstanceTypes().get());
		visitor.setFTSParser(parser);
		visitor.setProjection(arguments.getProjection());
		return visitor;
	}

	/**
	 * Gets the actual type by name.
	 *
	 * @param typeName
	 *            the type name
	 * @return the type
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Instance> getType(String typeName) {
		String nameOfType = typeName;
		if (nameOfType != null && !nameOfType.startsWith("http")
				&& nameOfType.contains(NamespaceRegistryService.SHORT_URI_DELIMITER)) {
			// build to full URI if needed
			nameOfType = namespaceRegistryService.buildFullUri(nameOfType);
		}

		return typeMapping.computeIfAbsent(nameOfType, type -> {
			DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(type);
			if (definition != null) {
				return (Class<? extends Instance>) definition.getJavaClass();
			}
			return null;
		});
	}

	/**
	 * Evaluate Tuple query - returns a map of property and value
	 *
	 * @param arguments
	 *            The search arguments
	 * @param queryString
	 *            The SPARQL Query as String
	 * @param bindings
	 *            Query variables values to be replaced in the Query
	 * @return Map of properties and their values
	 */
	<E extends Instance, S extends SearchArguments<E>> List<E> executeTupleQuery(S arguments, String queryString,
			Map<String, Serializable> bindings) {
		return executeTupleQuery(arguments, queryString, bindings, null);
	}

	@SuppressWarnings("boxing")
	<E extends Instance, S extends SearchArguments<E>> List<E> executeTupleQuery(S arguments, String queryString,
			Map<String, Serializable> bindings, String groupBy) {
		boolean includeInferred = getIncludeInferredParameter(arguments);
		try (TransactionalRepositoryConnection repositoryConnection = connection.get()) {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, queryString, bindings,
					includeInferred, arguments.getQueryTimeout(TimeUnit.SECONDS));

			// evaluate query
			TimeTracker tracker = statistics.createTimeStatistics(getClass(), "semanticSearchQueryExecution").begin();
			try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
				LOGGER.debug("Semantic query execution took {} ms", tracker.stop());

				tracker = statistics.createTimeStatistics(getClass(), "semanticSearchProcessingResults").begin();
				List<E> resultList = parseTupleQueryResult(resultIterator, arguments, groupBy);
				LOGGER.debug("Processing results took {} ms", tracker.stop());
				return resultList;
			}
		} catch (QueryInterruptedException e) {
			LOGGER.error("Semantic query execution exceeded the allowed execution time of {} s. The error is: {}",
					arguments.getQueryTimeout(TimeUnit.SECONDS), e.getMessage(), e);
			return Collections.emptyList();
		} catch (QueryEvaluationException e) {
			LOGGER.warn("Error while executing query: {}\n{}\nParameters: {}", e.getMessage(), queryString, bindings,
					e);
			return Collections.emptyList();
		} catch (OpenRDFException e) {
			LOGGER.error("Invalid search query:\n{}\nParameters: {}", queryString, bindings, e);
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Parses the tuple query result and converts the values from native type to types that can be used from the system.
	 *
	 * @param result
	 *            Query result
	 * @param arguments
	 *            The search arguments
	 * @param groupBy
	 *            The property for which data is aggregated
	 * @return The result of the query and all the values converted to types that can be used from the system
	 * @throws QueryEvaluationException
	 *             If an error occurs
	 */
	<E extends Instance, S extends SearchArguments<E>> List<E> parseTupleQueryResult(TupleQueryResultIterator result,
			S arguments, String groupBy) throws QueryEvaluationException {
		if (!result.hasNext()) {
			arguments.setTotalItems(0);
			return Collections.emptyList();
		}

		if (arguments.isCountOnly()) {
			// try to get the count from a count query if such
			Serializable serializable = convertValue(result.next().getValue(COUNT));
			if (serializable instanceof Number) {
				arguments.setTotalItems(Integer.parseInt(serializable.toString()));
				return Collections.emptyList();
			}
		}

		// the context wrapping here is necessary because when executing in
		// parallel the security
		// context will be lost otherwise
		boolean isParallel = configurations.getProcessResultsInParallel().get().booleanValue();

		Function<? super BindingSet, E> converter;
		if (arguments.shouldGroupBy() && groupBy != null) {
			arguments.getAggregatedData().put(groupBy, CollectionUtils.createHashMap(result.getCount()));
			converter = tuple -> processAggregatedResult(arguments.getAggregatedData().get(groupBy), tuple);
		} else {
			converter = tuple -> processResult(tuple);
		}

		Stream<E> processingStream = result
				.stream(isParallel)
					.map(securityContextManager.wrap().function(converter))
					.filter(Objects::nonNull);

		if (!configurations.getSortResultsInGdb().get().booleanValue()) {
			// append sorter when the results are not sorted in the DB
			Sorter sorter = arguments.getFirstSorter();
			if (sorter != null) {
				processingStream = processingStream.sorted(createPropertyComparator(sorter));
			}
		}

		List<E> results;
		try (Stream<E> stream = processingStream) {
			results = stream.collect(Collectors.toCollection(LinkedList::new));
		}
		arguments.setTotalItems(results.size());
		return results;
	}

	private <E extends Instance> E processAggregatedResult(Map<String, Serializable> mapping, BindingSet tuple) {
		Value nameValue = tuple.getValue("name");
		if (nameValue != null) {
			Serializable count = convertValue(tuple.getValue("count"));
			if (nameValue instanceof URI) {
				String shortUri = namespaceRegistryService.getShortUri((URI) nameValue);
				mapping.put(shortUri, count);
			} else {
				mapping.put(nameValue.stringValue(), count);
			}
		}
		return null;
	}

	<E extends Instance> E processResult(BindingSet tuple) {
		Serializable instanceType = convertValue(tuple.getValue(OBJECT_TYPE));
		Class<? extends Instance> instanceClass = getType((String) instanceType);
		if (instanceClass == null) {
			LOGGER.warn("Invalid type definition: {}", instanceType);
		} else {
			Instance instance = ReflectionUtils.newInstance(instanceClass);
			instance.setId(convertValue(tuple.getValue(OBJECT)));

			Set<String> bindingNames = tuple.getBindingNames();
			Map<String, Serializable> properties = CollectionUtils.createHashMap(bindingNames.size() + 1);

			bindingNames.forEach(name -> {
				Serializable convertedValue = convertValue(tuple.getValue(name));
				// extract the sorting field in different element of the map so
				// it can be used by
				// the comparator this is needed because the sorting field
				// variable name is generated
				if (CollectionUtils.addNonNullValue(properties, name, convertedValue)
						&& name.contains(SORT_VARIABLE_SUFFIX)) {
					properties.put(SORT_VARIABLE_SUFFIX, convertedValue);
				}
			});
			instance.setProperties(properties);
			return (E) instance;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	static <E extends Instance> Comparator<E> createPropertyComparator(Sorter sorter) {
		String tempSortField = sorter.getSortField();
		if (StringUtils.isNullOrEmpty(tempSortField)) {
			return (m1, m2) -> 0;
		}

		return (m1, m2) -> {
			Serializable field1 = m1.get(SORT_VARIABLE_SUFFIX);
			Serializable field2 = m2.get(SORT_VARIABLE_SUFFIX);

			int comparison = 0;

			// String comparison should be case INsensitive
			if (field1 instanceof String && field2 instanceof String) {
				comparison = ((String) field1).compareToIgnoreCase((String) field2);
			}
			// if one of the fields is null or not comparable or they are not
			// the same type it should return 0
			else if (field1 instanceof Comparable && field2 instanceof Comparable
					&& field1.getClass().isInstance(field2)) {
				comparison = ((Comparable<Object>) field1).compareTo(field2);
			}

			return sorter.isAscendingOrder() ? comparison : -comparison;
		};
	}

	/**
	 * Check if query is applicable for semantic.
	 *
	 * @param searchRequest
	 *            the search request
	 * @return true if so
	 */
	private static boolean isApplicable(SearchRequest searchRequest) {
		if (searchRequest == null || searchRequest.getRequest() == null) {
			return false;
		}

		if (!SearchDialects.SPARQL.equals(searchRequest.getDialect())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean prepareSearchArguments(SearchRequest request, SearchArguments<Instance> searchArgs) {
		if (!isApplicable(request)) {
			return false;
		}
		searchArgs.setDialect(SearchDialects.SPARQL);
		searchArgs.setQuery(Query.getEmpty());

		// When property from filter by facet panel is clicked from filters in
		// Task/Case page we have to keep type and context
		if (request.getFirst(SearchQueryParameters.QUERY_FROM_DEFINITION) != null
				&& request.getFirst(FacetQueryParameters.REQUEST_FACET_ARGUMENTS) != null) {
			prepareFacetArguments(request, searchArgs);
		}

		if (request.getFirst(SearchQueryParameters.FILTER_BY_PERMISSION) != null) {
			searchArgs.setPermissionsType(QueryResultPermissionFilter.WRITE);
		}

		String queryText = request.getFirst(SearchQueryParameters.QUERY_TEXT);
		Condition searchTree = request.getSearchTree();
		if (StringUtils.isNotNullOrEmpty(queryText)) {
			searchArgs.setStringQuery(queryText);
		} else if (searchTree != null) {
			searchArgs.setCondition(searchTree);
			searchArgs.setStringQuery(searchQueryBuilder.build(searchTree));
		}
		readQueryArguments(request, searchArgs);

		readSearchParameters(request, searchArgs);

		List<String> groupBy = request.get(SearchQueryParameters.GROUP_BY);
		if (!groupBy.isEmpty()) {
			searchArgs.setShouldGroupBy(true);
			searchArgs.setGroupBy(groupBy);

			List<String> ids = request.get(SearchQueryParameters.SELECTED_OBJECTS);
			if (CollectionUtils.isNotEmpty(ids)) {
				Rule unionRule = new Rule();
				unionRule.setOperation("in");
				unionRule.setField("instanceId");
				unionRule.setValues(ids);
				unionRule.setType("object");

				searchTree = new Condition();
				searchTree.setRules(Arrays.asList(unionRule));
				searchArgs.setCondition(searchTree);
				searchArgs.setStringQuery(searchQueryBuilder.build(searchTree));
			}
		}

		return true;
	}

	private static void readSearchParameters(SearchRequest request, SearchArguments<Instance> searchArgs) {
		if (searchArgs.getSorters().isEmpty()) {
			String orderBy = EMF.PREFIX + URI_SEPARATOR + EMF.MODIFIED_ON.getLocalName();
			searchArgs.setOrdered(true);
			searchArgs.addSorter(new Sorter(orderBy, Sorter.SORT_DESCENDING));
		}
	}

	/**
	 * Prepare the search arguments needed for correct faceting when property is selected from filters in Case/Task
	 *
	 * @param request
	 *            - the initial search request
	 * @param searchArgs
	 *            - search arguments
	 */
	private static void prepareFacetArguments(SearchRequest request, SearchArguments<Instance> searchArgs) {
		List<String> forTypeList = new ArrayList<>();
		if (TASK.equals(request.getFirst(SearchQueryParameters.INSTANCE_TYPE))) {
			forTypeList.add(EMF.TASK.stringValue());
			forTypeList.add(EMF.BUSINESS_PROCESS_TASK.stringValue());
		}

		if (CASE.equals(request.getFirst(SearchQueryParameters.INSTANCE_TYPE))) {
			forTypeList.add(EMF.CASE.stringValue());
		}

		searchArgs.getArguments().put(DefaultProperties.SEMANTIC_TYPE, (Serializable) forTypeList);
		List<String> forInstanceList = Arrays.asList(request.getFirst(SearchQueryParameters.INSTANCE_ID));
		searchArgs.getArguments().put(CONTEXT, (Serializable) forInstanceList);
		searchArgs.setStringQuery(null);
	}

	/**
	 * Read query arguments.
	 *
	 * @param request
	 *            the request
	 * @param searchArgs
	 *            the search args
	 */
	private void readQueryArguments(SearchRequest request, SearchArguments<Instance> searchArgs) {
		setNonRepeatingElements(request.get(SearchQueryParameters.LOCATION), CONTEXT, searchArgs);
		setNonRepeatingElements(request.get(SearchQueryParameters.OBJECT_RELATIONSHIP), ARGUMENT_RELATIONS, searchArgs);
		readTypes(request, searchArgs);

		readStringArgument(request, searchArgs, SearchQueryParameters.META_TEXT, ARGUMENT_FTS);

		readStringArgument(request, searchArgs, SearchQueryParameters.FQ, ARGUMENT_FQ);

		readStringArgument(request, searchArgs, SearchQueryParameters.MIMETYPE, ARGUMENT_MIMETYPE);

		readStringArgument(request, searchArgs, SearchQueryParameters.IDENTIFIER, ARGUMENT_IDENTIFIER);

		readDateRange(request, searchArgs);

		setNonRepeatingElements(request.get(SearchQueryParameters.CREATED_BY), ARGUMENT_CREATED_BY, searchArgs);
	}

	private static void readTypes(SearchRequest request, SearchArguments<Instance> searchArgs) {
		List<String> subType = request.get(SearchQueryParameters.SUB_TYPE);
		List<String> objectTypes = request.get(SearchQueryParameters.OBJECT_TYPE);

		Set<String> types = new HashSet<>(subType.size() + objectTypes.size());
		types.addAll(subType);
		types.addAll(objectTypes);

		if (types.isEmpty()) {
			return;
		}
		Set<String> subTypes = new HashSet<>();
		Set<String> rdfTypes = new HashSet<>();
		for (String typeString : types) {
			// check the subType if it is URI
			if (typeString.contains(":")) {
				rdfTypes.add(typeString);
			} else {
				subTypes.add(typeString);
			}
		}
		if (!subTypes.isEmpty()) {
			searchArgs.getArguments().put(EMF_TYPE, new ArrayList<>(subTypes));
		}
		// when there is an URI in the subType list
		if (!rdfTypes.isEmpty()) {
			searchArgs.getArguments().put(DefaultProperties.SEMANTIC_TYPE, new ArrayList<>(rdfTypes));
		}
	}

	/**
	 * Reads string argument from the input request and sets the argument in the SearchArguments
	 *
	 * @param request
	 *            Input request with search parameters
	 * @param searchArgs
	 *            Search arguments that will be executed by the service
	 * @param inputParameterName
	 *            Name of the parameter in the input request
	 * @param argumentName
	 *            Name of the argument in the Search Arguments map
	 */
	private static void readStringArgument(SearchRequest request, SearchArguments<Instance> searchArgs,
			String inputParameterName, String argumentName) {
		String stringValue = request.getFirst(inputParameterName);
		if (StringUtils.isNotNullOrEmpty(stringValue)) {
			searchArgs.getArguments().put(argumentName, stringValue);
		}
	}

	/**
	 * Sets the non repeating elements.
	 *
	 * @param <E>
	 *            the element type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param searchArgs
	 *            the search args
	 */
	private static <E> void setNonRepeatingElements(Collection<E> source, String target,
			SearchArguments<Instance> searchArgs) {
		if (source != null && !source.isEmpty()) {
			// remove duplicate values
			searchArgs.getArguments().put(target, new ArrayList<>(new HashSet<>(source)));
		}
	}

	/**
	 * Read date range.
	 *
	 * @param request
	 *            the request
	 * @param searchArgs
	 *            the search args
	 */
	private void readDateRange(SearchRequest request, SearchArguments<Instance> searchArgs) {
		Date start = null;
		Date end = null;
		String createdFrom = request.getFirst(SearchQueryParameters.CREATED_FROM_DATE);
		if (StringUtils.isNotNullOrEmpty(createdFrom)) {
			start = dateConverter.parseDate(createdFrom);
		}
		String createdTo = request.getFirst(SearchQueryParameters.CREATED_TO_DATE);
		if (StringUtils.isNotNullOrEmpty(createdTo)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateConverter.parseDate(createdTo));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			end = calendar.getTime();
		}

		if (start != null || end != null) {
			searchArgs.getArguments().put(ARGUMENT_CREATED_ON, new DateRange(start, end));
		}
	}

	/**
	 * Extracts the facet arguments from the search request and maps them to the corresponding facets in the search
	 * arguments.
	 *
	 * @param request
	 *            - the search request
	 * @param searchArgs
	 *            - the search arguments
	 */
	static <E extends Instance, S extends SearchArguments<E>> Map<String, Serializable> readFacetValues(S searchArgs) {
		List<Facet> facets = searchArgs.getFacetsWithSelectedValues();
		if (facets == null) {
			return CollectionUtils.EMPTY_MAP;
		}

		Map<String, Serializable> arguments = CollectionUtils.createHashMap(10);
		for (Facet facet : facets) {
			if (facet.getSelectedValues() == null) {
				continue;
			}
			readFacetValue(facet, arguments);
		}

		return arguments;
	}

	private static void readFacetValue(Facet facet, Map<String, Serializable> arguments) {
		Set<String> selectedValues = facet.getSelectedValues();
		String uri = facet.getUri();
		final List<Serializable> serializableValue = new ArrayList<>(selectedValues.size());
		if (RDF_TYPE.equals(uri) || EMF_TYPE.equals(uri)) {
			serializableValue.addAll(selectedValues);
			uri = FACET_PREFIX + uri;
		} else if ("dateTime".equals(facet.getRangeClass()) || "date".equals(facet.getRangeClass())) {
			serializableValue.addAll(readDateTimeFacetes(facet));
		} else if ("boolean".equals(facet.getRangeClass())) {
			facet.getSelectedValues().forEach(selectedValue -> serializableValue.add(Boolean.valueOf(selectedValue)));
		} else if (selectedValues.contains(NO_VALUE)) {
			arguments.put("-" + uri, NO_VALUE);
			if (selectedValues.size() != 1) {
				serializableValue.addAll(selectedValues);
				serializableValue.remove(NO_VALUE);
			}
		} else {
			serializableValue.addAll(selectedValues);
		}

		if (!serializableValue.isEmpty()) {
			if (!arguments.containsKey(uri)) {
				arguments.put(uri, (Serializable) serializableValue);
			} else {
				List<Serializable> values = (List<Serializable>) arguments.get(uri);
				values.addAll(serializableValue);
			}
		}
	}

	private static List<Serializable> readDateTimeFacetes(Facet facet) {
		List<Serializable> dateRanges = new ArrayList<>();
		for (String selectedValue : facet.getSelectedValues()) {
			String[] dates = selectedValue.split(";");
			Date start = null;
			Date end = null;
			if (!"*".equals(dates[0])) {
				DateTime startDate = new DateTime(dates[0], DateTimeZone.UTC);
				start = startDate.toDate();
			}
			if (!"*".equals(dates[1])) {
				DateTime endDate = new DateTime(dates[1], DateTimeZone.UTC);
				end = endDate.toDate();
			}
			dateRanges.add(new DateRange(start, end));
		}
		return dateRanges;
	}

	/**
	 * Appends permissions to a query if the user isn't Administrator. If the user is Administrator then skip the
	 * permissions clause
	 *
	 * @param arguments
	 *            Search arguments
	 * @param query
	 *            SPARQL query
	 * @return the query after permissions are applied
	 */
	private String appendPermissionsToQuery(SearchArguments<?> arguments, String query) {
		if (query.contains(HAS_PERMISSION) || !arguments.shouldApplyPermissions()) {
			return query;
		}

		String queryWithPermissions = query;
		QueryResultPermissionFilter permissionsType = arguments.getPermissionsType();

		if (queryWithPermissions.contains(PERMISSIONS_BLOCK_CONSTANT)) {
			while (queryWithPermissions.contains(PERMISSIONS_BLOCK_CONSTANT)) {
				queryWithPermissions = insertPermissionBlock(queryWithPermissions, PERMISSIONS_BLOCK_CONSTANT,
						permissionsType);
			}
		} else {
			queryWithPermissions = insertPermissionBlock(queryWithPermissions, CURLY_BRACKET_CLOSE, permissionsType);
		}

		return queryWithPermissions;
	}

	private String insertPermissionBlock(String query, String blockToReplace,
			QueryResultPermissionFilter permissionsType) {
		String instanceVariableName = OBJECT;
		int indexOfPermissionBlock;
		int blockToReplaceLength;
		Matcher matcher = SPARQLQueryHelper.PERMISSIONS_PATTERN.matcher(query);

		if (matcher.find()) {
			String matchedString = matcher.group();
			indexOfPermissionBlock = matcher.start();
			blockToReplaceLength = matchedString.length();

			int indexOfVariableName = matchedString.lastIndexOf('$') + 1;
			if (matchedString.length() > indexOfVariableName) {
				instanceVariableName = matchedString.substring(indexOfVariableName);
			}
		} else {
			indexOfPermissionBlock = query.lastIndexOf(blockToReplace);
			blockToReplaceLength = blockToReplace.length() > 1 ? blockToReplace.length() : 0;
		}

		if (indexOfPermissionBlock == -1) {
			LOGGER.error("Cannot insert permissions block in the query! {}", query);
			return query;
		}
		String beforeBlockEnd = query.substring(0, indexOfPermissionBlock).trim();
		String afterBlockEnd = query.substring(indexOfPermissionBlock + blockToReplaceLength);

		StringBuilder queryString = new StringBuilder(beforeBlockEnd);
		queryString.append(LINE_SEPARATOR);

		boolean isAdminOrSystemUser = authorityService.isAdminOrSystemUser();
		String permissionsFilter = SPARQLQueryHelper.getPermissionsFilter(instanceVariableName, isAdminOrSystemUser,
				QueryResultPermissionFilter.WRITE != permissionsType);

		if (!isAdminOrSystemUser) {
			permissionsFilter = permissionsFilter.replaceAll("\\" + VARIABLE + CURRENT_USER,
					securityContext.getEffectiveAuthentication().getSystemId().toString());
		}
		queryString.append(permissionsFilter).append(afterBlockEnd);

		return queryString.toString();
	}

	/**
	 * Provides include inferred parameter from the arguments or returns the default value - TRUE
	 *
	 * @param arguments
	 *            Search arguments
	 * @return Parameter include inferred
	 */
	static <E extends Instance, S extends SearchArguments<E>> boolean getIncludeInferredParameter(S arguments) {
		Serializable includeInferredParameter = arguments
				.getQueryConfigurations()
					.remove(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION);
		boolean includeInferred = true;
		if (includeInferredParameter != null) {
			includeInferred = Boolean.parseBoolean(includeInferredParameter.toString());
		}
		return includeInferred;
	}

	/**
	 * Converts Semantic Value to Serializable object
	 *
	 * @param value
	 *            Value from the semantics
	 * @return Serializable object or 'null' if the value is 'null'
	 */
	private Serializable convertValue(Value value) {
		if (value == null) {
			return null;
		} else if (value instanceof URI) {
			URI uri = (URI) value;

			if (XMLSchema.NAMESPACE.equals(uri.getNamespace())) {
				return uri.getLocalName();
			}
			return namespaceRegistryService.getShortUri(uri);
		} else {
			return ValueConverter.convertValue(value);
		}
	}

	/**
	 * Calculates the given offset before execution of the SPARQL query. If the calculated result size is larger than
	 * the configured maximum size then the offset is calculated to be az the maximum size If the query is facet then
	 * the offset is always 0 because we facet only the results to to the configured maximum size
	 *
	 * @param arguments
	 *            The search arguments that contain maximum size and page size
	 * @return Results skip count
	 */
	static <E extends Instance, S extends SearchArguments<E>> int calculateOffset(S arguments) {
		if (arguments.isFaceted()) {
			arguments.setSkipCount(0);
			return 0;
		}

		final int maxSize = arguments.getMaxSize();
		final int pageSize = arguments.getPageSize();
		int offset = 0;
		if (maxSize > 0) {
			offset = (arguments.getPageNumber() - 1) * pageSize;
			if (offset >= maxSize) {
				arguments.setSkipCount(offset - maxSize * (offset / maxSize));
				offset = maxSize * (offset / maxSize);
			} else {
				arguments.setSkipCount(offset);
				offset = 0;
			}
		}

		return offset;
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		if (SearchDialects.SPARQL.equals(dialect)) {
			// add proper escaping here
			return s -> s;
		}
		return null;
	}
}
