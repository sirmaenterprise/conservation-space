package com.sirma.itt.emf.semantic.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.openrdf.model.vocabulary.RDF;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.search.AbstractQueryVistor;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * @author kirq4e
 */
public class SemanticQueryVisitor extends AbstractQueryVistor {

	private static final String UNION = " UNION ";

	public static final String QUERY_STATEMENT_SEPARATOR = " . ";

	public static final String URI_SEPARATOR = ":";

	public static final String RDF_TYPE = RDF.PREFIX + URI_SEPARATOR + RDF.TYPE.getLocalName();
	public static final String EMF_TYPE = EMF.PREFIX + URI_SEPARATOR + EMF.TYPE.getLocalName();

	public static final String EMF_CREATED_BY = EMF.PREFIX + URI_SEPARATOR
			+ EMF.CREATED_BY.getLocalName();

	public static final String QUERY_OPEN_BRACKET = "( ";
	public static final String QUERY_CLOSE_BRACKET = " ) ";

	public static final String QUERY_VARIABLE = "?";

	public static final String QUERY_BLOCK_START = " { ";
	public static final String QUERY_BLOCK_END = " } ";

	public static final String QUERY_OBJECT = "instance";
	public static final String QUERY_OBJECT_TYPE = "instanceType";

	public static final String QUERY_OBJECT_VARIABLE = QUERY_VARIABLE + QUERY_OBJECT;
	public static final String QUERY_OBJECT_TYPE_VARIABLE = QUERY_VARIABLE + QUERY_OBJECT_TYPE;
	public static final String QUERY_COUNT = "count";
	public static final String QUERY_COUNT_VARIABLE = QUERY_VARIABLE + QUERY_COUNT;

	public static final String QUERY_QUERY_START = "SELECT DISTINCT " + QUERY_OBJECT_VARIABLE + " "
			+ QUERY_OBJECT_TYPE_VARIABLE + " WHERE" + QUERY_BLOCK_START;
	public static final String QUERY_COUNT_QUERY_START = "SELECT (count(distinct "
			+ QUERY_OBJECT_VARIABLE + ") as " + QUERY_COUNT_VARIABLE + ") WHERE"
			+ QUERY_BLOCK_START;

	public static final String QUERY_RELATION_RDF_TYPE_BLOCK = "?relation a emf:Relation"
			+ QUERY_STATEMENT_SEPARATOR + "?relation emf:isActive \"true\"^^xsd:boolean"
			+ QUERY_STATEMENT_SEPARATOR;

	public static final String QUERY_FILTER_BLOCK_START = " FILTER " + QUERY_OPEN_BRACKET;

	public static final String QUERY_STR_FUNCTION = "STR" + QUERY_OPEN_BRACKET;

	public static final String QUERY_ORDER_BY_BLOCK = " ORDER BY ";

	private Map<String, Serializable> bindings = new HashMap<String, Serializable>();
	private StringBuilder whereClause;

	private final List<StringBuilder> filterClauseList = new ArrayList<>();
	private final List<StringBuilder> filterOrClauseList = new ArrayList<>();
	private StringBuilder orderByClause;
	private String fullTextsearchIndex;

	/**
	 * The same query as the main but with select for count
	 */
	private StringBuilder countQuery;

	private boolean isContextSearch = false;
	private Sorter sorter;

	private long maxResultLimit = 1000l;
	/** The FTS parser that preprocess the term query. */
	private FTSQueryParser parser;

	private String caseInsenitiveOrderByList;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		builder.append(QUERY_QUERY_START);
		orderByClause = new StringBuilder();
		whereClause = new StringBuilder();
		whereClause.append(QUERY_OBJECT_VARIABLE).append(" a ?rdftype");
		whereClause.append(". ?rdftype emf:isSearchable \"true\"^^xsd:boolean .");
		whereClause.append("?rdftype").append(" emf:definitionId ")
				.append(QUERY_OBJECT_TYPE_VARIABLE).append(" . ").append(QUERY_OBJECT_VARIABLE)
				.append(" emf:isDeleted \"false\"^^xsd:boolean . ");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Query query) throws Exception {
		String subject = QUERY_OBJECT_VARIABLE;
		String predicate = query.getKey();
		Serializable value = query.getValue();

		StringBuilder filterClause = new StringBuilder();

		// if location then add filter by location
		if ("context".equals(predicate)) {
			predicate = "emf:destination";

			filterClause.append(QUERY_BLOCK_START);
			filterClause.append(QUERY_BLOCK_START);
			filterClause.append(QUERY_RELATION_RDF_TYPE_BLOCK);
			filterClause.append("?relation emf:source ").append(QUERY_OBJECT_VARIABLE)
					.append(QUERY_STATEMENT_SEPARATOR);
			// filterClause.append("?relation emf:destination ?destinationVariable .");

			String filterClauseRelation = buildListFilter("?relation", predicate,
					(List<String>) value);
			filterClause.append(filterClauseRelation);
			filterClause.append(QUERY_BLOCK_END).append(UNION);
			String filterClausePartOf = buildListFilter(subject, "ptop:partOf",
					(List<String>) value);
			filterClause.append(filterClausePartOf).append(QUERY_BLOCK_END)
					.append(QUERY_STATEMENT_SEPARATOR);

			isContextSearch = true;

		} else if ("relations".equals(predicate)) {

			for (StringBuilder builder : filterClauseList) {
				if (builder.indexOf(QUERY_RELATION_RDF_TYPE_BLOCK) > 0) {
					filterClause = builder;
					filterClauseList.remove(filterClause);
					break;
				}
			}

			if (filterClause.length() == 0) {
				filterClause.append(QUERY_BLOCK_START);
				filterClause.append(QUERY_RELATION_RDF_TYPE_BLOCK);
				filterClause.append("?relation emf:source ").append(QUERY_OBJECT_VARIABLE)
						.append(QUERY_STATEMENT_SEPARATOR);
				filterClause.append(QUERY_BLOCK_END);
			}

			subject = QUERY_VARIABLE + "relation";
			predicate = "emf:relationType";

			String relationTypeFilter = buildListFilter(subject, predicate, (List<String>) value);

			int relationIndexOf = filterClause.indexOf(QUERY_RELATION_RDF_TYPE_BLOCK);

			filterClause.insert(relationIndexOf + QUERY_RELATION_RDF_TYPE_BLOCK.length(),
					relationTypeFilter);

		} else if (RDF_TYPE.equals(predicate) || EMF_TYPE.equals(predicate)) {
			// build UNION clause for rdf:type and emf:type predicates
			String searchPredicate = EMF_TYPE;
			// check for the opposite clause
			if (EMF_TYPE.equals(predicate)) {
				searchPredicate = RDF_TYPE;
			}

			// find the existing clause
			boolean foundTypeClause = false;
			for (StringBuilder builder : filterClauseList) {
				if (builder.indexOf(searchPredicate) > 0) {
					filterClause = builder;
					filterClauseList.remove(filterClause);
					foundTypeClause = true;
					break;
				}
			}

			if (foundTypeClause) {
				// if the clause exists then append UNION block in the front
				StringBuilder tempFilter = new StringBuilder();
				tempFilter.append(QUERY_BLOCK_START).append(QUERY_BLOCK_START).append(filterClause);
				filterClause = tempFilter;
				filterClause.append(QUERY_BLOCK_END).append(UNION).append(QUERY_BLOCK_START);
			}

			// build the current clause
			filterClause.append(buildListFilter(subject, predicate, (List<String>) value)).append(
					QUERY_STATEMENT_SEPARATOR);

			if (foundTypeClause) {
				// close the UNION block
				filterClause.append(QUERY_BLOCK_END).append(QUERY_BLOCK_END);
			}

		} else if (value instanceof String) {
			String valueVariable = createVariableName(predicate);
			String valueString = (String) value;

			// if the search is full text search
			if ("fts".equals(predicate)) {
				if (valueString.contains(":")) {
					valueString = valueString.replace(":", "\\:");
				}

				if (valueString.toLowerCase().contains(" or ")) {
					valueString = replaceIgnoreCase(valueString, " or ", " OR ");
				}

				if (valueString.toLowerCase().contains(" and ")) {
					valueString = replaceIgnoreCase(valueString, " and ", " AND ");
				}

				if (fullTextsearchIndex.startsWith("luc")) {
					// lucene search
					filterClause.append(QUERY_OBJECT_VARIABLE).append(" ")
							.append(fullTextsearchIndex).append(" ").append(QUERY_VARIABLE)
							.append(valueVariable).append(QUERY_STATEMENT_SEPARATOR);
				} else if (fullTextsearchIndex.startsWith("solr")) {

					// solr search
					// ?search a solr:ftsearch ;
					// solr:query "*:*" ;
					// solr:entities ?entity .

					valueString = parser.prepare(valueString);

					if (valueString != null) {

						valueString = StringEscapeUtils.escapeJava(valueString);
						filterClause.append("?search a ").append(fullTextsearchIndex)
								.append(" ; solr:query ").append("\"").append(valueString)
								.append("\"").append(" ; solr:entities ")
								.append(QUERY_OBJECT_VARIABLE).append(QUERY_STATEMENT_SEPARATOR);
					}
					valueString = "";
				}

			} else if ("emf:createdBy".equals(predicate)) {
				filterClause.append(QUERY_OBJECT_VARIABLE).append(" ").append(predicate)
						.append(" ").append(QUERY_VARIABLE).append(valueVariable)
						.append(QUERY_STATEMENT_SEPARATOR);
			} else {
				// append regex search on the property that isn`t part of the lucene index
				String predicateVariable = createPredicateVariable(predicate);
				whereClause.append(QUERY_OBJECT_VARIABLE).append(" ").append(predicate).append(" ")
						.append(QUERY_VARIABLE).append(predicateVariable)
						.append(QUERY_STATEMENT_SEPARATOR);

				filterClause.append(QUERY_FILTER_BLOCK_START);
				filterClause.append("REGEX(").append(QUERY_STR_FUNCTION).append(QUERY_VARIABLE)
						.append(predicateVariable).append(QUERY_CLOSE_BRACKET);
				filterClause.append(" , ").append(QUERY_VARIABLE).append(valueVariable)
						.append(" , \"i\"").append(QUERY_CLOSE_BRACKET);

				filterClause.append(QUERY_CLOSE_BRACKET).append(QUERY_STATEMENT_SEPARATOR);
			}
			bindings.put(valueVariable, valueString);
		} else if (value instanceof DateRange) {
			DateRange dates = (DateRange) value;
			String valueVariable = createVariableName(predicate, "Gt");
			String predicateVariable = createPredicateVariable(predicate);
			whereClause.append(QUERY_OBJECT_VARIABLE).append(" ").append(predicate).append(" ")
					.append(QUERY_VARIABLE).append(predicateVariable)
					.append(QUERY_STATEMENT_SEPARATOR);

			filterClause.append(QUERY_FILTER_BLOCK_START);

			// FILTER (?createdOn > ?createdOnGtVariable && ?createdOn < ?createdOnLtVariable)
			// add start date
			if (dates.getFirst() != null) {
				filterClause.append(QUERY_VARIABLE).append(predicateVariable).append(" >= ")
						.append(QUERY_VARIABLE).append(valueVariable);
				bindings.put(valueVariable, dates.getFirst());
			}

			if ((dates.getFirst() != null) && (dates.getSecond() != null)
					&& !dates.getFirst().equals(dates.getSecond())) {
				filterClause.append(" && ");
			}

			// add end date
			if (dates.getSecond() != null) {
				valueVariable = createVariableName(predicate, "Lt");
				filterClause.append(QUERY_VARIABLE).append(predicateVariable).append(" <= ")
						.append(QUERY_VARIABLE).append(valueVariable);
				bindings.put(valueVariable, dates.getSecond());
			}

			filterClause.append(QUERY_CLOSE_BRACKET).append(QUERY_STATEMENT_SEPARATOR);
		} else if (value instanceof List<?>) {
			if (RDF_TYPE.equals(predicate) || EMF_TYPE.equals(predicate)) {
				for (StringBuilder builder : filterClauseList) {
					if (builder.indexOf(RDF_TYPE) > 0) {
						filterClause = builder;
						filterClauseList.remove(filterClause);
						break;
					}
				}
			}

			filterClause.append(buildListFilter(subject, predicate, (List<String>) value)).append(
					QUERY_STATEMENT_SEPARATOR);
		}

		if (query.getBoost().equals(QueryBoost.INCLUDE_AND)) {
			filterClauseList.add(filterClause);
		} else if (query.getBoost().equals(QueryBoost.INCLUDE_OR)) {
			filterOrClauseList.add(filterClause);
		}

	}

	/**
	 * Build filter for list of values. The clause looks like { { { ?instance ?predicate ?value1 } }
	 * UNION { { ?instance ?predicate ?value2 } } }
	 *
	 * @param subject
	 *            The subject in the filter clause
	 * @param predicate
	 *            The predicate in the filter clause
	 * @param valueList
	 *            The list of values - the list can contain URIs or simple string values
	 * @return The list filter clause as String
	 */
	private String buildListFilter(String subject, String predicate, List<String> valueList) {

		StringBuilder filterClause = new StringBuilder();
		// open UNION block
		filterClause.append(QUERY_BLOCK_START);

		for (int counter = 0; counter < valueList.size(); counter++) {
			String stringValue = valueList.get(counter);
			String valueVariable = createVariableName(predicate, "");

			filterClause.append(QUERY_BLOCK_START).append(subject).append(" ").append(predicate)
					.append(" ").append(QUERY_VARIABLE).append(valueVariable)
					.append(QUERY_BLOCK_END);

			bindings.put(valueVariable, stringValue);

			if (counter < (valueList.size() - 1)) {
				filterClause.append(UNION);
			}
		}
		filterClause.append(QUERY_BLOCK_END);

		return filterClause.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitStart(Query query, QueryBoost boost) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd(Query query) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void end() {
		countQuery = new StringBuilder(QUERY_COUNT_QUERY_START);

		if (sorter != null && !StringUtils.isNullOrEmpty(sorter.getSortField())) {
			String predicate = sorter.getSortField();
			String predicateVariable = QUERY_VARIABLE + createPredicateVariable(predicate);

			if (!whereClause.toString().contains(predicateVariable)) {
				whereClause.append(QUERY_OBJECT_VARIABLE).append(" ").append(predicate).append(" ")
						.append(predicateVariable).append(" ").append(QUERY_STATEMENT_SEPARATOR);
			}
//			if (StringUtils.isNotNullOrEmpty(caseInsenitiveOrderByList)
//					&& caseInsenitiveOrderByList.indexOf(predicate) > -1) {
//				whereClause.append(" bind(lcase(").append(predicateVariable)
//						.append(") as ?sortVariable) . ");
//				predicateVariable = "?sortVariable";
//			}
			String sorterDirection;
			if (sorter.isAscendingOrder()) {
				sorterDirection = Sorter.SORT_ASCENDING;
			} else {
				sorterDirection = Sorter.SORT_DESCENDING;
			}
			orderByClause.append(QUERY_ORDER_BY_BLOCK).append(sorterDirection)
					.append(QUERY_OPEN_BRACKET).append(predicateVariable)
					.append(QUERY_CLOSE_BRACKET);

		}

		StringBuilder queryBody = new StringBuilder(whereClause);

		for (StringBuilder filterClause : filterClauseList) {
			// if there is a context search and relations search then skip the ?object ?relation
			// ?context filter clause from the relations
			if (isContextSearch && filterClause.indexOf("?context") > 0) {
				continue;
			}
			queryBody.append(filterClause);
		}

		if (!filterOrClauseList.isEmpty()) {
			queryBody.append(QUERY_BLOCK_START);
			for (int counter = 0; counter < filterOrClauseList.size(); counter++) {
				StringBuilder filterClause = filterOrClauseList.get(counter);
				queryBody.append(QUERY_BLOCK_START).append(filterClause).append(QUERY_BLOCK_END);
				if (counter < filterOrClauseList.size() - 1) {
					queryBody.append(UNION);
				}
			}
			queryBody.append(QUERY_BLOCK_END).append(QUERY_STATEMENT_SEPARATOR);
		}

		queryBody.append(QUERY_BLOCK_END);

		builder.append(queryBody);
		countQuery.append(builder).append("LIMIT ").append(maxResultLimit).append(" ")
				.append(QUERY_BLOCK_END);

		builder.append(orderByClause);
	}

	/**
	 * Creates a variable name for the binding value of the predicate. The variable is created by
	 * the predicateVariable with appended suffixes and 'Variable' string. If the variable name is
	 * used in the bindings an index is appended
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

		for (String string : suffix) {
			valueVariable.append(string);
		}
		valueVariable.append("Variable");

		int index = 1;
		String valueVariableTmp = valueVariable.toString();
		while (bindings.containsKey(valueVariableTmp)) {
			valueVariableTmp = valueVariable.toString() + index;
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
	private String createPredicateVariable(String predicate) {
		if (predicate.contains(":")) {
			return predicate.substring(0, predicate.lastIndexOf(':'))
					+ predicate.substring(predicate.lastIndexOf(':') + 1);
		} else if (predicate.startsWith("?")) {
			return predicate.substring(1);
		} else {
			return "variable";
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
	private String replaceIgnoreCase(String text, String searchString, String replaceString) {
		StringBuilder result = new StringBuilder(text);

		int index = org.apache.commons.lang.StringUtils.indexOfIgnoreCase(text, searchString);
		while (index > -1) {
			result.replace(index, index + searchString.length(), replaceString);
			index = org.apache.commons.lang.StringUtils.indexOfIgnoreCase(text, searchString,
					index + 1);
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

		for (String binding : bindings.keySet()) {
			query = query.replace(QUERY_VARIABLE + binding, bindings.get(binding).toString());
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
	 * Setter method for sorter.
	 *
	 * @param sorter
	 *            the sorter to set
	 */
	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
	}

	/**
	 * Getter method for fullTextsearchIndex.
	 *
	 * @return the fullTextsearchIndex
	 */
	public String getFullTextsearchIndex() {
		return fullTextsearchIndex;
	}

	/**
	 * Setter method for fullTextsearchIndex.
	 *
	 * @param fullTextsearchIndex
	 *            the fullTextsearchIndex to set
	 */
	public void setFullTextsearchIndex(String fullTextsearchIndex) {
		this.fullTextsearchIndex = fullTextsearchIndex;
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
	 * Getter method for caseInsenitiveOrderByList.
	 *
	 * @return the caseInsenitiveOrderByList
	 */
	public String getCaseInsenitiveOrderByList() {
		return caseInsenitiveOrderByList;
	}

	/**
	 * Setter method for comma separated list of prefix:name properties that are considered to be
	 * case insensitive
	 *
	 * @param caseInsenitiveOrderByList
	 *            the caseInsenitiveOrderByList to set
	 */
	public void setCaseInsenitiveOrderByList(String caseInsenitiveOrderByList) {
		this.caseInsenitiveOrderByList = caseInsenitiveOrderByList;
	}

}
