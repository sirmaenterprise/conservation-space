package com.sirma.itt.emf.semantic.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchEngine;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.semantic.SemanticSearchService;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.query.QueryParser;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;
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

	/** The service proxy. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The type mapping. */
	private final Map<String, Class<? extends Instance>> typeMapping = new HashMap<>();

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private SemanticSearchService searchService;

	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private QueryParser queryParser;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	@Config(name = SemanticConfigurationProperties.FULL_TEXT_SEARCH_INDEX_NAME, defaultValue = "luc:ftsearch")
	private String ftsIndexName;

	@Inject
	@Config(name = SemanticConfigurationProperties.SEARCH_CASE_INSENITIVE_ORDERBY_LIST, defaultValue = "dcterms:title")
	private String listOfCaseInsensitiveProperties;

	@Inject
	private FTSQueryParser parser;

	/**
	 * Initializes typeMapping for OWL classes that aren`t part of the EMF project
	 */
	@PostConstruct
	public void init() {
		typeMapping.put(OWL.CLASS.stringValue(), CommonInstance.class);
		typeMapping.put(OWL.DATATYPEPROPERTY.stringValue(), CommonInstance.class);
		typeMapping.put(OWL.OBJECTPROPERTY.stringValue(), CommonInstance.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Instance, S extends SearchArguments<E>> boolean isSupported(Class<?> target,
			S arguments) {
		// the search should be called with Instance.class as target
		// TODO: add more specific checks.. like to check something in the arguments or so
		return SearchDialects.SPARQL.equals(arguments.getDialect())
				|| Instance.class.isAssignableFrom(target) || arguments.isSparqlQuery();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target,
			S arguments) {

		TimeTracker timeTracker = new TimeTracker().begin();

		Serializable includeInferred = arguments.getArguments().remove("includeInferred");
		boolean includeInferredBoolean = true;
		if (includeInferred != null) {
			includeInferredBoolean = Boolean.valueOf(includeInferred.toString());
		}

		List<E> result;
		int queryReturnSize = 0;
		if (!arguments.isSparqlQuery()) {

			// perform the search
			List<Pair<Class<? extends Instance>, Serializable>> list = performSearch(arguments,
					includeInferredBoolean);
			queryReturnSize = list.size();
			result = (List<E>) BatchEntityLoader.load(list, serviceRegister, taskExecutor);

		} else {
			LOGGER.info("Executing prepared query: [{}]", arguments.getQueryName());

			String query = arguments.getStringQuery();

			if (!query.contains("LIMIT") && (arguments.getPageSize() > 0)) {
				query = query + " LIMIT " + arguments.getPageSize();
			}

			List<Map<String, Serializable>> queryResult = performSparqlQuery(query,
					arguments.getArguments(), includeInferredBoolean, arguments.getQueryTimeout());

			if (arguments.isCountOnly()) {
				if (!queryResult.isEmpty()) {
					// try to get the count from a count query if such
					Serializable serializable = queryResult.get(0).get("count");
					if (serializable instanceof Number) {
						arguments.setTotalItems(Integer.parseInt(serializable.toString()));
						return;
					}
				}
			}

			queryReturnSize = queryResult.size();
			arguments.setTotalItems(queryReturnSize);
			result = new ArrayList<E>(queryResult.size());
			for (Map<String, Serializable> entity : queryResult) {

				String type = (String) entity.get(SemanticQueryVisitor.QUERY_OBJECT_TYPE);
				Instance instance = null;

				Class<?> instanceType = getType(type);
				instance = (Instance) ReflectionUtils.newInstance(instanceType);
				instance.setId(entity.get(SemanticQueryVisitor.QUERY_OBJECT));

				instance.setProperties(new HashMap<>(entity));
				result.add((E) instance);
			}
		}
		// update the result
		arguments.setResult(result);
		LOGGER.debug(
				"Semantic DB search took {} s and returning {} entries out of {} and {} in total",
				timeTracker.stopInSeconds(), result.size(), queryReturnSize,
				arguments.getTotalItems());
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
		if ((typeName != null) && !typeName.startsWith("http")
				&& typeName.contains(NamespaceRegistryService.SHORT_URI_DELIMITER)) {
			// build to full URI if needed
			typeName = namespaceRegistryService.buildFullUri(typeName);
		}

		// use localized mapping for high speed
		Class<? extends Instance> result = typeMapping.get(typeName);
		if (result == null) {
			DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(typeName);
			if (definition != null) {
				result = typeConverter.convert(Class.class, definition.getJavaClassName());
			} else {
				result = CommonInstance.class;
			}
			typeMapping.put(typeName, result);
		}
		return result;
	}

	/**
	 * Perform the actual search in the semantic database.
	 *
	 * @param arguments
	 *            the arguments
	 * @param includeInferred
	 *            Boolean flag if the search must include the inferred statements
	 * @return the list
	 */
	protected List<Pair<Class<? extends Instance>, Serializable>> performSearch(
			SearchArguments<?> arguments, boolean includeInferred) {

		String queryString = arguments.getStringQuery();
		String query;
		String countQuery = "";
		Map<String, Serializable> bindings;

		if (queryString != null) {
			// SparqlQuery sparqlQuery = queryParser.parseQuery(queryString);
			//
			// query = sparqlQuery.buildQuery();
			// bindings = sparqlQuery.getBindings();

			query = queryString;
			bindings = new HashMap<>();

			countQuery = SemanticQueryVisitor.QUERY_COUNT_QUERY_START + query + "LIMIT "
					+ arguments.getMaxSize() + " " + SemanticQueryVisitor.QUERY_BLOCK_END;

		} else {
			Map<String, Serializable> argumentsMap = arguments.getArguments();

			Query searchQuery = Query.getEmpty();
			searchQuery.and(Query.fromMap(argumentsMap, QueryBoost.INCLUDE_AND));

			SemanticQueryVisitor visitor = new SemanticQueryVisitor();
			visitor.setSorter(arguments.getSorter());
			visitor.setMaxResultLimit(arguments.getMaxSize());
			visitor.setFullTextsearchIndex(ftsIndexName);
			visitor.setFTSParser(parser);
			visitor.setCaseInsenitiveOrderByList(listOfCaseInsensitiveProperties);
			try {
				searchQuery.visit(visitor);
			} catch (Exception e) {
				LOGGER.error("Error parsing the search query", e);
			}

			query = visitor.getQuery().toString();
			bindings = visitor.getBindings();
			countQuery = visitor.getCountQuery();
			LOGGER.debug("Query with bindings: {}", visitor.getQueryWithBindings());
		}

		query = query + "LIMIT " + arguments.getPageSize();

		int offset = (arguments.getPageNumber() - 1) * arguments.getPageSize();

		if (offset > 0) {
			query = query + " OFFSET " + offset;
		}

		LOGGER.debug("Query: {}", query);

		Integer resultCount = null;

		TimeTracker tracker = TimeTracker.createAndStart();
		// perform count query
		List<Map<String, Serializable>> queryResult = performSparqlQuery(countQuery, bindings,
				includeInferred, arguments.getQueryTimeout());
		if (!queryResult.isEmpty()) {
			Map<String, Serializable> resultMap = queryResult.get(0);
			Serializable count = resultMap.get(SemanticQueryVisitor.QUERY_COUNT);
			if (count != null) {
				resultCount = Integer.parseInt(count.toString());
				LOGGER.info("Search result count: {} and took {} s to compute", resultCount,
						tracker.stopInSeconds());
				arguments.setTotalItems(resultCount.intValue());
			}
		}

		// if we have any results and we are not interested only in the count we execute the query
		if ((resultCount != null) && (resultCount != 0) && !arguments.isCountOnly()) {
			// perform real query
			queryResult = performSparqlQuery(query, bindings, includeInferred,
					arguments.getQueryTimeout());
		} else {
			queryResult = Collections.emptyList();
		}

		Map<String, Pair<Class<? extends Instance>, Serializable>> results = new LinkedHashMap<>(
				queryResult.size());

		for (Map<String, Serializable> resultMap : queryResult) {
			String instanceId = (String) resultMap.get(SemanticQueryVisitor.QUERY_OBJECT);
			String instanceType = (String) resultMap.get(SemanticQueryVisitor.QUERY_OBJECT_TYPE);

			Class<? extends Instance> instanceClass = getType(instanceType);
			if (instanceClass == null) {
				LOGGER.warn("Invalid type definition: {}", instanceType);
				continue;
			}

			if (!results.containsKey(instanceId)) {
				results.put(instanceId, new Pair<Class<? extends Instance>, Serializable>(
						instanceClass, instanceId));
			}
		}

		return new ArrayList<Pair<Class<? extends Instance>, Serializable>>(results.values());
	}

	/**
	 * Executes a string query
	 *
	 * @param query
	 *            The query
	 * @param arguments
	 *            The arguments
	 * @param includeInferred
	 *            Flag that shows if the query should include inferred statements
	 * @param queryTimeout
	 *            the query timeout to use
	 * @return List of instances
	 */
	protected List<Map<String, Serializable>> performSparqlQuery(String query,
			Map<String, Serializable> arguments, boolean includeInferred, int queryTimeout) {

		Map<String, Value> bindings = new HashMap<>();
		for (Entry<String, Serializable> entry : arguments.entrySet()) {
			Value value = createValue(entry.getValue());
			if (value != null) {
				bindings.put(entry.getKey(), value);
			}
		}

		List<Map<String, Value>> queryResult = searchService.executeTupleQuery(query, bindings,
				includeInferred, queryTimeout);

		List<Map<String, Serializable>> result = new ArrayList<>(queryResult.size());
		for (Map<String, Value> entity : queryResult) {

			Map<String, Serializable> properties = CollectionUtils.createHashMap(entity.size());
			for (Entry<String, Value> entry : entity.entrySet()) {
				properties.put(entry.getKey(), convertValue(entry.getValue()));
			}

			result.add(properties);
		}

		return result;
	}

	/**
	 * Creates literal {@link Value} by given {@link Serializable} value.
	 *
	 * @param value
	 *            the value
	 * @return the value or <code>null</code> if the value type is not recognized
	 */
	private Value createValue(Serializable value) {
		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.contains(":") && !stringValue.contains("\\:")) {
				if (stringValue.startsWith("http://")) {
					return valueFactory.createURI(stringValue);
				}
				return valueFactory.createURI(namespaceRegistryService.buildFullUri(stringValue));
			}
			if (stringValue.contains("\\:")) {
				stringValue = stringValue.replace("\\:", ":");
			}
			return valueFactory.createLiteral(stringValue);
		}
		return ValueConverter.createLiteral(value);
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
}
