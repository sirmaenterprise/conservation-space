/**
 * 
 */
package com.sirma.itt.emf.semantic.query.grammar;

import org.antlr.v4.runtime.misc.NotNull;

import com.sirma.itt.emf.semantic.query.Operator;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser.ObjectContext;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser.OperatorContext;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser.QueryContext;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser.SubjectContext;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser.ValueContext;

/**
 * @author kirq4e
 */
public class QueryToSparql extends SirmaBaseListener {

	SparqlQuery query = new SparqlQuery();

	public static final String QUERY_OPEN_BRACKET = "( ";
	public static final String QUERY_CLOSE_BRACKET = " ) ";

	public static final String QUERY_VARIABLE = "?";

	public static final String QUERY_EXTERNAL_ID = "externalID";
	public static final String QUERY_OBJECT_TYPE = "type";

	public static final String QUERY_FILTER_BLOCK_START = "FILTER " + QUERY_OPEN_BRACKET;

	public static final String QUERY_ORDER_BY_BLOCK = " ORDER BY ";

	public static final String QUERY_REGEX_FUNCTION_FORMAT = "REGEX(%s, %s, 'i')";

	private StringBuilder filterClause;

	private String variable;

	private String valueVariable;

	@Override
	public void enterQuery(@NotNull QueryContext ctx) {
		query.appendWhereClause("object", "emf:externalID", QUERY_EXTERNAL_ID, true);
		query.appendSelectVariable(QUERY_EXTERNAL_ID);
		query.appendSelectVariable("object");
		query.appendSelectVariable(QUERY_OBJECT_TYPE);
		query.appendWhereClause("object", "a", QUERY_OBJECT_TYPE, false);
		query.appendWhereClause(QUERY_OBJECT_TYPE, "emf:isSearchable", "isSearchable", true, false);
	}

	@Override
	public void enterObject(@NotNull ObjectContext ctx) {
		String className = ctx.v;
		if (className != null && !className.equalsIgnoreCase("ALL")) {
			query.addBinding("type", className);
		}
	}

	@Override
	public void enterSubject(@NotNull SubjectContext ctx) {
		String value = ctx.v;
		
		if(value.contains("#")) {
			variable = value.substring(value.indexOf("#") + 1);
		} else {
			variable = value.substring(value.lastIndexOf("/") + 1);
		}
		
		valueVariable = variable + ((Double) (Math.random() * 100)).intValue();

		query.appendWhereClause("object", "<" + ctx.v + ">", variable, false);
	}

	@Override
	public void enterOperator(@NotNull OperatorContext ctx) {
		filterClause = new StringBuilder();
		filterClause.append(QUERY_FILTER_BLOCK_START);
		boolean appendFilterClause = true;

		if (Operator.LIKE.equals(ctx.op)) {
			filterClause.append(String.format(QUERY_REGEX_FUNCTION_FORMAT, QUERY_VARIABLE
					+ variable, QUERY_VARIABLE + valueVariable));
		} else if (Operator.EQUALS.equals(ctx.op)) {
			valueVariable = variable;
			appendFilterClause = false;
		} else {
			filterClause.append(QUERY_VARIABLE).append(variable);
			filterClause.append(" " + ctx.getText() + " ").append(QUERY_VARIABLE)
					.append(valueVariable);
		}

		if (appendFilterClause) {
			filterClause.append(QUERY_CLOSE_BRACKET);
			query.appendFilterClause(filterClause.toString());
		}
		filterClause = null;

	}

	@Override
	public void enterValue(@NotNull ValueContext ctx) {
		query.addBinding(valueVariable, ctx.v);
		variable = "";
		valueVariable = "";
	}

	/**
	 * Returns the query
	 * 
	 * @return The query
	 */
	public SparqlQuery getQuery() {
		return query;
	}

}
