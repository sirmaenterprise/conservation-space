package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.plugin.Extension;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.URI_SEPARATOR;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.DOUBLE_QUOTE;
import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.INSTANCE_VAR;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.semantic.model.vocabulary.EMF.PREFIX;

/**
 * Builds a SPARQL query for an equals statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 10)
public class EqualsSearchOperation implements SearchOperation {

	private static final String EQUALS_OPERATION = "equals";

	@Inject
	private NamespaceRegistry nameSpaceRegistry;

	@Override
	public boolean isApplicable(Rule rule) {
		return EQUALS_OPERATION.equalsIgnoreCase(rule.getOperation());
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
			appendSemanticClassesAndDefinitions(builder, rule);
			break;
		}
	}

	private void appendSemanticClassesAndDefinitions(StringBuilder builder, Rule rule) {
		List<String> semanticClasses = getSemanticClasses(rule);
		List<String> definitionIds = getDefinitionIds(rule);

		Runnable appendSemanticClasses = () -> appendSemanticClasses(builder, semanticClasses);
		Runnable appendDefinitionIds = () -> appendDefinitionIds(builder, rule, definitionIds);

		if (CollectionUtils.isNotEmpty(semanticClasses) && CollectionUtils.isNotEmpty(definitionIds)) {
			SemanticSearchOperationUtils.appendUnionBlock(builder, appendSemanticClasses, appendDefinitionIds);
		} else {
			appendSemanticClasses.run();
			appendDefinitionIds.run();
		}
	}

	private void appendSemanticClasses(StringBuilder builder, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			Runnable appendSemanticClass = () -> appendSemanticClass(builder, values.get(0), SEMANTIC_TYPE);
			if (values.size() > 1) {
				// when more than one value is present then surround the statement in brackets
				SemanticSearchOperationUtils.appendBracketBlock(builder, appendSemanticClass);
				for (int i = 1; i < values.size(); i++) {
					int index = i;
					builder.append(UNION);
					SemanticSearchOperationUtils.appendBracketBlock(builder,
							() -> appendSemanticClass(builder, values.get(index), SEMANTIC_TYPE));
				}
			} else {
				// only one value is present and no bracket blocks are needed
				appendSemanticClass.run();
			}
		}
	}

	private void appendSemanticClass(StringBuilder builder, String value, String field) {
		String fullUri = nameSpaceRegistry.buildFullUri(value);
		fullUri = "<" + fullUri + ">";
		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, field, fullUri);
	}

	private static void appendDate(StringBuilder builder, Rule rule) {
		if (!isSingleValuedAndNotBlank(rule)) {
			return;
		}

		String value = rule.getValues().get(0);
		StringBuilder valueBuilder = new StringBuilder(50);
		valueBuilder.append(DOUBLE_QUOTE).append(value).append(DOUBLE_QUOTE).append("^^xsd:").append(rule.getType());

		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, rule.getField(), valueBuilder.toString());
	}

	private static void appendDefinitionIds(StringBuilder builder, Rule rule, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			Rule localRule = SearchCriteriaBuilder.createRuleBuilder()
					.setField(PREFIX + URI_SEPARATOR + TYPE)
					.setType("string")
					.setOperation(rule.getOperation())
					.setValues(values)
					.setId(rule.getId())
					.build();
			SemanticSearchOperationUtils.appendStringTripleAndFilter(builder, localRule, true, true);
		}
	}

	private static List<String> getDefinitionIds(Rule rule) {
		return rule.getValues()
				.stream()
				.filter(value -> !value.startsWith("http") && !value.contains(":"))
				.collect(Collectors.toList());
	}

	private static List<String> getSemanticClasses(Rule rule) {
		return rule.getValues()
				.stream()
				.filter(value -> value.startsWith("http") || value.contains(":"))
				.collect(Collectors.toList());
	}

	private static boolean isSingleValuedAndNotBlank(Rule rule) {
		if (rule.getValues().size() != 1) {
			return false;
		}

		String value = rule.getValues().get(0);
		return !StringUtils.isBlank(value);
	}
}
