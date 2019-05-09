package com.sirma.itt.emf.semantic.search;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_END;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CLOSE_BRACKET;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONNECTOR_NAME_CONSTANT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONTEXT_PREDICATE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.DEFAULT_PROJECTION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.EMF_TYPE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FACET_PREFIX;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FILTER_BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.IS_NOT_DELETED;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.NOT_BOUND_START_CLAUSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_TYPE_VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OPEN_BRACKET;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OPTIONAL_BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.QUERY_EXISTING_BODY;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RDF_TYPE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RELATIONS_PREDICATE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RELATION_RDF_TYPE_BLOCK;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RELATION_SOURCE_BLOCK;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.START_COUNT_QUERY;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.START_QUERY;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.STATEMENT_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.STR_FUNCTION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VALUES;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.WHERE;
import static com.sirma.itt.seip.collections.CollectionUtils.createLinkedHashSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.AbstractQueryVistor;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.domain.search.tree.CriteriaWildcards;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * @author kirq4e
 */
public class SemanticQueryVisitor extends AbstractQueryVistor {

	public static final String OR = QueryBoost.INCLUDE_OR.toString();
	public static final String OR_LOWER_CASE = OR.toLowerCase();
	public static final String AND = QueryBoost.INCLUDE_AND.toString();
	public static final String AND_LOWER_CASE = AND.toLowerCase();

	public static final String CONNECTOR_QUERY_PREDICATE = "connectorQuery";

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticQueryVisitor.class);

	private Map<String, Serializable> bindings = new HashMap<>();
	private StringBuilder whereClause;

	private final List<StringBuilder> filterClauseList = new ArrayList<>();
	private final List<StringBuilder> filterOrClauseList = new ArrayList<>();
	private String projection;

	/**
	 * The same query as the main but with select for count
	 */
	private StringBuilder countQuery;

	private long maxResultLimit = 1000L;
	/** The FTS parser that preprocess the term query. */
	private FTSQueryParser parser;

	private boolean useSimpleRelationSearch = true;

	private final Map<String, Pair<QueryBoost, Serializable>> pendingPredicates = new HashMap<>(7);
	private Set<String> listOfPendingPredicates;
	private String ignoreInstancesForTypeClause;
	private boolean applyFilterForType = true;
	private Map<String, Serializable> unprocessedParameters = CollectionUtils.createHashMap(5);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		listOfPendingPredicates = createLinkedHashSet(8);
		Collections.addAll(listOfPendingPredicates, CONTEXT_PREDICATE, RELATIONS_PREDICATE, RDF_TYPE, EMF_TYPE,
				QUERY_EXISTING_BODY, FACET_PREFIX + RDF_TYPE, FACET_PREFIX + EMF_TYPE, OBJECT);

		builder.append(START_QUERY).append(DEFAULT_PROJECTION);
		if (StringUtils.isNotBlank(projection)) {
			builder.append(projection);
		}
		builder.append(LINE_SEPARATOR).append(WHERE).append(BLOCK_START).append(LINE_SEPARATOR);

		whereClause = new StringBuilder(1024);
		appendPredicateDeclaration(EMF.PREFIX + URI_SEPARATOR + EMF.INSTANCE_TYPE.getLocalName(), OBJECT_TYPE_VARIABLE);
		whereClause.append(OBJECT_VARIABLE).append(IS_NOT_DELETED).append(LINE_SEPARATOR);
		if (StringUtils.isNotBlank(ignoreInstancesForTypeClause) && applyFilterForType) {
			whereClause.append(ignoreInstancesForTypeClause);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Query query) throws Exception {
		String predicate = query.getKey();
		Serializable value = query.getValue();

		StringBuilder filterClause = new StringBuilder(256);

		if (listOfPendingPredicates.contains(predicate) || predicate.startsWith(FACET_PREFIX)
				|| predicate.startsWith("-")) {
			// we will process these at the end of the query building when we
			// now all parameters
			pendingPredicates.put(predicate, new Pair<>(query.getBoost(), value));
		} else if (value instanceof String) {
			buildStringClause(predicate, value, filterClause, query.getBoost());
		} else if (value instanceof DateRange) {
			buildDateRangeClause(predicate, value, filterClause);
		} else if (value instanceof Boolean) {
			buildBooleanClause(predicate, value, filterClause);
		} else if (value instanceof List) {
			List<Serializable> listValue = (List<Serializable>) value;
			buildListFilter(OBJECT_VARIABLE, predicate, listValue, filterClause);
		} else {
			unprocessedParameters.put(predicate, value);
		}

		storeClause(query.getBoost(), filterClause);
	}

	private void buildStringClause(String predicate, Serializable value, StringBuilder filterClause,
			QueryBoost queryBoost) {
		String stringValue = (String) value;
		if ("fts".equals(predicate)) {
			if (stringValue.contains(":")) {
				stringValue = stringValue.replace(":", "\\:");
			}

			stringValue = parser.prepare(stringValue);
			appendConnectorQuery(stringValue, queryBoost);
		} else if ("fq".equals(predicate)) {
			appendConnectorQuery(stringValue, queryBoost);
		} else {
			buildStringSearchClause(predicate, value, filterClause);
		}
	}

	private void appendConnectorQuery(String value, QueryBoost queryBoost) {
		Pair<QueryBoost, Serializable> connectorQueryPair = pendingPredicates.get(CONNECTOR_QUERY_PREDICATE);
		if (connectorQueryPair == null) {
			connectorQueryPair = new Pair<>(queryBoost, "");
			pendingPredicates.put(CONNECTOR_QUERY_PREDICATE, connectorQueryPair);
		}

		StringBuilder stringValue = new StringBuilder((String) connectorQueryPair.getSecond());
		if (stringValue.length() > 0) {
			stringValue.append(queryBoost.toString());
		}
		stringValue.append(OPEN_BRACKET).append(value).append(CLOSE_BRACKET);
		connectorQueryPair.setSecond(stringValue.toString());
	}

	/**
	 * Store clause.
	 *
	 * @param boost
	 *            the boost
	 * @param filterClause
	 *            the filter clause
	 */
	private void storeClause(QueryBoost boost, StringBuilder filterClause) {
		if (filterClause.length() == 0) {
			return;
		}
		if (boost == QueryBoost.INCLUDE_AND) {
			filterClauseList.add(filterClause);
		} else if (boost == QueryBoost.INCLUDE_OR) {
			filterOrClauseList.add(filterClause);
		}
	}

	/**
	 * Builds the type filter clause.
	 *
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @param clause
	 *            the clause
	 */
	@SuppressWarnings("unchecked")
	private void buildTypeFilterClause(String predicate, Serializable value, StringBuilder clause) {
		// build the current clause
		if (value instanceof String) {
			buildStringSearchClause(predicate, value, clause);
		} else {
			buildListFilter(OBJECT_VARIABLE, predicate, (List<Serializable>) value, clause);
		}
	}

	/**
	 * Builds the full text search clause.
	 */
	private void addConnectorQuery() {
		Pair<QueryBoost, Serializable> connectorQueryPair = pendingPredicates.remove(CONNECTOR_QUERY_PREDICATE);
		if (connectorQueryPair == null) {
			return;
		}

		String stringValue = (String) connectorQueryPair.getSecond();
		if (StringUtils.isBlank(stringValue)) {
			return;
		}

		if (stringValue.toLowerCase().contains(OR_LOWER_CASE)) {
			stringValue = replaceIgnoreCase(stringValue, OR_LOWER_CASE, OR);
		}

		if (stringValue.toLowerCase().contains(AND_LOWER_CASE)) {
			stringValue = replaceIgnoreCase(stringValue, AND_LOWER_CASE, AND);
		}

		// solr search
		// ?search a solr:ftsearch
		// solr:query "*:*"
		// solr:entities ?entity .

		stringValue = StringEscapeUtils.escapeJava(stringValue);
		whereClause
		.append("?search a ")
		.append(CONNECTOR_NAME_CONSTANT)
		.append(" ; solr:query ")
		.append("\"")
		.append(stringValue)
		.append("\"")
		.append(" ; solr:entities ")
		.append(OBJECT_VARIABLE)
		.append(STATEMENT_SEPARATOR);
	}

	/**
	 * Builds String search clause.
	 *
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 */
	private void buildStringSearchClause(String predicate, Serializable value, StringBuilder filterClause) {
		String valueVariable = createVariableName(predicate);
		String stringValue = (String) value;

		if (StringUtils.isBlank(stringValue)) {
			return;
		}

		// if the search is by URI
		if (stringValue.contains(":") && !stringValue.contains("\\:")) {
			filterClause
			.append(OBJECT_VARIABLE)
			.append(" ")
			.append(predicate)
			.append(" ")
			.append(VARIABLE)
			.append(valueVariable)
			.append(STATEMENT_SEPARATOR);
		} else {
			// append regex search on the property that isn`t part of the lucene
			// index
			String predicateVariable = createPredicateVariable(predicate);
			appendPredicateDeclaration(predicate, predicateVariable);

			filterClause.append(FILTER_BLOCK_START);
			filterClause.append("REGEX(").append(STR_FUNCTION).append(VARIABLE).append(predicateVariable).append(
					CLOSE_BRACKET);
			filterClause.append(" , ").append(VARIABLE).append(valueVariable).append(" , \"i\"").append(CLOSE_BRACKET);

			filterClause.append(CLOSE_BRACKET).append(STATEMENT_SEPARATOR);
		}
		bindings.put(valueVariable, stringValue);
	}

	/**
	 * Builds the date range clause.
	 *
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 */
	private void buildDateRangeClause(String predicate, Serializable value, StringBuilder filterClause) {
		DateRange dates = (DateRange) value;
		String predicateVariable = createPredicateVariable(predicate);

		appendPredicateDeclaration(predicate, predicateVariable);

		boolean equalDates = false;

		filterClause.append(FILTER_BLOCK_START);

		// FILTER (?createdOn > ?createdOnGtVariable && ?createdOn <
		// ?createdOnLtVariable)
		// add start date
		if (dates.getFirst() != null) {
			equalDates = dates.getFirst().equals(dates.getSecond());
			filterClause.append(VARIABLE).append(predicateVariable);
			if (!equalDates) {
				filterClause.append(" >= ");
			} else {
				filterClause.append(" = ");
			}

			filterClause.append("xsd:dateTime(\"").append(ISO8601DateFormat.format(dates.getFirst())).append("\")");
		}

		if (dates.getFirst() != null && dates.getSecond() != null && !equalDates) {
			filterClause.append(" && ");
		}

		// add end date
		if (dates.getSecond() != null && !equalDates) {
			filterClause
					.append(VARIABLE)
						.append(predicateVariable)
						.append(" <= ")
						.append("xsd:dateTime(\"")
						.append(ISO8601DateFormat.format(dates.getSecond()))
						.append("\")");
		}

		filterClause.append(CLOSE_BRACKET).append(STATEMENT_SEPARATOR);
	}

	private void appendPredicateDeclaration(String predicate, String predicateVariable) {
		appendPredicateDeclaration(predicate, predicateVariable, false);
	}

	private void appendPredicateDeclaration(String predicate, String predicateVariable, boolean optional) {
		StringBuilder predicateClause = new StringBuilder();
		String variable = predicateVariable;
		if (!variable.trim().startsWith(VARIABLE)) {
			variable = VARIABLE + variable;
		}

		if (optional) {
			predicateClause.append(OPTIONAL_BLOCK_START);
		}

		predicateClause.append(OBJECT_VARIABLE).append(" ").append(predicate).append(" ").append(variable).append(
				STATEMENT_SEPARATOR);

		if (optional) {
			predicateClause.append(BLOCK_END);
		}

		if (whereClause.indexOf(predicateClause.toString()) == -1) {
			whereClause.append(predicateClause).append(LINE_SEPARATOR);
		}
	}

	/**
	 * Builds boolean clause
	 *
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 */
	private void buildBooleanClause(String predicate, Serializable value, StringBuilder filterClause) {
		String valueVariable = createVariableName(predicate, (String[]) null);
		filterClause.append(OBJECT_VARIABLE).append(" ").append(predicate).append(" ?").append(valueVariable).append(
				STATEMENT_SEPARATOR);
		bindings.put(valueVariable, value);
	}

	/**
	 * Builds the relations clause.
	 *
	 * @param subject
	 *            the subject
	 * @param value
	 *            the value
	 * @param clause
	 *            the clause
	 */
	@SuppressWarnings("unchecked")
	private void addRelationsClause(String subject, Serializable value, StringBuilder clause) {
		String predicate;

		if (clause.length() == 0) {
			// append relation rdf block
			clause.append(BLOCK_START);
			clause.append(RELATION_RDF_TYPE_BLOCK);
			clause.append("?relation emf:source ").append(subject);
			clause.append(BLOCK_END);
		}

		predicate = "emf:relationType";

		StringBuilder relationTypeFilter = new StringBuilder();
		buildListFilter(VARIABLE + "relation", predicate, (List<Serializable>) value, relationTypeFilter);

		int relationIndexOf = clause.indexOf(RELATION_RDF_TYPE_BLOCK);

		clause.insert(relationIndexOf + RELATION_RDF_TYPE_BLOCK.length(), relationTypeFilter);
	}

	/**
	 * Builds the relations clause.
	 *
	 * @param subject
	 *            the subject
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 * @return the string builder
	 */
	@SuppressWarnings("unchecked")
	private StringBuilder buildSimpleRelationsClause(String subject, Serializable value, StringBuilder filterClause) {
		filterClause.append(buildPredicateListFilter(subject, (List<String>) value));
		return filterClause;
	}

	/**
	 * Builds the context clause.
	 * <p>
	 * <b>NOTE: if you modify this query check the implementation of the method
	 * {@link SemanticQueryVisitor#buildRelationsClause(String, Serializable, StringBuilder)} because it depends heavily
	 * on it's implementation!!! </b>
	 *
	 * @param subject
	 *            the subject
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 */
	@SuppressWarnings("unchecked")
	private void buildSimpleContextClause(String subject, Serializable value, StringBuilder filterClause) {
		String relationPredicate = VARIABLE + createVariableName(CriteriaWildcards.ANY_RELATION);
		buildListFilter(subject, relationPredicate, (List<Serializable>) value, filterClause);
	}

	/**
	 * Builds the context clause.
	 * <p>
	 * <b>NOTE: if you modify this query check the implementation of the method
	 * {@link SemanticQueryVisitor#buildRelationsClause(String, Serializable, StringBuilder)} because it depends heavily
	 * on it's implementation!!! </b>
	 *
	 * @param subject
	 *            the subject
	 * @param value
	 *            the value
	 * @param filterClause
	 *            the filter clause
	 * @param addPartOf
	 *            if should include all sub children clause or not.
	 */
	@SuppressWarnings("unchecked")
	private void buildContextClause(String subject, Serializable value, StringBuilder filterClause, boolean addPartOf) {
		String predicate;
		predicate = "emf:destination";

		if (addPartOf) {
			filterClause.append(BLOCK_START);
		}
		filterClause.append(BLOCK_START);
		filterClause.append(RELATION_RDF_TYPE_BLOCK);
		// revision modification to the query
		filterClause.append(BLOCK_START);
		filterClause.append(RELATION_SOURCE_BLOCK);

		/*
		 * The block creates: <pre> ?relation a emf:Relation . ?relation emf:isActive "true"^^xsd:boolean . { ?relation
		 * emf:source ?instance . ?relation emf:destination ?destinationVariable . } UNION { ?relation emf:source
		 * ?destinationVariable . { ?relation emf:destination ?dest. ?dest emf:actualOf ?instance. }</pre>
		 */
		buildListFilter("?relation", predicate, (List<Serializable>) value, filterClause);
		filterClause.append(BLOCK_END);
		// end of relation modifications

		filterClause.append(BLOCK_END);
		if (addPartOf) {
			filterClause.append(UNION);
			buildListFilter(subject, "ptop:partOf", (List<Serializable>) value, filterClause);
			filterClause.append(BLOCK_END).append(STATEMENT_SEPARATOR);
		}
	}

	/**
	 * Build filter for list of predicates. The clause looks like <code> { { ?instance ?predicate1 ?predicate1Value
	 * } UNION { ?instance ?predicate1? ?predicate2Value } } </code>
	 *
	 * @param subject
	 *            The subject in the filter clause
	 * @param predicates
	 *            the predicates
	 * @return The list filter clause as String
	 */
	private String buildPredicateListFilter(String subject, List<String> predicates) {
		StringBuilder filterClause = new StringBuilder(128);
		// open UNION block
		filterClause.append(BLOCK_START);
		boolean isUnionNeeded = predicates.size() > 1;
		for (int counter = 0; counter < predicates.size(); counter++) {
			String predicate = predicates.get(counter);
			String valueVariable = createVariableName(predicate, "");

			if (isUnionNeeded) {
				filterClause.append(BLOCK_START);
			}

			filterClause
			.append(subject)
			.append(" ")
			.append(predicate)
			.append(" ")
			.append(VARIABLE)
			.append(valueVariable)
			.append(STATEMENT_SEPARATOR);
			filterClause.append(VARIABLE).append(valueVariable).append(IS_NOT_DELETED);

			if (isUnionNeeded) {
				filterClause.append(BLOCK_END);
			}

			if (counter < predicates.size() - 1) {
				filterClause.append(UNION);
			}
		}
		filterClause.append(BLOCK_END);

		return filterClause.toString();
	}

	/**
	 * Build filter for list of values. The clause looks like <code> { { ?instance ?predicate ?value1 }
	 * UNION { ?instance ?predicate ?value2 } } </code>
	 *
	 * @param subject
	 *            The subject in the filter clause
	 * @param predicate
	 *            The predicate in the filter clause
	 * @param valueList
	 *            The list of values - the list can contain URIs or simple string values
	 * @param filterClause
	 * @return The list filter clause as String
	 */
	private void buildListFilter(String subject, String predicate, List<Serializable> valueList,
			StringBuilder filterClause) {
		// open FILTER block
		String filterValueVariable = VARIABLE + createVariableName(predicate, "Main");
		filterClause
		.append(subject)
		.append(" ")
		.append(predicate)
		.append(" ")
		.append(filterValueVariable)
		.append(" ")
		.append(STATEMENT_SEPARATOR);

		if(valueList.size() > 1000) {
			filterClause.append(VALUES).append(filterValueVariable).append(" ").append(BLOCK_START);
			for (Serializable value : valueList) {
				filterClause.append(value).append(" ");
			}
			filterClause.append(BLOCK_END);
		} else {
			filterClause.append(FILTER_BLOCK_START);
			for (int counter = 0; counter < valueList.size(); counter++) {
				Serializable value = valueList.get(counter);
				String valueVariable = createVariableName(predicate, "");

				if (value instanceof DateRange) {
					buildDateRangeClause(predicate, value, filterClause);
				} else {
					filterClause.append(filterValueVariable).append(" = ").append(VARIABLE).append(valueVariable);
					bindings.put(valueVariable, value);
				}

				if (counter < valueList.size() - 1) {
					filterClause.append(" || ");
				}
			}
			filterClause.append(CLOSE_BRACKET).append(STATEMENT_SEPARATOR);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitStart(Query query, QueryBoost boost) {
		// unused method
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd(Query query) {
		// unused method
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void end() {
		buildPendingPredicates();

		StringBuilder queryBody = new StringBuilder(whereClause);

		appendAndFilterClauses(queryBody);

		appendOrClases(queryBody);

		queryBody.append(BLOCK_END);

		builder.append(queryBody.toString());

		addUnprocessedParametersToBindings();

		createCountQuery();
	}

	private void addUnprocessedParametersToBindings() {
		if (unprocessedParameters.isEmpty()) {
			return;
		}

		String query = builder.toString();
		unprocessedParameters.forEach((predicate, value) -> {
			if (query.contains(predicate)) {
				bindings.put(predicate, value);
			}
		});
	}

	/**
	 * Creates the count query.
	 */
	private void createCountQuery() {
		countQuery = new StringBuilder(START_COUNT_QUERY);
		countQuery.append(builder).append("LIMIT ").append(maxResultLimit).append(" ").append(BLOCK_END);
	}

	/**
	 * Append or clases.
	 *
	 * @param queryBody
	 *            the query body
	 */
	private void appendOrClases(StringBuilder queryBody) {
		if (!filterOrClauseList.isEmpty()) {
			queryBody.append(BLOCK_START);
			for (int counter = 0; counter < filterOrClauseList.size(); counter++) {
				StringBuilder filterClause = filterOrClauseList.get(counter);
				queryBody.append(BLOCK_START).append(filterClause).append(LINE_SEPARATOR).append(BLOCK_END);
				if (counter < filterOrClauseList.size() - 1) {
					queryBody.append(UNION);
				}
			}
			queryBody.append(BLOCK_END).append(STATEMENT_SEPARATOR);
		}
	}

	/**
	 * Append and filter clauses.
	 *
	 * @param queryBody
	 *            the query body
	 */
	private void appendAndFilterClauses(StringBuilder queryBody) {
		for (StringBuilder filterClause : filterClauseList) {
			queryBody.append(filterClause).append(LINE_SEPARATOR);
		}
	}

	/**
	 * Builds the pending predicates.
	 */
	private void buildPendingPredicates() {
		if (pendingPredicates.isEmpty()) {
			return;
		}

		addInstanceBinding();

		addContextAndRelations();

		addConnectorQuery();

		addTypesFilterClauses();

		addExistingBody();

		addNotExistsFilter();

		printNotProcessedPendingPredicates();
	}

	private void addInstanceBinding() {
		if (pendingPredicates.containsKey(OBJECT)) {
			Pair<QueryBoost, Serializable> pair = pendingPredicates.get(OBJECT);
			bindings.put(OBJECT, pair.getSecond());
		}
	}

	private void printNotProcessedPendingPredicates() {
		pendingPredicates
		.forEach((key, value) -> LOGGER.debug("Unprocessed pending predicate: {} and value {} ", key, value));
	}

	private void addNotExistsFilter() {
		pendingPredicates.entrySet().removeIf(entry -> {
			String predicate = entry.getKey();
			if (!predicate.startsWith("-")) {
				return false;
			}
			String predicateUri = predicate.substring(1);
			// build variable and predicate declaration
			String predicateVariable = createPredicateVariable(predicateUri);
			appendPredicateDeclaration(predicateUri, predicateVariable, true);
			whereClause
			.append(FILTER_BLOCK_START)
			.append(NOT_BOUND_START_CLAUSE)
			.append(VARIABLE)
			.append(predicateVariable)
			.append(CLOSE_BRACKET)
			.append(CLOSE_BRACKET)
			.append(STATEMENT_SEPARATOR)
			.append(LINE_SEPARATOR);
			return true;
		});
	}

	/**
	 * Adds the context and relations.
	 */
	private void addContextAndRelations() {
		boolean relations = pendingPredicates.containsKey(RELATIONS_PREDICATE);
		boolean context = pendingPredicates.containsKey(CONTEXT_PREDICATE);

		String subject = OBJECT_VARIABLE;

		StringBuilder filterClause = new StringBuilder(256);
		QueryBoost boost = null;
		if (relations && context) {
			if (isUseSimpleRelationSearch()) {
				// build relations and predicates
				buildContextAndRelationsFilterClause(subject);
			} else {
				Pair<QueryBoost, Serializable> pair = pendingPredicates.remove(CONTEXT_PREDICATE);
				boost = pair.getFirst();
				Serializable value = pair.getSecond();
				buildContextClause(subject, value, filterClause, false);

				pair = pendingPredicates.remove(RELATIONS_PREDICATE);
				value = pair.getSecond();
				addRelationsClause(subject, value, filterClause);
			}
		} else if (relations && !context) {
			Pair<QueryBoost, Serializable> pair = pendingPredicates.remove(RELATIONS_PREDICATE);
			boost = pair.getFirst();
			Serializable value = pair.getSecond();
			if (isUseSimpleRelationSearch()) {
				filterClause = buildSimpleRelationsClause(subject, value, filterClause);
			} else {
				addRelationsClause(subject, value, filterClause);
			}
		} else if (context && !relations) {
			Pair<QueryBoost, Serializable> pair = pendingPredicates.remove(CONTEXT_PREDICATE);
			boost = pair.getFirst();
			Serializable value = pair.getSecond();
			if (isUseSimpleRelationSearch()) {
				buildSimpleContextClause(subject, value, filterClause);
			} else {
				buildContextClause(subject, value, filterClause, true);
			}
		}
		storeClause(boost, filterClause);
	}

	/**
	 * Builds the context and relations filter clause. Note this builds only a simple relation clauses!
	 *
	 * @param subject
	 *            the subject
	 */
	@SuppressWarnings("unchecked")
	private void buildContextAndRelationsFilterClause(String subject) {
		Pair<QueryBoost, Serializable> contextPair = pendingPredicates.remove(CONTEXT_PREDICATE);
		List<String> contexts = (List<String>) contextPair.getSecond();

		Pair<QueryBoost, Serializable> repationsPair = pendingPredicates.remove(RELATIONS_PREDICATE);
		List<String> relations = (List<String>) repationsPair.getSecond();

		StringBuilder filterClause = new StringBuilder(256);

		// create union for each predicate that contains a union for all
		// contexts
		boolean isUnionNeeded = relations.size() > 1;
		for (int i = 0; i < relations.size(); i++) {
			String predicate = relations.get(i);

			if (isUnionNeeded) {
				filterClause.append(BLOCK_START);
			}

			filterClause.append(buildContextListFilter(subject, predicate, contexts));

			if (isUnionNeeded) {
				filterClause.append(BLOCK_END);
			}
			if (i < relations.size() - 1) {
				filterClause.append(UNION);
			}
		}
		// end of internal union
		filterClause.append(STATEMENT_SEPARATOR);
		storeClause(contextPair.getFirst(), filterClause);
	}

	/**
	 * Build filter for list of context values. The clause looks like
	 * <code> { { ?subject ?predicate ?someValue. ?subject rdf:type ?value1. }
	 * UNION { ?instance ?predicate ?someValue1. ?subject rdf:type ?value2. } } </code>
	 *
	 * @param subject
	 *            The subject in the filter clause
	 * @param predicate
	 *            The predicate in the filter clause
	 * @param contextList
	 *            The list of contexts - the list can contain URIs or simple string values
	 * @return The list filter clause as String
	 */
	private String buildContextListFilter(String subject, String predicate, List<String> contextList) {

		StringBuilder filterClause = new StringBuilder(128);
		// open UNION block
		filterClause.append(BLOCK_START);
		boolean isUnionNeeded = contextList.size() > 1;
		for (int counter = 0; counter < contextList.size(); counter++) {
			String stringValue = contextList.get(counter);
			String contextVariableName = createVariableName(predicate);
			String contextVariable = VARIABLE + contextVariableName;
			bindings.put(contextVariableName, stringValue);

			if (isUnionNeeded) {
				filterClause.append(BLOCK_START);
			}

			filterClause.append(subject).append(" ").append(predicate).append(" ").append(contextVariable).append(
					STATEMENT_SEPARATOR);
			filterClause.append(contextVariable).append(IS_NOT_DELETED);

			if (isUnionNeeded) {
				filterClause.append(BLOCK_END);
			}

			if (counter < contextList.size() - 1) {
				filterClause.append(UNION);
			}
		}
		filterClause.append(BLOCK_END);

		return filterClause.toString();
	}

	/**
	 * Adds the types filter clauses.
	 */
	private void addTypesFilterClauses() {
		StringBuilder filterClause = new StringBuilder(256);
		QueryBoost boost = QueryBoost.INCLUDE_AND;

		if (pendingPredicates.containsKey(FACET_PREFIX + RDF_TYPE)
				|| pendingPredicates.containsKey(FACET_PREFIX + EMF_TYPE)) {
			// if the type comes from the facets then we search only by the
			// selected type and skip all chosen types in the type field
			addFacetTypeFilters(filterClause);
		}
		boolean containsRdfType = pendingPredicates.containsKey(RDF_TYPE);
		boolean containsEmfType = pendingPredicates.containsKey(EMF_TYPE);
		if (containsRdfType && containsEmfType) {
			// build RDF and EMF type predicates
			buildMixedTypeFilterClause();
		} else if (containsRdfType && !containsEmfType) {
			Pair<QueryBoost, Serializable> pair = pendingPredicates.remove(RDF_TYPE);
			boost = pair.getFirst();
			Serializable value = pair.getSecond();
			buildTypeFilterClause(RDF_TYPE, value, filterClause);
		} else if (containsEmfType && !containsRdfType) {
			Pair<QueryBoost, Serializable> pair = pendingPredicates.remove(EMF_TYPE);
			boost = pair.getFirst();
			Serializable value = pair.getSecond();
			buildTypeFilterClause(EMF_TYPE, value, filterClause);
		}

		storeClause(boost, filterClause);
	}

	private void addFacetTypeFilters(StringBuilder filterClause) {
		Pair<QueryBoost, Serializable> pair;
		if (pendingPredicates.containsKey(FACET_PREFIX + RDF_TYPE)) {
			pair = pendingPredicates.remove(FACET_PREFIX + RDF_TYPE);
			buildTypeFilterClause(RDF_TYPE, pair.getSecond(), filterClause);
		}
		if (pendingPredicates.containsKey(FACET_PREFIX + EMF_TYPE)) {
			pair = pendingPredicates.remove(FACET_PREFIX + EMF_TYPE);
			buildTypeFilterClause(EMF_TYPE, pair.getSecond(), filterClause);
		}
	}

	/**
	 * Builds query from existing body. Probably the body is already generated and a start and end of the query must be
	 * appended
	 */
	private void addExistingBody() {
		if (pendingPredicates.containsKey(QUERY_EXISTING_BODY)) {
			Pair<QueryBoost, Serializable> existingBody = pendingPredicates.remove(QUERY_EXISTING_BODY);
			QueryBoost boost = existingBody.getFirst();
			Serializable value = existingBody.getSecond();

			StringBuilder filterClause = new StringBuilder(256);
			filterClause.append(value);
			storeClause(boost, filterClause);
		}
	}

	/**
	 * build RDF and EMF type predicates
	 */
	@SuppressWarnings("unchecked")
	private void buildMixedTypeFilterClause() {
		Pair<QueryBoost, Serializable> rdfTypePair = pendingPredicates.get(RDF_TYPE);
		List<Serializable> rdfTypes = (List<Serializable>) rdfTypePair.getSecond();

		Pair<QueryBoost, Serializable> emfTypePair = pendingPredicates.get(EMF_TYPE);
		List<Serializable> emfTypes = (List<Serializable>) emfTypePair.getSecond();

		StringBuilder filterClause = new StringBuilder(256);
		filterClause.append(BLOCK_START);
		buildListFilter(OBJECT_VARIABLE, EMF_TYPE, emfTypes, filterClause);
		filterClause.append(BLOCK_END);
		filterClause.append(UNION);
		filterClause.append(BLOCK_START);
		buildListFilter(OBJECT_VARIABLE, RDF_TYPE, rdfTypes, filterClause);
		filterClause.append(BLOCK_END);

		storeClause(rdfTypePair.getFirst(), filterClause);
	}

	/**
	 * Creates a variable name for the binding value of the predicate. The variable is created by the predicateVariable
	 * with appended suffixes and 'Variable' string. If the variable name is used in the bindings an index is appended
	 *
	 * @param predicate
	 *            Name of the predicate
	 * @param suffix
	 *            Suffixes that are going to be appended to the variable name
	 * @return Variable name to be used in the filter clause
	 */
	private String createVariableName(String predicate, String... suffix) {
		String predicateVariable = createPredicateVariable(predicate);
		StringBuilder valueVariable = new StringBuilder(predicateVariable);

		if (suffix != null) {
			for (String string : suffix) {
				valueVariable.append(string);
			}
		}
		valueVariable.append("Variable");

		int index = 1;
		String variableAsString = valueVariable.toString();
		String valueVariableTmp = variableAsString;
		while (bindings.containsKey(valueVariableTmp)) {
			valueVariableTmp = variableAsString + index;
			index++;
		}
		return valueVariableTmp;
	}

	/**
	 * Creates a variable name for the predicate value that will be used in the where clause
	 *
	 * @param predicate
	 *            The predicate
	 * @return Variable name
	 */
	private static String createPredicateVariable(String predicate) {
		if (predicate.contains(URI_SEPARATOR)) {
			int collonIndex = predicate.lastIndexOf(URI_SEPARATOR);
			return predicate.substring(0, collonIndex) + predicate.substring(collonIndex + 1);
		} else if (predicate.startsWith(VARIABLE)) {
			return predicate.substring(1);
		} else {
			return predicate;
		}
	}

	/**
	 * Replace the searchString in the text with the replaceString
	 *
	 * @param text
	 *            Text in which we will replace
	 * @param searchString
	 *            Search string to be replaced
	 * @param replaceString
	 *            Replace string
	 * @return Text with replaced search string by the replace string
	 */
	private static String replaceIgnoreCase(String text, String searchString, String replaceString) {
		StringBuilder result = new StringBuilder(text);

		int index = org.apache.commons.lang.StringUtils.indexOfIgnoreCase(text, searchString);
		while (index > -1) {
			result.replace(index, index + searchString.length(), replaceString);
			index = org.apache.commons.lang.StringUtils.indexOfIgnoreCase(text, searchString, index + 1);
		}

		return result.toString();
	}

	/**
	 * Getter method for bindings.
	 *
	 * @return the bindings
	 */
	public Map<String, Serializable> getBindings() {
		return bindings;
	}

	/**
	 * Setter method for bindings.
	 *
	 * @param bindings
	 *            the bindings to set
	 */
	public void setBindings(Map<String, Serializable> bindings) {
		this.bindings = bindings;
	}

	/**
	 * Prints the query with replaced binding variables with their values
	 *
	 * @return Query with replaced binding variables with their values
	 */
	public String getQueryWithBindings() {
		String query = builder.toString();

		for (Entry<String, Serializable> entry : bindings.entrySet()) {
			String value = entry.getValue().toString();
			if (value.startsWith("http")) {
				value = "<" + value + ">";
			} else if (!value.contains(URI_SEPARATOR)) {
				value = "\"" + value + "\"";
			}
			query = query.replace(VARIABLE + entry.getKey() + " ", value + " ");
		}

		return query;
	}

	/**
	 * Getter method for countQuery.
	 *
	 * @return the countQuery
	 */
	public String getCountQuery() {
		if (countQuery != null) {
			return countQuery.toString();
		}
		return "";
	}

	/**
	 * Getter method for maxResultLimit.
	 *
	 * @return the maxResultLimit
	 */
	public long getMaxResultLimit() {
		return maxResultLimit;
	}

	/**
	 * Setter method for maxResultLimit.
	 *
	 * @param maxResultLimit
	 *            the maxResultLimit to set
	 */
	public void setMaxResultLimit(long maxResultLimit) {
		this.maxResultLimit = maxResultLimit;
	}

	/**
	 * Sets the stadanlone fts parser.
	 *
	 * @param parser
	 *            the parser to set
	 */
	public void setFTSParser(FTSQueryParser parser) {
		this.parser = parser;
	}

	/**
	 * Getter method for useSimpleRelationSearch.
	 *
	 * @return the useSimpleRelationSearch
	 */
	public boolean isUseSimpleRelationSearch() {
		return useSimpleRelationSearch;
	}

	/**
	 * Setter method for useSimpleRelationSearch.
	 *
	 * @param useSimpleRelationSearch
	 *            the useSimpleRelationSearch to set
	 */
	public void setUseSimpleRelationSearch(boolean useSimpleRelationSearch) {
		this.useSimpleRelationSearch = useSimpleRelationSearch;
	}

	/**
	 * @return the projection
	 */
	public String getProjection() {
		return projection;
	}

	/**
	 * @param projection
	 *            the projection to set
	 */
	public void setProjection(String projection) {
		this.projection = projection;
	}

	/**
	 * @param ignoreInstancesForType
	 *            the ignoreInstancesForType to set
	 */
	public void setIgnoreInstancesForType(String ignoreInstancesForType) {
		ignoreInstancesForTypeClause = SemanticSearchOperationUtils.buildIgnoreInstancesForType(ignoreInstancesForType);
	}

	/**
	 * @param applyFilterForType
	 *            the applyFilterForType to set
	 */
	public void setApplyFilterForType(boolean applyFilterForType) {
		this.applyFilterForType = applyFilterForType;
	}

}
