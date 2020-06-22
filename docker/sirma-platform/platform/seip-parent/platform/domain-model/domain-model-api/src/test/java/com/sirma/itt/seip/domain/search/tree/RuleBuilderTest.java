package com.sirma.itt.seip.domain.search.tree;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_RELATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link RuleBuilder}
 *
 * @author radoslav
 */
public class RuleBuilderTest {

	private RuleBuilder ruleBuilder;

	@Before
	public void init() {
		ruleBuilder = new RuleBuilder();
	}

	@Test
	public void testBuild() {
		Rule rule = ruleBuilder.setId("dummyId")
				.setField("type")
				.setType("string")
				.setOperation("equals")
				.addValue(ANY_OBJECT)
				.build();

		assertEquals(rule.getId(), "dummyId");
		assertEquals(rule.getField(), "type");
		assertEquals(rule.getType(), "string");
		assertEquals(rule.getOperation(), "equals");
		assertEquals(rule.getValues().get(0), ANY_OBJECT);
		assertEquals(rule.getNodeType(), SearchNode.NodeType.RULE);
	}

	@Test
	public void testBuildWithReadyValues() {
		List<String> values = Collections.singletonList(ANY_OBJECT);

		Rule rule = ruleBuilder.setId("dummyId")
				.setField("type")
				.setType("string")
				.setOperation("equals")
				.setValues(values)
				.build();

		assertEquals("dummyId", rule.getId());
		assertEquals("type", rule.getField());
		assertEquals("string", rule.getType());
		assertEquals("equals", rule.getOperation());
		assertEquals(values, rule.getValues());
	}

	@Test
	public void testBuildWithNoId() {
		Rule rule = ruleBuilder.setField("type").setType("string").setOperation("equals").build();

		Assert.assertTrue(StringUtils.isNotBlank(rule.getId()));
		assertEquals("type", rule.getField());
		assertEquals("string", rule.getType());
		assertEquals("equals", rule.getOperation());
	}

	@Test
	public void testBuildRuleWithMinimumFields() {
		Rule rule = ruleBuilder.setOperation("equals").build();
		assertNotNull(rule);
		assertEquals(CriteriaWildcards.ANY_FIELD, rule.getField());
	}

	@Test
	public void testBuildWithoutField() {
		Rule rule = ruleBuilder.setOperation("equals").build();
		// Should automatically assign any field.
		assertEquals(CriteriaWildcards.ANY_FIELD, rule.getField());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithInvalidRuleOperationMissing() {
		ruleBuilder.setOperation(null).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithInvalidEmptyRule() {
		ruleBuilder.build();
	}

	@Test
	public void testFrom() {
		Rule mockRule = Mockito.mock(Rule.class);

		Mockito.when(mockRule.getId()).thenReturn("dummyId");
		Mockito.when(mockRule.getField()).thenReturn(ANY_RELATION);
		Mockito.when(mockRule.getOperation()).thenReturn("set_to");
		List<String> values = Collections.singletonList("dummy");
		Mockito.when(mockRule.getValues()).thenReturn(values);

		Rule rule = ruleBuilder.from(mockRule).build();
		assertEquals("dummyId", rule.getId());
		assertEquals(ANY_RELATION, rule.getField());
		assertEquals("set_to", rule.getOperation());
		assertEquals(0, rule.getValues().size());
	}
}
