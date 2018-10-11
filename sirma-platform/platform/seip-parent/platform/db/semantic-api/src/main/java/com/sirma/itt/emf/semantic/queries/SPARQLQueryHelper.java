package com.sirma.itt.emf.semantic.queries;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.itt.semantic.namespaces.DefaultNamespaces;

/**
 * @author kirq4e
 */
public class SPARQLQueryHelper {

	private static final String PREFIX_IDENTIFIER = "PREFIX ";

	private static final Logger LOGGER = LoggerFactory.getLogger(SPARQLQueryHelper.class);

	private static final Random RANDOM_SUFFIX_GENERATOR = new Random();

	public static final String QUERY_ID_BLOCK = "# Query ID: ";
	public static final String LINE_SEPARATOR = System.lineSeparator();
	public static final String OPEN_BRACKET = "( ";
	public static final String CLOSE_BRACKET = " ) ";
	public static final String VARIABLE = "?";

	public static final String CURLY_BRACKET_OPEN = "{";
	public static final String CURLY_BRACKET_CLOSE = "}";
	public static final String BLOCK_START = " " + CURLY_BRACKET_OPEN + " ";
	public static final String BLOCK_END = " " + CURLY_BRACKET_CLOSE + " ";
	public static final String DOT = ".";
	public static final String STATEMENT_SEPARATOR = " " + DOT + " ";
	public static final String URI_SEPARATOR = ":";

	public static final String SELECT = "SELECT ";
	public static final String START_QUERY = SELECT + "DISTINCT ";
	public static final String WHERE = " WHERE";
	public static final String NOT = " ! ";
	public static final String NOT_EXISTS = "NOT EXISTS ";
	public static final String UNION = " UNION ";
	public static final String MINUS = " MINUS ";
	public static final String VALUES = " VALUES ";
	public static final String BOUND_START_CLAUSE = "BOUND " + OPEN_BRACKET;
	public static final String NOT_BOUND_START_CLAUSE = NOT + BOUND_START_CLAUSE;
	public static final String NOT_EXISTS_START = NOT_EXISTS + BLOCK_START;
	public static final String FILTER = " FILTER ";
	public static final String FILTER_BLOCK_START = FILTER + OPEN_BRACKET;
	public static final String FILTER_OR = " || ";
	public static final String OPTIONAL_BLOCK_START = " OPTIONAL " + BLOCK_START;
	public static final String COUNT = "count";
	public static final String STR_FUNCTION = "STR" + OPEN_BRACKET;
	public static final String ORDER_BY_BLOCK = " ORDER BY ";
	public static final String LIMIT = " LIMIT ";
	public static final String OFFSET = " OFFSET ";
	public static final String FALSE = " \"false\"^^xsd:boolean";
	public static final String INCLUDE_INFERRED_CONFIGURATION = "includeInferred";
	public static final String AS = "AS";
	public static final String GROUP_BY = "GROUP BY";

	public static final String PERMISSIONS_BLOCK_CONSTANT = "$permissions_block$";
	public static final String PERMISSIONS_SUFFIX = "%suffix%";
	public static final String PERMISSIONS_BLOCK_REGULAR_EXPRESSION = "\\$permissions_block\\$\\w*";
	public static final String CONNECTOR_NAME_CONSTANT = "$connectorName$";

	public static final String OBJECT = "instance";
	public static final String OBJECT_TYPE = "instanceType";
	public static final String ALL_OTHERS = "allOthers";
	public static final String CURRENT_USER = "currentUser";
	public static final String RELATIONS_PREDICATE = "relations";
	public static final String CONTEXT_PREDICATE = "context";
	public static final String QUERY_EXISTING_BODY = "queryExistingBody";
	public static final String SORT_VARIABLE_SUFFIX = "sort";
	public static final String NO_VALUE = "NO_VALUE";
	public static final String FACET_PREFIX = "facet-";
	public static final String APPLY_INSTANCES_BY_TYPE_FILTER_FLAG = "enableInstancesByTypeFilter";

	public static final String EMF_DEFAULT_HEADER_PREDICATE = EMF.PREFIX + URI_SEPARATOR
			+ EMF.DEFAULT_HEADER.getLocalName();
	public static final String EMF_COMPACT_HEADER_PREDICATE = EMF.PREFIX + URI_SEPARATOR
			+ EMF.COMPACT_HEADER.getLocalName();
	public static final String EMF_IS_DELETED_PREDICATE = EMF.PREFIX + URI_SEPARATOR + EMF.IS_DELETED.getLocalName();

	public static final String EMF_DEFAULT_HEADER_VARIABLE = VARIABLE + EMF.DEFAULT_HEADER.getLocalName();
	public static final String EMF_COMPACT_HEADER_VARIABLE = VARIABLE + EMF.COMPACT_HEADER.getLocalName();
	public static final String RDF_TYPE = RDF.PREFIX + URI_SEPARATOR + RDF.TYPE.getLocalName();
	public static final String EMF_REVISION_TYPE = EMF.PREFIX + URI_SEPARATOR + EMF.REVISION_TYPE.getLocalName();
	public static final String EMF_TYPE = EMF.PREFIX + URI_SEPARATOR + EMF.TYPE.getLocalName();
	public static final String PTOP_PART_OF = Proton.PREFIX + URI_SEPARATOR + Proton.PART_OF.getLocalName();
	public static final String EMF_CREATED_BY = EMF.PREFIX + URI_SEPARATOR + EMF.CREATED_BY.getLocalName();
	public static final String HAS_PERMISSION = Security.PREFIX + URI_SEPARATOR
			+ Security.HAS_PERMISSION.getLocalName();
	public static final String IS_MANAGER_OF = Security.PREFIX + URI_SEPARATOR + Security.IS_MANAGER_OF.getLocalName();
	public static final String ASSIGNED_TO = Security.PREFIX + URI_SEPARATOR + Security.ASSIGNED_TO.getLocalName();
	public static final String OBJECT_TYPE_VARIABLE = VARIABLE + OBJECT_TYPE;
	public static final String OBJECT_VARIABLE = VARIABLE + OBJECT;
	public static final String OBJECT_VARIABLE_FOR_REPLACE = "%" + OBJECT + "%";
	public static final String COUNT_VARIABLE = VARIABLE + COUNT;
	public static final String CURRENT_USER_VARIABLE = VARIABLE + CURRENT_USER;
	public static final String ALL_OTHER_USERS_VARIABLE = VARIABLE + ALL_OTHERS;
	public static final String IS_MEMBER_OF_GROUP = " emf:isMemberOf " + VARIABLE + "group ";
	public static final String FORBIDDEN_ROLE_URI = "forbiddenRoleUri";
	public static final String PERMISSIONS_ROLE_TYPE = "permissionsRoleType";
	public static final String NO_PERMISSIONS_ROLE_VARIABLE = VARIABLE + "noPermissionsRole ";
	public static final String HAS_ROLE_TYPE = Security.PREFIX + URI_SEPARATOR + Security.HAS_ROLE_TYPE.getLocalName();
	public static final String GROUP_BY_VARIABLE = VARIABLE + "groupByProperty";

	public static final String SOLR_PREFIX = "solr";
	public static final String SOLR_QUERY = SOLR_PREFIX + URI_SEPARATOR + "query";
	public static final String SOLR_ENTITIES = SOLR_PREFIX + URI_SEPARATOR + "entities";
	public static final String SOLR_SCORE = SOLR_PREFIX + URI_SEPARATOR + "score";

	/**
	 * Check readPermissionsQuery.sparql for full query
	 */
	public static final String READ_PERMISSIONS_FILTER = BLOCK_START + LINE_SEPARATOR
			+ ResourceLoadUtil.loadResource(SPARQLQueryHelper.class, "readPermissionsQuery.sparql") + LINE_SEPARATOR
			+ BLOCK_END;

	/**
	 * Check writePermissionsQuery.sparql for full query
	 */
	public static final String WRITE_PERMISSIONS_FILTER = BLOCK_START + LINE_SEPARATOR
			+ ResourceLoadUtil.loadResource(SPARQLQueryHelper.class, "writePermissionsQuery.sparql") + LINE_SEPARATOR
			+ BLOCK_END;

	public static final String DEFAULT_PROJECTION = OBJECT_VARIABLE + " " + OBJECT_TYPE_VARIABLE + " ";

	public static final String START_COUNT_QUERY = SELECT + "(count(distinct " + OBJECT_VARIABLE + ") as "
			+ COUNT_VARIABLE + ")" + WHERE + BLOCK_START;

	public static final String RELATION_RDF_TYPE_BLOCK = VARIABLE + "relation a emf:Relation" + STATEMENT_SEPARATOR
			+ VARIABLE + "relation emf:isActive \"true\"^^xsd:boolean" + STATEMENT_SEPARATOR;

	public static final String RELATION_SOURCE_BLOCK = VARIABLE + "relation emf:source " + OBJECT_VARIABLE
			+ STATEMENT_SEPARATOR;

	public static final String IS_NOT_DELETED = " " + EMF_IS_DELETED_PREDICATE + FALSE + STATEMENT_SEPARATOR;
	/**
	 * Used for sorting related object properties
	 */
	public static final String SORT_TITLE = EMF.PREFIX + URI_SEPARATOR + EMF.ALT_TITLE.getLocalName();

	private static String caseInsenitiveOrderByList;
	private static NamespaceRegistryService namespaceRegistryService;

	protected static final Map<String, Sorter> ORDER_BY_FIELDS_MAP = new HashMap<>(8);

	private static final Pattern OFFSET_PATTERN = Pattern.compile("\\b[Oo][fF][fF][sS][eE][tT]\\b");
	private static final Pattern LIMIT_PATTERN = Pattern.compile("\\b[Ll][iI][Mm][iI][tT]\\b");
	public static final Pattern PERMISSIONS_PATTERN = Pattern.compile(PERMISSIONS_BLOCK_REGULAR_EXPRESSION);

	static {
		// TODO static map for mapping the sort fields from comments dashlet to
		// URI-s. TO BE REMOVED after refactoring the dashlet
		ORDER_BY_FIELDS_MAP.put("modifiedOn",
				Sorter.ascendingSorter(EMF.PREFIX + URI_SEPARATOR + EMF.MODIFIED_ON.getLocalName()));
		ORDER_BY_FIELDS_MAP.put("modifiedBy",
				Sorter.ascendingSorter(EMF.PREFIX + URI_SEPARATOR + EMF.MODIFIED_BY.getLocalName()).setObjectProperty(
						true));
		ORDER_BY_FIELDS_MAP.put("title", Sorter.ascendingSorter(DC.PREFIX + URI_SEPARATOR + DC.TITLE.getLocalName()));
		ORDER_BY_FIELDS_MAP.put("createdOn",
				Sorter.ascendingSorter(EMF.PREFIX + URI_SEPARATOR + EMF.CREATED_ON.getLocalName()));
		ORDER_BY_FIELDS_MAP.put("createdBy",
				Sorter.ascendingSorter(EMF.PREFIX + URI_SEPARATOR + EMF.CREATED_BY.getLocalName()).setObjectProperty(
						true));
	}

	public static final String RANKING_SORTER_FIELD = "relevance";

	/**
	 * Utility class and should not have public default constructor
	 */
	private SPARQLQueryHelper() {
		// Utility class and should not have public default constructor
	}

	/**
	 * Returns the permissions filter, only for read access, for SPARQL query for the passed instance variable
	 *
	 * @param instanceVariableName
	 *            name of the variable
	 * @param isAdminUser
	 *            If the user is Administrator
	 * @param isReadOnly
	 *            Return filter only for read or for read/write permissions
	 * @return permissions filter for SPARQL query for the passed instance variable
	 */
	public static String getPermissionsFilter(String instanceVariableName, boolean isAdminUser, boolean isReadOnly) {
		String instanceVariable = instanceVariableName;
		if (StringUtils.isBlank(instanceVariable)) {
			instanceVariable = OBJECT_VARIABLE;
		}

		if (!instanceVariable.startsWith(VARIABLE)) {
			instanceVariable = VARIABLE + instanceVariable;
		}

		String template = "";
		if (!isAdminUser) {
			template = isReadOnly ? READ_PERMISSIONS_FILTER : WRITE_PERMISSIONS_FILTER;
		}

		return template.replace(PERMISSIONS_SUFFIX, "" + RANDOM_SUFFIX_GENERATOR.nextInt(99999)).replace(
				OBJECT_VARIABLE_FOR_REPLACE, instanceVariable);
	}

	/**
	 * Append order by clause to a query.
	 *
	 * @param query
	 *            SPARQL query
	 * @param sortFields
	 *            Sort fields
	 * @param appendOrderByClause
	 *            Boolean flag that allows append of order by clause. If this flag is false then only the field
	 *            declaration will be appended
	 * @return the query after order by is applied
	 */
	public static String appendOrderByToQuery(String query, List<Sorter> sortFields, boolean appendOrderByClause) {
		int indexOfBlockEnd = query.lastIndexOf('}');
		if (sortFields.isEmpty() || indexOfBlockEnd == -1) {
			return query;
		}

		StringBuilder projection = new StringBuilder();
		StringBuilder fieldDeclaration = new StringBuilder();
		StringBuilder orderByClauseDeclaration = new StringBuilder();

		processSortFields(sortFields, projection, fieldDeclaration, orderByClauseDeclaration);

		String orderByClauseDeclarationString = orderByClauseDeclaration.toString();
		// skip adding order by clause if the declaration is empty
		if (StringUtils.isNotBlank(orderByClauseDeclarationString)) {
			StringBuilder queryString = new StringBuilder();

			String beforeBlockEnd = query.substring(0, indexOfBlockEnd).trim();
			if (beforeBlockEnd.endsWith(DOT)) {
				beforeBlockEnd = beforeBlockEnd.substring(0, beforeBlockEnd.length() - 1);
			}
			// append sort variable to the projection
			int whereClauseIndex = beforeBlockEnd.toUpperCase().indexOf(WHERE);
			String queryProjection = query.substring(0, whereClauseIndex).trim();
			beforeBlockEnd = queryProjection + projection.toString() + " "
					+ query.substring(whereClauseIndex, beforeBlockEnd.length()).trim();

			String afterBlockEnd = query.substring(indexOfBlockEnd + 1);
			queryString.append(beforeBlockEnd).append(fieldDeclaration).append(BLOCK_END);
			if (appendOrderByClause) {
				queryString.append(ORDER_BY_BLOCK).append(orderByClauseDeclarationString);
			}
			queryString.append(afterBlockEnd);
			return queryString.toString();
		}
		return query;
	}

	private static void processSortFields(List<Sorter> sortFields, StringBuilder projection,
			StringBuilder fieldDeclaration, StringBuilder orderByClauseDeclaration) {
		for (Sorter sorter : sortFields) {
			boolean isObjectProperty = sorter.isObjectProperty();
			String sortField = sorter.getSortField();
			if (ORDER_BY_FIELDS_MAP.containsKey(sortField)) {
				Sorter mappedSorter = ORDER_BY_FIELDS_MAP.get(sortField);
				sortField = mappedSorter.getSortField();
				isObjectProperty = mappedSorter.isObjectProperty();
			} else if (isRankingSorter(sorter)) {
				sortField = SOLR_SCORE;
				isObjectProperty = false;
			}
			if (StringUtils.isNotBlank(sortField) && !"unsorted".equals(sortField)) {
				if (isObjectProperty) {
					appendObjectSortField(sorter, sortField, projection, fieldDeclaration, orderByClauseDeclaration);
				} else {
					appendDataSortField(sorter, sortField, projection, fieldDeclaration, orderByClauseDeclaration);
				}
			}
		}
	}

	private static void appendObjectSortField(Sorter sorter, String sortField, StringBuilder projection,
			StringBuilder fieldDeclaration, StringBuilder orderByClauseDeclaration) {
		String sortVariable = generateVarName() + SORT_VARIABLE_SUFFIX;
		// generate new sort variable to use for sorting
		String projectionSortVariable = sortVariable + "_";

		fieldDeclaration.append(STATEMENT_SEPARATOR).append(LINE_SEPARATOR);
		if (sorter.isMissingValuesAllowed()) {
			fieldDeclaration.append(OPTIONAL_BLOCK_START);
		}

		// Performs the sorting by the emf:altTitle field of the related instances. This field is selected using a
		// subquery to limit the possible values of the related instances otherwise the final result will have an
		// instance with multiple relations multiple times. The MIN(S) aggregation function picks the first value after
		// sorting (by index) all values for the instance
		fieldDeclaration
				.append(String.format(
						"{ select " + OBJECT_VARIABLE + " (MIN(lcase(%s)) as %s) where {" + LINE_SEPARATOR + " "
								+ OBJECT_VARIABLE + " %s ?sortInstance ." + LINE_SEPARATOR + " ?sortInstance "
								+ SORT_TITLE + " %1$s." + LINE_SEPARATOR + " } group by " + OBJECT_VARIABLE + " }",
						sortVariable, projectionSortVariable, sortField))
					.append(LINE_SEPARATOR);

		if (sorter.isMissingValuesAllowed()) {
			// close optional block
			fieldDeclaration.append(BLOCK_END);
		}
		fieldDeclaration.append(LINE_SEPARATOR);

		appendOrderByClause(orderByClauseDeclaration, sorter, projectionSortVariable);
		projection.append(" ").append(projectionSortVariable);
	}

	private static void appendDataSortField(Sorter sorter, String sortField, StringBuilder projection,
			StringBuilder fieldDeclaration, StringBuilder orderByClauseDeclaration) {
		String sortVariable = generateVarName() + SORT_VARIABLE_SUFFIX;

		fieldDeclaration.append(STATEMENT_SEPARATOR).append(LINE_SEPARATOR);
		if (sorter.isMissingValuesAllowed()) {
			fieldDeclaration.append(OPTIONAL_BLOCK_START);
		}

		fieldDeclaration.append(OBJECT_VARIABLE).append(" ").append(sortField).append(" ").append(sortVariable).append(
				STATEMENT_SEPARATOR);

		if (sorter.isMissingValuesAllowed()) {
			// close optional block
			fieldDeclaration.append(BLOCK_END);
		}
		fieldDeclaration.append(LINE_SEPARATOR);

		if (caseInsenitiveOrderByList.contains(sortField)) {
			fieldDeclaration.append(" bind(lcase(").append(sortVariable).append(") as ");
			// generate new sort variable to use for sorting
			sortVariable = sortVariable + "_";
			fieldDeclaration.append(sortVariable).append(CLOSE_BRACKET).append(STATEMENT_SEPARATOR);
		}

		appendOrderByClause(orderByClauseDeclaration, sorter, sortVariable);
		projection.append(" ").append(sortVariable);
	}

	private static void appendOrderByClause(StringBuilder orderByClauseDeclaration, Sorter sorter,
			String sortVariable) {
		orderByClauseDeclaration
				.append(sorter.isAscendingOrder() ? " ASC " : " DESC ")
					.append(OPEN_BRACKET)
					.append(sortVariable)
					.append(CLOSE_BRACKET);
	}

	private static boolean isRankingSorter(Sorter sorter) {
		return RANKING_SORTER_FIELD.equalsIgnoreCase(sorter.getSortField());
	}

	/**
	 * Prepares tuple query - sets bindings, include inferred flag
	 *
	 * @param repositoryConnection
	 *            Connection to the semantic repository
	 * @param queryString
	 *            Query for execution
	 * @param bindings
	 *            Query parameters
	 * @param includeInferred
	 *            Include inferred flag
	 * @return Tuple query that can be executed on the semantic repository
	 * @throws SemanticPersistenceException
	 *             If and error occurs while creating the query
	 */
	public static TupleQuery prepareTupleQuery(RepositoryConnection repositoryConnection, String queryString,
			Map<String, Serializable> bindings, boolean includeInferred) {
		return prepareTupleQuery(repositoryConnection, queryString, bindings, includeInferred, 0);
	}

	/**
	 * Prepares tuple query - sets bindings, include inferred flag, query timeout
	 *
	 * @param repositoryConnection
	 *            Connection to the semantic repository
	 * @param queryString
	 *            Query for execution
	 * @param bindings
	 *            Query parameters
	 * @param includeInferred
	 *            Include inferred flag
	 * @param queryTimeout
	 *            Query timeout
	 * @return Tuple query that can be executed on the semantic repository
	 * @throws SemanticPersistenceException
	 *             If and error occurs while creating the query
	 */
	public static TupleQuery prepareTupleQuery(RepositoryConnection repositoryConnection, String queryString,
			Map<String, Serializable> bindings, boolean includeInferred, int queryTimeout) {
		String query = appendQueryId(queryString);
		query = appendNamespaces(query);

		TupleQuery tupleQuery;
		try {
			tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
			tupleQuery.setIncludeInferred(includeInferred);
			setQueryTimeout(queryTimeout, tupleQuery::setMaxExecutionTime);
			setQueryBindings(query, bindings, tupleQuery::setBinding);
			return tupleQuery;
		} catch (RepositoryException | MalformedQueryException e) {
			throw new SemanticPersistenceException("Error creating Tuple Query", e);
		}
	}

	/**
	 * Prepares update SPARQL query - sets bindings, include inferred flag
	 *
	 * @param repositoryConnection
	 *            Connection to the semantic repository
	 * @param queryString
	 *            Query for execution
	 * @param bindings
	 *            Query parameters
	 * @param includeInferred
	 *            Include inferred flag
	 * @return Update query that can be executed on the semantic repository
	 * @throws SemanticPersistenceException
	 *             If and error occurs while creating the query
	 */
	public static Update prepareUpdateQuery(RepositoryConnection repositoryConnection, String queryString,
			Map<String, Serializable> bindings, boolean includeInferred) {
		String query = appendQueryId(queryString);
		query = appendNamespaces(query);

		Update updateQuery;
		try {
			updateQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, query);
			updateQuery.setIncludeInferred(includeInferred);
			setQueryBindings(query, bindings, updateQuery::setBinding);
			return updateQuery;
		} catch (RepositoryException | MalformedQueryException e) {
			throw new SemanticPersistenceException("Error creating Update Query", e);
		}
	}

	/**
	 * Prepares boolean query - sets bindings, include inferred flag, query timeout
	 *
	 * @param repositoryConnection
	 *            Connection to the semantic repository
	 * @param queryString
	 *            Query for execution
	 * @param bindings
	 *            Query parameters
	 * @param includeInferred
	 *            Include inferred flag
	 * @param queryTimeout
	 *            Query timeout
	 * @return Boolean query that can be executed on the semantic repository * @throws SemanticPersistenceException If
	 *         and error occurs while creating the query
	 */
	public static BooleanQuery prepareBooleanQuery(RepositoryConnection repositoryConnection, String queryString,
			Map<String, Serializable> bindings, boolean includeInferred, int queryTimeout) {
		String query = appendNamespaces(queryString);

		BooleanQuery booleanQuery;
		try {
			booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			booleanQuery.setIncludeInferred(includeInferred);
			setQueryTimeout(queryTimeout, booleanQuery::setMaxQueryTime);
			setQueryBindings(query, bindings, booleanQuery::setBinding);
			return booleanQuery;
		} catch (RepositoryException | MalformedQueryException e) {
			throw new SemanticPersistenceException("Error creating Boolean query!", e);
		}
	}

	private static String appendNamespaces(String query) {
		String localQuery = query;
		String namespaces;
		if (localQuery.contains(PREFIX_IDENTIFIER)) {
			// skip appending the prefixes if they are defined in the query
			return localQuery;
		}

		if (SPARQLQueryHelper.namespaceRegistryService == null) {
			namespaces = DefaultNamespaces.ALL_NAMESPACES
					.stream()
						.map(namespace -> new StringBuilder(100)
								.append(PREFIX_IDENTIFIER)
									.append(namespace.getFirst().trim())
									.append(NamespaceRegistryService.SHORT_URI_DELIMITER)
									.append("<")
									.append(namespace.getSecond())
									.append(">")
									.toString())
						.collect(Collectors.joining("\n"));
		} else {
			namespaces = SPARQLQueryHelper.namespaceRegistryService.getNamespaces();

		}
		if (!localQuery.startsWith("PREFIX")) {
			// append all namespaces that are used in the repository
			localQuery = namespaces + "\n" + query;
		}
		return localQuery;
	}

	private static String appendQueryId(String query) {
		StringBuilder localQuery = new StringBuilder(QUERY_ID_BLOCK.length() + 40 + query.length());
		String queryId = UUID.randomUUID().toString();
		localQuery.append(QUERY_ID_BLOCK).append(queryId).append(LINE_SEPARATOR).append(query);

		LOGGER.debug("Prepearing TupleQuery with ID: {}", queryId);
		return localQuery.toString();
	}

	private static void setQueryTimeout(int queryTimeout, IntConsumer consumer) {
		// set the query timeout if a needed
		if (queryTimeout > 0) {
			consumer.accept(queryTimeout);
		}
	}

	private static void setQueryBindings(String query, Map<String, Serializable> bindings,
			BiConsumer<String, Value> consumer) {
		// set query parameters
		bindings.forEach((key, bindingValue) -> {
			if (query.contains(key)) {
				Value value = createValue(bindingValue);
				if (value != null) {
					consumer.accept(key, value);
				}
			}
		});
	}

	/**
	 * Creates literal {@link Value} by given {@link Serializable} value.
	 *
	 * @param value
	 *            the value
	 * @return the value or <code>null</code> if the value type is not recognized
	 */
	private static Value createValue(Serializable value) {
		if (value == null) {
			return null;
		}
		Serializable local = value;
		if (local instanceof String) {
			String stringValue = (String) local;
			if (stringValue.contains(":") && !stringValue.contains("\\:")) {
				return namespaceRegistryService.buildUri(stringValue);
			}
			// if the : are escaped means that it should be threaded as value
			// and not as URI
			if (stringValue.contains("\\:")) {
				stringValue = stringValue.replace("\\:", ":");
			}
			local = stringValue;
		} else if (local instanceof Value) {
			return (Value) local;
		}
		return ValueConverter.createLiteral(local);
	}

	/**
	 * Appends offset clause to the query
	 *
	 * @param query
	 *            SPARQL Query
	 * @param offset
	 *            offset number
	 * @return SPARQL query with appended offset clause
	 */
	public static String appendOffsetToQuery(String query, int offset) {
		String localQuery = query;
		if (!OFFSET_PATTERN.matcher(query).find() && offset > 0) {
			localQuery = localQuery + OFFSET + offset;
		}
		return localQuery;
	}

	/**
	 * Appends limit clause to the query
	 *
	 * @param query
	 *            SPARQL Query
	 * @param limit
	 *            limit number
	 * @return SPARQL query with appended limit clause
	 */
	public static String appendLimitToQuery(String query, int limit) {
		String localQuery = query;
		if (!LIMIT_PATTERN.matcher(query).find() && limit > 0) {
			localQuery = localQuery + LIMIT + limit;
		}
		return localQuery;
	}

	/**
	 * Setter method for comma separated list of prefix:name properties that are considered to be case insensitive
	 *
	 * @param listOfCaseInsensitiveProperties
	 *            the caseInsenitiveOrderByList to set
	 */
	public static void setCaseInsenitiveOrderByList(String listOfCaseInsensitiveProperties) {
		caseInsenitiveOrderByList = listOfCaseInsensitiveProperties;
	}

	/**
	 * @return the namespaceRegistryService
	 */
	public static NamespaceRegistryService getNamespaceRegistryService() {
		return namespaceRegistryService;
	}

	/**
	 * @param namespaceRegistryService
	 *            the namespaceRegistryService to set
	 */
	public static void setNamespaceRegistryService(NamespaceRegistryService namespaceRegistryService) {
		SPARQLQueryHelper.namespaceRegistryService = namespaceRegistryService;
	}

	/**
	 * Invoke all filters for the given variable and returns the result query fragment.
	 *
	 * @param variable
	 *            the variable to pass to filters
	 * @param filters
	 *            the filters to invoke
	 * @return the query fragment or empty string if no filters are passed or the filters result was not valid.
	 */
	public static String buildFilters(String variable, List<Function<String, String>> filters) {
		StringBuilder builder = new StringBuilder(512);

		for (Function<String, String> filterBuilder : filters) {
			String filter = filterBuilder.apply(variable);
			if (StringUtils.isNotBlank(filter)) {
				builder.append(filter);
			}
		}
		return builder.toString();
	}

	/**
	 * Wraps the provided query string with another select clause for grouping by specific property. Used for
	 * aggregating results.
	 *
	 * @param query
	 *            - the string query for wrapping
	 * @param groupByProperty
	 *            - the property which is used for the group by statement
	 * @param isObjectProperty
	 *            - If the property is Object property then we must apply a check if the values are instances that are
	 *            not deleted
	 * @return the wrapped query with grouping by
	 */
	public static String wrapQueryWithGroupBy(String query, String groupByProperty, boolean isObjectProperty) {
		StringBuilder builder = new StringBuilder(query.length() + 128);

		builder.append(SELECT).append("(").append(GROUP_BY_VARIABLE).append(" ").append(AS).append(" ?name) ");
		builder.append("(").append(COUNT).append("(").append(GROUP_BY_VARIABLE).append(") ").append(AS).append(
				" ?count)");

		builder.append(WHERE).append(" { ");
		builder
				.append(OBJECT_VARIABLE)
					.append(" ")
					.append(groupByProperty)
					.append(" ")
					.append(GROUP_BY_VARIABLE)
					.append(" . ");

		if (isObjectProperty) {
			builder.append(GROUP_BY_VARIABLE).append(IS_NOT_DELETED);
		}

		builder.append(" { ").append(query).append(" } ");
		builder.append(" } ").append(GROUP_BY).append(" ").append(GROUP_BY_VARIABLE);

		return builder.toString();
	}

	/**
	 * Generates an universally unique ID for a SPARQL variable.
	 *
	 * @return - the generated variable name
	 */
	public static String generateVarName() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return new StringBuilder(30).append(VARIABLE).append("v").append(uuid).toString();
	}
}
