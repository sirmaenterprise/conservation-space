package com.sirma.itt.emf.solr.services.query.parser;

import com.sirma.itt.emf.solr.configuration.RankConfigurations;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfigurations;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SimpleParams;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Solr specific implementation for {@link FreeTextSolrProcessor}.
 * Prepares free text search request as JSON {@link String} for Solr ranking by processing provided user input terms.
 * <p>
 * Specific ranking properties are read from {@link RankConfigurations}.
 * The used Solr query handler for the ranking is eDisMax.
 * <p>
 * This implementation escapes quotation marks for exact matching to avoid possible request errors and any character
 * configured in {@link SolrSearchConfigurations#getTermEscapeRegex()}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class FreeTextSolrProcessor implements FreeTextSearchProcessor {

	/**
	 * Pattern matching all search terms & respecting exact matches as single term. Skips whitespace to form groups.
	 */
	private static final Pattern TERM_EXTRACT_PATTERN = Pattern.compile("(\".*?\"|\\S+)");

	private static final String AUTO_SUGGEST_QUERY_TEMPLATE = "({!edismax v=$uq qf=%s})";

	@Inject
	private RankConfigurations rankConfigurations;

	@Inject
	private SolrSearchConfigurations solrSearchConfigurations;

	@Override
	public String buildFreeTextSearchQuery(String searchTerms) {
		if (StringUtils.isBlank(searchTerms)) {
			throw new IllegalArgumentException("Cannot process null or empty search terms!");
		}

		String processedTerms = searchTerms;
		if (solrSearchConfigurations.enableTermWildcards().get()) {
			processedTerms = addTermsWildcards(searchTerms);
		}
		String query = escapeTerms(processedTerms);
		return new SolrDismaxQueryBuilder().setUserQuery(query)
				.setQuery(rankConfigurations.getQueryTemplate().get())
				.setQueryField(rankConfigurations.getQueryFields().get())
				.setPhraseField(rankConfigurations.getPhraseFields().get())
				.setScoreTieBreaker(rankConfigurations.getTieBreaker().get())
				.build();
	}

	@Override
	public String buildFieldSuggestionQuery(String field, String searchPhrase) {
		if (StringUtils.isBlank(searchPhrase)) {
			throw new IllegalArgumentException("Cannot process null or empty search terms!");
		}
		if (StringUtils.isBlank(field)) {
			throw new IllegalArgumentException("Cannot process search without field!");
		}

		String query = StringUtils.wrap(escapeSearchPhrase(searchPhrase), "*");
		String queryTemplate = String.format(AUTO_SUGGEST_QUERY_TEMPLATE, field);
		return new SolrDismaxQueryBuilder().setUserQuery(query)
				.setQuery(queryTemplate)
				.build();
	}

	private String escapeTerms(String searchTerms) {
		String escapedTerms = StringEscapeUtils.escapeJava(searchTerms);
		Matcher matcher = solrSearchConfigurations.getTermEscapeRegex().get().matcher(escapedTerms);
		return matcher.replaceAll("\\\\$1");
	}

	private static String addTermsWildcards(String searchTerms) {
		List<String> processedTerms = new LinkedList<>();
		Matcher matcher = TERM_EXTRACT_PATTERN.matcher(searchTerms);
		while (matcher.find()) {
			String currentTerm = matcher.group();
			if (!StringUtils.startsWith(currentTerm, "\"") || !StringUtils.endsWith(currentTerm, "\"")) {
				currentTerm = StringUtils.wrap(currentTerm, "*");
			}
			processedTerms.add(currentTerm);
		}
		return StringUtils.join(processedTerms, " ");
	}

	private String escapeSearchPhrase(String searchPhrase) {
		return Arrays.stream(searchPhrase.split(" ")).filter(StringUtils::isNotBlank).map(term -> {
			if (SimpleParams.AND_OPERATOR.equalsIgnoreCase(term) || SimpleParams.OR_OPERATOR.equalsIgnoreCase(term)) {
				return StringUtils.wrap(term, "\"");
			}
			return term;
		}).map(ClientUtils::escapeQueryChars).collect(Collectors.joining(" AND "));
	}

	private final class SolrDismaxQueryBuilder {

		private JsonObjectBuilder requestBuilder = Json.createObjectBuilder();

		private SolrDismaxQueryBuilder() {
			requestBuilder.add(SolrQueryConstants.DEF_TYPE, SolrQueryConstants.EDISMAX_DEF_TYPE);
			setScoreTieBreaker(SolrQueryConstants.DEFAULT_TIE_BREAKER);
		}

		private SolrDismaxQueryBuilder setScoreTieBreaker(String tie) {
			requestBuilder.add(DisMaxParams.TIE, tie);
			return this;
		}

		private SolrDismaxQueryBuilder setPhraseField(String phraseField) {
			requestBuilder.add(DisMaxParams.PF, phraseField);
			return this;
		}

		private SolrDismaxQueryBuilder setQueryField(String queryField) {
			requestBuilder.add(DisMaxParams.QF, queryField);
			return this;
		}

		private SolrDismaxQueryBuilder setUserQuery(String userQuery) {
			requestBuilder.add(SolrQueryConstants.USER_QUERY, userQuery);
			return this;
		}

		private SolrDismaxQueryBuilder setQuery(String query) {
			requestBuilder.add(CommonParams.Q, query);
			return this;
		}

		private String build() {
			return requestBuilder.build().toString();
		}
	}
}
