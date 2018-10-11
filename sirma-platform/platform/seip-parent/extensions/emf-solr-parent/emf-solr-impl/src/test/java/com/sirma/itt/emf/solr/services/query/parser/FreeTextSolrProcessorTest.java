package com.sirma.itt.emf.solr.services.query.parser;

import com.sirma.itt.emf.solr.configuration.RankConfigurations;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfigurations;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

/**
 * Test the Solr free text processing logic in {@link FreeTextSolrProcessor} as an implementation of
 * {@link FreeTextSearchProcessor}.
 *
 * @author Mihail Radkov
 */
public class FreeTextSolrProcessorTest {

	private final static String QUERY_TEMPLATE = "({!join from=id to=assignee score=max}{!edismax v=$qq qf=title^8} OR {!edismax v=$qq})";

	@Mock
	private RankConfigurations rankConfigurations;

	@Mock
	private SolrSearchConfigurations solrSearchConfigurations;

	@InjectMocks
	private FreeTextSearchProcessor freeTextSolrProcessor;

	@Before
	public void beforeEach() {
		freeTextSolrProcessor = new FreeTextSolrProcessor();
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
	public void should_BuildQuery_When_WildcardTermsAreEnabled() {
		enableWildcards(true);
		String field = "assigne";
		String searchTerms = "keywords";
		String query = freeTextSolrProcessor.process(field, searchTerms, true);

		Assert.assertEquals("{\"q\":\"*keywords*\",\"df\":\"assigne\"}", query);
	}

	@Test
	public void should_BuildQuery_When_WildcardTermsConfigurationAreEnabledButPassedArgumentsIsFalse() {
		enableWildcards(true);
		String field = "assigne";
		String searchTerms = "keywords";
		String query = freeTextSolrProcessor.process(field, searchTerms, false);

		Assert.assertEquals("{\"q\":\"keywords\",\"df\":\"assigne\"}", query);
	}

	@Test
	public void should_BuildQuery_When_WildcardTermsAreNotEnabled() {
		String field = "assigne";
		String searchTerms = "keywords";
		String query = freeTextSolrProcessor.process(field, searchTerms, false);

		Assert.assertEquals("{\"q\":\"keywords\",\"df\":\"assigne\"}", query);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_When_SearchThermIsBlank() {
		freeTextSolrProcessor.process(null, null, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowException_When_FieldIsBlank() {
		freeTextSolrProcessor.process(null, "keywords", false);
	}

	@Test
	public void shouldConstructSolrEDisMaxRankingRequest() {
		String processedKeywords = freeTextSolrProcessor.process("one two three");
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
		String processedKeywords = freeTextSolrProcessor.process("\"exact match\"");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"exact match\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldEscapeConfiguredSymbols() {
		String processedKeywords = freeTextSolrProcessor.process("symbols [for] escape");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("symbols \\[for\\] escape", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldDisallowNullSearchTerms() {
		freeTextSolrProcessor.process(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldDisallowEmptySearchTerms() {
		freeTextSolrProcessor.process("");
	}

	@Test
	public void shouldAppendWildcardsIfConfigured() {
		enableWildcards(true);
		// Single term
		String processedKeywords = freeTextSolrProcessor.process("foo");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("*foo*", solrRequest.getString(SolrQueryConstants.USER_QUERY));
		// Multiple terms
		processedKeywords = freeTextSolrProcessor.process("foo bar");
		solrRequest = readRequest(processedKeywords);
		assertEquals("*foo* *bar*", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldNotAppendWildcardsForExactMatch() {
		enableWildcards(true);
		// Single term
		String processedKeywords = freeTextSolrProcessor.process("\"foo\"");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"foo\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
		// Multiple terms
		processedKeywords = freeTextSolrProcessor.process("\"foo bar\"");
		solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"foo bar\\\"", solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldAppendWildcardsToAllTermsExceptExactMatches() {
		enableWildcards(true);
		String processedKeywords = freeTextSolrProcessor.process("\"one and a half\" two \"three\" four");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("\\\"one and a half\\\" *two* \\\"three\\\" *four*",
					 solrRequest.getString(SolrQueryConstants.USER_QUERY));
	}

	@Test
	public void shouldRecognizeSingleQuotationMark() {
		enableWildcards(true);
		String processedKeywords = freeTextSolrProcessor.process("\"foo");
		JsonObject solrRequest = readRequest(processedKeywords);
		assertEquals("*\\\"foo*", solrRequest.getString(SolrQueryConstants.USER_QUERY));

		processedKeywords = freeTextSolrProcessor.process("foo\"");
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
