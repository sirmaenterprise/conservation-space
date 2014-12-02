// Generated from A:/Development/Workspaces/IDoc/sep-bundle/platform/emf-semantic/emf-semantic-impl/src/main/resources/com/sirma/itt/emf/semantic/query/grammar/Sirma.g4 by ANTLR 4.1

package com.sirma.itt.emf.semantic.query.grammar;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SirmaParser}.
 */
public interface SirmaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SirmaParser#orClause}.
	 * @param ctx the parse tree
	 */
	void enterOrClause(@NotNull SirmaParser.OrClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#orClause}.
	 * @param ctx the parse tree
	 */
	void exitOrClause(@NotNull SirmaParser.OrClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#booleanClause}.
	 * @param ctx the parse tree
	 */
	void enterBooleanClause(@NotNull SirmaParser.BooleanClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#booleanClause}.
	 * @param ctx the parse tree
	 */
	void exitBooleanClause(@NotNull SirmaParser.BooleanClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#subStatement}.
	 * @param ctx the parse tree
	 */
	void enterSubStatement(@NotNull SirmaParser.SubStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#subStatement}.
	 * @param ctx the parse tree
	 */
	void exitSubStatement(@NotNull SirmaParser.SubStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#subject}.
	 * @param ctx the parse tree
	 */
	void enterSubject(@NotNull SirmaParser.SubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#subject}.
	 * @param ctx the parse tree
	 */
	void exitSubject(@NotNull SirmaParser.SubjectContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(@NotNull SirmaParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(@NotNull SirmaParser.QueryContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(@NotNull SirmaParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(@NotNull SirmaParser.RelationContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(@NotNull SirmaParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(@NotNull SirmaParser.ValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(@NotNull SirmaParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(@NotNull SirmaParser.ObjectContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#listOfValues}.
	 * @param ctx the parse tree
	 */
	void enterListOfValues(@NotNull SirmaParser.ListOfValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#listOfValues}.
	 * @param ctx the parse tree
	 */
	void exitListOfValues(@NotNull SirmaParser.ListOfValuesContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(@NotNull SirmaParser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(@NotNull SirmaParser.DateContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(@NotNull SirmaParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(@NotNull SirmaParser.OperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link SirmaParser#clause}.
	 * @param ctx the parse tree
	 */
	void enterClause(@NotNull SirmaParser.ClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SirmaParser#clause}.
	 * @param ctx the parse tree
	 */
	void exitClause(@NotNull SirmaParser.ClauseContext ctx);
}