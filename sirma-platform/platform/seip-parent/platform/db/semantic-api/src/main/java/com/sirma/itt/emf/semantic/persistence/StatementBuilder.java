package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;

/**
 * Defines a function for {@link Statement} building.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface StatementBuilder {

	/**
	 * Builds a statement based on the given arguments. The value is internally converted to {@link Literal} or to
	 * {@link IRI} based on it's value.
	 *
	 * @param subject
	 *            the subject of the statement
	 * @param predicate
	 *            the predicate of the statement
	 * @param value
	 *            the value of the statement
	 * @return the build statement or <code>null</code> if any of the arguments are null
	 */
	Statement build(Object subject, Object predicate, Serializable value);

	/**
	 * Builds statement that is intended for adding to the database.
	 *
	 * @param subject
	 *            the subject
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @return the local statement
	 */
	default LocalStatement buildAddStatement(Object subject, Object predicate, Serializable value) {
		return LocalStatement.toAdd(build(subject, predicate, value));
	}

	/**
	 * Builds statement that is intended for removing from the database
	 *
	 * @param subject
	 *            the subject
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 * @return the local statement
	 */
	default LocalStatement buildRemoveStatement(Object subject, Object predicate, Serializable value) {
		return LocalStatement.toRemove(build(subject, predicate, value));
	}
}
