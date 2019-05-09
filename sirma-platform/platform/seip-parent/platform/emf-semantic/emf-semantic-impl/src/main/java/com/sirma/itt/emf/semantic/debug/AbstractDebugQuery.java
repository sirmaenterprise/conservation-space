package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.query.Query;

/**
 * Base query proxy for implementing other debug query implementations.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2019
 */
public abstract class AbstractDebugQuery<Q extends Query> extends AbstractDebugOperation<Q> implements Query {

	public AbstractDebugQuery(Q delegate, String query) {
		super(delegate, query);
	}

	@Override
	public void setMaxQueryTime(int maxQueryTime) {
		getDelegate().setMaxExecutionTime(maxQueryTime);
	}

	@Override
	public int getMaxQueryTime() {
		return getDelegate().getMaxExecutionTime();
	}
}
