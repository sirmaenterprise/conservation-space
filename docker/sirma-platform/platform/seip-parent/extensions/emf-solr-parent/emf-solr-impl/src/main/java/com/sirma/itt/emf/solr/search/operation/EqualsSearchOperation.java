package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.CLOSE_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OPEN_BRACKET;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.OR;
import static com.sirma.itt.emf.solr.search.operation.SolrSearchOperationUtils.buildSingleValuedClause;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;

/**
 * Builds a Solr query for an equals statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 10)
public class EqualsSearchOperation extends AbstractSolrSearchOperation {

	private static final String EQUALS_OPERATION = "equals";

	private static final String OBJECT_TYPE_FIELD = "objecttype";
	private static final String OBJECT_SUB_TYPE_FIELD = "objectsubtype";

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public boolean isApplicable(Rule rule) {
		return EQUALS_OPERATION.equalsIgnoreCase(rule.getOperation());
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

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		// TODO: This should be a search operation in the audit log, not here.
		if ("types".equalsIgnoreCase(rule.getField())) {
			if (rule.getValues().contains(ANY_OBJECT)) {
				builder.append("*:*");
				return;
			}

			builder.append(OPEN_BRACKET);

			List<String> semanticClasses = getSemanticClasses(rule);
			if (!semanticClasses.isEmpty()) {
				appendSemanticClasses(builder, semanticClasses);
			}

			List<String> definitionIds = getDefinitionIds(rule);
			if (!definitionIds.isEmpty()) {
				if (!semanticClasses.isEmpty()) {
					builder.append(OR);
				}
				appendDefinitionTypes(builder, definitionIds);
			}

			builder.append(CLOSE_BRACKET);
		} else {
			appendQuery(builder, rule, "%s");
		}
	}

	private void appendSemanticClasses(StringBuilder builder, List<String> semanticClasses) {
		builder.append(semanticClasses.stream().map(semanticClass -> {
			Set<ClassInstance> subClasses = semanticDefinitionService.collectSubclasses(semanticClass);
			Set<ClassInstance> allClasses = CollectionUtils.createLinkedHashSet(subClasses.size() + 1);
			// Include the main type too, there may be abstract records
			allClasses.add(semanticDefinitionService.getClassInstance(semanticClass));
			allClasses.addAll(subClasses);

			return allClasses.stream()
					.map(EmfInstance::getId)
					.map(id -> typeConverter.convert(ShortUri.class, id))
					.map(shortUri -> buildSemanticTypeClause(shortUri.toString()))
					.collect(Collectors.joining(OR));

		}).collect(Collectors.joining(OR)));
	}

	private static void appendDefinitionTypes(StringBuilder builder, List<String> definitionIds) {
		builder.append(definitionIds.stream()
							   .map(EqualsSearchOperation::buildDefinitionTypeClause)
							   .collect(Collectors.joining(OR)));
	}

	private static String buildSemanticTypeClause(String uri) {
		return buildSingleValuedClause(OBJECT_TYPE_FIELD, uri);
	}

	private static String buildDefinitionTypeClause(String definitionId) {
		return buildSingleValuedClause(OBJECT_SUB_TYPE_FIELD, definitionId);
	}
}
