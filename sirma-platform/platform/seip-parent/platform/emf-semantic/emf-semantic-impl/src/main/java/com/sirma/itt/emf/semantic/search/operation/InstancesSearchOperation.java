package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CLOSE_BRACKET;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FILTER_BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FILTER_OR;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.INSTANCE_VAR;

import java.util.List;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a filter clause that will filter the results based on the provided instance ids.
 *
 * @author nvelkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 260)
public class InstancesSearchOperation implements SearchOperation {

	private static final String IN_INSTANCE_IDS_OPERATION = "in";

	@Override
	public boolean isApplicable(Rule rule) {
		return IN_INSTANCE_IDS_OPERATION.equalsIgnoreCase(rule.getOperation()) && "instanceId".equals(rule.getField());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		if (values.size() > 1000) {
			SemanticSearchOperationUtils.appendValuesBlock(builder, INSTANCE_VAR, values);
		} else {
			builder.append(values.stream().map(id -> INSTANCE_VAR + " = " + id).collect(
					Collectors.joining(FILTER_OR, FILTER_BLOCK_START, CLOSE_BRACKET)));
		}
	}

}
