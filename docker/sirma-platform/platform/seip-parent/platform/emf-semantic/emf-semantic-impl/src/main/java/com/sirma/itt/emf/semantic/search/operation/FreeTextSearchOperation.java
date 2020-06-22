package com.sirma.itt.emf.semantic.search.operation;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.search.SearchContext;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
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
	public void buildOperation(StringBuilder builder, Rule rule, SearchContext searchContext) {
		if (!FreeTextSearchOperation.isValid(rule)) {
			return;
		}
		List<String> types = getTypes(searchContext);

		String preparedSolrQuery = freeTextSearchProcessor.buildFreeTextSearchQuery(rule.getValues().get(0), types);
		buildOperation(builder, preparedSolrQuery);
	}

	private List<String> getTypes(SearchContext searchContext) {
		List<String> types = Collections.emptyList();
		if (searchContext.getRoot().getRules().isEmpty()) {
			return types;
		}
		SearchNode searchNode = searchContext.getRoot().getRules().get(0);
		if (searchNode instanceof Condition && !((Condition) searchNode).getRules().isEmpty()) {
			SearchNode typesNode = ((Condition) searchNode).getRules().get(0);
			if (typesNode instanceof Rule && "types".equals(((Rule) typesNode).getField())) {
				 types = ((Rule) typesNode).getValues();
			}
		}
		if (types.size() == 1 && types.contains("anyObject")) {
			types = Collections.emptyList();
		}
		return types;
	}

	private static boolean isValid(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && StringUtils.isNotBlank(rule.getValues().get(0));
	}

}
