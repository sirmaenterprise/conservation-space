package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Builds a Solr query for a date range statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 130)
public class DateRangeSearchOperation extends AbstractSolrSearchOperation {

	private static final Collection<String> DATE_RANGE_OPERATIONS = new HashSet<>(
			Arrays.asList("between", "is", "within"));

	@Override
	public boolean isApplicable(Rule rule) {
		String operation = rule.getOperation();

		boolean isOperationValid = operation != null && DATE_RANGE_OPERATIONS.contains(operation.toLowerCase());
		boolean isRuleValid = rule.getValues().size() == 2;
		boolean isTypeValid = XMLSchema.DATETIME.getLocalName().equalsIgnoreCase(rule.getType())
				|| XMLSchema.DATE.getLocalName().equalsIgnoreCase(rule.getType());

		return isOperationValid && isRuleValid && isTypeValid;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		String fromValue = values.get(0);
		String toValue = values.get(1);

		boolean isBlankFrom = StringUtils.isBlank(fromValue);
		boolean isBlankTo = StringUtils.isBlank(toValue);

		if (isBlankFrom && isBlankTo) {
			return;
		}
		builder.append(rule.getField()).append(":[").append(isBlankFrom ? "*" : fromValue).append(" TO ").append(isBlankTo ? "*" : toValue).append("]");
	}
}
