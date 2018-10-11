package com.sirma.itt.emf.semantic.search;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.*;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.domain.search.tree.SearchNode.NodeType;
import com.sirma.itt.seip.plugin.ExtensionPoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Builder for a SPARQL query using the {@link Condition search tree model}.
 *
 * @author yasko
 */
@ApplicationScoped
public class SearchQueryBuilder {
	private static final char SPACE = ' ';
	private static final String ERROR_LABEL_ID = "query.builder.operation_not_found";

	@Inject
	@ExtensionPoint(SearchOperation.SPARQL_SEARCH_OPERATION)
	private Iterable<SearchOperation> sparqlSearchOperations;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Walks the provided search tree and generates a SPARQL where clause.
	 *
	 * @param tree
	 *            {@link Condition Search tree} to walk.
	 * @return The generated SPARQL where clause as string.
	 */
	public String build(Condition tree) {
		StringBuilder q = new StringBuilder(512);

		q.append(SPARQLQueryHelper.CURLY_BRACKET_OPEN);
		buildRules(q, tree.getRules(), tree.getCondition());
		q.append(SPACE).append(PERMISSIONS_BLOCK_CONSTANT).append(OBJECT).append(CURLY_BRACKET_CLOSE);
		return q.toString();
	}

	private void buildRules(StringBuilder q, List<SearchNode> rules, Junction junction) {
		Iterator<SearchNode> iterator = rules.iterator();

		while (iterator.hasNext()) {
			SearchNode node = iterator.next();

			if (Junction.OR == junction) {
				q.append(CURLY_BRACKET_OPEN);
			}

			if (NodeType.CONDITION == node.getNodeType()) {
				Condition c = (Condition) node;
				buildRules(q, c.getRules(), c.getCondition());
			} else {
				buildRule(q, (Rule) node);
			}

			if (Junction.OR == junction) {
				q.append(CURLY_BRACKET_CLOSE);
				if (iterator.hasNext()) {
					q.append(UNION);
				}
			}
		}
	}

	private void buildRule(StringBuilder builder, Rule rule) {
		if (SemanticSearchOperationUtils.isRuleEmpty(rule)) {
			return;
		}
		SearchOperation operation = null;
		for (SearchOperation op : sparqlSearchOperations) {
			if (op.isApplicable(rule)) {
				operation = op;
				break;
			}
		}

		if (operation == null) {
			throw new EmfApplicationException(
					String.format(MessageFormat.format(labelProvider.getValue(ERROR_LABEL_ID), rule.getOperation())));
		}

		operation.buildOperation(builder, rule);
	}



}
