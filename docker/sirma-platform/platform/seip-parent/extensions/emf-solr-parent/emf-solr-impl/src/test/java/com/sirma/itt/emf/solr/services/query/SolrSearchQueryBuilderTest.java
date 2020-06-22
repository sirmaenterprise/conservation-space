package com.sirma.itt.emf.solr.services.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.solr.search.operation.ContainsSearchOperation;
import com.sirma.itt.emf.solr.search.operation.inverse.NotSetToSearchOperation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import com.sirma.itt.seip.util.ReflectionUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link SolrSearchQueryBuilder}.
 *
 * @author Hristo Lungov
 */
public class SolrSearchQueryBuilderTest {

	@InjectMocks
	private SolrSearchQueryBuilder solrSearchQueryBuilder;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		List<SearchOperation> solrOperations = new ArrayList<>();
		solrOperations.add(new ContainsSearchOperation());
		solrOperations.add(new NotSetToSearchOperation());
		ReflectionUtils.setFieldValue(solrSearchQueryBuilder, "solrSearchOperations", solrOperations);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_not_find_operation() {
		ConditionBuilder cond = SearchCriteriaBuilder.createConditionBuilder();
		Rule r1 = SearchOperationUtils.createRule("a", "", "missing", "test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "missing", "test2");
		cond.setRules(Arrays.asList(r1, r2));
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("test");
		solrSearchQueryBuilder.buildSolrQuery(cond.build());
	}

	@Test
	public void should_create_solr_or_query() {
		ConditionBuilder cond = SearchCriteriaBuilder.createConditionBuilder().setCondition(Condition.Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "contains", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "contains", "emf:test2");
		cond.setRules(Arrays.asList(r1, r2));

		String q = solrSearchQueryBuilder.buildSolrQuery(cond.build());
		Assert.assertEquals("(a:(*emf:test*)) OR (b:(*emf:test2*))", q);
	}

	@Test
	public void should_create_solr_and_query() {
		ConditionBuilder cond = SearchCriteriaBuilder.createConditionBuilder().setCondition(Condition.Junction.AND);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "contains", "emf:test");
		Rule r2 = SearchOperationUtils.createRule("b", "", "contains", "emf:test2");
		cond.setRules(Arrays.asList(r1, r2));

		String q = solrSearchQueryBuilder.buildSolrQuery(cond.build());
		Assert.assertEquals("(a:(*emf:test*)) AND (b:(*emf:test2*))", q);
	}

	@Test
	public void should_create_solr_nested_query() {
		ConditionBuilder nested = SearchCriteriaBuilder.createConditionBuilder().setCondition(Condition.Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "contains", "emf:1");
		Rule r2 = SearchOperationUtils.createRule("a", "", "contains", "emf:2");
		nested.setRules(Arrays.asList(r1, r2));

		ConditionBuilder main = new ConditionBuilder();
		Rule r3 = SearchOperationUtils.createRule("b", "", "contains", "emf:3");
		main.setRules(Arrays.asList(nested.build(), r3));

		String q = solrSearchQueryBuilder.buildSolrQuery(main.build());
		Assert.assertEquals("((a:(*emf:1*)) OR (a:(*emf:2*))) AND (b:(*emf:3*))", q);
	}

	@Test
	public void should_not_append_empty_conditions_and_rules() {
		ConditionBuilder nested = SearchCriteriaBuilder.createConditionBuilder().setCondition(Condition.Junction.OR);
		// Without a type the values are treated like URIs - easier for testing
		Rule r1 = SearchOperationUtils.createRule("a", "", "contains", "emf:1");
		Rule r2 = SearchOperationUtils.createRule("empty_rule", "", "contains");
		nested.setRules(Arrays.asList(r1, r2));

		ConditionBuilder main = new ConditionBuilder();
		Rule r3 = SearchOperationUtils.createRule("b", "", "contains", "emf:3");

		Condition emptyCondition = new ConditionBuilder().build();
		main.setRules(Arrays.asList(nested.build(), r3, emptyCondition));

		String q = solrSearchQueryBuilder.buildSolrQuery(main.build());
		Assert.assertEquals("((a:(*emf:1*))) AND (b:(*emf:3*))", q);
	}

	@Test
	public void should_not_append_empty_conditions_and_not_set_to_rule() {
		ConditionBuilder nested = SearchCriteriaBuilder.createConditionBuilder().setCondition(Condition.Junction.OR);
		Rule r1 = SearchOperationUtils.createRule("a", "", "not_set_to", "emf:1");
		nested.setRules(Arrays.asList(r1));

		ConditionBuilder main = new ConditionBuilder();
		Rule r2 = SearchOperationUtils.createRule("b", "", "not_set_to", "emf:3");

		Condition emptyCondition = new ConditionBuilder().build();
		main.setRules(Arrays.asList(nested.build(), r2, emptyCondition));

		String q = solrSearchQueryBuilder.buildSolrQuery(main.build());
		Assert.assertEquals("-(a:(emf\\:1)) AND -(b:(emf\\:3))", q);
	}
}
