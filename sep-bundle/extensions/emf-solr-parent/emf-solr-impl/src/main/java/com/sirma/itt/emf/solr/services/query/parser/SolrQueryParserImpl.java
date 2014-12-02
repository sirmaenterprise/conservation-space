package com.sirma.itt.emf.solr.services.query.parser;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.solr.configuration.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * The FTSQueryParser is the solr implementation of {@link FTSQueryParser}
 *
 * @author bbanchev
 */

@ApplicationScoped
public class SolrQueryParserImpl implements FTSQueryParser {

	/** Delimiter used as special marker. */
	public static final String DELIMITER = "\u00B6";
	/** The template. */
	@Inject
	@Config(name = SolrConfigurationProperties.FULL_TEXT_SEARCH_QUERY, defaultValue = "all_text:({0})")
	private String template;

	/** The template. */
	@Inject
	@Config(name = SolrConfigurationProperties.FULL_TEXT_SEARCH_ESCAPE_REGEX, defaultValue = "([:\\[\\]])")
	private String escapeRegex;

	/** The fts escape regex pattern. */
	private Pattern ftsEscapeRegexPattern;

	/**
	 * Inits the parsert by constructing the escape chars regex.
	 */
	@PostConstruct
	public void init() {
		if (StringUtils.isNotNullOrEmpty(escapeRegex)) {
			ftsEscapeRegexPattern = Pattern.compile(escapeRegex);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param rawQuery
	 *            the raw query to process - list of toke/phrases, containing keywords as NOT OR AND
	 */
	@Override
	public String prepare(String rawQuery) {
		return prepareInternal(template, rawQuery);
	}

	/**
	 * Prepare internal the query for solr.
	 *
	 * @param template
	 *            the template to set on each token/phrase
	 * @param rawQuery
	 *            the raw query to process - list of toke/phrases, containing keywords as NOT OR AND
	 * @return the fts ready query
	 */
	private String prepareInternal(String template, String rawQuery) {
		if (StringUtils.isNullOrEmpty(rawQuery)) {
			return SolrQueryConstants.QUERY_DEFAULT_EMPTY;
		}

		String rawTrimmed = rawQuery.trim();
		// leave now only not escaped "
		rawTrimmed = rawTrimmed.replace("\\\"", DELIMITER);
		StringBuffer escapedTerm = new StringBuffer();
		int lastEscapedEnd = -1;
		if (ftsEscapeRegexPattern != null) {
			Matcher matcher = ftsEscapeRegexPattern.matcher(rawTrimmed);
			while (matcher.find()) {
				matcher.appendReplacement(escapedTerm, "\\\\" + matcher.group(1));
				lastEscapedEnd = matcher.end();
			}
			if (lastEscapedEnd > 0) {
				escapedTerm.append(rawTrimmed.substring(lastEscapedEnd));
			}
		}
		if (lastEscapedEnd == -1) {
			escapedTerm.append(rawTrimmed);
		}
		rawTrimmed = escapedTerm.toString();
		escapedTerm = null;
		int phraseIndexEnd = 0;
		int phraseIndexStart = -1;
		StringBuilder result = new StringBuilder();
		while ((phraseIndexStart = rawTrimmed.indexOf("\"", phraseIndexEnd)) > -1) {
			if (phraseIndexEnd > 0) {
				String gap = rawTrimmed.substring(phraseIndexEnd, phraseIndexStart);
				result.append(notPhraseParse(template, gap));
			} else {
				result.append(notPhraseParse(template, rawTrimmed.substring(0, phraseIndexStart)));
			}
			phraseIndexEnd = rawTrimmed.indexOf("\"", phraseIndexStart + 1) + 1;
			// invalid close
			if (phraseIndexEnd == 0) {
				return SolrQueryConstants.QUERY_DEFAULT_EMPTY;
			}
			String token = rawTrimmed.substring(phraseIndexStart, phraseIndexEnd);
			result.append(MessageFormat.format(template, token));
		}
		if (phraseIndexEnd > 0) {
			result.append(notPhraseParse(template,
					rawTrimmed.substring(phraseIndexEnd, rawTrimmed.length())));
		} else {
			result.append(notPhraseParse(template, rawTrimmed));
		}
		return result.toString().replace(DELIMITER, "\\\"");
	}

	/**
	 * Not phrase parse
	 *
	 * @param template
	 *            the template
	 * @param gap
	 *            the gap
	 * @return the string builder
	 */
	private StringBuilder notPhraseParse(String template, String gap) {
		StringBuilder subsequence = new StringBuilder();
		int length = gap.length();
		StringBuilder bufferConsumer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = gap.charAt(i);
			// || c == '\\'
			if (c == '(' || c == ')' || c == ' ') {
				consume(template, subsequence, bufferConsumer);
				bufferConsumer.replace(0, bufferConsumer.length(), "");
				subsequence.append(c);
			} else {
				bufferConsumer.append(c);
			}
		}
		consume(template, subsequence, bufferConsumer);
		return subsequence;

	}

	/**
	 * Consumes a token until any special symbol reached.
	 *
	 * @param template
	 *            the template
	 * @param subsequence
	 *            the subsequence
	 * @param bufferConsumer
	 *            the buffer consumer
	 */
	private void consume(String template, StringBuilder subsequence, StringBuilder bufferConsumer) {
		if (bufferConsumer.length() > 0) {
			String consumed = bufferConsumer.toString().toUpperCase();
			if ("OR".equals(consumed) || "AND".equals(consumed) || "NOT".equals(consumed)) {
				subsequence.append(consumed);
			} else {
				subsequence.append(MessageFormat.format(template, bufferConsumer));
			}
		}
	}
}
