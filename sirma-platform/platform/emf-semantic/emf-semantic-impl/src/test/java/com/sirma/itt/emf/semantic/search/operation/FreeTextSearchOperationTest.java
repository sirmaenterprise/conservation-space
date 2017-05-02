package com.sirma.itt.emf.semantic.search.operation;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Tests the query building in {@link FreeTextSearchOperation}.
 * 
 * @author Mihail Radkov
 */
public class FreeTextSearchOperationTest {

	@Mock
	private FTSQueryParser ftsQueryParser;

	@Mock
	private SemanticConfiguration semanticConfigurations;

	@InjectMocks
	private FreeTextSearchOperation freeTextSearchOperation;

	@Before
	public void initialize() {
		freeTextSearchOperation = new FreeTextSearchOperation();
		MockitoAnnotations.initMocks(this);

		Mockito.when(ftsQueryParser.prepare(Matchers.anyString())).thenReturn("enhanced-fts-query");

		ConfigurationPropertyMock<String> ftsIndexName = new ConfigurationPropertyMock<>("test-index");
		Mockito.when(semanticConfigurations.getFtsIndexName()).thenReturn(ftsIndexName);
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		Assert.assertTrue(freeTextSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("freeText", "string", "contains", Collections.singletonList("123"));
		Assert.assertFalse(freeTextSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("freeText", "fts", "", Collections.singletonList("123"));
		Assert.assertFalse(freeTextSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperationForRuleWithoutValues() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationForBlankRuleValue() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList(""));
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals(
				"?search a test-index ; solr:query \"enhanced-fts-query\" ; solr:entities ?instance . ",
				builder.toString());
	}

	@Test
	public void shouldEscapeEmbededdedQuotationMarks() {
		// Query with quotation marks
		Mockito.when(ftsQueryParser.prepare(Matchers.anyString())).thenReturn("(\"test\")");

		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("?search a test-index ; solr:query \"(\\\"test\\\")\" ; solr:entities ?instance . ",
				builder.toString());
	}
}
