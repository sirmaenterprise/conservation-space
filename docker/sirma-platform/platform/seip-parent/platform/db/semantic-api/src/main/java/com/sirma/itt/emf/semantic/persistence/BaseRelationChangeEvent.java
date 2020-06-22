package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyEvent;

/**
 * Low level event fired before adding/removing relation statement. The event observer could provide additional
 * statements to be added/removed to the database if needed.
 *
 * @author BBonev
 */
public abstract class BaseRelationChangeEvent implements ObjectPropertyEvent {

	private final LocalStatement statement;
	private final Function<IRI, String> shortUriConverter;

	private List<LocalStatement> statements;

	/**
	 * Instantiates a new base relation change event.
	 *
	 * @param statementTrigger
	 *            the statement trigger
	 * @param shortUriConverter
	 *            the short uri converter will be used to convert statement URIs to short uri format, expected by the
	 *            methods of {@link ObjectPropertyEvent}
	 */
	public BaseRelationChangeEvent(LocalStatement statementTrigger, Function<IRI, String> shortUriConverter) {
		statement = statementTrigger;
		this.shortUriConverter = shortUriConverter;
	}

	/**
	 * Gets the statement that triggered the relation change
	 *
	 * @return the statement
	 */
	public LocalStatement getStatement() {
		return statement;
	}

	@Override
	public Serializable getSourceId() {
		return shortUriConverter.apply((IRI) statement.getStatement().getSubject());
	}

	@Override
	public String getObjectPropertyName() {
		return shortUriConverter.apply(statement.getStatement().getPredicate());
	}

	@Override
	public Serializable getTargetId() {
		return shortUriConverter.apply((IRI) statement.getStatement().getObject());
	}

	/**
	 * Adds the given {@link LocalStatement} to the event result if non <code>null</code>
	 *
	 * @param newStatement
	 *            the new statement
	 */
	public void add(LocalStatement newStatement) {
		if (newStatement != null) {
			getNonNullStatements().add(newStatement);
		}
	}

	/**
	 * Adds a persist statement to the event result
	 *
	 * @param statementToPersist
	 *            the statement to persist
	 */
	public void addPersistStatement(Statement statementToPersist) {
		if (statementToPersist != null) {
			add(LocalStatement.toAdd(statementToPersist));
		}
	}

	/**
	 * Adds a remove statement to the event result
	 *
	 * @param statementToRemove
	 *            the statement to remove
	 */
	public void addRemoveStatement(Statement statementToRemove) {
		if (statementToRemove != null) {
			add(LocalStatement.toRemove(statementToRemove));
		}
	}

	private List<LocalStatement> getNonNullStatements() {
		if (statements == null) {
			statements = new LinkedList<>();
		}
		return statements;
	}

	/**
	 * Gets a stream of all added statements to the event. Note that trigger statement will not be present in the
	 * returned stream. If no statements are added the method should return empty stream.
	 *
	 * @return the added statements stream
	 */
	public Stream<LocalStatement> getStatements() {
		if (statements == null) {
			return Stream.empty();
		}
		return statements.stream();
	}
}
