package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Wrapper class for tuple query to add debug logic
 * 
 * @author kirq4e
 */
public class DebugTupleQuery implements TupleQuery {

	private TupleQuery query;
	private String queryString;

	/**
	 * Initializes the instance and delegate
	 * 
	 * @param query
	 *            The real query instance
	 * @param queryString
	 *            The String representation of the query, because it cannot be retrieved from the query instance
	 */
	public DebugTupleQuery(TupleQuery query, String queryString) {
		this.query = query;
		this.queryString = queryString;
	}

	@Override
	public void setMaxQueryTime(int maxQueryTime) {
		getQuery().setMaxQueryTime(maxQueryTime);
	}

	@Override
	public int getMaxQueryTime() {
		return getQuery().getMaxQueryTime();
	}

	@Override
	public void setBinding(String name, Value value) {
		getQuery().setBinding(name, value);
	}

	@Override
	public void removeBinding(String name) {
		getQuery().removeBinding(name);
	}

	@Override
	public void clearBindings() {
		getQuery().clearBindings();
	}

	@Override
	public BindingSet getBindings() {
		return getQuery().getBindings();
	}

	@Override
	public void setDataset(Dataset dataset) {
		getQuery().setDataset(dataset);
	}

	@Override
	public Dataset getDataset() {
		return getQuery().getDataset();
	}

	@Override
	public void setIncludeInferred(boolean includeInferred) {
		getQuery().setIncludeInferred(includeInferred);
	}

	@Override
	public boolean getIncludeInferred() {
		return getQuery().getIncludeInferred();
	}

	@Override
	public void setMaxExecutionTime(int maxExecTime) {
		getQuery().setMaxExecutionTime(maxExecTime);
	}

	@Override
	public int getMaxExecutionTime() {
		return getQuery().getMaxExecutionTime();
	}

	@Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.TUPLE_QUERY_OPERATION, queryString,
				getBindings());
		return getQuery().evaluate();
	}

	@Override
	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.TUPLE_QUERY_OPERATION, queryString,
				getBindings());
		getQuery().evaluate(handler);
	}

	/**
	 * @return the query
	 */
	public TupleQuery getQuery() {
		return query;
	}

}
