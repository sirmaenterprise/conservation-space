package com.sirma.itt.emf.semantic.search.operation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sirma.itt.seip.domain.search.SearchContext;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Arrays;
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

		Mockito.when(freeTextSearchProcessor.buildFreeTextSearchQuery(Matchers.anyString(), Matchers.anyList())).thenReturn("enhanced-fts-query");

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

		freeTextSearchOperation.buildOperation(builder, rule, new SearchContext(mock(Condition.class)));

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationForBlankRuleValue() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList(""));
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule, new SearchContext(mock(Condition.class)));

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		freeTextSearchOperation.buildOperation(builder, rule, new SearchContext(mock(Condition.class)));

		Pattern expectedPattern = Pattern.compile(
				"\\s+\\?search a test-index ;\\s+solr:query '''enhanced-fts-query''' ;\\s+solr:entities \\?instance.\\s+");
		Matcher expectedMatcher = expectedPattern.matcher(builder.toString());

		Assert.assertTrue(expectedMatcher.matches());
	}

	@Test
	public void testBuildOperationWithTypeFiltering_anyObject() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		String query = "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"rules\":[{\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"anyObject\"],\"id\":\"2617c2ec-d5be-432a-cba0-4fa278cfbe11\"},{\"id\":\"920bed08-1516-4c26-8758-970e9289e75c\",\"condition\":\"AND\",\"rules\":[{\"id\":\"2bc9d375-681c-48eb-82be-82026a2f9102\",\"field\":\"freeText\",\"type\":\"fts\",\"operator\":\"contains\",\"value\":\"test\"}]}],\"id\":\"8fbd9bcc-934a-4e1d-ca75-8b426a49ff05\"}],\"id\":\"28c3d20b-d0f7-412f-a0f7-eeb699e49434\"}";
		JsonToConditionConverter converter = new JsonToConditionConverter();
		Condition tree = JSON.readObject(query, converter::parseCondition);

		freeTextSearchOperation.buildOperation(builder, rule, new SearchContext(tree));

		Pattern expectedPattern = Pattern.compile(
				"\\s+\\?search a test-index ;\\s+solr:query '''enhanced-fts-query''' ;\\s+solr:entities \\?instance.\\s+");
		Matcher expectedMatcher = expectedPattern.matcher(builder.toString());

		Assert.assertTrue(expectedMatcher.matches());
		verify(freeTextSearchProcessor).buildFreeTextSearchQuery("123", Collections.emptyList());
	}

	@Test
	public void testBuildOperationWithTypeFiltering_specifiedTypes() {
		Rule rule = SearchOperationUtils.createRule("freeText", "fts", "contains", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		String query = "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"rules\":[{\"field\":\"types\",\"type\":\"\",\"operator\":\"equals\",\"value\":[\"ISOAPPS003\",\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document\"],\"id\":\"2617c2ec-d5be-432a-cba0-4fa278cfbe11\"},{\"id\":\"6dee3809-97e7-4a37-83d8-d39bbff8b6e6\",\"condition\":\"AND\",\"rules\":[{\"id\":\"b4f2f226-99cf-4fcb-ffa7-7ec9ba3247dc\",\"field\":\"freeText\",\"type\":\"fts\",\"operator\":\"contains\",\"value\":\"\"}]}],\"id\":\"8fbd9bcc-934a-4e1d-ca75-8b426a49ff05\"}],\"id\":\"28c3d20b-d0f7-412f-a0f7-eeb699e49434\"}";
		JsonToConditionConverter converter = new JsonToConditionConverter();
		Condition tree = JSON.readObject(query, converter::parseCondition);

		freeTextSearchOperation.buildOperation(builder, rule, new SearchContext(tree));

		Pattern expectedPattern = Pattern.compile(
				"\\s+\\?search a test-index ;\\s+solr:query '''enhanced-fts-query''' ;\\s+solr:entities \\?instance.\\s+");
		Matcher expectedMatcher = expectedPattern.matcher(builder.toString());

		Assert.assertTrue(expectedMatcher.matches());
		verify(freeTextSearchProcessor).buildFreeTextSearchQuery("123", Arrays.asList("ISOAPPS003", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"));
	}
}
