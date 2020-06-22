package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Boolean query proxy to track debug info.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2019
 */
public class DebugBooleanQuery extends AbstractDebugQuery<BooleanQuery> implements BooleanQuery {

	public DebugBooleanQuery(BooleanQuery delegate, String query) {
		super(delegate, query);
	}

	@Override
	public boolean evaluate() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.BOOLEAN_QUERY_OPERATION, getQueryString(), getBindings());
		try {
			return getDelegate().evaluate();
		} catch (RepositoryException re) {
			DebugRepositoryConnection.onRepositoryException(re);
			throw re;
		}
	}
}
