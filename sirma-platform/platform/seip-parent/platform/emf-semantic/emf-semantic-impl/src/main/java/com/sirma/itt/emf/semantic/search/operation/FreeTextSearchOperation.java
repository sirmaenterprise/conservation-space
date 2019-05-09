package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a free text search statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 250)
public class FreeTextSearchOperation extends SolrSearchOperation {

	@Inject
	private FreeTextSearchProcessor freeTextSearchProcessor;

	@Override
	public boolean isApplicable(Rule rule) {
		return "contains".equals(rule.getOperation()) && "fts".equals(rule.getType());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		if (!FreeTextSearchOperation.isValid(rule)) {
			return;
		}

		String preparedSolrQuery = freeTextSearchProcessor.buildFreeTextSearchQuery(rule.getValues().get(0));
		buildOperation(builder, preparedSolrQuery);
	}

	private static boolean isValid(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && StringUtils.isNotBlank(rule.getValues().get(0));
	}

}
