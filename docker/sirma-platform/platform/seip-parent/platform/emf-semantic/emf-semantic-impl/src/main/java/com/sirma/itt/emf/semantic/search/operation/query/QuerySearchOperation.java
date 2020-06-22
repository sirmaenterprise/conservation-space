package com.sirma.itt.emf.semantic.search.operation.query;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.IS_NOT_DELETED;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.domain.search.tree.SearchNode.NodeType;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Builds a SPARQL query for an embedded query with statement from a provided {@link Rule}. The embedded query is
 * generated and appended in here but the triple that links the outer query to the embedded one must be generated and
 * appended by the implementing class.
 *
 * @author nvelkov
 */
public abstract class QuerySearchOperation implements SearchOperation {

	private static final int MAX_LEVEL = 3;

	@Inject
	private SearchQueryBuilder searchQueryBuilder;

	@Inject
	private JsonToConditionConverter converter;

	@Inject
	private SemanticSearchConfigurations configurations;

	@Override
	public boolean isApplicable(Rule rule) {
		// Allow at most 3 query levels, begin from second for the provided rule
		return rule.getValues().size() == 1
				&& isQueryValid(JSON.readObject(rule.getValues().get(0), converter::parseCondition), 2);
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		String instanceVariableName = SPARQLQueryHelper.generateVarName();
		appendQuery(builder, rule, instanceVariableName);
	}

	/**
	 * Check if the embedded query has more embedded queries in it. This check limits the embedded queries to three
	 * levels.
	 *
	 * @param condition
	 *            the embedded query condition
	 * @return true if it has no more embedded queries, false otherwise
	 */
	private boolean isQueryValid(Condition condition, int level) {
		for (SearchNode node : condition.getRules()) {
			if (NodeType.CONDITION == node.getNodeType()) {
				return isQueryValid((Condition) node, level);
			}
			for (String value : ((Rule) node).getValues()) {
				if (JsonUtil.isJsonObject(value)) {
					return level < MAX_LEVEL && isQueryValid(JSON.readObject(value, converter::parseCondition), level + 1);
				}
			}
		}
		return true;
	}

	/**
	 * Replace all occurrences of the instance variable name in the query with the new one. Used in the embedded query
	 * to replace the added ?instance variables and the $permission_block$instance with the newly generated ones.
	 *
	 * @param instanceVariableName
	 *            the new variable name
	 * @param query
	 *            the query in which the variable should be replaced
	 * @return the query with replaced values
	 */
	private static String replaceInstanceVariableName(String instanceVariableName, String query) {
		return query.replaceAll("\\" + VARIABLE + OBJECT, VARIABLE + instanceVariableName);
	}

	/**
	 * Appends a SPARQL query from the provided {@link Rule} that contains an embedded {@link Condition} tree. All
	 * instance variable occurrences are replaced with the given variable name for uniqueness.
	 * <p>
	 * The query also includes any instance types in {@link SemanticSearchConfigurations#getIgnoreInstanceTypes()} that
	 * should be ignored.
	 *
	 * @param builder
	 * 		- the builder where the query will be appended
	 * @param rule
	 * 		- contains the condition tree
	 * @param instanceVariableName
	 * 		- the unique variable name for the embedded tree
	 */
	protected void appendEmbeddedQuery(StringBuilder builder, Rule rule, String instanceVariableName) {
		appendInstanceIgnoreTypes(builder, instanceVariableName);

		Condition condition = JSON.readObject(rule.getValues().get(0), converter::parseCondition);
		String subQuery = searchQueryBuilder.build(condition);

		String innerQuery = replaceInstanceVariableName(instanceVariableName.substring(1), subQuery);
		builder.append(innerQuery);
	}

	/**
	 * Appends a set of ignored instance types to given query builder for specific instance variable.
	 *
	 * @param builder
	 * 		- the builder where ignores will be appended
	 * @param instanceVariableName
	 * 		- the unique instance variable name
	 */
	private void appendInstanceIgnoreTypes(StringBuilder builder, String instanceVariableName) {
		// Append the instance not deleted and ignore instance types triples.
		builder.append(instanceVariableName).append(IS_NOT_DELETED);
		String instanceTypeVariable = instanceVariableName + "_type";
		SemanticSearchOperationUtils.appendTriple(builder, instanceVariableName,
												  EMF.PREFIX + URI_SEPARATOR + EMF.INSTANCE_TYPE.getLocalName(), instanceTypeVariable);
		builder.append(SemanticSearchOperationUtils
							   .buildIgnoreInstancesForType(configurations.getIgnoreInstanceTypes().get(), instanceTypeVariable));
	}

	/**
	 * Append the triple that will link the outer query to the embedded one.
	 *
	 * @param builder
	 *            the string builder where the clause will be appended
	 * @param rule
	 *            the rule containing the criteria information
	 * @param instanceVariableName
	 *            the new object variable
	 */
	protected abstract void appendQuery(StringBuilder builder, Rule rule, String instanceVariableName);
}
