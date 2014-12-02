/**
 * 
 */
package com.sirma.itt.emf.semantic.query.grammar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kirq4e
 */
public class SparqlQuery {

	private List<String> selectVariables = new LinkedList<>();

	private StringBuilder whereClause = new StringBuilder("?object emf:externalID ?externalID . \n");

	private Map<String, Serializable> bindings = new HashMap<String, Serializable>();
	private List<StringBuilder> clauseList = new LinkedList<>();

	private Set<String> variables = new HashSet<>();

	/**
	 * Append triplet where clause to the current query
	 * 
	 * @param subject
	 *            Subject of the clause
	 * @param predicate
	 *            Predicate of the clause
	 * @param variableName
	 *            Variable name of the binding
	 * @param isOptional
	 *            True if the clause is optional
	 */
	public void appendWhereClause(String subject, String predicate, String variableName,
			boolean isOptional) {
		StringBuilder builder = new StringBuilder();
		if (isOptional) {
			builder.append("OPTIONAL {");
		}
		builder.append("?").append(subject).append(" ").append(predicate).append(" ").append("?")
				.append(variableName).append(" . ");
		if (isOptional) {
			builder.append("}");
		}
		builder.append("\n");
		if (!variables.contains(variableName)) {
			clauseList.add(builder);
			variables.add(variableName);
		}
	}

	/**
	 * Append triplet where clause to the current query
	 * 
	 * @param subject
	 *            Subject of the clause
	 * @param predicate
	 *            Predicate of the clause
	 * @param variableName
	 *            Variable name of the binding
	 * @param value
	 *            The variable value
	 * @param isOptional
	 *            True if the clause is optional
	 */
	public void appendWhereClause(String subject, String predicate, String variableName,
			Serializable value, boolean isOptional) {
		appendWhereClause(subject, predicate, variableName, isOptional);

		if (value != null) {
			bindings.put(variableName, value);
		}
	}

	/**
	 * Add variable to the result of the query
	 * 
	 * @param variableName
	 *            Variable name
	 */
	public void appendSelectVariable(String variableName) {
		selectVariables.add("?" + variableName);
	}

	/**
	 * Add binding to a variable
	 * 
	 * @param key
	 *            The variable name
	 * @param value
	 *            The binding value
	 */
	public void addBinding(String key, Serializable value) {
		bindings.put(key, value);
	}

	/**
	 * Append filter clause to the query
	 * 
	 * @param filterClause
	 *            The filter clause
	 */
	public void appendFilterClause(String filterClause) {
		StringBuilder builder = new StringBuilder();
		builder.append(filterClause).append(" . ").append("\n");
		clauseList.add(builder);
	}

	/**
	 * Build query as string
	 * 
	 * @return The Saprql query as sting
	 */
	public String buildQuery() {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT DISTINCT ");
		for (String variableName : selectVariables) {
			builder.append(variableName).append(" ");
		}
		builder.append("WHERE {\n");

		for (StringBuilder clause : clauseList) {
			builder.append(clause);
		}
		builder.append("}");

		return builder.toString();
	}

	/**
	 * Return the bindings for the query
	 * 
	 * @return The bindings for the query
	 */
	public Map<String, Serializable> getBindings() {
		return bindings;
	}

}
