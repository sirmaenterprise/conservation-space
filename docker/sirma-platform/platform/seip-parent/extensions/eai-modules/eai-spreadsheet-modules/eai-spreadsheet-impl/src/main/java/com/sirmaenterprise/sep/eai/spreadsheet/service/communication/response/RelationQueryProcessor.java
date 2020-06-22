package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldableNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ValueQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;

/**
 * {@link RelationQueryProcessor} parse and convert a string based query for object property of given object. The
 * queries should be compatible with the lucene query language.
 * 
 * @author bbanchev
 */
@Singleton
class RelationQueryProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern PATTERN_URI_ESCAPE = Pattern.compile("(\\w+)((?<!\\\\):)(\\w+)");
	private static final Pattern PATTERN_IS_URI = Pattern.compile("\\S+(?<!\\\\)[:#]\\S+");
	private static final String OPERATOR_EQUALS = "equals";
	private static final String OPERATOR_BETWEEN = "between";

	private static final StandardSyntaxParser QUERY_PARSER = new StandardSyntaxParser();
	@Inject
	private ModelService modelService;
	@Inject
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Inject
	private DateConverter dateConverter;

	RelationQueryProcessor() {
		// utility class
	}

	static class ContextProcessorParameters {
		private QueryNode query;
		private Instance context;
		private Serializable providedValue;

		protected ContextProcessorParameters(QueryNode query) {
			this.query = query;
		}

		QueryNode getQuery() {
			return query;
		}

		Serializable getProvidedValue() {
			return providedValue;
		}

		ContextProcessorParameters setProvidedValue(Serializable providedValue) {
			this.providedValue = providedValue;
			return this;
		}

		Instance getContext() {
			return context;
		}

		ContextProcessorParameters setContext(Instance context) {
			this.context = context;
			return this;

		}
	}

	private static class QueryNodeParameters {
		private String key;
		private Serializable value;
		private String operator;
		private QueryNode source;

		QueryNodeParameters(String key, Serializable value, String operator, QueryNode source) {
			this.key = key;
			this.value = value;
			this.operator = operator;
			this.source = source;
		}

		String getKey() {
			return key;
		}

		Serializable getValue() {
			return value;
		}

		String getOperator() {
			return operator;
		}

		QueryNode getSource() {
			return source;
		}

	}

	private static class ContextProcessorConfig {
		private String typeUri;
		private ModelConfiguration modelConfiguration;
		private EAIModelConverter modelConverter;

		ContextProcessorConfig(String typeUri, ModelConfiguration modelConfiguration,
				EAIModelConverter modelConverter) {
			this.typeUri = typeUri;
			this.modelConfiguration = modelConfiguration;
			this.modelConverter = modelConverter;
		}

		String getTypeUri() {
			return typeUri;
		}

		ModelConfiguration getModelConfiguration() {
			return modelConfiguration;
		}

		EAIModelConverter getModelConverter() {
			return modelConverter;
		}

	}

	/***
	 * Generates a {@link ContextProcessorParameters} source argument based on provided raw string query. If query is
	 * not valid a {@link EAIException} is thrown
	 * 
	 * @param configuration
	 *            is the query value
	 * @return the {@link ContextProcessorParameters} with filled {@link ContextProcessorParameters#getQuery()} value
	 * @throws EAIException
	 *             on parsing error
	 */
	static ContextProcessorParameters prepareParameters(String configuration) throws EAIException {
		String queryValue = null;
		try {
			synchronized (RelationQueryProcessor.class) {
				queryValue = prepareQuery(configuration);
				LOGGER.debug("Updated query '{}' to '{}'!", configuration, queryValue);
				return new ContextProcessorParameters(QUERY_PARSER.parse(queryValue, null));
			}
		} catch (QueryNodeParseException e) {
			throw new EAIException("Failed to parse query: " + queryValue, e);
		}
	}

	private static String prepareQuery(String configuration) {
		// update keyword since lucence parsers works with upper case
		String query = configuration.replaceAll("\\s+and\\s+", " AND ").replaceAll("\\s+or\\s+", " OR ");
		Matcher matcher = PATTERN_URI_ESCAPE.matcher(query);
		return updateByPattern(matcher, "\\\\:");
	}

	private static String updateByPattern(Matcher matcher, String join) {
		StringBuffer sb = new StringBuffer();// NOSONAR
		while (matcher.find()) {
			String group1 = matcher.group(1);
			String group3 = matcher.group(3);
			StringBuilder replacement = new StringBuilder(group1.length() + group3.length() + join.length());
			replacement.append(group1);
			replacement.append(join);
			replacement.append(group3);
			matcher.appendReplacement(sb, replacement.toString());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Converts a {@link ContextProcessorParameters} to SEP search {@link Condition} tree. Nested rules are supported
	 * 
	 * @param paramaters
	 *            is the source value for converting. {@link ContextProcessorParameters#getQuery()} and
	 *            {@link ContextProcessorParameters#getProvidedValue()} parameters should be non null
	 * @return the converted Condition or throws exception on failure
	 * @throws EAIException
	 *             on processing failure
	 */
	Condition convertToCondtion(ContextProcessorParameters paramaters) throws EAIException {
		Objects.requireNonNull(paramaters.getQuery(), "Query parameter should be valid query!");
		Objects.requireNonNull(paramaters.getProvidedValue(), "Search criteria should be valid parameter!");
		return iterateQuery(SearchCriteriaBuilder.createConditionBuilder(), paramaters.getQuery(), paramaters,
							new ContextProcessorConfig(getTypePropertyURI(), getModelConfiguration(), getModelConverter()));
	}

	private Condition iterateQuery(ConditionBuilder tree, QueryNode query, ContextProcessorParameters paramaters,
			ContextProcessorConfig contextProcessorConfig) throws EAIException {
		if (query instanceof AndQueryNode) {
			tree.setCondition(Junction.AND);
		} else if (query instanceof OrQueryNode) {
			tree.setCondition(Junction.OR);
		} else if (query instanceof RangeQueryNode && query.getChildren().size() == 2) {
			QueryNodeParameters node = getQueryKeyValue(query);
			tree.setRules(new ArrayList<>(1));
			tree.addRule(convertFieldToRule(node, paramaters, contextProcessorConfig));
			return tree.build();
		} else if (query == null || query.getChildren() == null) {
			throw new EAIRuntimeException("Failed to parse search query! Unexpected node: " + query);
		}
		List<QueryNode> children = query.getChildren();
		tree.setRules(new ArrayList<>(children.size()));
		for (QueryNode queryNode : children) {
			if (!queryNode.isLeaf() || queryNode instanceof GroupQueryNode) {
				iterateGroupQuery(tree, paramaters, contextProcessorConfig, queryNode);
			} else {
				QueryNodeParameters keyValue = getQueryKeyValue(queryNode);
				tree.addRule(convertFieldToRule(keyValue, paramaters, contextProcessorConfig));
			}
		}
		return tree.build();
	}

	private void iterateGroupQuery(ConditionBuilder tree, ContextProcessorParameters paramaters,
			ContextProcessorConfig contextProcessorConfig, QueryNode queryNode) throws EAIException {
		OrQueryNode booleanQuery = null;
		if (queryNode.getChildren().size() == 1 && queryNode instanceof GroupQueryNode) {
			if (queryNode.getChildren().get(0) instanceof OrQueryNode) {
				booleanQuery = (OrQueryNode) queryNode.getChildren().get(0);
			}
		} else if (queryNode instanceof OrQueryNode) {
			booleanQuery = (OrQueryNode) queryNode;
		}
		if (booleanQuery == null) {
			ConditionBuilder nested = SearchCriteriaBuilder.createConditionBuilder();
			iterateQuery(nested, queryNode, paramaters, contextProcessorConfig);
			tree.addRule(nested.build());
			return;
		}
		iterateOrClause(tree, paramaters, contextProcessorConfig, booleanQuery);
	}

	private void iterateOrClause(ConditionBuilder tree, ContextProcessorParameters paramaters,
			ContextProcessorConfig contextProcessorConfig, OrQueryNode booleanQuery) throws EAIException {
		Set<Serializable> value = new HashSet<>();
		QueryNodeParameters lastParameter = null;
		for (QueryNode queryNode : booleanQuery.getChildren()) {
			if (queryNode instanceof FieldableNode || queryNode instanceof FieldQueryNode
					|| queryNode instanceof ValueQueryNode) {
				QueryNodeParameters queryKeyValue = getQueryKeyValue(queryNode);
				if (lastParameter != null && !lastParameter.getKey().equals(queryKeyValue.getKey())) {
					value = null;
					break;
				}
				lastParameter = queryKeyValue;
				CollectionUtils.addValue(value, queryKeyValue.getValue(), true);
			}
		}
		if (value == null || lastParameter == null) {
			ConditionBuilder nested = SearchCriteriaBuilder.createConditionBuilder();

			iterateQuery(nested, booleanQuery, paramaters, contextProcessorConfig);
			tree.addRule(nested.build());
		} else {
			QueryNodeParameters keyValue = new QueryNodeParameters(lastParameter.getKey(), new ArrayList<>(value),
					lastParameter.getOperator(), booleanQuery);
			tree.addRule(convertFieldToRule(keyValue, paramaters, contextProcessorConfig));
		}
	}

	private Map<String, EntityType> detectType(QueryNode root, ContextProcessorConfig contextProcessorConfig)
			throws EAIException {
		return detectType(root, contextProcessorConfig, new HashMap<>(), new HashSet<>());
	}

	private Map<String, EntityType> detectType(QueryNode root, ContextProcessorConfig contextProcessorConfig,
			Map<String, EntityType> entityTypes, Set<QueryNode> visited) throws EAIException {
		if (root == null) {
			return entityTypes;
		}
		visited.add(root);
		if (root instanceof AndQueryNode) {
			// recursive check and siblings
			for (QueryNode queryNode : root.getChildren()) {
				detectType(queryNode, contextProcessorConfig, entityTypes, visited);
			}
			// if not or clause or just group
		} else if (!(root instanceof BooleanQueryNode || root instanceof GroupQueryNode)) {
			detectType(root, contextProcessorConfig, entityTypes);
			// expand group query
		} else if (root instanceof GroupQueryNode && root.getChildren().size() == 1) {
			QueryNode groupChild = root.getChildren().get(0);
			if (groupChild.getChildren() != null)
				for (QueryNode queryNode : groupChild.getChildren()) {
					detectType(queryNode, contextProcessorConfig, entityTypes, visited);
				}
		}
		if (entityTypes.isEmpty() && !visited.contains(root.getParent())) {
			detectType(root.getParent(), contextProcessorConfig, entityTypes, visited);
		}
		return entityTypes;
	}

	private void detectType(QueryNode root, ContextProcessorConfig contextProcessorConfig,
			Map<String, EntityType> entityTypes) throws EAIException {
		QueryNodeParameters keyValue = getQueryKeyValue(root);
		if (contextProcessorConfig.getTypeUri().equals(keyValue.getKey())) {
			EntityType typeByExternalName = getEntityTypeByValue(contextProcessorConfig, (String) keyValue.getValue());
			entityTypes.put((String) keyValue.getValue(), typeByExternalName);
		}
	}

	private static EntityType getEntityTypeByValue(ContextProcessorConfig contextProcessorConfig, String value)
			throws EAIReportableException {
		EntityType typeByExternalName = contextProcessorConfig.getModelConfiguration().getTypeByExternalName(value);
		if (typeByExternalName == null) {
			throw new EAIReportableException(
					"Object type cound not be resolved for value: " + value + ". Check your query!");
		}
		return typeByExternalName;
	}

	private QueryNodeParameters getQueryKeyValue(QueryNode query) {
		if (query instanceof FieldableNode && query instanceof ValueQueryNode) {
			return new QueryNodeParameters(Objects.toString(((FieldableNode) query).getField()),
					Objects.toString(((ValueQueryNode<?>) query).getValue(), null), OPERATOR_EQUALS, query);
		} else if (query instanceof FieldQueryNode) {
			return new QueryNodeParameters(Objects.toString(((FieldQueryNode) query).getField()),
					Objects.toString(((FieldQueryNode) query).getTextAsString(), null), OPERATOR_EQUALS, query);

		} else if (query instanceof RangeQueryNode) {
			Serializable value = extractRange((RangeQueryNode<?>) query);
			return new QueryNodeParameters(Objects.toString(((RangeQueryNode<?>) query).getField()), value,
					OPERATOR_BETWEEN, query);
		}
		throw new EAIRuntimeException("Unsupported expression: " + query + "!");
	}

	private Serializable extractRange(RangeQueryNode<?> query) {
		if (query instanceof TermRangeQueryNode) {
			FieldQueryNode lowerBound = (FieldQueryNode) query.getLowerBound();
			FieldQueryNode upperBound = (FieldQueryNode) query.getUpperBound();
			ArrayList<Serializable> list = new ArrayList<>(2);
			if ("*".equals(lowerBound.getTextAsString())) {
				list.add(null);
			} else {
				appendRangeValue(lowerBound, list);
			}
			if ("*".equals(upperBound.getTextAsString())) {
				list.add(null);
			} else {
				appendRangeValue(upperBound, list);
			}
			return list;
		}
		throw new EAIRuntimeException("Unsupported query node: " + query + "!");
	}

	private void appendRangeValue(FieldQueryNode upperBound, ArrayList<Serializable> list) {
		String valueAsString = upperBound.getTextAsString();
		DateFormat dateFormat = dateConverter.getDateFormat();
		if (!(dateFormat instanceof SimpleDateFormat)
				|| ((SimpleDateFormat) dateFormat).toPattern().length() == valueAsString.length()) {
			try {
				Date parsed = dateConverter.getDateFormat().parse(valueAsString);
				list.add(parsed);
			} catch (@SuppressWarnings("unused") ParseException e) {// NOSONAR
				// skip and use as standard string value
				list.add(valueAsString);
			}
		} else {
			list.add(valueAsString);
		}
	}

	private Rule convertFieldToRule(QueryNodeParameters node, ContextProcessorParameters paramaters,
			ContextProcessorConfig contextProcessorConfig) throws EAIException {
		String key = node.getKey();
		if (key.equals(contextProcessorConfig.getTypeUri())) {
			return createTypeRule(node, contextProcessorConfig);
		}
		return createDefaultRule(node, paramaters, contextProcessorConfig);
	}

	@SuppressWarnings("unchecked")
	private static Rule createTypeRule(QueryNodeParameters node, ContextProcessorConfig contextProcessorConfig)
			throws EAIException {
		RuleBuilder ruleBuilder = SearchCriteriaBuilder.createRuleBuilder();
		ruleBuilder.setField("types");
		ruleBuilder.setOperation(OPERATOR_EQUALS);
		ruleBuilder.setType("");
		boolean emptyValues = true;
		if (node.getValue() instanceof Collection) {
			ruleBuilder.setValues(new ArrayList<>(((Collection<?>) node.getValue()).size()));
			for (Serializable nextType : (Collection<Serializable>) node.getValue()) {
				ruleBuilder.addValue(getEntityTypeByValue(contextProcessorConfig, (String) nextType).getUri());
				emptyValues = false;
			}
		} else if (StringUtils.isNotBlank((CharSequence) node.getValue())) {
			ruleBuilder.setValues(Collections
					.singletonList(getEntityTypeByValue(contextProcessorConfig, (String) node.getValue()).getUri()));
			emptyValues = false;
		}
		if (emptyValues) {
			ruleBuilder.setValues(Collections.singletonList("any"));
		}
		return ruleBuilder.build();
	}

	private Rule createDefaultRule(QueryNodeParameters node, ContextProcessorParameters paramaters,
			ContextProcessorConfig contextProcessorConfig) throws EAIException {
		Map<String, EntityType> entityTypes = detectType(node.getSource(), contextProcessorConfig);
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField(node.getKey());
		rule.setOperation(node.getOperator());
		Serializable valueToSet;
		if (EqualsHelper.nullSafeEquals("?", node.getValue())) { // bind value
			valueToSet = paramaters.getProvidedValue();
		} else if (paramaters.getContext() != null && "current".equals(node.getValue())) {
			valueToSet = paramaters.getContext().getId();
		} else {
			valueToSet = node.getValue(); // set as is
		}
		return setDefaultRuleValue(contextProcessorConfig, entityTypes, rule.build(), valueToSet);
	}

	private static Rule setDefaultRuleValue(ContextProcessorConfig contextProcessorConfig,
			Map<String, EntityType> entityTypes, Rule rule, Serializable valueToSet) throws EAIModelException {
		if (entityTypes.isEmpty()) {
			return setValue(rule, valueToSet);
		}
		ModelConfiguration modelConfiguration = contextProcessorConfig.getModelConfiguration();
		for (Entry<String, EntityType> nextEntity : entityTypes.entrySet()) {
			EntityType entityType = nextEntity.getValue();
			EntityProperty propertyByExternalName = modelConfiguration
					.getPropertyByExternalName(entityType.getIdentifier(), rule.getField());
			if (propertyByExternalName != null) {
				Pair<String, Serializable> converted = convertExternalValueToRuleValue(contextProcessorConfig,
						entityType, propertyByExternalName, valueToSet);
				return setValue(rule, converted.getSecond());
			}
			throw new EAIRuntimeException(
					"Failed to find property " + rule.getField() + " in type " + entityType.getIdentifier());
		}
		throw new EAIRuntimeException("Failed to convert value " + valueToSet + " based on property model!");
	}

	@SuppressWarnings("unchecked")
	private static Pair<String, Serializable> convertExternalValueToRuleValue(
			ContextProcessorConfig contextProcessorConfig, EntityType entityType, EntityProperty propertyByExternalName,
			Serializable valueToSet) throws EAIModelException {
		EAIModelConverter modelConverter = contextProcessorConfig.getModelConverter();
		String entityIdentifier = entityType.getIdentifier();
		String fieldId = propertyByExternalName.getUri();
		if (!(valueToSet instanceof Collection)) {
			return modelConverter.convertExternaltoSEIPProperty(fieldId, valueToSet, entityIdentifier);
		}
		// we should convert each entry independently since it might be single value
		Optional<PropertyDefinition> property = modelConverter.findInternalFieldForType(entityIdentifier,
				PropertyDefinition.hasName(propertyByExternalName.getPropertyId()));
		Pair<String, Serializable> converted = null;
		if (!property.isPresent()) {
			throw new EAIModelException("Missing model definition " + entityIdentifier + " or field with name "
					+ propertyByExternalName.getPropertyId());
		}
		if (!property.get().isMultiValued().booleanValue()) {
			ArrayList<Serializable> rangeValues = new ArrayList<>(((Collection<Serializable>) valueToSet).size());
			for (Serializable rangeValue : (Collection<Serializable>) valueToSet) {
				if (converted == null) {
					// convert the initial value
					converted = modelConverter.convertExternaltoSEIPProperty(fieldId, rangeValue, entityIdentifier);
					rangeValues.add(converted.getSecond());
					// now convert converted to multivalue again
					converted = new Pair<>(converted.getFirst(), rangeValues);
				} else {
					// add the next values
					((Collection<Serializable>) converted.getSecond()).add(modelConverter
							.convertExternaltoSEIPProperty(fieldId, rangeValue, entityIdentifier)
								.getSecond());
				}
			}
			return converted;
		}
		return modelConverter.convertExternaltoSEIPProperty(fieldId, valueToSet, entityIdentifier);
	}

	private static Rule setValue(Rule node, Serializable providedValue) {
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder().from(node);
		if (providedValue instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Serializable> items = (Collection<Serializable>) providedValue;
			rule.setValues(new ArrayList<>(items.size()));
			items.forEach(item -> collectValue(rule, item));
		} else {
			rule.setValues(new ArrayList<>(1));
			collectValue(rule, providedValue);
		}
		return rule.build();
	}

	private static void collectValue(RuleBuilder node, Serializable value) {
		if (value instanceof String) {
			// base check for object property value
			boolean uriFormat = PATTERN_IS_URI.matcher(value.toString()).matches();
			if (uriFormat) {
				node.setType("object");
				node.setOperation("set_to");
			} else {
				node.setType("rdfs:Literal");
			}
			node.addValue(value.toString());
		} else if (value instanceof Boolean) {
			node.setType("rdfs:Literal");
			node.addValue(value.toString());
		} else if (value instanceof Number) {
			node.setType("numeric");
			node.addValue(value.toString());
		} else if (value instanceof Date) {
			node.setType("dateTime");
			node.addValue(ISO8601DateFormat.format((Date) value));
		} else if (value == null) {
			// add range values
			node.addValue(null);
		} else {
			throw new EAIRuntimeException("Unsupported value provided for context resolving: " + value);
		}
	}

	private String getTypePropertyURI() {
		return eaiConfiguration.getTypePropertyURI().get();
	}

	private ModelConfiguration getModelConfiguration() {
		return modelService.getModelConfiguration(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID);
	}

	private EAIModelConverter getModelConverter() {
		return modelService.provideModelConverter(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID);
	}
}
