package com.sirma.itt.emf.semantic.search.operation.query;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.IS_NOT_DELETED;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchTreeNode;
import com.sirma.itt.seip.domain.search.tree.SearchTreeNode.NodeType;
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

	@Inject
	private SearchQueryBuilder searchQueryBuilder;

	@Inject
	private JsonToConditionConverter convertor;

	@Inject
	private SemanticSearchConfigurations configurations;

	@Override
	public boolean isApplicable(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && rule.getValues().size() == 1
				&& isQueryValid(JSON.readObject(rule.getValues().get(0), convertor::parseCondition));
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		String instanceVariableName = SPARQLQueryHelper.generateVarName();
		appendQuery(builder, rule, instanceVariableName);
	}

	/**
	 * Check if the embedded query has more embedded queries in it. This check limits the embedded queries to two
	 * levels.
	 *
	 * @param condition
	 *            the embedded query condition
	 * @return true if it has no more embedded queries, false otherwise
	 */
	private static boolean isQueryValid(Condition condition) {
		for (SearchTreeNode node : condition.getRules()) {
			if (NodeType.CONDITION == node.getNodeType()) {
				return isQueryValid((Condition) node);
			}
			for (String value : ((Rule) node).getValues()) {
				if (JsonUtil.isJsonObject(value)) {
					return false;
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
	protected static String replaceInstanceVariableName(String instanceVariableName, String query) {
		return query.replaceAll(OBJECT, instanceVariableName);
	}

	/**
	 * Append the embedded query. A newly generated predicate variable name will be generated and it will replace all
	 * occurrences of the original one in the embedded query.
	 *
	 * @param builder
	 *            the string builder where the clause will be appended
	 * @param rule
	 *            the rule containing the criteria information
	 * @param query
	 *            the embedded query
	 */
	protected void appendEmbeddedQuery(StringBuilder builder, Rule rule, String instanceVariableName) {
		Condition condition = JSON.readObject(rule.getValues().get(0), convertor::parseCondition);
		String subQuery = searchQueryBuilder.build(condition);
		
		builder.append(CURLY_BRACKET_OPEN).append(SPARQLQueryHelper.SELECT)
			.append(instanceVariableName)
			.append(SPARQLQueryHelper.WHERE)
			.append(CURLY_BRACKET_OPEN).append(" ");

		// Append the instance not deleted and ignore instance types triples.
		builder.append(instanceVariableName).append(IS_NOT_DELETED);
		String instanceTypeVariable = instanceVariableName + "_type";
		SemanticSearchOperationUtils.appendTriple(builder, instanceVariableName,
				EMF.PREFIX + URI_SEPARATOR + EMF.INSTANCE_TYPE.getLocalName(), instanceTypeVariable);
		builder.append(SemanticSearchOperationUtils
				.buildIgnoreInstancesForType(configurations.getIgnoreInstanceTypes().get(), instanceTypeVariable));

		String localQuery = replaceInstanceVariableName(instanceVariableName.substring(1), subQuery);
		
		builder.append(localQuery);
		builder.append(CURLY_BRACKET_CLOSE).append(CURLY_BRACKET_CLOSE);
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
