package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;

/**
 * Builds SPARQL query, which execute solr keyword search in a field. The keyword and field are provided from {@link Rule}.
 * The keyword is taken from {@link Rule#getValues()}. The field is taken from {@link Rule#getField()}.
 * The query will contains three solr queries exact match, start with and contains, they will be executed in same order.
 *
 * Example of build query:
 * <pre>
 *     {
 *        ?search a solr-inst:fts_docker_bg ;
 *        solr:query '''{"q":<b>"user"</b>,"df":<b>"altTitle"</b>}''' ;
 *        solr:entities ?instance.
 *     } UNION {
 *        ?search a solr-inst:fts_docker_bg ;
 *        solr:query '''{"q":<b>"user*"</b>,"df":"<b>altTitle"</b>}''' ;
 *        solr:entities ?instance.
 *     } UNION {
 *        ?search a solr-inst:fts_docker_bg ;
 *        solr:query '''{"q":<b>"*user*"</b>,"df":<b>"altTitle"</b>}''' ;
 *        solr:entities ?instance.
 *     }
 * </pre>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>, btonchev
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 255)
public class SuggestSearchOperation implements SearchOperation {

	private static final String OPERATION = "suggest";

	@Inject
	private FreeTextSearchProcessor freeTextSearchProcessor;

	@Inject
	private SemanticConfiguration semanticConfigurations;

	@Override
	public boolean isApplicable(Rule rule) {
		return OPERATION.equals(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {

		if (!SuggestSearchOperation.isValid(rule)) {
			return;
		}

		String ftsIndexName = semanticConfigurations.getFtsIndexName().get();
		String field = rule.getField();
		String value = rule.getValues().get(0);

		buildExactMatchSolrQuery(builder, ftsIndexName, field, value);
		builder.append(SPARQLQueryHelper.UNION);
		getStartWithSolrQuery(builder, ftsIndexName, field, value);
		builder.append(SPARQLQueryHelper.UNION);
		getContainsSolrQuery(builder, ftsIndexName, field, value);
	}

	private static boolean isValid(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && StringUtils.isNotBlank(rule.getValues().get(0));
	}

	private void buildExactMatchSolrQuery(StringBuilder builder, String ftsIndexName, String field,
			String searchTerms) {
		String exactMatchSolrQuery = getSolrQuery(field, searchTerms);
		buildSubOperation(builder, ftsIndexName, exactMatchSolrQuery);
	}

	private void getStartWithSolrQuery(StringBuilder builder, String ftsIndexName, String field, String searchTerms) {
		String exactMatchSolrQuery = getSolrQuery(field, searchTerms + "*");
		buildSubOperation(builder, ftsIndexName, exactMatchSolrQuery);
	}

	private void getContainsSolrQuery(StringBuilder builder, String ftsIndexName, String field, String searchTerms) {
		String exactMatchSolrQuery = getSolrQuery(field, "*" + searchTerms + "*");
		buildSubOperation(builder, ftsIndexName, exactMatchSolrQuery);
	}

	private String getSolrQuery(String field, String searchTerms) {
		return freeTextSearchProcessor.process(field, searchTerms, false);
	}

	private void buildSubOperation(StringBuilder builder, String ftsIndexName, String solrSearchQuery) {
		builder.append(SPARQLQueryHelper.CURLY_BRACKET_OPEN);
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		builder.append("?search a ").append(ftsIndexName).append(" ; ");
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		builder.append(SPARQLQueryHelper.SOLR_QUERY).append(" '''").append(solrSearchQuery).append("''' ; ");
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		SemanticSearchOperationUtils.appendTriple(builder, "", SPARQLQueryHelper.SOLR_ENTITIES,
												  SPARQLQueryHelper.OBJECT_VARIABLE);
		builder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		builder.append(SPARQLQueryHelper.CURLY_BRACKET_CLOSE);
	}
}
