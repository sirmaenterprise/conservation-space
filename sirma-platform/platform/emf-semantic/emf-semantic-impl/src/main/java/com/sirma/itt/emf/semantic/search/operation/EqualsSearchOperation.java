package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.DOUBLE_QUOTE;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.INSTANCE_VAR;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.semantic.model.vocabulary.EMF.PREFIX;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for an equals statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.EXTENSION_NAME, order = 10)
public class EqualsSearchOperation implements SearchOperation {

	private static final String EQUALS_OPERATION = "equals";

	@Inject
	private NamespaceRegistry nameSpaceRegistry;

	@Override
	public boolean isApplicable(Rule rule) {
		return EQUALS_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& !SemanticSearchOperationUtils.isRuleEmpty(rule);
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		switch (rule.getType()) {
			case "date":
			case "dateTime":
				appendDate(builder, rule);
				break;
			case "rdfs:Literal":
			case "string":
				SemanticSearchOperationUtils.appendStringTripleAndFilter(builder, rule, true, true);
				break;
			case "numeric":
				SemanticSearchOperationUtils.appendNumericStatement(builder, rule, ArithmeticOperators.EQUALS);
				break;
			default:
				List<String> semanticClasses = getSemanticClasses(rule);
				List<String> definitionIds = getDefinitionIds(rule);
				appendSemanticClasses(builder, semanticClasses);
				if (CollectionUtils.isNotEmpty(semanticClasses) && CollectionUtils.isNotEmpty(definitionIds)) {
					builder.append(UNION);
				}
				appendDefinitionIds(builder, rule, definitionIds);
				break;
		}

	}

	private static void appendDate(StringBuilder builder, Rule rule) {
		if (!isSingleValuedAndNotBlank(rule)) {
			return;
		}

		String value = rule.getValues().get(0);

		builder.append(CURLY_BRACKET_OPEN);

		StringBuilder valueBuilder = new StringBuilder(50);
		valueBuilder.append(DOUBLE_QUOTE).append(value).append(DOUBLE_QUOTE).append("^^xsd:").append(rule.getType());

		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, rule.getField(), valueBuilder.toString());
		builder.append(CURLY_BRACKET_CLOSE);
	}

	private void appendSemanticClasses(StringBuilder builder, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			appendSemanticClass(builder, values.get(0), SEMANTIC_TYPE);
			for (int i = 1; i < values.size(); i++) {
				builder.append(UNION);
				appendSemanticClass(builder, values.get(i), SEMANTIC_TYPE);
			}
		}
	}

	private void appendSemanticClass(StringBuilder builder, String value, String field) {
		String fullUri = nameSpaceRegistry.buildFullUri(value);
		fullUri = "<" + fullUri + ">";

		builder.append(CURLY_BRACKET_OPEN);
		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, field, fullUri);
		builder.append(CURLY_BRACKET_CLOSE);
	}

	private static void appendDefinitionIds(StringBuilder builder, Rule rule, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			Rule localRule = new Rule();
			localRule.setField(PREFIX + URI_SEPARATOR + TYPE);
			localRule.setType("string");
			localRule.setOperation(rule.getOperation());
			localRule.setValues(values);
			localRule.setId(rule.getId());
			SemanticSearchOperationUtils.appendStringTripleAndFilter(builder, localRule, true, true);
		}
	}

	private static List<String> getDefinitionIds(Rule rule) {
		return rule.getValues().stream().filter(value -> !value.startsWith("http") && !value.contains(":")).collect(
				Collectors.toList());
	}

	private static List<String> getSemanticClasses(Rule rule) {
		return rule.getValues().stream().filter(value -> value.startsWith("http") || value.contains(":")).collect(
				Collectors.toList());
	}

	private static boolean isSingleValuedAndNotBlank(Rule rule) {
		if (rule.getValues().size() != 1) {
			return false;
		}

		String value = rule.getValues().get(0);
		if (StringUtils.isBlank(value)) {
			return false;
		}

		return true;
	}
}
