package com.sirma.itt.emf.solr.services.query.parser;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.configuration.TokenProcessorConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.semantic.search.FTSQueryParser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The FTSQueryParser is the solr implementation of {@link FTSQueryParser}
 *
 * @author bbanchev
 * @see FreeTextSolrProcessor
 *
 * @deprecated (Deprecated use FreeTextSolrProcessor instead.)
 */
@Deprecated
@ApplicationScoped
public class SolrQueryParserImpl implements FTSQueryParser {

	/** Delimiter used as special marker. */
	public static final String DELIMITER = "\u00B6";
	@Inject
	private SolrSearchConfiguration searchConfiguration;

	/**
	 * {@inheritDoc}
	 *
	 * @param rawQuery
	 *            the raw query to process - list of toke/phrases, containing keywords as NOT OR AND
	 */
	@Override
	public String prepare(String rawQuery) {
		return prepareInternal(rawQuery);
	}

	/**
	 * Prepare internal the query for solr. Iterates over the provided raw string and generates template
	 *
	 * @param rawQuery
	 *            the raw query to process - list of toke/phrases, containing keywords as NOT OR AND
	 * @return the fts ready query
	 */
	private String prepareInternal(String rawQuery) {
		if (StringUtils.isBlank(rawQuery)) {
			return SolrQueryConstants.QUERY_DEFAULT_EMPTY;
		}


		String rawTrimmed = rawQuery.trim();
		// leave now only not escaped "
		rawTrimmed = rawTrimmed.replace("\\\"", DELIMITER);
		int phraseIndexEnd = 0;
		int phraseIndexStart = -1;
		StringBuilder result = new StringBuilder();
		String template = searchConfiguration.getFullTextSearchTemplate().get();
		List<TokenProcessorConfiguration> tokenPreprocessors = searchConfiguration.getFullTextTokenPreprocessModel().get();
		Pattern ftsEscapeRegexPattern = searchConfiguration.getFullTextSearchEscapePattern().get();
		while ((phraseIndexStart = rawTrimmed.indexOf('\"', phraseIndexEnd)) > -1) {
			if (phraseIndexEnd > 0) {
				String gap = rawTrimmed.substring(phraseIndexEnd, phraseIndexStart);
				result.append(notPhraseParse(template, gap, ftsEscapeRegexPattern));
			} else {
				result.append(
						notPhraseParse(template, rawTrimmed.substring(0, phraseIndexStart), ftsEscapeRegexPattern));
			}
			phraseIndexEnd = rawTrimmed.indexOf('\"', phraseIndexStart + 1) + 1;
			// invalid close
			if (phraseIndexEnd == 0) {
				return SolrQueryConstants.QUERY_DEFAULT_EMPTY;
			}
			String token = rawTrimmed.substring(phraseIndexStart, phraseIndexEnd);
			for (TokenProcessorConfiguration tokenProcessor : tokenPreprocessors) {
				token = tokenProcessor.process(token);
			}
			result.append(MessageFormat.format(template, token));
		}
		if (phraseIndexEnd > 0) {
			result.append(notPhraseParse(template,
					rawTrimmed.substring(phraseIndexEnd, rawTrimmed.length()),
					ftsEscapeRegexPattern));
		} else {
			result.append(notPhraseParse(template, rawTrimmed, ftsEscapeRegexPattern));
		}
		return result.toString().replace(DELIMITER, "\\\"");
	}

	/**
	 * Not phrase parse - it means this is string that is not surrounded with "
	 *
	 * @param template
	 *            the template
	 * @param gap
	 *            the gap is th string that is not phrase
	 * @param ftsEscapeRegexPattern
	 *            the pattern to escape
	 * @return the string builder of parsed gap
	 */
	private static StringBuilder notPhraseParse(String template, String gap, Pattern ftsEscapeRegexPattern) {

		if (gap.length() <= 0) {
			return new StringBuilder(0);
		}
		String nonPhraseString = gap;
		if (!ftsEscapeRegexPattern.pattern().isEmpty()) {
			Matcher regexMatcher = ftsEscapeRegexPattern.matcher(nonPhraseString);
			nonPhraseString = regexMatcher.replaceAll("\\\\$1");
		}
		int length = nonPhraseString.length();
		StringBuilder subsequence = new StringBuilder();
		StringBuilder bufferConsumer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = nonPhraseString.charAt(i);
			if (c == '(' || c == ')' || c == ' ') {
				consume(template, subsequence, bufferConsumer);
				bufferConsumer.delete(0, bufferConsumer.length());
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
	private static void consume(String template, StringBuilder subsequence, StringBuilder bufferConsumer) {
		if (bufferConsumer.length() > 0) {
			String consumed = bufferConsumer.toString();
			String consumedUpperCase = consumed.toUpperCase();
			if ("OR".equals(consumedUpperCase) || "AND".equals(consumedUpperCase) || "NOT".equals(consumedUpperCase)) {
				subsequence.append(consumedUpperCase);
			} else {
				subsequence.append(MessageFormat.format(template, consumed));
			}
		}
	}
}
