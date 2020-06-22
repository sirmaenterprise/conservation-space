package com.sirma.itt.emf.semantic.search;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.emf.semantic.search.operation.EqualsSearchOperation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link SearchQueryBuilder}.
 *
 * @author yasko
 */
public class SearchQueryBuilderTest {

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private SearchQueryBuilder builder;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		NamespaceRegistry nameSpaceRegistry = Mockito.mock(NamespaceRegistry.class);
		Mockito.when(nameSpaceRegistry.buildFullUri(Mockito.anyString())).then(AdditionalAnswers.returnsFirstArg());

		EqualsSearchOperation operation = new EqualsSearchOperation();
		ReflectionUtils.setFieldValue(operation, "nameSpaceRegistry", nameSpaceRegistry);

		List<SearchOperation> sparqlOperations = new ArrayList<>();
		sparqlOperations.add(operation);
		ReflectionUtils.setFieldValue(builder, "sparqlSearchOperations", sparqlOperations);
	}

	@Test
	public void testRulesInConjunction() {
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "equals", "emf:test2");
		conditionBuilder.setRules(Arrays.asList(r1, r2));

		String q = builder.build(conditionBuilder.build());
		String expected = "{ ?instance rdf:type <emf:test>.  ?instance rdf:type <emf:test2>.  $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test
	public void testRulesInDisjunction() {
		ConditionBuilder cond = SearchCriteriaBuilder.createConditionBuilder().setCondition(Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "equals", "emf:test2");
		cond.setRules(Arrays.asList(r1, r2));

		String q = builder.build(cond.build());
		String expected = "{{ ?instance rdf:type <emf:test>. } UNION { ?instance rdf:type <emf:test2>. } $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test
	public void testNestedCondition() {
		ConditionBuilder nestedCondtionBuilder = SearchCriteriaBuilder.createConditionBuilder().setCondition(Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:1");
		Rule r2 = SearchOperationUtils.createRule("a", "", "equals", "emf:2");
		nestedCondtionBuilder.setRules(Arrays.asList(r1, r2));

		ConditionBuilder mainConditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		Rule r3 = SearchOperationUtils.createRule("b", "", "equals", "emf:3");
		mainConditionBuilder.setRules(Arrays.asList(nestedCondtionBuilder.build(), r3));

		String q = builder.build(mainConditionBuilder.build());
		String expected = "{{ ?instance rdf:type <emf:1>. } UNION { ?instance rdf:type <emf:2>. } ?instance rdf:type <emf:3>.  $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test(expected = EmfApplicationException.class)
	public void testWithMissingOperation() {
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		Rule r1 = SearchOperationUtils.createRule("a", "", "missing", "test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "missing", "test2");
		conditionBuilder.setRules(Arrays.asList(r1, r2));

		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("test");
		builder.build(conditionBuilder.build());
	}

	@Test
	public void testBuildWithEmptyRuleValues() {
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		Rule rule = SearchOperationUtils.createRule("dummyField", "", "missing");
		conditionBuilder.addRule(rule);

		String query = builder.build(conditionBuilder.build());
		Assert.assertFalse(query.contains("dummyField"));
	}
}
