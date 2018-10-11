package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;

import java.util.stream.Collectors;

import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.CLOSE_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OPEN_BRACKET;

/**
 * Abstract class for most common logic for build solr queries.
 *
 * @author Hristo Lungov
 */
public abstract class AbstractSolrSearchOperation implements SearchOperation {

    /**
     * Builds base query.
     *
     * @param builder - the query builder where query will be appended
     * @param rule - the provided search rule with value &amp; operation
     * @param queryFormat - the string format to use for appending to query
     */
    public void appendQuery(StringBuilder builder, Rule rule, String queryFormat) {
        builder.append(OPEN_BRACKET);
        builder.append(rule.getValues().stream().map(value -> rule.getField()+ ":(" + String.format(queryFormat, value) + ")").collect(Collectors.joining(" OR ")));
        builder.append(CLOSE_BRACKET);
    }

    /**
     * Builds base negated query.
     *
     * @param builder
     *            - the query builder where query will be appended
     * @param rule
     *            - the provided search rule with value &amp; operation
     * @param queryFormat - the string format to use for appending to query
     */
    public void appendNegatedQuery(StringBuilder builder, Rule rule, String queryFormat) {
        builder.append("-");
        appendQuery(builder, rule, queryFormat);
    }
}
