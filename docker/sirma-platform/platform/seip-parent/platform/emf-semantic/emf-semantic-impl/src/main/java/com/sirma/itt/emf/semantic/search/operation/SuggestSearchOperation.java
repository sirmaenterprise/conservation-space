package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;

/**
 * Builds SPARQL query, which execute solr keyword search in a field. The keyword and field are provided from {@link Rule}.
 * The keyword is taken from {@link Rule#getValues()}. The field is taken from {@link Rule#getField()}.
 *
 * Example of build query:
 * <pre>
 *     {
 *        ?search a solr-inst:fts_docker_bg ;
 *        solr:query '''
 *           {
 *              "q":"*Mimetype*",
 *              "q":"({!edismax v=q qf=altTitle})",
 *              "tie":"0.1","defType":"edismax"
 *            }''' ;
 *        solr:entities ?instance.
 *     }
 * </pre>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>, btonchev
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 255)
public class SuggestSearchOperation extends SolrSearchOperation {

	private static final String OPERATION = "suggest";

	@Inject
	private FreeTextSearchProcessor freeTextSearchProcessor;


	@Override
	public boolean isApplicable(Rule rule) {
		return OPERATION.equals(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {

		if (!SuggestSearchOperation.isValid(rule)) {
			return;
		}

		String field = rule.getField();
		String searchTerms = rule.getValues().get(0);
		buildOperation(builder, freeTextSearchProcessor.buildFieldSuggestionQuery(field, searchTerms));
	}

	private static boolean isValid(Rule rule) {
		return !SemanticSearchOperationUtils.isRuleEmpty(rule) && StringUtils.isNotBlank(rule.getValues().get(0));
	}
}
