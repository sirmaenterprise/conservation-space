package com.sirma.itt.semantic.search;

/**
 * The FTSQueryParser is responsible to parse the query to the specific system fts language - solr, lucene. On the other
 * hand should enrich the provided raw text with the exact fts fields.
 *
 * @see FreeTextSearchProcessor
 *
 * @deprecated (Deprecated use FreeTextSearchProcessor instead.)
 */
@Deprecated
public interface FTSQueryParser {

	/**
	 * Parses the raw query and organize it as subqueries. The process convert each phrase, token into single subquery
	 * with all the fields described in the template
	 *
	 * @param rawQuery
	 *            the raw query
	 * @return the enriched/escaped query or null if query might be not correct
	 */
	String prepare(String rawQuery);

}