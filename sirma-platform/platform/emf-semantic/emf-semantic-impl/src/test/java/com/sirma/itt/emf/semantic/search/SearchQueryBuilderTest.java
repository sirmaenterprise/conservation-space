package com.sirma.itt.emf.semantic.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.emf.semantic.search.operation.EqualsSearchOperation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Unit tests for {@link SearchQueryBuilder}.
 *
 * @author yasko
 */
public class SearchQueryBuilderTest {

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private SearchQueryBuilder builder = new SearchQueryBuilder();

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		NamespaceRegistry nameSpaceRegistry = Mockito.mock(NamespaceRegistry.class);
		Mockito.when(nameSpaceRegistry.buildFullUri(Mockito.anyString())).then(AdditionalAnswers.returnsFirstArg());

		EqualsSearchOperation operation = new EqualsSearchOperation();
		ReflectionUtils.setField(operation, "nameSpaceRegistry", nameSpaceRegistry);

		List<SearchOperation> operations = new ArrayList<>();
		operations.add(operation);
		ReflectionUtils.setField(builder, "searchOperations", operations);
	}

	@Test
	public void testRulesInConjunction() {
		Condition cond = new Condition();
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "equals", "emf:test2");
		cond.setRules(Arrays.asList(r1, r2));

		String q = builder.build(cond);
		String expected = "{{{ ?instance rdf:type <emf:test> }}{{ ?instance rdf:type <emf:test2> }} $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test
	public void testRulesInDisjunction() {
		Condition cond = new Condition(Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "equals", "emf:test2");
		cond.setRules(Arrays.asList(r1, r2));

		String q = builder.build(cond);
		String expected = "{{{ ?instance rdf:type <emf:test> }} UNION {{ ?instance rdf:type <emf:test2> }} $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test
	public void testNestedCondition() {
		Condition nested = new Condition(Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "equals", "emf:1");
		Rule r2 = SearchOperationUtils.createRule("a", "", "equals", "emf:2");
		nested.setRules(Arrays.asList(r1, r2));

		Condition main = new Condition();
		Rule r3 = SearchOperationUtils.createRule("b", "", "equals", "emf:3");
		main.setRules(Arrays.asList(nested, r3));

		String q = builder.build(main);
		String expected = "{{{{ ?instance rdf:type <emf:1> }} UNION {{ ?instance rdf:type <emf:2> }}}{{ ?instance rdf:type <emf:3> }} $permissions_block$instance}";
		Assert.assertEquals(expected, q);
	}

	@Test(expected = EmfApplicationException.class)
	public void testWithMissingOperation() {
		Condition cond = new Condition();
		Rule r1 = SearchOperationUtils.createRule("a", "", "missing", "test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "missing", "test2");
		cond.setRules(Arrays.asList(r1, r2));

		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("test");
		builder.build(cond);
	}

}
