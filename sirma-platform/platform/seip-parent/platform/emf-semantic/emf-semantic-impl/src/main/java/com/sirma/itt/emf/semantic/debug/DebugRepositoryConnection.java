package com.sirma.itt.emf.semantic.debug;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper;

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
	private List<Map<String, Object>> transactions;

	@Override
	public void begin() {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.BEGIN_OPERATION, transactions,
				System.identityHashCode(getDelegate()));
		super.begin();
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
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
			throws MalformedQueryException, RepositoryException {
		return new DebugTupleQuery(getDelegate().prepareTupleQuery(ql, query, baseURI), query);
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
			throws MalformedQueryException, RepositoryException {
		return new DebugUpdateQuery(super.prepareUpdate(ql, update, baseURI), update);
	}

	@Override
	public void commit() throws RepositoryException {
		SemanticOperationLogger.addLogOperation(SemanticOperationLogger.COMMIT_OPERATION, transactions,
				System.identityHashCode(getDelegate()));

		transactions = new LinkedList<>();
		super.commit();
	}

	@Override
	public void removeNamespace(String prefix) throws RepositoryException {
		Map<String, Object> operationParameters = new HashMap<>();
		operationParameters.put("prefix", prefix);
		operationParameters.put(TYPE_PARAMETER, REMOVE_NAMESPACE_OPERATION);
		transactions.add(operationParameters);

		super.removeNamespace(prefix);
	}

	@Override
	public void setNamespace(String prefix, String name) throws RepositoryException {
		Map<String, Object> operationParameters = new HashMap<>();
		operationParameters.put("prefix", prefix);
		operationParameters.put("name", name);
		operationParameters.put(TYPE_PARAMETER, SET_NAMESPACE_OPERATION);
		transactions.add(operationParameters);

		super.setNamespace(prefix, name);
	}

	@Override
	public void add(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
		for (Statement statement : statements) {
			createOperation(ADD_OPERATION, statement.getSubject(), statement.getPredicate(), statement.getObject(),
					contexts);
		}
		super.add(statements, contexts);
	}

	@Override
	public void add(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
		createOperation(ADD_OPERATION, subject, predicate, object, contexts);
		super.add(subject, predicate, object, contexts);
	}

	@Override
	public void add(Statement st, Resource... contexts) throws RepositoryException {
		createOperation(ADD_OPERATION, st.getSubject(), st.getPredicate(), st.getObject(), contexts);
		super.add(st, contexts);
	}

	@Override
	public void remove(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
		for (Statement statement : statements) {
			createOperation(REMOVE_OPERATION, statement.getSubject(), statement.getPredicate(), statement.getObject(),
					contexts);
		}
		super.remove(statements, contexts);
	}

	@Override
	public void remove(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
		createOperation(REMOVE_OPERATION, subject, predicate, object, contexts);
		super.remove(subject, predicate, object, contexts);
	}

	@Override
	public void remove(Statement st, Resource... contexts) throws RepositoryException {
		createOperation(REMOVE_OPERATION, st.getSubject(), st.getPredicate(), st.getObject(), contexts);

		super.remove(st, contexts);
	}

	@Override
	public void close() throws RepositoryException {
		transactions = null;
		super.close();
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
