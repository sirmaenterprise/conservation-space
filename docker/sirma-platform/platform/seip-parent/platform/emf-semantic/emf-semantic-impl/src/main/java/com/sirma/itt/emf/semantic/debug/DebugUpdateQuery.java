package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Wrapper class for update query for adding debug logic
 * 
 * @author kirq4e
 */
public class DebugUpdateQuery extends AbstractDebugOperation<Update> implements Update {

	/**
	 * Initializes the instance and delegate
	 * 
	 * @param update
	 *            the real update query instance
	 * @param updateQuery
	 *            String representation of the query, because it cannot be retrieved from the real query instance
	 */
	public DebugUpdateQuery(Update update, String updateQuery) {
		super(update, updateQuery);
	}


	@Override
	public void execute() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.UPDATE_QUERY_OPERATION, getQueryString(), getBindings());
		try {
			getDelegate().execute();
		} catch (RepositoryException | UpdateExecutionException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}
}
