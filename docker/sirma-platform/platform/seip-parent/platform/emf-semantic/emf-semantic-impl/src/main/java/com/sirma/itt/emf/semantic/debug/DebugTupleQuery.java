package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Wrapper class for tuple query to add debug logic
 * 
 * @author kirq4e
 */
public class DebugTupleQuery extends AbstractDebugQuery<TupleQuery> implements TupleQuery {

	/**
	 * Initializes the instance and delegate
	 * 
	 * @param query
	 *            The real query instance
	 * @param queryString
	 *            The String representation of the query, because it cannot be retrieved from the query instance
	 */
	public DebugTupleQuery(TupleQuery query, String queryString) {
		super(query, queryString);
	}

	@Override
	public TupleQueryResult evaluate() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.TUPLE_QUERY_OPERATION, getQueryString(),
				getBindings());
		try {
			return getDelegate().evaluate();
		} catch (RepositoryException | QueryEvaluationException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}

	@Override
	public void evaluate(TupleQueryResultHandler handler) {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.TUPLE_QUERY_OPERATION, getQueryString(),
				getBindings());
		try {
			getDelegate().evaluate(handler);
		} catch (RepositoryException | QueryEvaluationException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}
}
