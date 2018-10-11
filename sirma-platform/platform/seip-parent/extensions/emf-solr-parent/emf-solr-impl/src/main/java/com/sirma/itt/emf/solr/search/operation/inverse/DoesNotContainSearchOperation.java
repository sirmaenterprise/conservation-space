package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.emf.solr.search.operation.AbstractSolrSearchOperation;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a does not contain statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 40)
public class DoesNotContainSearchOperation extends AbstractSolrSearchOperation {

    private static final String DOES_NOT_CONTAIN_OPERATION = "does_not_contain";
    private static final String DOES_NOT_CONTAIN_QUERY = "*%s*";

    @Override
    public boolean isApplicable(Rule rule) {
        return DOES_NOT_CONTAIN_OPERATION.equalsIgnoreCase(rule.getOperation());
    }

    @Override
    public void buildOperation(StringBuilder builder, Rule rule) {
        appendNegatedQuery(builder, rule, DOES_NOT_CONTAIN_QUERY);
    }
}
