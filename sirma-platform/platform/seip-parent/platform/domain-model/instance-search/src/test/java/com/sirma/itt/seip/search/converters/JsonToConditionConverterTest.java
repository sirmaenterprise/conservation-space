package com.sirma.itt.seip.search.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import com.sirma.itt.seip.domain.search.tree.CriteriaWildcards;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Test for {@link JsonToConditionConverter}.
 *
 * @author A. Kunchev
 */
public class JsonToConditionConverterTest {

	private JsonToConditionConverter converter;

	@Before
	public void setup() {
		converter = new JsonToConditionConverter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCondition_nullJson() {
		converter.parseCondition(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCondition_emptyJson() {
		converter.parseCondition(Json.createObjectBuilder().build());
	}

	@Test
	public void parseCondition_withoutConditionId() {
		JsonObject object = Json.createObjectBuilder().add("condition", "AND").build();
		Condition condition  = converter.parseCondition(object);
		assertTrue(condition.getId().length() > 0);
	}

	@Test
	public void parseCondition_withCorrectJson() {
		Condition condition = JSON.readObject(
				JsonToConditionConverterTest.class.getResourceAsStream("/json-condition-converter-test.json"),
				converter::parseCondition);

		assertEquals("e3d612ea-b87a-4dc5-e16a-fdb50f0529a0", condition.getId());
		assertEquals(Junction.OR, condition.getCondition());
		assertNotNull(condition.getRules());
		assertEquals(1, condition.getRules().size());
	}

	@Test
	public void parseCondition_withCorrectJsonObject() {
		Condition condition = JSON.readObject(
				JsonToConditionConverterTest.class.getResourceAsStream("/json-condition-converter-test.json"),
				converter::parseCondition);
		Rule jsonObjectRule = (Rule) ((Condition) condition.getRules().get(0)).getRules().get(5);
		assertEquals("d631324e-250f-4ab6-bef5-5a1a242f9c79", jsonObjectRule.getId());
		assertEquals(CriteriaWildcards.ANY_RELATION, jsonObjectRule.getField());
		assertEquals("object", jsonObjectRule.getType());
		assertNotNull(jsonObjectRule.getValues());
	}

	@Test
	public void parseCondition_WithSingleRule() {
		Condition condition = JSON.readObject(
				JsonToConditionConverterTest.class.getResourceAsStream("/json-condition-single-rule.json"),
				converter::parseCondition);
		assertEquals(Junction.AND, condition.getCondition());
		Rule rule = (Rule) condition.getRules().get(0);
		assertEquals("emf:status", rule.getField());
		assertEquals("codeList", rule.getType());
		assertEquals("in", rule.getOperation());
		assertEquals("ACTIVE", rule.getValues().get(0));
	}
}
