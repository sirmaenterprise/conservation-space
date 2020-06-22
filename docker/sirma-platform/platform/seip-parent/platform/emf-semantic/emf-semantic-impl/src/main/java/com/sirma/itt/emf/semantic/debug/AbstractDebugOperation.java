package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.Operation;

/**
 * Base query proxy for implementing other debug query implementations.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2019
 */
public abstract class AbstractDebugOperation<Q extends Operation> implements Operation {
	private final Q delegate;
	private final String query;

	public AbstractDebugOperation(Q delegate, String query) {
		this.delegate = delegate;
		this.query = query;
	}

	@Override
	public void setBinding(String name, Value value) {
		getDelegate().setBinding(name, value);
	}

	@Override
	public void removeBinding(String name) {
		getDelegate().removeBinding(name);
	}

	@Override
	public void clearBindings() {
		getDelegate().clearBindings();
	}

	@Override
	public BindingSet getBindings() {
		return getDelegate().getBindings();
	}

	@Override
	public void setDataset(Dataset dataset) {
		getDelegate().setDataset(dataset);
	}

	@Override
	public Dataset getDataset() {
		return getDelegate().getDataset();
	}

	@Override
	public void setIncludeInferred(boolean includeInferred) {
		getDelegate().setIncludeInferred(includeInferred);
	}

	@Override
	public boolean getIncludeInferred() {
		return getDelegate().getIncludeInferred();
	}

	@Override
	public void setMaxExecutionTime(int maxExecTime) {
		getDelegate().setMaxExecutionTime(maxExecTime);
	}

	@Override
	public int getMaxExecutionTime() {
		return getDelegate().getMaxExecutionTime();
	}

	public Q getDelegate() {
		return delegate;
	}
	
	public String getQueryString() {
		return query;
	}
}
