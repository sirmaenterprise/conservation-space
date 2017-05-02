package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Builds a SPARQL query for a free text search statement from a provided {@link Rule}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.EXTENSION_NAME, order = 250)
public class FreeTextSearchOperation implements SearchOperation {

	@Inject
	private FTSQueryParser ftsQueryParser;

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
		builder.append("?search a ").append(ftsIndexName).append(" ; ");

		String preparedSolrQuery = ftsQueryParser.prepare(rule.getValues().get(0));
		String escapedSolrQuery = StringEscapeUtils.escapeJava(preparedSolrQuery);

		builder.append("solr:query \"").append(escapedSolrQuery).append("\" ; ");
		builder.append("solr:entities ?instance . ");
	}

	private static boolean isValid(Rule rule) {
		if (SemanticSearchOperationUtils.isRuleEmpty(rule)) {
			return false;
		}

		String value = rule.getValues().get(0);
		if (StringUtils.isBlank(value)) {
			return false;
		}

		return true;
	}

}
