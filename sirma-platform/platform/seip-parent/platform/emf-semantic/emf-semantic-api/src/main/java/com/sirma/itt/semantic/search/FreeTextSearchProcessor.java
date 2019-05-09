package com.sirma.itt.semantic.search;

/**
 * Defines a processor where implementations process search terms to a specific query language.
 * <p>
 * All implementations must properly escape quotation marks (used for exact matching) and anything language specific.
 *
 * @author Mihail Radkov
 */
public interface FreeTextSearchProcessor {

	/**
	 * Builds free text search query form provided free text search terms into a language specific request.
	 *
	 * @param searchTerms - free text search terms for processing
	 * @return the processed search terms, could be a multiline string
	 * @throws IllegalArgumentException if the search terms are <code>null</code> or empty
	 */
	String buildFreeTextSearchQuery(String searchTerms);

	/**
	 * Builds <code>field</code> suggestion query. When the query is executed all returned instances must have <code>field</code>,
	 * which value contains <code>searchPhrase</code>.
	 *
	 * @param field        -   the field to be searched for
	 * @param searchPhrase - the search phrase
	 * @return built suggestion query.
	 * @throws IllegalArgumentException if the search phrase or field are <code>null</code> or empty
	 */
	String buildFieldSuggestionQuery(String field, String searchPhrase);
}
