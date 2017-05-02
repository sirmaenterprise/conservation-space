package com.sirma.itt.emf.semantic.persistence;

/**
 * Provider that produces different type of {@link StatementBuilder}s
 *
 * @author BBonev
 */
public interface StatementBuilderProvider {

	/**
	 * Literal statement builder that produces statements for literal values
	 *
	 * @return the statement builder
	 */
	StatementBuilder literalStatementBuilder();

	/**
	 * Relation statement builder that produces statements for non literal values
	 *
	 * @return the statement builder
	 */
	StatementBuilder relationStatementBuilder();
}
