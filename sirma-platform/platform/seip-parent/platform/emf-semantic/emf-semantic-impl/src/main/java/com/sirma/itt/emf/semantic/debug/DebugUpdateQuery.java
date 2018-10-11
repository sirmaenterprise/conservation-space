package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Wrapper class for update query for adding debug logic
 * 
 * @author kirq4e
 */
public class DebugUpdateQuery implements Update {

	private Update update;
	private String updateQuery;

	/**
	 * Initializes the instance and delegate
	 * 
	 * @param update
	 *            the real update query instance
	 * @param updateQuery
	 *            String representation of the query, because it cannot be retrieved from the real query instance
	 */
	public DebugUpdateQuery(Update update, String updateQuery) {
		this.update = update;
		this.updateQuery = updateQuery;
	}

	@Override
	public void setBinding(String name, Value value) {
		getUpdate().setBinding(name, value);
	}

	@Override
	public void removeBinding(String name) {
		getUpdate().removeBinding(name);
	}

	@Override
	public void clearBindings() {
		getUpdate().clearBindings();
	}

	@Override
	public BindingSet getBindings() {
		return getUpdate().getBindings();
	}

	@Override
	public void setDataset(Dataset dataset) {
		getUpdate().setDataset(dataset);
	}

	@Override
	public Dataset getDataset() {
		return getUpdate().getDataset();
	}

	@Override
	public void setIncludeInferred(boolean includeInferred) {
		getUpdate().setIncludeInferred(includeInferred);
	}

	@Override
	public boolean getIncludeInferred() {
		return getUpdate().getIncludeInferred();
	}

	@Override
	public void setMaxExecutionTime(int maxExecTime) {
		getUpdate().setMaxExecutionTime(maxExecTime);
	}

	@Override
	public int getMaxExecutionTime() {
		return getUpdate().getMaxExecutionTime();
	}

	@Override
	public void execute() throws UpdateExecutionException {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.UPDATE_QUERY_OPERATION, updateQuery, getBindings());
		getUpdate().execute();
	}

	/**
	 * @return the update
	 */
	public Update getUpdate() {
		return update;
	}

}
