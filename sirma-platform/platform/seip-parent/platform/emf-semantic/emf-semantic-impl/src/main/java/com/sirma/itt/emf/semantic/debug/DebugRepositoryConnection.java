package com.sirma.itt.emf.semantic.debug;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper;
import org.eclipse.rdf4j.rio.RDFHandler;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

/**
 * Wrapper class for RepositoryConnection to add debug logger
 *
 * @author kirq4e
 */
public class DebugRepositoryConnection extends RepositoryConnectionWrapper {

	private static final String TYPE_PARAMETER = "type";
	private static final String SET_NAMESPACE_OPERATION = "SET_NAMESPACE";
	private static final String REMOVE_NAMESPACE_OPERATION = "REMOVE_NAMESPACE";
	private static final String REMOVE_OPERATION = "REMOVE";
	private static final String ADD_OPERATION = "ADD";
	private static final String ASK_OPERATION = "ASK";
	private static final String EXPORT_OPERATION = "EXP";
	private List<Map<String, Object>> transactions;

	@Override
	public void begin() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.BEGIN_OPERATION, null,
				System.identityHashCode(getDelegate()));
		super.begin();
	}

	@Override
	public void begin(IsolationLevel level) {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.BEGIN_OPERATION, null,
				System.identityHashCode(getDelegate()));
		super.begin(level);
	}

	/**
	 * Initializes the instance and delegate connection
	 *
	 * @param repository
	 *            The repository
	 * @param connection
	 *            The connection
	 */
	public DebugRepositoryConnection(Repository repository, RepositoryConnection connection) {
		super(repository);
		transactions = new LinkedList<>();
		setDelegate(connection);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(String query) {
		return new DebugBooleanQuery(getDelegate().prepareBooleanQuery(query), query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query) {
		return new DebugBooleanQuery(getDelegate().prepareBooleanQuery(ql, query), query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI) {
		return new DebugBooleanQuery(getDelegate().prepareBooleanQuery(ql, query, baseURI), query);
	}

	@Override
	public TupleQuery prepareTupleQuery(String query) {
		return new DebugTupleQuery(getDelegate().prepareTupleQuery(query), query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query) {
		return new DebugTupleQuery(getDelegate().prepareTupleQuery(ql, query), query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI) {
		return new DebugTupleQuery(getDelegate().prepareTupleQuery(ql, query, baseURI), query);
	}

	@Override
	public Update prepareUpdate(String update) {
		return new DebugUpdateQuery(super.prepareUpdate(update), update);
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update) {
		return new DebugUpdateQuery(super.prepareUpdate(ql, update), update);
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI) {
		return new DebugUpdateQuery(super.prepareUpdate(ql, update, baseURI), update);
	}

	@Override
	public GraphQuery prepareGraphQuery(String query) {
		return new DebugGraphQuery(super.prepareGraphQuery(query), query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query) {
		return new DebugGraphQuery(super.prepareGraphQuery(ql, query), query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI) {
		return new DebugGraphQuery(super.prepareGraphQuery(ql, query, baseURI), query);
	}

	@Override
	public void commit() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.COMMIT_OPERATION, transactions,
				System.identityHashCode(getDelegate()));
		transactions = new LinkedList<>();
		try {
			super.commit();
		} catch (RepositoryException re) {
			onConnectionFail(re, getDelegate());
			throw re;
		}
	}

	private static boolean isConnectionProblemException(Throwable throwable) {
		return throwable instanceof NoHttpResponseException
				|| throwable instanceof NoRouteToHostException
				|| throwable instanceof HttpHostConnectException
				|| throwable instanceof UnknownHostException;
	}

	@Override
	public void rollback() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.ROLLBACK_OPERATION, transactions,
				System.identityHashCode(getDelegate()));
		transactions = new LinkedList<>();
		super.rollback();
	}

	@Override
	public void removeNamespace(String prefix) {
		Map<String, Object> operationParameters = new HashMap<>();
		operationParameters.put("prefix", prefix);
		operationParameters.put(TYPE_PARAMETER, REMOVE_NAMESPACE_OPERATION);
		transactions.add(operationParameters);

		super.removeNamespace(prefix);
	}

	@Override
	public void setNamespace(String prefix, String name) {
		Map<String, Object> operationParameters = new HashMap<>();
		operationParameters.put("prefix", prefix);
		operationParameters.put("name", name);
		operationParameters.put(TYPE_PARAMETER, SET_NAMESPACE_OPERATION);
		transactions.add(operationParameters);

		super.setNamespace(prefix, name);
	}

	@Override
	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts) {
		createOperation(ASK_OPERATION, st.getSubject(), st.getPredicate(), st.getObject(), contexts);
		return super.hasStatement(st, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Resource subj, IRI pred, Value obj, boolean includeInferred,
			Resource... contexts) {
		createOperation(ASK_OPERATION, subj, pred, obj, contexts);
		return super.hasStatement(subj, pred, obj, includeInferred, contexts);
	}

	@Override
	public void exportStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts) {
		createOperation(EXPORT_OPERATION, subj, pred, obj, contexts);
		super.exportStatements(subj, pred, obj, includeInferred, handler, contexts);
	}

	@Override
	public void export(RDFHandler handler, Resource... contexts) {
		createOperation(EXPORT_OPERATION, null, null, null, contexts);
		super.export(handler, contexts);
	}

	@Override
	public void add(Iterable<? extends Statement> statements, Resource... contexts) {
		for (Statement statement : statements) {
			createOperation(ADD_OPERATION, statement.getSubject(), statement.getPredicate(), statement.getObject(),
					contexts);
		}
		super.add(statements, contexts);
	}

	@Override
	public void add(Resource subject, IRI predicate, Value object, Resource... contexts) {
		createOperation(ADD_OPERATION, subject, predicate, object, contexts);
		super.add(subject, predicate, object, contexts);
	}

	@Override
	public void add(Statement st, Resource... contexts) {
		createOperation(ADD_OPERATION, st.getSubject(), st.getPredicate(), st.getObject(), contexts);
		super.add(st, contexts);
	}

	@Override
	public void remove(Iterable<? extends Statement> statements, Resource... contexts) {
		for (Statement statement : statements) {
			createOperation(REMOVE_OPERATION, statement.getSubject(), statement.getPredicate(), statement.getObject(),
					contexts);
		}
		super.remove(statements, contexts);
	}

	@Override
	public void remove(Resource subject, IRI predicate, Value object, Resource... contexts) {
		createOperation(REMOVE_OPERATION, subject, predicate, object, contexts);
		super.remove(subject, predicate, object, contexts);
	}

	@Override
	public void remove(Statement st, Resource... contexts) {
		createOperation(REMOVE_OPERATION, st.getSubject(), st.getPredicate(), st.getObject(), contexts);

		super.remove(st, contexts);
	}

	@Override
	public void clear(Resource... contexts) {
		createOperation(REMOVE_OPERATION, null, null, null, contexts);
		super.clear(contexts);
	}

	@Override
	public void close() {
		transactions = null;
		try {
			super.close();
		} catch (RepositoryException re) {
			if (isConnectionProblemException(re.getCause())) {
				onConnectionFail(re, getDelegate());
			}
			throw re;
		}
	}

	static void onRepositoryException(Throwable re) {
		if (isConnectionProblemException(re.getCause())) {
			onConnectionFail(re, null);
		}
	}

	private static void onConnectionFail(Throwable re, RepositoryConnection connection) {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.FAIL_OPERATION,
				re.getCause().getMessage(), System.identityHashCode(connection));
		SemanticOperationLogger.saveLog();
	}

	private void createOperation(String type, Resource subject, IRI predicate, Value object, Resource... contexts) {
		Map<String, Object> operationParameters = new HashMap<>();
		operationParameters.put("subject", subject);
		operationParameters.put("predicate", predicate);
		operationParameters.put("object", object);
		operationParameters.put("contexts", contexts);
		operationParameters.put(TYPE_PARAMETER, type);

		transactions.add(operationParameters);
	}
}
