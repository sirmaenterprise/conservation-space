package com.sirma.itt.emf.semantic.search.operation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;

/**
 * Tests for {@link SuggestSearchOperation}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class SuggestSearchOperationTest {

	@Mock
	private FreeTextSearchProcessor freeTextSearchProcessor;

	@Mock
	private SemanticConfiguration semanticConfigurations;

	@InjectMocks
	private SuggestSearchOperation suggestSearchOperation;

	@Before
	public void init() {
		ConfigurationPropertyMock<String> ftsIndexName = new ConfigurationPropertyMock<>("solr-inst:ftsearch");
		Mockito.when(semanticConfigurations.getFtsIndexName()).thenReturn(ftsIndexName);
		Mockito.when(freeTextSearchProcessor.process(Matchers.anyString(), Matchers.anyString(), Matchers.eq(false)))
				.thenReturn("solrSearch");
	}

	@Test
	public void should_BuildQuery() {
		String expectedQuery = "{"
				+ "?searchasolr-inst:ftsearch;solr:query'''solrSearch''';solr:entities?instance."
				+ "}UNION{"
				+ "?searchasolr-inst:ftsearch;solr:query'''solrSearch''';solr:entities?instance."
				+ "}UNION{"
				+ "?searchasolr-inst:ftsearch;solr:query'''solrSearch''';solr:entities?instance."
				+ "}";
		StringBuilder query = new StringBuilder();
		Rule rule = new RuleBuilder().setOperation("suggest").addValue("some").setField("someField").build();
		suggestSearchOperation.buildOperation(query, rule);

		Assert.assertEquals(expectedQuery, query.toString().replaceAll("\\s+", ""));
	}

	@Test
	public void should_NotBuildQuery_When_ValuesOfRuleAreEmpty() {
		StringBuilder query = new StringBuilder();
		Rule rule = new RuleBuilder().setOperation("equals").addValue("").build();
		suggestSearchOperation.buildOperation(query, rule);

		Assert.assertEquals("", query.toString());
	}

	@Test
	public void should_NotBuildQuery_When_RuleIsEmpty() {
		StringBuilder query = new StringBuilder();
		Rule rule = new RuleBuilder().setOperation("equals").build();
		suggestSearchOperation.buildOperation(query, rule);

		Assert.assertEquals("", query.toString());
	}

	@Test
	public void should_NotBeApplicable_When_OperationIsNotSuggest() {
		Rule rule = new RuleBuilder().setOperation("equals").build();
		Assert.assertFalse(suggestSearchOperation.isApplicable(rule));
	}

	@Test
	public void should_BeApplicable_When_OperationIsSuggest() {
		Rule rule = new RuleBuilder().setOperation("suggest").build();
		Assert.assertTrue(suggestSearchOperation.isApplicable(rule));
	}
}