package com.sirma.itt.seip.domain.search.tree;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ConditionBuilder}
 *
 * @author radoslav
 */
public class ConditionBuilderTest {

	private ConditionBuilder conditionBuilder;

	@Before
	public void init() {
		conditionBuilder = new ConditionBuilder();
	}

	@Test
	public void testBuild() {
		SearchNode searchNode = Mockito.mock(SearchNode.class);
		Condition condition = conditionBuilder.setId("dummyId").addRule(searchNode).build();

		assertEquals("dummyId", condition.getId());
		assertEquals(Condition.Junction.AND, condition.getCondition());
		assertEquals(1, condition.getRules().size());
		assertEquals(SearchNode.NodeType.CONDITION, condition.getNodeType());
	}

	@Test
	public void testBuildWithReadyRules() {
		SearchNode searchNode = Mockito.mock(SearchNode.class);
		List<SearchNode> rules = Collections.singletonList(searchNode);

		Condition condition = conditionBuilder.setId("dummyId").setRules(rules).build();

		assertEquals("dummyId", condition.getId());
		assertEquals(Condition.Junction.AND, condition.getCondition());
		assertEquals(condition.getRules(), rules);
	}

	@Test
	public void testBuildWithNoRules() {
		Condition condition = conditionBuilder.setId("dummyId").setCondition(Condition.Junction.AND).build();

		assertEquals("dummyId", condition.getId());
		assertEquals(Condition.Junction.AND, condition.getCondition());
	}

	@Test
	public void testBuildWithNoId() {
		Condition condition = conditionBuilder.setCondition(Condition.Junction.AND).build();
		Assert.assertNotNull(condition.getId());
	}

	@Test
	public void testBuildWithValidCondition() {
		conditionBuilder.setCondition(Condition.Junction.OR).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithInvalidConditionOperatorMissing() {
		conditionBuilder.setCondition(null).build();
	}

	@Test
	public void testFrom() {
		Condition mock = Mockito.mock(Condition.class);
		when(mock.getCondition()).thenReturn(Condition.Junction.OR);
		when(mock.getId()).thenReturn("dummyId");

		SearchNode searchNode = Mockito.mock(SearchNode.class);
		List<SearchNode> rules = Collections.singletonList(searchNode);

		when(mock.getRules()).thenReturn(rules);

		Condition condition = conditionBuilder.from(mock).build();

		assertEquals("dummyId", condition.getId());
		assertEquals(Condition.Junction.OR, condition.getCondition());
		assertEquals(0, condition.getRules().size());
	}
}
