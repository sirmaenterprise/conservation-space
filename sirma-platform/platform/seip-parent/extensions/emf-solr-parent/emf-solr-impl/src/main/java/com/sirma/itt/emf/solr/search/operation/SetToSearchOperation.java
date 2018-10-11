package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

import java.util.stream.Collectors;

import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.CLOSE_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OPEN_BRACKET;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;

/**
 * Builds a Solr query statement for set relations from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 150)
public class SetToSearchOperation extends AbstractSolrSearchOperation {

	private static final String SET_TO_OPERATION = "set_to";

	@Override
	public boolean isApplicable(Rule rule) {
		return SET_TO_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		builder.append(OPEN_BRACKET);
		builder.append(rule.getValues().stream().map(value -> rule.getField() + (ANY_OBJECT.equalsIgnoreCase(value) ? ":(*)" : ":(" + value.replace(":", "\\:") + ")")).collect(Collectors.joining(" OR ")));
		builder.append(CLOSE_BRACKET);
	}
}
