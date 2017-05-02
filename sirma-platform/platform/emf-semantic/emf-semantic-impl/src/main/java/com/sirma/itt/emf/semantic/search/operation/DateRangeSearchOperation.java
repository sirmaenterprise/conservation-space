package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.INSTANCE_VAR;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a date range statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.EXTENSION_NAME, order = 130)
public class DateRangeSearchOperation implements SearchOperation {

	private static final Collection<String> DATE_RANGE_OPERATIONS = new HashSet<>(
			Arrays.asList("between", "is", "within"));

	@Override
	public boolean isApplicable(Rule rule) {
		String dateType = XMLSchema.DATE.getLocalName();
		String dateTimeType = XMLSchema.DATETIME.getLocalName();
		String operation = rule.getOperation();
		return operation != null && (dateTimeType.equalsIgnoreCase(rule.getType()) || dateType.equalsIgnoreCase(
				rule.getType())) && DATE_RANGE_OPERATIONS.contains(operation.toLowerCase())
				&& !SemanticSearchOperationUtils.isRuleEmpty(rule) && rule.getValues().size() == 2;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		String from = values.get(0);
		String to = values.get(1);

		boolean isFromBlank = StringUtils.isBlank(from);
		boolean isToBlank = StringUtils.isBlank(to);

		if (isFromBlank && isToBlank) {
			return;
		}

		builder.append(CURLY_BRACKET_OPEN);

		String var = SPARQLQueryHelper.generateVarName();
		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, rule.getField(), var);

		if (!isFromBlank) {
			// After filter
			SemanticSearchOperationUtils.appendDateFilter(builder, var, from, false);
		}

		if (!isToBlank) {
			// Before filter
			SemanticSearchOperationUtils.appendDateFilter(builder, var, to, true);
		}

		builder.append(CURLY_BRACKET_CLOSE);
	}

}
