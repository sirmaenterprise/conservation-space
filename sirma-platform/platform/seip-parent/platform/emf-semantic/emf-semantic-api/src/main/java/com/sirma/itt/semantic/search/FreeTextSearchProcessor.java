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
	 * Processes the provided free text search terms into a language specific request.
	 *
	 * @param searchTerms - free text search terms for processing
	 * @return the processed search terms, could be a multiline string
	 * @throws IllegalArgumentException if the search terms are <code>null</code> or empty
	 */
	String process(String searchTerms);

	/**
	 * Processes the provided free text search terms for the given field into a language specific request.
	 *
	 * @param field               -   the field to search in
	 * @param searchTerms         - free text search terms for processing
	 * @param enableTermWildcards -
	 *                            <pre>   Enables the processing of search terms to include wildcards. For example:
	 *                               <ol>
	 *                                  <li>
	 *                                      if enableTermWildcards is true and the search term is <i>bar</i>,
	 *                                      it will apply wildcards and would find <i>toolbar</i> and <i>bartender</i> too.
	 *                                  </li>
	 *                                  <li>
	 *                                      if enableTermWildcards is true and the search term is <i>"bar"</i>,
	 *                                      it would not apply wildcards and will find only <i>bar</i>
	 *                                 </li>
	 *                              </ol>
	 *                            </pre>
	 * @return the processed search terms, could be a multiline string
	 * @throws IllegalArgumentException if the search terms or field are <code>null</code> or empty
	 */
	String process(String field, String searchTerms, boolean enableTermWildcards);
}
