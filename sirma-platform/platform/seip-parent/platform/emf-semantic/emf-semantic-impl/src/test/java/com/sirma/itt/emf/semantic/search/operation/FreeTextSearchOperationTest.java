package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests the free text query building in {@link FreeTextSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class FreeTextSearchOperationTest {

	@Mock
	private FreeTextSearchProcessor freeTextSearchProcessor;

	@Mock
	private SemanticConfiguration semanticConfigurations;

	@InjectMocks
	private FreeTextSearchOperation freeTextSearchOperation;

	@Before
	public void initialize() {
		freeTextSearchOperation = new FreeTextSearchOperation();
		MockitoAnnotations.initMocks(this);

		Mockito.when(freeTextSearchProcessor.buildFreeTextSearchQuery(Matchers.anyString())).thenReturn("enhanced-fts-query");

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

		Pattern expectedPattern = Pattern.compile(
				"\\s+\\?search a test-index ;\\s+solr:query '''enhanced-fts-query''' ;\\s+solr:entities \\?instance.\\s+");
		Matcher expectedMatcher = expectedPattern.matcher(builder.toString());

		Assert.assertTrue(expectedMatcher.matches());
	}

}
