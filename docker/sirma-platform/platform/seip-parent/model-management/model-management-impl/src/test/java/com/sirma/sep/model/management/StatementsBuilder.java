package com.sirma.sep.model.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * Helper class for building different {@link Statement} for add or remove.
 *
 * @param <T> type of the statement builder, used for chaining methods
 */
public class StatementsBuilder<T extends StatementsBuilder> {
	private final Consumer<Statement> statementConsumer;
	private final Consumer<Statement> statementRemover;
	private final Resource subject;
	private final ValueFactory valueFactory;
	private final Resource context;

	StatementsBuilder(Resource subject, ValueFactory valueFactory, Consumer<Statement> statementConsumer,
			Consumer<Statement> statementRemover) {
		this.subject = subject;
		this.valueFactory = valueFactory;
		this.statementConsumer = statementConsumer;
		this.statementRemover = statementRemover;
		this.context = null;
	}

	public Resource getSubject() {
		return subject;
	}

	T withLiteral(IRI predicate, String value, String lang) {
		add(buildLiteral(subject, predicate, value, lang));
		return (T) this;
	}

	T withLiteral(IRI predicate, String value) {
		add(buildLiteral(subject, predicate, value));
		return (T) this;
	}

	T withLiteral(IRI predicate, boolean value) {
		add(buildLiteral(subject, predicate, value));
		return (T) this;
	}

	T withObject(IRI predicate, String value) {
		add(buildObject(subject, predicate, value));
		return (T) this;
	}

	T withObject(IRI predicate, IRI value) {
		add(buildObject(subject, predicate, value));
		return (T) this;
	}

	public T withoutLiteral(IRI iri, String value) {
		remove(buildLiteral(getSubject(), iri, value));
		return (T) this;
	}

	public T withoutLiteral(IRI iri, String label, String language) {
		remove(buildLiteral(getSubject(), iri, label, language));
		return (T) this;
	}

	public T withoutObject(IRI iri, String value) {
		remove(buildObject(getSubject(), iri, value));
		return (T) this;
	}

	protected Statement buildLiteral(Resource subject, IRI predicate, String value) {
		return valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(value), context);
	}

	protected Statement buildLiteral(Resource subject, IRI predicate, boolean value) {
		return valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(value), context);
	}

	protected Statement buildLiteral(Resource subject, IRI predicate, String label, String lang) {
		return valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(label, lang), context);
	}

	protected Statement buildObject(Resource subject, IRI predicate, String value) {
		return valueFactory.createStatement(subject, predicate, valueFactory.createIRI(value), context);
	}

	protected Statement buildObject(Resource subject, IRI predicate, IRI value) {
		return valueFactory.createStatement(subject, predicate, value, context);
	}

	private void add(Statement statement) {
		statementConsumer.accept(statement);
	}

	private void remove(Statement statement) {
		statementRemover.accept(statement);
	}

	/**
	 * Statement builder for adding/removing statements in provided {@link RepositoryConnection}
	 */
	public static class DatabaseBuilder extends StatementsBuilder<DatabaseBuilder> {
		public DatabaseBuilder(Resource subject, RepositoryConnection connection) {
			super(subject, connection.getValueFactory(), connection::add, connection::remove);
		}
	}

	/**
	 * Statement builder for validating added/removed statements in provided {@link RepositoryConnection}
	 */
	public static class DatabaseValidator extends StatementsBuilder<DatabaseValidator> {
		private final RepositoryConnection connection;
		private final List<Statement> statements;
		private final List<Statement> missingStatements;

		public DatabaseValidator(Resource subject, RepositoryConnection connection) {
			this(subject, connection, new LinkedList<>(), new LinkedList<>());
		}

		public DatabaseValidator(Resource subject, RepositoryConnection connection, List<Statement> statements,
				List<Statement> removedStatements) {
			super(subject, connection.getValueFactory(), statements::add, removedStatements::add);
			this.connection = connection;
			this.statements = statements;
			missingStatements = new LinkedList<>();
		}

		public void validateState() {
			for (Statement statement : statements) {
				RepositoryResult<Statement> result = connection.getStatements(statement.getSubject(),
						statement.getPredicate(), statement.getObject());
				assertTrue("Should have statement " + statement, result.hasNext());
				assertEquals(statement, result.next());
				assertFalse("Didn't expect more statements for " + statement, result.hasNext());
			}

			for (Statement statement : missingStatements) {
				RepositoryResult<Statement> result = connection.getStatements(statement.getSubject(),
						statement.getPredicate(), statement.getObject());
				assertFalse("Should have no statement " + statement, result.hasNext());
			}
		}
	}

}