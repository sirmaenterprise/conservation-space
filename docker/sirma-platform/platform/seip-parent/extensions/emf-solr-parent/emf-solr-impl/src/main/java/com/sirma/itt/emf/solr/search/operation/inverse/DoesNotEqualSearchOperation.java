package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.emf.solr.search.operation.AbstractSolrSearchOperation;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Builds a Solr query for a does not equal statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 20)
public class DoesNotEqualSearchOperation extends AbstractSolrSearchOperation {

	private static final Collection<String> DOES_NOT_EQUAL_OPERATIONS = new HashSet<>(
			Arrays.asList("does_not_equal", "not_in"));
	private static final String DOES_NOT_EQUAL_QUERY_FORMAT = "%s";

	@Override
	public boolean isApplicable(Rule rule) {
		String operation = rule.getOperation();
		return operation != null && DOES_NOT_EQUAL_OPERATIONS.contains(operation.toLowerCase());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendNegatedQuery(builder, rule, DOES_NOT_EQUAL_QUERY_FORMAT);
	}

}
