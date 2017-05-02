package com.sirma.itt.emf.content.patch.criteria;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Performs migration from basic to advanced search criteria model and converts all short URIs to full format.
 * 
 * @author Mihail Radkov
 *
 */
public class SearchCriteriaMigrator {

	private static final String RULES = "rules";
	private static final String CONDITION = "condition";

	private static final String ID = "id";
	private static final String FIELD = "field";
	private static final String TYPE = "type";
	private static final String VALUE = "value";

	private static final String OBJECT_RULE_TYPE = "object";
	private static final String SET_TO_OPERATOR = "set_to";

	private final NamespaceRegistryService namespaceRegistryService;

	/**
	 * Constructs a search criteria migrator with the provided namespace service.
	 * 
	 * @param namespaceRegistryService
	 *            - a service for URI conversions
	 */
	public SearchCriteriaMigrator(NamespaceRegistryService namespaceRegistryService) {
		this.namespaceRegistryService = namespaceRegistryService;
	}

	/**
	 * Decodes the provided string configuration from Base64.
	 * 
	 * @param configAttr
	 *            - the provided string configuration
	 * @return the decoded configuration
	 * @throws UnsupportedEncodingException
	 *             if the configuration cannot be decoded with UTF8
	 */
	static JsonObject decodeConfiguration(String configAttr) throws UnsupportedEncodingException {
		byte[] decoded = Base64.getDecoder().decode(configAttr);
		String decodedString = new String(decoded, StandardCharsets.UTF_8.name());
		return new JsonParser().parse(decodedString).getAsJsonObject();
	}

	/**
	 * Encodes the provided configuration to Base64 format.
	 * 
	 * @param configuration
	 *            - the provided config for encoding
	 * @return the encoded config
	 */
	static String encodeConfiguration(JsonObject configuration) {
		String migratedConfiguration = configuration.toString();
		return Base64.getEncoder().encodeToString(migratedConfiguration.getBytes());
	}

	/**
	 * Migrates the provided configuration to advanced search model. Additionally converts short to full URIs.
	 * 
	 * The provided configuration is decoded from Base64 and then converted to a JsonObject. After the migration is
	 * completed, the configuration is encoded again in Base64.
	 * 
	 * If the configuration lacks a selectObjectMode, searchMode or criteria fields, nothing is converted and an empty
	 * Optional is returned.
	 * 
	 * @param configAttr
	 *            - the configuration attribute for migration
	 * @return an Optional of the migrated configuration or empty if the configuration was not migrated.
	 * @throws UnsupportedEncodingException
	 *             if the configuration cannot be decoded with UTF8
	 */
	Optional<String> migrateSearchCriteria(String configAttr) throws UnsupportedEncodingException {
		if (StringUtils.isNotBlank(configAttr)) {
			JsonObject configuration = decodeConfiguration(configAttr);
			JsonElement selectMode = configuration.get("selectObjectMode");

			if (selectMode != null && !"current".equals(selectMode.getAsString())) {
				JsonElement criteria = configuration.get("criteria");
				JsonElement searchMode = configuration.get("searchMode");
				if (criteria == null) {
					// Nothing to migrate without a criteria...
					return Optional.empty();
				}

				if (searchMode == null || "".equals(searchMode.getAsString())) {
					// Will try to migrate criteria without search mode as a basic search
					searchMode = new JsonPrimitive("basic");
				}

				return migrate(configuration, criteria, searchMode);
			}
		}
		return Optional.empty();
	}

	private Optional<String> migrate(JsonObject configuration, JsonElement criteria, JsonElement searchMode) {
		Optional<JsonObject> migratedCriteria = getMigratedCriteria(criteria.getAsJsonObject(),
				searchMode.getAsString());
		if (!migratedCriteria.isPresent()) {
			// Noting was migrated
			return Optional.empty();
		}

		configuration.add("criteria", migratedCriteria.get());

		String encoded = encodeConfiguration(configuration);
		return Optional.of(encoded);
	}

	Optional<JsonObject> getMigratedCriteria(JsonObject criteria, String searchMode) {
		if ("basic".equals(searchMode)) {
			return migrateBasicCriteria(criteria);
		} else if ("advanced".equals(searchMode)) {
			return migrateAdvancedSearchTypes(criteria);
		}
		return Optional.empty();
	}

	private Optional<JsonObject> migrateBasicCriteria(JsonObject criteria) {
		if (criteria.get(RULES) == null) {
			// No rules, no migration
			return Optional.empty();
		}
		JsonArray rules = criteria.get(RULES).getAsJsonArray();
		Map<String, JsonObject> ruleMap = SearchCriteriaMigrator.getRuleMap(rules);

		if (ruleMap.size() < 2) {
			// Having less than 2 rules means the criteria is not a basic or is already migrated and there is no need of
			// further processing.
			return Optional.empty();
		}

		// Array for criteria related to the object types
		JsonArray migratedInnerRules = new JsonArray();

		// META TEXT
		JsonObject metaTextRule = ruleMap.get("metaText");
		if (SearchCriteriaMigrator.ruleHasValues(metaTextRule)) {
			JsonObject migratedMetaTextRule = SearchCriteriaMigrator.buildRule("freeText", "fts", "contains",
					metaTextRule.get(VALUE));
			migratedInnerRules.add(migratedMetaTextRule);
		}

		// CREATED BY
		JsonObject createdByRule = ruleMap.get("createdBy");
		if (SearchCriteriaMigrator.ruleHasValues(createdByRule)) {
			JsonObject migratedCreatedByRule = SearchCriteriaMigrator.buildRule("emf:createdBy", OBJECT_RULE_TYPE,
					SET_TO_OPERATOR, createdByRule.get(VALUE));
			migratedInnerRules.add(migratedCreatedByRule);
		}

		// CREATED ON
		JsonObject createdFromDateRule = ruleMap.get("createdFromDate");
		JsonObject createdToDateRule = ruleMap.get("createdToDate");
		if (SearchCriteriaMigrator.ruleHasValues(createdFromDateRule)
				|| SearchCriteriaMigrator.ruleHasValues(createdToDateRule)) {
			JsonObject migratedCreatedOnRule = SearchCriteriaMigrator.buildCreatedOn(createdFromDateRule,
					createdToDateRule);
			migratedInnerRules.add(migratedCreatedOnRule);
		}

		// RELATIONS & CONTEXT
		JsonObject relationsRule = ruleMap.get("relationship");
		JsonObject contextRule = ruleMap.get("location");
		SearchCriteriaMigrator.buildRelation(relationsRule, contextRule, migratedInnerRules);

		// OBJECT TYPES
		JsonObject typesRule = ruleMap.get("types");
		JsonObject subTypesRule = ruleMap.get("subtypes");
		JsonObject migratedTypesRule = buildTypes(typesRule, subTypesRule);

		JsonObject migratedInnerRulesCondition = SearchCriteriaMigrator.buildCondition(Condition.Junction.AND.name(),
				migratedInnerRules);

		JsonArray sectionRules = new JsonArray();
		sectionRules.add(migratedTypesRule);
		sectionRules.add(migratedInnerRulesCondition);

		JsonObject sectionCondition = SearchCriteriaMigrator.buildCondition(Condition.Junction.AND.name(),
				sectionRules);

		JsonArray sections = new JsonArray();
		sections.add(sectionCondition);

		JsonObject rootCondition = SearchCriteriaMigrator.buildCondition(Condition.Junction.OR.name(), sections);
		// Restoring root ID for reference in versions or other purposes
		rootCondition.add(ID, criteria.get(ID));
		return Optional.of(rootCondition);
	}

	private static JsonObject buildCreatedOn(JsonObject createdFromDateRule, JsonObject createdToDateRule) {
		JsonArray createdOnValue = new JsonArray();

		if (!SearchCriteriaMigrator.ruleHasValues(createdFromDateRule)) {
			createdOnValue.add(new JsonPrimitive(""));
		} else {
			createdOnValue.add(createdFromDateRule.get(VALUE));
		}

		if (!SearchCriteriaMigrator.ruleHasValues(createdToDateRule)) {
			createdOnValue.add(new JsonPrimitive(""));
		} else {
			createdOnValue.add(createdToDateRule.get(VALUE));
		}

		return SearchCriteriaMigrator.buildRule("emf:createdOn", "dateTime", "between", createdOnValue);
	}

	private static void buildRelation(JsonObject relationsRule, JsonObject contextRule, JsonArray migratedInnerRules) {
		boolean hasRelations = SearchCriteriaMigrator.ruleHasValues(relationsRule);
		boolean hasContext = SearchCriteriaMigrator.ruleHasValues(contextRule);

		if (hasRelations) {
			JsonArray relations = relationsRule.get(VALUE).getAsJsonArray();
			JsonArray context;
			if (hasContext) {
				context = contextRule.get(VALUE).getAsJsonArray();
			} else {
				context = new JsonArray();
				context.add(new JsonPrimitive("anyObject"));
			}
			for (JsonElement relation : relations) {
				String field = relation.getAsString();
				JsonObject contextToRelation = SearchCriteriaMigrator.buildRule(field, OBJECT_RULE_TYPE,
						SET_TO_OPERATOR, context);
				migratedInnerRules.add(contextToRelation);
			}
		} else if (hasContext) {
			JsonArray context = contextRule.get(VALUE).getAsJsonArray();
			JsonObject contextToRelation = SearchCriteriaMigrator.buildRule("anyRelation", OBJECT_RULE_TYPE,
					SET_TO_OPERATOR, context);
			migratedInnerRules.add(contextToRelation);
		}
	}

	private JsonObject buildTypes(JsonObject typesRule, JsonObject subTypesRule) {
		JsonArray types = new JsonArray();
		if (SearchCriteriaMigrator.ruleHasValues(typesRule)) {
			types.addAll(typesRule.get(VALUE).getAsJsonArray());
		}
		if (SearchCriteriaMigrator.ruleHasValues(subTypesRule)) {
			types.addAll(subTypesRule.get(VALUE).getAsJsonArray());
		}
		Optional<JsonArray> migratedTypes = migrateTypes(types);
		if (migratedTypes.isPresent()) {
			types = migratedTypes.get();
		} else {
			types = new JsonArray();
			types.add(new JsonPrimitive("anyObject"));
		}
		return SearchCriteriaMigrator.buildRule("types", "", "equals", types);
	}

	private Optional<JsonObject> migrateAdvancedSearchTypes(JsonObject criteria) {
		JsonArray sections = criteria.get(RULES).getAsJsonArray();
		JsonArray sectionRules = sections.get(0).getAsJsonObject().get(RULES).getAsJsonArray();
		JsonObject typesRule = sectionRules.get(0).getAsJsonObject();
		JsonElement valueElement = typesRule.get(VALUE);
		if (valueElement == null || !valueElement.isJsonArray()) {
			return Optional.empty();
		}
		JsonArray types = valueElement.getAsJsonArray();
		Optional<JsonArray> migrated = migrateTypes(types);
		if (migrated.isPresent()) {
			typesRule.add(VALUE, migrated.get());
			return Optional.of(criteria);
		}
		return Optional.empty();
	}

	private Optional<JsonArray> migrateTypes(JsonArray types) {
		if (types.size() > 0) {
			JsonArray migratedTypes = new JsonArray();
			for (JsonElement type : types) {
				String typeAsString = type.getAsString();
				if (typeAsString.indexOf(":") > 0) {
					String migratedType = namespaceRegistryService.buildFullUri(typeAsString);
					migratedTypes.add(new JsonPrimitive(migratedType));
				} else {
					migratedTypes.add(type);
				}
			}
			return Optional.of(migratedTypes);
		}
		return Optional.empty();
	}

	private static Map<String, JsonObject> getRuleMap(JsonArray rules) {
		Map<String, JsonObject> ruleMap = CollectionUtils.createHashMap(8);
		for (JsonElement rule : rules) {
			JsonObject ruleAsObject = rule.getAsJsonObject();
			JsonElement fieldJson = ruleAsObject.get(FIELD);
			if (fieldJson != null) {
				String ruleField = fieldJson.getAsString();
				ruleMap.put(ruleField, ruleAsObject);
			}
		}
		return ruleMap;
	}

	private static boolean ruleHasValues(JsonObject rule) {
		if (rule == null) {
			return false;
		}
		JsonElement value = rule.get(VALUE);
		if (value != null) {
			if (value.isJsonArray()) {
				return value.getAsJsonArray().size() > 0;
			} else if (value.isJsonPrimitive()) {
				return value.getAsString().length() > 0;
			}
		}
		return false;
	}

	private static JsonObject buildRule(String field, String type, String operator, JsonElement value) {
		JsonObject rule = new JsonObject();
		rule.addProperty(ID, UUID.randomUUID().toString());
		rule.addProperty(FIELD, field);
		rule.addProperty(TYPE, type);
		rule.addProperty("operator", operator);
		rule.add(VALUE, value);
		return rule;
	}

	private static JsonObject buildCondition(String condition, JsonArray rules) {
		JsonObject conditionJson = new JsonObject();
		conditionJson.addProperty(ID, UUID.randomUUID().toString());
		conditionJson.addProperty(CONDITION, condition);
		conditionJson.add(RULES, rules);
		return conditionJson;
	}
}
