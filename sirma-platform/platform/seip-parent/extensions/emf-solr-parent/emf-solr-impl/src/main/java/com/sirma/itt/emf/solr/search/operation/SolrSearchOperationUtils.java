package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Utility methods for building Solr statements and filters out of provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
public final class SolrSearchOperationUtils {

	public static final String OPEN_BRACKET = "(";
	public static final String CLOSE_BRACKET = ")";
	public static final String AND = " AND ";
	public static final String OR = " OR ";

	/**
	 * Private constructor for utility class.
	 */
	private SolrSearchOperationUtils() {
		// Prevents instantiation.
	}

	/**
	 * Checks the provided {@link Rule}'s values if they exists and if there are elements it is considered to be non
	 * empty.
	 *
	 * @param rule
	 * 		- the provided search rule with values
	 * @return true if the provided rule has no values or if they are empty and false if not
	 */
	public static boolean isRuleEmpty(Rule rule) {
		return rule.getValues() == null || rule.getValues().isEmpty();
	}

	/**
	 * Shortcut method to build single valued Solr clause with given field and value.
	 *
	 * @param field
	 * 		- the Solr field name
	 * @param value
	 * 		- the value
	 * @return constructed clause in the following format: <pre>field:("value")</pre>
	 */
	public static String buildSingleValuedClause(String field, String value) {
		return field + ":(\"" + value + "\")";
	}
}
