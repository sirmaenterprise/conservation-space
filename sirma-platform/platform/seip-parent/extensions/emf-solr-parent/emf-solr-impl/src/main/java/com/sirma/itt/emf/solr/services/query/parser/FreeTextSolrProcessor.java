package com.sirma.itt.emf.solr.services.query.parser;

import com.sirma.itt.emf.solr.configuration.RankConfigurations;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfigurations;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.semantic.search.FreeTextSearchProcessor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Inject
	private RankConfigurations rankConfigurations;

	@Inject
	private SolrSearchConfigurations solrSearchConfigurations;

	@Override
	public String process(String searchTerms) {
		if (StringUtils.isBlank(searchTerms)) {
			throw new IllegalArgumentException("Cannot process null or empty search terms!");
		}

		String processedTerms = searchTerms;
		if (solrSearchConfigurations.enableTermWildcards().get()) {
			processedTerms = addTermsWildcards(searchTerms);
		}
		processedTerms = escapeTerms(processedTerms);
		return buildJsonStringRequest(processedTerms);
	}

	@Override
	public String process(String field, String searchTerms, boolean enableTermWildcards) {
		if (StringUtils.isBlank(searchTerms)) {
			throw new IllegalArgumentException("Cannot process null or empty search terms!");
		}
		if (StringUtils.isBlank(field)) {
			throw new IllegalArgumentException("Cannot process search without field!");
		}

		String processedTerms = searchTerms;
		if (enableTermWildcards && solrSearchConfigurations.enableTermWildcards().get()) {
			processedTerms = addTermsWildcards(searchTerms);
		}
		processedTerms = escapeTerms(processedTerms);
		return buildJsonStringRequest(field, processedTerms);
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

	private String buildJsonStringRequest(String searchTerms) {
		JsonObjectBuilder requestBuilder = Json.createObjectBuilder();

		requestBuilder.add(SolrQueryConstants.USER_QUERY, searchTerms);
		requestBuilder.add(CommonParams.Q, rankConfigurations.getQueryTemplate().get());
		requestBuilder.add(DisMaxParams.QF, rankConfigurations.getQueryFields().get());
		requestBuilder.add(DisMaxParams.PF, rankConfigurations.getPhraseFields().get());
		requestBuilder.add(DisMaxParams.TIE, rankConfigurations.getTieBreaker().get());
		requestBuilder.add(SolrQueryConstants.DEF_TYPE, SolrQueryConstants.EDISMAX_DEF_TYPE);

		return requestBuilder.build().toString();
	}

	private String buildJsonStringRequest(String field, String searchTerms) {
		JsonObjectBuilder requestBuilder = Json.createObjectBuilder();
		requestBuilder.add(CommonParams.Q, searchTerms);
		requestBuilder.add(CommonParams.DF, field);

		return requestBuilder.build().toString();
	}
}
