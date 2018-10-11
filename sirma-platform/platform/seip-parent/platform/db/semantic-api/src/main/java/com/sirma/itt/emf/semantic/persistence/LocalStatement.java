package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Objects;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

/**
 * {@link Statement} wrapper that carry information about the purpose of the original statement if it's for persist or
 * for delete of single piece of information <br>
 * Concrete purpose instances should be created using the factory methods {@link #toAdd(Statement)} and
 * {@link #toRemove(Statement)}. <br>
 * This wrapper object is used to that stream lined logic could be implemented for database persist
 *
 * @author BBonev
 */
public class LocalStatement {
	private final Statement statement;
	private final boolean toAdd;

	/**
	 * Instantiates a new local statement.
	 *
	 * @param statement
	 *            the statement
	 * @param toAdd
	 *            <code>true</code> indicates that the statement is for addition and <code>false</code> for delete
	 */
	LocalStatement(Statement statement, boolean toAdd) {
		this.statement = statement;
		this.toAdd = toAdd;
	}

	/**
	 * Copy constructor
	 *
	 * @param localStatement
	 *            the local statement
	 */
	public LocalStatement(LocalStatement localStatement) {
		Objects.requireNonNull(localStatement);
		statement = localStatement.statement;
		toAdd = localStatement.toAdd;
	}

	/**
	 * Creates {@link LocalStatement} that will trigger an add operation to the persistence store
	 *
	 * @param statement
	 *            the statement to persist
	 * @return the local statement
	 */
	public static LocalStatement toAdd(Statement statement) {
		return new LocalStatement(statement, true);
	}

	/**
	 * Creates {@link LocalStatement} that will trigger a delete operation to the persistence store
	 *
	 * @param statement
	 *            the statement to delete
	 * @return the local statement
	 */
	public static LocalStatement toRemove(Statement statement) {
		return new LocalStatement(statement, false);
	}

	/**
	 * Gets the actual statement.
	 *
	 * @return the statement
	 */
	public Statement getStatement() {
		return statement;
	}

	/**
	 * If the current statement is for persist this method should return <code>true</code>.
	 *
	 * @return <code>true</code>, if is to add and <code>false</code> if it's for delete
	 */
	public boolean isToAdd() {
		return toAdd;
	}

	/**
	 * Adds the current statement to one of the two models depending on it's purpose.
	 *
	 * @param persistModel
	 *            the persist model for statements that should be persisted
	 * @param deleteModel
	 *            the delete model for statements that should be deleted
	 * @return <code>true</code>, if the current statement was added to one of the two models. <code>false</code> could
	 *         be returned if the current statement is <code>null</code> or the target model already contains the
	 *         current statement
	 */
	public boolean addTo(Model persistModel, Model deleteModel) {
		if (statement == null) {
			return false;
		}
		if (toAdd) {
			return persistModel.add(statement);
		}
		return deleteModel.add(statement);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (statement == null ? 0 : statement.hashCode());
		result = prime * result + (toAdd ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LocalStatement)) {
			return false;
		}
		LocalStatement other = (LocalStatement) obj;
		return nullSafeEquals(statement, other.statement) && toAdd == other.toAdd;
	}

	/**
	 * Checks if the current local statement contains a {@link Statement}.
	 *
	 * @return true, if the method {@link #getStatement()} will return non <code>null</code> value
	 */
	public boolean hasStatement() {
		return statement != null;
	}

	/**
	 * Checks if the current and the given statements represents the same statement ignoring the {@link #isToAdd()}
	 * flag.
	 *
	 * @param other
	 *            the other statement to check
	 * @return <code>true</code>, if the given statement is non <code>null</code> and it's equal to the other statement
	 */
	public boolean isSame(LocalStatement other) {
		if (other != null) {
			return nullSafeEquals(statement, other.getStatement());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		if (toAdd) {
			builder.append("+");
		} else {
			builder.append("-");
		}
		builder.append(statement);
		return builder.toString();
	}
}