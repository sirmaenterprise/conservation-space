package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_END;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CLOSE_BRACKET;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.DOT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.FILTER_BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.NOT_EXISTS_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_TYPE_VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VALUES;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.generateVarName;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_FIELD;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_RELATION;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Utility methods for building SPARQL statements and filters out of provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
public final class SemanticSearchOperationUtils {

	public static final String INSTANCE_VAR = VARIABLE + OBJECT;
	public static final String DOUBLE_QUOTE = "\"";

	/**
	 * Private constructor for utility class.
	 */
	private SemanticSearchOperationUtils() {
		// Prevents instantiation.
	}

	/**
	 * Checks the provided {@link Rule}'s values if they exists and if there are elements it is considered to be non
	 * empty.
	 *
	 * @param rule
	 *            - the provided search rule with values
	 * @return true if the provided rule has no values or if they are empty and false if not
	 */
	public static boolean isRuleEmpty(Rule rule) {
		return rule.getValues() == null || rule.getValues().isEmpty();
	}

	/**
	 * Appends a triple statement in the provided {@link StringBuilder} with the given values. If the predicate matches
	 * anyRelation a random variable name will be generated for it instead.
	 *
	 * @param builder
	 *            - the string builder where the statement will be appended
	 * @param sub
	 *            - the triple subject
	 * @param pred
	 *            - the triple predicate
	 * @param obj
	 *            - the triple object
	 */
	public static void appendTriple(StringBuilder builder, String sub, String pred, String obj) {
		String predLocal = pred;
		if (ANY_RELATION.equals(predLocal)) {
			predLocal = EMF.PREFIX + SPARQLQueryHelper.URI_SEPARATOR + EMF.HAS_RELATION.getLocalName();
		}

		if (ANY_FIELD.equals(predLocal)) {
			predLocal = generateVarName();
		}

		String objLocal = obj;
		if (ANY_OBJECT.equals(objLocal)) {
			objLocal = generateVarName();
		}
		builder.append(" ").append(sub).append(" ").append(predLocal).append(" ").append(objLocal);
		builder.append(DOT).append(" ");
	}

	/**
	 * Appends a triple statement with a variable as object and a regex filter clause for that variable. The regex
	 * filter is case <b>in</b>sensitive and could be configured to be inclusive or exclusive on both ends of the
	 * {@link Rule} values.
	 *
	 * @param builder
	 *            - the string builder where the statement will be appended
	 * @param rule
	 *            - the rule with values for appending
	 * @param startsWith
	 *            - defines if the regex filter should be constructed to starts with the value or not
	 * @param endsWith
	 *            - defines if the regex filter should be constructed to ends with the value or not
	 */
	public static void appendStringTripleAndFilter(StringBuilder builder, Rule rule, boolean startsWith,
			boolean endsWith) {
		if (!rule.getValues().contains(ANY_OBJECT)) {
			String var = generateVarName();
			appendTriple(builder, INSTANCE_VAR, rule.getField(), var);
			appendStringRegexFilter(builder, var, rule.getValues(), startsWith, endsWith);
		}
	}

	/**
	 * Appends a triple statement with a variable as object and a regex filter clause for that variable. The regex
	 * filter is case <b>in</b>sensitive and could be configured to be inclusive or exclusive on both ends of the
	 * {@link Rule} values. The difference with
	 * {@link #appendStringTripleAndFilter(StringBuilder, Rule, boolean, boolean)} is this constructs the regex filter
	 * to be inverted e.g. to not contain specific {@link Rule} value.
	 *
	 * @param builder
	 *            - the string builder where the statement will be appended
	 * @param rule
	 *            - the rule with values for appending
	 * @param startsWith
	 *            - defines if the regex filter should be constructed to does not start with the value or not
	 * @param endsWith
	 *            - defines if the regex filter should be constructed to does not end with the value or not
	 */
	public static void appendStringTripleAndInversedFilter(StringBuilder builder, Rule rule, boolean startsWith,
			boolean endsWith) {
		String var = generateVarName();
		appendUnionBlock(builder, () -> appendNegateTriple(builder, rule, var), () -> {
			appendTriple(builder, INSTANCE_VAR, rule.getField(), var);
			appendInversedStringRegexFilter(builder, var, rule.getValues(), startsWith, endsWith);
		});
	}

	/**
	 * Appends a SPARQL date statement with a filter for before or after depending on the provided parameters. If the
	 * provided rule has values' count different than one, nothing will be appended.
	 *
	 * @param builder
	 *            - the string builder where the statement is appended.
	 * @param rule
	 *            - the rule containing the date information
	 * @param before
	 *            - determines if the statement will be before or after the {@link Rule} value.
	 */
	public static void appendSingleDateStatement(StringBuilder builder, Rule rule, boolean before) {
		if (rule.getValues().size() != 1) {
			return;
		}
		String value = rule.getValues().get(0);
		if (StringUtils.isBlank(value)) {
			return;
		}

		String var = generateVarName();
		appendTriple(builder, INSTANCE_VAR, rule.getField(), var);
		appendDateFilter(builder, var, value, before);
	}

	/**
	 * Appends a filter clause in the provided builder for before or after depending on the provided parameters.
	 *
	 * @param builder
	 *            - the string builder where the clause will be appended
	 * @param var
	 *            - the statement variable
	 * @param val
	 *            - the value to be appended in the clause
	 * @param before
	 *            - determines if the statement will be before or after the {@link Rule} value.
	 */
	public static void appendDateFilter(StringBuilder builder, String var, Object val, boolean before) {
		builder.append(FILTER_BLOCK_START).append(var);
		if (before) {
			builder.append(" < ");
		} else {
			builder.append(" >= ");
		}
		builder.append("xsd:dateTime(").append(DOUBLE_QUOTE).append(val).append(DOUBLE_QUOTE).append(")) ");
	}

	/**
	 * Appends a numeric statement to the provided builder for numeric comparison depending on the given operation
	 *
	 * @param builder
	 *            the builder on which to append the statement
	 * @param rule
	 *            the rule containing the numeric information
	 * @param operation
	 *            the comparison operation to be performed
	 */
	public static void appendNumericStatement(StringBuilder builder, Rule rule, ArithmeticOperators operation) {
		if (rule.getValues().size() != 1) {
			return;
		}

		String var = generateVarName();
		appendTriple(builder, INSTANCE_VAR, rule.getField(), var);
		appendNumericFilter(builder, rule.getValues().get(0), var, operation);
	}

	/**
	 * Appends a filter clause in the provided builder providing statement that finds a number that comparable to the
	 * specified operation
	 *
	 * @param builder
	 *            the builder on which to append the filter
	 * @param value
	 *            the value to be compared
	 * @param variable
	 *            the statement variable
	 * @param operation
	 *            the comparison operation to be performed on the given value
	 */
	public static void appendNumericFilter(StringBuilder builder, Object value, String variable,
			ArithmeticOperators operation) {
		builder.append(FILTER_BLOCK_START);
		builder.append(variable).append(" ").append(operation.value()).append(" ").append(value);
		builder.append(CLOSE_BRACKET);
	}

	/**
	 * Appends a SPARQL boolean statement for the given field with the given value.
	 *
	 * @param builder
	 *            the string builder where the statement is appended
	 * @param field
	 *            the statement field
	 * @param value
	 *            the statement boolean value
	 */
	public static void appendBooleanStatement(StringBuilder builder, String field, String value) {
		appendTriple(builder, INSTANCE_VAR, field, "\"" + value + "\"^^xsd:boolean");
	}

	private static void appendStringRegexFilter(StringBuilder builder, String var, List<String> values,
			boolean startsWith, boolean endsWith) {
		builder.append(FILTER_BLOCK_START);

		appendStringRegex(builder, var, values.get(0), startsWith, endsWith);

		for (int i = 1; i < values.size(); i++) {
			builder.append(" || ");
			appendStringRegex(builder, var, values.get(i), startsWith, endsWith);
		}

		builder.append(") ");
	}

	private static void appendStringRegex(StringBuilder builder, String var, String val, boolean startsWith,
			boolean endsWith) {
		builder.append("regex(lcase(str(").append(var).append(")), \"");
		if (startsWith) {
			builder.append("^");
		}
		builder.append(escapeRegexValue(val.toLowerCase()));
		if (endsWith) {
			builder.append("$");
		}
		builder.append("\", \"i\")");
	}

	private static void appendInversedStringRegexFilter(StringBuilder builder, String var, List<String> values,
			boolean startsWith, boolean endsWith) {
		builder.append(FILTER_BLOCK_START);
		appendInvertedStringRegex(builder, var, values.get(0), startsWith, endsWith);

		for (int i = 1; i < values.size(); i++) {
			builder.append(" && ");
			appendInvertedStringRegex(builder, var, values.get(i), startsWith, endsWith);
		}

		builder.append(") ");
	}

	private static void appendInvertedStringRegex(StringBuilder builder, String var, String val, boolean startsWith,
			boolean endsWith) {
		builder.append("regex(lcase(str(").append(var).append(")), \"^");
		String escapedValue = escapeRegexValue(val.toLowerCase());
		if (startsWith && endsWith) {
			// Does not equal
			builder.append("(?!").append(escapedValue).append("$).*");
		} else if (!startsWith && !endsWith) {
			// Does not contain
			builder.append("((?!").append(escapedValue).append(").)*");
		} else if (startsWith) {
			// Does not start with
			builder.append("(?!").append(escapedValue).append(").*");
		} else {
			// Does not end with
			builder.append(".*(?<!").append(escapedValue).append(")");
		}
		builder.append("$\", \"i\")");
	}

	private static String escapeRegexValue(String value) {
		String escapedValue = StringEscapeUtils.escapeJava(value);
		return "\\\\Q" + escapedValue + "\\\\E";
	}

	/**
	 * Appends a negate triple statement in the provided {@link StringBuilder} with the given values. Uses filter not
	 * bound to check for the absence of triples.
	 *
	 * @param builder
	 *            - the string builder where the clause will be appended
	 * @param rule
	 *            - the rule containing the date information
	 * @param var
	 *            - the triple object
	 */
	public static void appendNegateTriple(StringBuilder builder, Rule rule, String var) {
		// append negate triple without using a callback embedded query
		appendNegateTriple(builder, rule, var, () -> {
		});
	}

	/**
	 * Appends a negate triple statement in the provided {@link StringBuilder} with the given values. Uses filter not
	 * bound to check for the absence of triples. The final parameter specifies a callback method that is called inside
	 * the filter negate block, this callback may be used to modify the builder and insert a query inside the filter
	 * negate block
	 *
	 * @param builder
	 *            - the string builder where the clause will be appended
	 * @param rule
	 *            - the rule containing the date information
	 * @param var
	 *            - the triple object
	 * @param callback
	 *            - the callback which could modify the existing builder
	 */
	public static void appendNegateTriple(StringBuilder builder, Rule rule, String var, Runnable callback) {
		builder.append(FILTER_BLOCK_START).append(NOT_EXISTS_START);

		appendTriple(builder, INSTANCE_VAR, rule.getField(), var);
		callback.run();
		builder.append(CURLY_BRACKET_CLOSE).append(CLOSE_BRACKET);
	}

	/**
	 * Appends a block of opening & closing curly brackets around a given statement
	 * 
	 * @param builder
	 *            the builder at which to append the block and the statement
	 * @param runnable
	 *            the statement
	 */
	public static void appendBracketBlock(StringBuilder builder, Runnable runnable) {
		builder.append(CURLY_BRACKET_OPEN);
		runnable.run();
		builder.append(CURLY_BRACKET_CLOSE);
	}

	/**
	 * Appends a union block to a builder between two statements
	 * 
	 * @param builder
	 *            the builder
	 * @param left
	 *            the left hand side of the union
	 * @param right
	 *            the right hand side of the union
	 */
	public static void appendUnionBlock(StringBuilder builder, Runnable left, Runnable right) {
		appendBracketBlock(builder, left);
		builder.append(UNION);
		appendBracketBlock(builder, right);
	}

	/**
	 * Appends Values block that specifies a list of values for the given variable name. The variable must exist in a
	 * triplet that is already append to the query
	 * 
	 * @param builder
	 *            the builder
	 * @param variableName
	 *            name of the variable to set the values
	 * @param values
	 *            List of values
	 */
	public static void appendValuesBlock(StringBuilder builder, String variableName, List<String> values) {
		if (values == null || values.isEmpty()) {
			return;
		}

		String variableNameLocal = variableName;
		if (StringUtils.isNotEmpty(variableName) && !variableName.startsWith(VARIABLE)) {
			variableNameLocal = VARIABLE + variableNameLocal;
		}

		builder.append(VALUES + variableNameLocal + BLOCK_START);
		for (Serializable value : values) {
			builder.append(value).append(" ");
		}
		builder.append(BLOCK_END);
	}
	
	/**
	 * Build the ignored instance types triple. The passed ignored instance types should be split with a comma between
	 * them.
	 *
	 * @param ignoreInstancesForType
	 *            the ignored instance types.
	 * @return the built triple
	 */
	public static String buildIgnoreInstancesForType(String ignoreInstancesForType) {
		return buildIgnoreInstancesForType(ignoreInstancesForType, OBJECT_TYPE_VARIABLE);
	}

	/**
	 * Build the ignored instance types triple according to the passed instance type variable. The passed ignored
	 * instance types should be split with a comma between them.
	 *
	 * @param ignoreInstancesForType
	 *            the ignored instance types.
	 * @param instanceTypeVariable
	 *            instance type variable name
	 * @return the built triple
	 */
	public static String buildIgnoreInstancesForType(String ignoreInstancesForType, String instanceTypeVariable) {
		if (StringUtils.isNotBlank(ignoreInstancesForType)) {
			// if the filter configuration is provided then build a query like this:
			String ignoreClause = Arrays.stream(ignoreInstancesForType.split(","))
					.map(instanceType -> instanceType.trim())
					.filter(instanceType -> StringUtils.isNotBlank(instanceType))
					.map(instanceType -> instanceTypeVariable + " != \"" + instanceType + "\"")
					.collect(Collectors.joining(" && "));
			if (StringUtils.isNotBlank(ignoreClause)) {
				return FILTER_BLOCK_START + ignoreClause + CLOSE_BRACKET + LINE_SEPARATOR;
			}
		}
		return null;
	}

}
