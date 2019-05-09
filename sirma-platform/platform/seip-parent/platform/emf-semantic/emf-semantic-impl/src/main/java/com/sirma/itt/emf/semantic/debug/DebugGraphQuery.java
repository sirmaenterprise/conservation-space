package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandler;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Graph query proxy to track debug info.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2019
 */
public class DebugGraphQuery extends AbstractDebugQuery<GraphQuery> implements GraphQuery {

	public DebugGraphQuery(GraphQuery delegate, String query) {
		super(delegate, query);
	}

	@Override
	public GraphQueryResult evaluate() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.GRAPH_QUERY_OPERATION, getQueryString(), getBindings());
		try {
			return getDelegate().evaluate();
		} catch (RepositoryException | QueryEvaluationException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}

	@Override
	public void evaluate(RDFHandler handler) {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.GRAPH_QUERY_OPERATION, getQueryString(), getBindings());
		try {
			getDelegate().evaluate(handler);
		} catch (RepositoryException | QueryEvaluationException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}
}
