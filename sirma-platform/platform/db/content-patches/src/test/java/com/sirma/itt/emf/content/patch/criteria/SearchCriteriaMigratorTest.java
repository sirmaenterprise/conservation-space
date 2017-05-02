package com.sirma.itt.emf.content.patch.criteria;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the search criteria migration logic in {@link SearchCriteriaMigrator}.
 * 
 * @author Mihail Radkov
 */
public class SearchCriteriaMigratorTest {

	@Mock
	private NamespaceRegistryService namespaceService;

	private SearchCriteriaMigrator migrator;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		migrator = new SearchCriteriaMigrator(namespaceService);

		Mockito.when(namespaceService.buildFullUri(Matchers.anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if ("emf:Document".equals(arguments[0])) {
					return "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document";
				}
				return arguments[0].toString();
			}
		});
	}

	@Test
	public void testEmptyConfig() throws UnsupportedEncodingException {
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria("");
		Assert.assertFalse(migratedSearchCriteria.isPresent());
	}

	@Test
	public void shouldNotMigrateWithoutObjectSelectMode() throws UnsupportedEncodingException {
		String config = encode("{}");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertFalse(migratedSearchCriteria.isPresent());
	}

	@Test
	public void shouldNotMigrateConfigForCurrentObject() throws UnsupportedEncodingException {
		String config = encode("{ \"selectObjectMode\": \"current\"}");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertFalse(migratedSearchCriteria.isPresent());
	}

	@Test
	public void shouldNotMigrateConfigWithEmptyCriteria() throws IOException {
		String config = loadTestJson("empty_criteria.json");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertFalse(migratedSearchCriteria.isPresent());
	}

	@Test
	public void shouldNotTouchAnythingExceptTheCriteriaField() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");

		Assert.assertNotNull(decoded.get("selectObjectMode"));
		Assert.assertEquals("automatically", decoded.get("selectObjectMode").getAsString());

		Assert.assertNotNull(decoded.get("searchMode"));
		Assert.assertEquals("basic", decoded.get("searchMode").getAsString());

		Assert.assertNotNull(decoded.get("other-properties"));
		JsonObject otherProperties = decoded.get("other-properties").getAsJsonObject();

		Assert.assertNotNull(otherProperties.get("one"));
		Assert.assertEquals("1", otherProperties.get("one").getAsString());

		Assert.assertNotNull(otherProperties.get("two"));
		Assert.assertTrue(otherProperties.get("two").isJsonArray());
		Assert.assertEquals(1, otherProperties.get("two").getAsJsonArray().size());
		Assert.assertEquals("2", otherProperties.get("two").getAsJsonArray().get(0).getAsString());
	}

	@Test
	public void shouldMigrateBasicSearchTypes() throws IOException {
		String config = loadTestJson("basic_criteria.json");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertTrue(migratedSearchCriteria.isPresent());

		JsonObject decoded = decode(migratedSearchCriteria.get());
		assertMigratedTypes(decoded);
	}

	@Test
	public void shouldRestoreRootCriteriaId() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");
		JsonObject criteria = decoded.get("criteria").getAsJsonObject();
		String rootCriteriaId = criteria.get("id").getAsString();
		Assert.assertEquals("criteria-id", rootCriteriaId);
	}

	@Test
	public void shouldMigrateConfigWithoutSearchMode() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_no_search_mode.json");
		assertMigratedTypes(decoded);
	}

	private void assertMigratedTypes(JsonObject config) {
		Assert.assertNotNull(config.get("criteria"));

		JsonObject criteria = config.get("criteria").getAsJsonObject();
		Assert.assertNotNull(criteria.get("id"));

		JsonObject typeRule = getTypeRule(criteria);
		Assert.assertNotNull(typeRule.get("id"));
		Assert.assertEquals("types", typeRule.get("field").getAsString());

		assertRule(typeRule, "", "equals",
				Arrays.asList("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document", "CASE0001"));
	}

	@Test
	public void shouldMigrateFreeTextCriteria() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");

		JsonObject freeTextRule = getTypeCriteriaRule(decoded, "freeText");
		assertRule(freeTextRule, "fts", "contains", "123");
	}

	@Test
	public void shouldMigrateCreatedByCriteria() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");

		JsonObject createdByRule = getTypeCriteriaRule(decoded, "emf:createdBy");
		assertRule(createdByRule, "object", "set_to", Arrays.asList("emf:admin"));
	}

	@Test
	public void shouldMigrateCreatedOnCriteria() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");

		JsonObject createdOnRule = getTypeCriteriaRule(decoded, "emf:createdOn");
		assertRule(createdOnRule, "dateTime", "between",
				Arrays.asList("2016-08-01T21:00:00.000Z", "2016-08-25T21:00:00.000Z"));
	}

	@Test
	public void shouldMigrateRelationsAndContextCriteria() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria.json");

		JsonObject modifiedByRule = getTypeCriteriaRule(decoded, "emf:modifiedBy");
		assertRule(modifiedByRule, "object", "set_to", Arrays.asList("emf:123-456-789"));
	}

	@Test
	public void shouldNotMigrateMigratedCriteria() throws IOException {
		String config = loadTestJson("migrated_criteria.json");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertFalse(migratedSearchCriteria.isPresent());
	}

	@Test
	public void shouldMigrateAdvancedSearchTypes() throws IOException {
		String config = loadTestJson("advanced_criteria.json");
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		Assert.assertTrue(migratedSearchCriteria.isPresent());

		JsonObject decoded = decode(migratedSearchCriteria.get());

		Assert.assertNotNull(decoded.get("criteria"));
		JsonObject criteria = decoded.get("criteria").getAsJsonObject();

		JsonObject typeRule = getTypeRule(criteria);
		Assert.assertEquals("types", typeRule.get("field").getAsString());

		assertRule(typeRule, "", "equals",
				Arrays.asList("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject",
						"CASE0001", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"));
	}

	@Test
	public void shouldNotFailWhenMigratingEmptyRules() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_missing_fields.json");
		Assert.assertNotNull(decoded.get("criteria"));
	}

	@Test
	public void shouldMigrateEmptyTypesRule() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_missing_fields.json");

		Assert.assertNotNull(decoded.get("criteria"));
		JsonObject criteria = decoded.get("criteria").getAsJsonObject();

		JsonObject typeRule = getTypeRule(criteria);
		assertRule(typeRule, "", "equals", Arrays.asList("anyObject"));
	}

	@Test
	public void shouldNotFailWhenMigratingMissingDateRules() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_missing_dates.json");

		Assert.assertNotNull(decoded.get("criteria"));
		JsonObject criteria = decoded.get("criteria").getAsJsonObject();

		JsonArray typeCriteriaRules = getTypeCriteriaRules(criteria);
		Assert.assertEquals(1, typeCriteriaRules.size());
	}

	@Test
	public void shouldMigrateEmptyCriteriaRules() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_missing_fields.json");

		Assert.assertNotNull(decoded.get("criteria"));
		JsonObject criteria = decoded.get("criteria").getAsJsonObject();

		JsonArray typeCriteriaRules = getTypeCriteriaRules(criteria);
		Assert.assertEquals(0, typeCriteriaRules.size());
	}

	@Test
	public void shouldMigrateCriteriaWithoutContext() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_no_context.json");
		
		JsonObject modifiedByRule = getTypeCriteriaRule(decoded, "emf:modifiedBy");
		assertRule(modifiedByRule, "object", "set_to", Arrays.asList("anyObject"));
	}

	@Test
	public void shouldMigrateCriteriaWithoutRelations() throws IOException {
		JsonObject decoded = migrateJson("basic_criteria_no_relations.json");

		JsonObject anyRelationRule = getTypeCriteriaRule(decoded, "anyRelation");
		assertRule(anyRelationRule, "object", "set_to", Arrays.asList("emf:123-456-789"));
	}

	private JsonObject migrateJson(String fileName) throws IOException {
		String config = loadTestJson(fileName);
		Optional<String> migratedSearchCriteria = migrator.migrateSearchCriteria(config);
		return decode(migratedSearchCriteria.get());
	}

	private JsonObject getTypeCriteriaRule(JsonObject configuration, String field) {
		Assert.assertNotNull(configuration.get("criteria"));

		JsonObject criteria = configuration.get("criteria").getAsJsonObject();
		JsonArray typeCriteriaRules = getTypeCriteriaRules(criteria);

		for (JsonElement rule : typeCriteriaRules) {
			JsonObject ruleAsObject = rule.getAsJsonObject();
			if (field.equals(ruleAsObject.get("field").getAsString())) {
				return ruleAsObject;
			}
		}
		return null;
	}

	private JsonObject getTypeRule(JsonObject criteria) {
		JsonArray sectionRules = getSectionRules(criteria);
		Assert.assertEquals(2, sectionRules.size());
		return sectionRules.get(0).getAsJsonObject();
	}

	private JsonArray getTypeCriteriaRules(JsonObject criteria) {
		JsonArray sectionRules = getSectionRules(criteria);
		Assert.assertEquals(2, sectionRules.size());

		JsonObject typeCriteriaRulesCondition = sectionRules.get(1).getAsJsonObject();
		Assert.assertNotNull(typeCriteriaRulesCondition.get("condition"));
		Assert.assertEquals("AND", typeCriteriaRulesCondition.get("condition").getAsString());
		Assert.assertNotNull(typeCriteriaRulesCondition.get("rules"));

		return typeCriteriaRulesCondition.get("rules").getAsJsonArray();
	}

	private JsonArray getSectionRules(JsonObject criteria) {
		Assert.assertNotNull(criteria.get("condition"));
		Assert.assertEquals("OR", criteria.get("condition").getAsString());
		Assert.assertNotNull(criteria.get("rules"));

		JsonArray sections = criteria.get("rules").getAsJsonArray();
		Assert.assertEquals(1, sections.size());

		JsonObject section = sections.get(0).getAsJsonObject();
		Assert.assertNotNull(section.get("condition"));
		Assert.assertEquals("AND", section.get("condition").getAsString());
		Assert.assertNotNull(section.get("rules"));

		return section.get("rules").getAsJsonArray();
	}

	private void assertRule(JsonObject rule, String type, String operator) {
		Assert.assertNotNull(rule);
		Assert.assertNotNull(rule.get("id"));

		JsonElement typeElement = rule.get("type");
		Assert.assertNotNull(typeElement);
		Assert.assertEquals(type, typeElement.getAsString());

		JsonElement operatorElement = rule.get("operator");
		Assert.assertNotNull(operatorElement);
		Assert.assertEquals(operator, operatorElement.getAsString());
	}

	private void assertRule(JsonObject rule, String type, String operator, String value) {
		assertRule(rule, type, operator);

		JsonElement valueElement = rule.get("value");
		Assert.assertNotNull(valueElement);
		Assert.assertEquals(value, valueElement.getAsString());
	}

	private void assertRule(JsonObject rule, String type, String operator, List<String> values) {
		assertRule(rule, type, operator);

		Assert.assertTrue(rule.get("value").isJsonArray());
		JsonArray valueArray = rule.get("value").getAsJsonArray();

		for (int i = 0; i < values.size(); i++) {
			Assert.assertTrue(values.get(i).equals(valueArray.get(i).getAsString()));
		}
	}

	private String loadTestJson(String name) throws IOException {
		InputStream resourceAsStream = SearchCriteriaMigratorTest.class.getResourceAsStream(name);
		String parsed = IOUtils.toString(resourceAsStream);
		return encode(parsed);
	}

	private String encode(String toEncode) {
		return Base64.getEncoder().encodeToString(toEncode.getBytes());
	}

	private JsonObject decode(String toDecode) throws UnsupportedEncodingException {
		return SearchCriteriaMigrator.decodeConfiguration(toDecode);
	}
}
