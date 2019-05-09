package com.sirma.itt.emf.solr.services.query.parser;

import com.sirma.itt.emf.solr.configuration.RankConfigurations;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfigurations;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test the Solr free text processing logic in {@link FreeTextSolrProcessor} as an implementation of
 * {@link FreeTextSearchProcessor}.
 *
 * @author Mihail Radkov
 */
@RunWith(DataProviderRunner.class)
public class FreeTextSolrProcessorTest {

	private final static String QUERY_TEMPLATE = "({!join from=id to=assignee score=max}{!edismax v=$qq qf=title^8} OR {!edismax v=$qq})";

	@Mock
	private RankConfigurations rankConfigurations;

	@Mock
	private SolrSearchConfigurations solrSearchConfigurations;

	@InjectMocks
	private FreeTextSolrProcessor freeTextSolrProcessor;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(rankConfigurations.getQueryTemplate()).thenReturn(new ConfigurationPropertyMock<>(QUERY_TEMPLATE));
		when(rankConfigurations.getQueryFields()).thenReturn(new ConfigurationPropertyMock<>("title^4 description"));
		when(rankConfigurations.getPhraseFields()).thenReturn(
				new ConfigurationPropertyMock<>("title~5 description~10"));
		when(rankConfigurations.getTieBreaker()).thenReturn(new ConfigurationPropertyMock<>("0.5"));

		Pattern escapePattern = Pattern.compile("([\\[\\]])");
		when(solrSearchConfigurations.getTermEscapeRegex()).thenReturn(new ConfigurationPropertyMock<>(escapePattern));
		// Disable wildcards by default for easier testing
		enableWildcards(false);
	}

	@Test
	@UseDataProvider( "processSuggestDP" )
	public void should_BuildSuggestQuery(String field, String searchPhrase, String expectedUserQuery, String testDescription) {
		String query = freeTextSolrProcessor.buildFieldSuggestionQuery(field, searchPhrase);
		JsonObject solrRequest = readRequest(query);

		assertEquals("Wrong user query in test :" + testDescription, expectedUserQuery, solrRequest.getString(SolrQueryConstants.USER_QUERY));


		assertEquals("Wrong query in test :" + testDescription, "({!edismax v=$uq qf=" + field +"})", solrRequest.getString(CommonParams.Q));
		assertEquals("Wrong tie breaker query in test :" + testDescription, SolrQueryConstants.DEFAULT_TIE_BREAKER, solrRequest.getString(DisMaxParams.TIE));
		assertEquals("Wrong solr query handler in test :" + testDescription, SolrQueryConstants.EDISMAX_DEF_TYPE, solrRequest.getString(SolrQueryConstants.DEF_TYPE));
	}

	@DataProvider
	public static Object[][] processSuggestDP() {
		return new Object[][] {
				{"assignees", "single-term", "*single\\-term*", "Field assignees. Single term."},
				{"assignees", "single-term", "*single\\-term*", "Field assignees. Single term."},
				{"assignees", "search phrase", "*search AND phrase*", "Field assignees. Multi terms."},
				{"assignees", "search        phrase", "*search AND phrase*", "Field assignees. Many spaces."},
				{"assignees", "key and key", "*key AND \\\"and\\\" AND key*", "Field assignees. Lowercase and."},
				{"assignees", "key AND key", "*key AND \\\"AND\\\" AND key*", "Field assignees. Uppercase and."},
				{"assignees", "key AnD key", "*key AND \\\"AnD\\\" AND key*", "Field assignees. Mixed case and."},
				{"assignees", "key or key", "*key AND \\\"or\\\" AND key*", "Field assignees. Lowercase or."},
				{"assignees", "key OR key", "*key AND \\\"OR\\\" AND key*", "Field assignees. Uppercase or."},
				{"assignees", "key Or key", "*key AND \\\"Or\\\" AND key*", "Field assignees. Mixed case or."},
				{"createdBy", "single-term", "*single\\-term*", "Field createdBy. Single term."},
		};
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_When_SearchThermIsBlank() {
		freeTextSolrProcessor.buildFieldSuggestionQuery(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_When_FieldIsBlank() {
		freeTextSolrProcessor.buildFieldSuggestionQuery(null, "keywords");
	}

	@Test
	public void shouldConstructSolrEDisMaxRankingRequest() {
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("one two three");
		JsonObject solrRequest = readRequest(processedKeywords);

		assertEquals("one two three", solrRequest.getString(SolrQueryConstants.USER_QUERY));
		assertEquals(QUERY_TEMPLATE, solrRequest.getString(CommonParams.Q));
		assertEquals("title^4 description", solrRequest.getString(DisMaxParams.QF));
		assertEquals("title~5 description~10", solrRequest.getString(DisMaxParams.PF));
		assertEquals("0.5", solrRequest.getString(DisMaxParams.TIE));
		assertEquals(SolrQueryConstants.EDISMAX_DEF_TYPE, solrRequest.getString(SolrQueryConstants.DEF_TYPE));
	}

	@Test
	public void shouldEscapeEmbeddedQuotationMarksForExactMatching() {
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("\"exact match\"");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"exact match\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldEscapeConfiguredSymbols() {
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("symbols [for] escape");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("symbols \\[for\\] escape", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldDisallowNullSearchTerms() {
		freeTextSolrProcessor.buildFreeTextSearchQuery(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldDisallowEmptySearchTerms() {
		freeTextSolrProcessor.buildFreeTextSearchQuery("");
	}

	@Test
	public void shouldAppendWildcardsIfConfigured() {
		enableWildcards(true);
		// Single term
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("foo");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("*foo*", solrRequest.getString(SolrQueryConstants.USER_QUERY));
		// Multiple terms
		processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("foo bar");
		solrRequest = readRequest(processedKeywords);
		assertEquals("*foo* *bar*", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldNotAppendWildcardsForExactMatch() {
		enableWildcards(true);
		// Single term
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("\"foo\"");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"foo\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
		// Multiple terms
		processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("\"foo bar\"");
		solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"foo bar\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldAppendWildcardsToAllTermsExceptExactMatches() {
		enableWildcards(true);
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("\"one and a half\" two \"three\" four");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"one and a half\\\" *two* \\\"three\\\" *four*",
					 solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldRecognizeSingleQuotationMark() {
		enableWildcards(true);
		String processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("\"foo");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("*\\\"foo*", solrRequest.getString(SolrQueryConstants.USER_QUERY));

		processedKeywords = freeTextSolrProcessor.buildFreeTextSearchQuery("foo\"");
		solrRequest = readRequest(processedKeywords);
		assertEquals("*foo\\\"*", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	private void enableWildcards(boolean enable) {
		when(solrSearchConfigurations.enableTermWildcards()).thenReturn(new ConfigurationPropertyMock<>(enable));
	}

	private static JsonObject readRequest(String request) {
		try (JsonReader jsonReader = Json.createReader(new StringReader(request))) {
			return jsonReader.readObject();
		}
	}
}
