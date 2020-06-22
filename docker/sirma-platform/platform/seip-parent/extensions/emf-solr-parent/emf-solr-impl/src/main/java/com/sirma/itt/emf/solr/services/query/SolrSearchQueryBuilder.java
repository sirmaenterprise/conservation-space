package com.sirma.itt.emf.solr.services.query;

import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.AND;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.CLOSE_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OPEN_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OR;

import java.text.MessageFormat;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Builder for a Solr query using the {@link Condition search tree model}.
 *
 * @author Hristo Lungov
 */
@ApplicationScoped
public class SolrSearchQueryBuilder {

	private static final String ERROR_LABEL_ID = "query.builder.operation_not_found";
	private static final int BUILDER_FACTOR = 16;

	@Inject
	@ExtensionPoint(SearchOperation.SOLR_SEARCH_OPERATION)
	private Iterable<SearchOperation> solrSearchOperations;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Walks the provided search tree and generates a solr query.
	 *
	 * @param tree {@link Condition Search tree} to walk.
	 * @return The generated solr query as string.
	 */
	public String buildSolrQuery(Condition tree) {
		StringBuilder queryBuilder = new StringBuilder(512);
		buildSolrRules(queryBuilder, tree.getRules(), tree.getCondition());
		return queryBuilder.toString();
	}

	private void buildSolrRules(StringBuilder builder, List<SearchNode> nodes, Condition.Junction junction) {
		for (SearchNode node : nodes) {
			// Inner builder used to skip appending eventual empty nested rules/conditions
			StringBuilder innerBuilder;

			if (SearchNode.NodeType.CONDITION == node.getNodeType()) {
				innerBuilder = new StringBuilder(BUILDER_FACTOR * nodes.size());
				Condition condition = (Condition) node;
				buildSolrRules(innerBuilder, condition.getRules(), condition.getCondition());
			} else {
				innerBuilder = new StringBuilder(BUILDER_FACTOR);
				buildSolrRule(innerBuilder, (Rule) node);
			}

			if (innerBuilder.length() > 0) {
				// Append AND/OR only if there are previous statements for the current level
				if (Condition.Junction.OR == junction && builder.length() > 0) {
					builder.append(OR);
				} else if (builder.length() > 0) {
					builder.append(AND);
				}
				if (SearchNode.NodeType.CONDITION == node.getNodeType()) {
					// If the operation is not_set_to then we should not wrap in brackets. Currently there's a
					// problem in solr is we wish to run something like (-(a:b)). The parser can't handle with it the
					// outer bracket.
					// TODO: After solr update remove the if and re-test this CMF-25915
					if (innerBuilder.toString().indexOf('-') == 0) {
						builder.append(innerBuilder);
					} else {
						// Wrap nested conditions with brackets
						builder.append(OPEN_BRACKET).append(innerBuilder).append(CLOSE_BRACKET);
					}
				} else {
					builder.append(innerBuilder);
				}
			}
		}
	}

	private void buildSolrRule(StringBuilder builder, Rule rule) {
		if (SolrSearchOperationUtils.isRuleEmpty(rule)) {
			return;
		}

		for (SearchOperation operation : solrSearchOperations) {
			if (operation.isApplicable(rule)) {
				operation.buildOperation(builder, rule);
				return;
			}
		}

		throw new EmfApplicationException(
				MessageFormat.format(labelProvider.getValue(ERROR_LABEL_ID), rule.getOperation()));
	}
}
