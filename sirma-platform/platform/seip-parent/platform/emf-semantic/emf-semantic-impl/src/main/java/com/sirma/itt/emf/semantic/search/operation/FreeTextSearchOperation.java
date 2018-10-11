package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Builds a SPARQL query for a free text search statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 250)
public class FreeTextSearchOperation implements SearchOperation {

	@Inject
	private FreeTextSearchProcessor freeTextSearchProcessor;

	@Inject
	private SemanticConfiguration semanticConfigurations;

	@Override
	public boolean isApplicable(Rule rule) {
		return "contains".equals(rule.getOperation()) && "fts".equals(rule.getType());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		if (!FreeTextSearchOperation.isValid(rule)) {
			return;
		}

		String ftsIndexName = semanticConfigurations.getFtsIndexName().get();
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		builder.append("?search a ").append(ftsIndexName).append(" ; ");

		String preparedSolrQuery = freeTextSearchProcessor.process(rule.getValues().get(0));

		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		builder.append(SPARQLQueryHelper.SOLR_QUERY).append(" '''").append(preparedSolrQuery).append("''' ; ");

		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		SemanticSearchOperationUtils.appendTriple(builder, "", SPARQLQueryHelper.SOLR_ENTITIES,
												  SPARQLQueryHelper.OBJECT_VARIABLE);
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
	}

	private static boolean isValid(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && StringUtils.isNotBlank(rule.getValues().get(0));
	}

}
