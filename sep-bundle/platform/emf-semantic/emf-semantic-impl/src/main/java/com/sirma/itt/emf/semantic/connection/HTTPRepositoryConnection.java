/*
 * Licensed to Aduna under one or more contributor license agreements. See the NOTICE.txt file
 * distributed with this work for additional information regarding copyright ownership. Aduna
 * licenses this file to you under the terms of the Aduna BSD License (the "License"); you may not
 * use this file except in compliance with the License. See the LICENSE.txt file distributed with
 * this work for the full License. Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.sirma.itt.emf.semantic.connection;

import info.aduna.iteration.CloseableIteratorIteration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.http.client.SesameHTTPClient;
import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SPARQLUpdateOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

// TODO: Auto-generated Javadoc
/**
 * RepositoryConnection that communicates with a server using the HTTP protocol. Methods in this
 * class may throw the specific RepositoryException subclasses UnautorizedException and
 * NotAllowedException, the semantics of which are defined by the HTTP protocol.
 *
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @see org.openrdf.http.protocol.NotAllowedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 */
public class HTTPRepositoryConnection extends RepositoryConnectionBase {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(HTTPRepositoryConnection.class);

	/*
	 * Note: the following debugEnabled method are private so that they can be removed when open
	 * connections no longer block other connections and they can be closed silently (just like in
	 * JDBC).
	 */
	/**
	 * Debug enabled.
	 *
	 * @return true, if successful
	 */
	private static boolean debugEnabled() {
		try {
			return System.getProperty("org.openrdf.repository.debug") != null;
		} catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example
			// when running in applets
			return false;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/** The txn. */
	private final List<TransactionOperation> txn = Collections
			.synchronizedList(new ArrayList<TransactionOperation>());

	/** The active. */
	private boolean active;

	/*
	 * Stores a stack trace that indicates where this connection as created if debugging is enabled.
	 */
	/** The creator trace. */
	private Throwable creatorTrace;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Instantiates a new hTTP repository connection.
	 *
	 * @param httpRepository the http repository
	 */
	public HTTPRepositoryConnection(HTTPRepository httpRepository) {
		super(httpRepository);

		// parser used for locally processing input data to be sent to the server should be strict,
		// and should preserve bnode ids.
		setParserConfig(new ParserConfig());
		getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		if (debugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HTTPRepository getRepository() {
		return (HTTPRepository) super.getRepository();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			if (isOpen()) {
				if (creatorTrace != null) {
					logger.warn(
							"Closing connection due to garbage collection, connection was create in:",
							creatorTrace);
				}
				close();
			}
		} finally {
			super.finalize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void begin() throws RepositoryException {
		verifyIsOpen();
		verifyNotTxnActive("Connection already has an active transaction");

		active = true;
	}

	/**
	 * Prepares a {@Link Query} for evaluation on this repository. Note that the preferred
	 * way of preparing queries is to use the more specific
	 *
	 * @param ql the ql
	 * @param queryString the query string
	 * @param baseURI the base uri
	 * @return the query
	 * {@link #prepareTupleQuery(QueryLanguage, String, String)},
	 * {@link #prepareBooleanQuery(QueryLanguage, String, String)}, or
	 * {@link #prepareGraphQuery(QueryLanguage, String, String)} methods instead.
	 */
	@Override
	public Query prepareQuery(QueryLanguage ql, String queryString, String baseURI) {
		if (QueryLanguage.SPARQL.equals(ql)) {
			String strippedQuery = QueryParserUtil.removeSPARQLQueryProlog(queryString)
					.toUpperCase();
			if (strippedQuery.startsWith("SELECT")) {
				return prepareTupleQuery(ql, queryString, baseURI);
			} else if (strippedQuery.startsWith("ASK")) {
				return prepareBooleanQuery(ql, queryString, baseURI);
			} else {
				return prepareGraphQuery(ql, queryString, baseURI);
			}
		} else if (QueryLanguage.SERQL.equals(ql)) {
			String strippedQuery = queryString;

			// remove all opening brackets
			strippedQuery = strippedQuery.replace('(', ' ');
			strippedQuery = strippedQuery.trim();

			if (strippedQuery.toUpperCase().startsWith("SELECT")) {
				return prepareTupleQuery(ql, queryString, baseURI);
			} else {
				return prepareGraphQuery(ql, queryString, baseURI);
			}
		} else {
			throw new UnsupportedOperationException("Operation not supported for query language "
					+ ql);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPTupleQuery(this, ql, queryString, baseURI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPGraphQuery(this, ql, queryString, baseURI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI) {
		return new HTTPBooleanQuery(this, ql, queryString, baseURI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
		try {
			List<Resource> contextList = new ArrayList<Resource>();

			TupleQueryResult contextIDs = getRepository().getHTTPClient().getContextIDs();
			try {
				while (contextIDs.hasNext()) {
					BindingSet bindingSet = contextIDs.next();
					Value context = bindingSet.getValue("contextID");

					if (context instanceof Resource) {
						contextList.add((Resource) context);
					}
				}
			} finally {
				contextIDs.close();
			}

			return createRepositoryResult(contextList);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts) throws RepositoryException {
		try {
			StatementCollector collector = new StatementCollector();
			exportStatements(subj, pred, obj, includeInferred, collector, contexts);
			return createRepositoryResult(collector.getStatements());
		} catch (RDFHandlerException e) {
			// found a bug in StatementCollector?
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts) throws RDFHandlerException,
			RepositoryException {
		try {
			getRepository().getHTTPClient().getStatements(subj, pred, obj, includeInferred,
					handler, contexts);
		} catch (IOException e) {
			throw new RepositoryException(e);
		} catch (QueryInterruptedException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size(Resource... contexts) throws RepositoryException {
		try {
			return getRepository().getHTTPClient().size(contexts);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() throws RepositoryException {
		synchronized (txn) {
			if (txn.size() > 0) {
				try {
					getRepository().getHTTPClient().sendTransaction(txn);
					logTransactionOperations();
					txn.clear();
				} catch (IOException e) {
					throw new RepositoryException(e);
				}
			}
			active = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() {
		txn.clear();
		active = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws RepositoryException {
		if (txn.size() > 0) {
			logger.warn("Rolling back transaction due to connection close", new Throwable());
			rollback();
		}

		super.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}
		if (dataFormat == null) {
			dataFormat = Rio.getParserFormatForFileName(file.getName());
		}

		InputStream in = new FileInputStream(file);
		try {
			add(in, baseURI, dataFormat, contexts);
		} finally {
			in.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		URLConnection con = url.openConnection();

		// Set appropriate Accept headers
		if (dataFormat != null) {
			for (String mimeType : dataFormat.getMIMETypes()) {
				con.addRequestProperty("Accept", mimeType);
			}
		} else {
			Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
			List<String> acceptParams = RDFFormat.getAcceptParams(rdfFormats, true, null);
			for (String acceptParam : acceptParams) {
				con.addRequestProperty("Accept", acceptParam);
			}
		}

		InputStream in = con.getInputStream();

		if (dataFormat == null) {
			// Try to determine the data's MIME type
			String mimeType = con.getContentType();
			int semiColonIdx = mimeType.indexOf(';');
			if (semiColonIdx >= 0) {
				mimeType = mimeType.substring(0, semiColonIdx);
			}
			dataFormat = Rio.getParserFormatForMIMEType(mimeType);

			// Fall back to using file name extensions
			if (dataFormat == null) {
				dataFormat = Rio.getParserFormatForFileName(url.getPath());
			}
		}

		try {
			add(in, baseURI, dataFormat, contexts);
		} finally {
			in.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		// FIXME (J1) in the new setup, isActive is always true when you get to
		// this point. We need transactional support
		// in the httpclient itself (and thus in the protocol).
		if (!isActive()) {
			// Send bytes directly to the server
			SesameHTTPClient httpClient = getRepository().getHTTPClient();
			httpClient.upload(in, baseURI, dataFormat, false, contexts);
		} else {
			// Parse files locally
			super.add(in, baseURI, dataFormat, contexts);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		// FIXME (J1) in the new setup, isActive is always true when you get to
		// this point. We need transactional support
		// in the httpclient itself (and thus in the protocol).
		if (!isActive()) {
			// Send bytes directly to the server
			SesameHTTPClient httpClient = getRepository().getHTTPClient();
			httpClient.upload(reader, baseURI, dataFormat, false, contexts);
		} else {
			// Parse files locally
			super.add(reader, baseURI, dataFormat, contexts);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts) throws RepositoryException {
		txn.add(new AddStatementOperation(subject, predicate, object, contexts));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object,
			Resource... contexts) throws RepositoryException {
		txn.add(new RemoveStatementsOperation(subject, predicate, object, contexts));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(Resource... contexts) throws RepositoryException {
		boolean localTransaction = startLocalTransaction();

		txn.add(new ClearOperation(contexts));

		conditionalCommit(localTransaction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNamespace(String prefix) throws RepositoryException {
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}

		boolean localTransaction = startLocalTransaction();

		txn.add(new RemoveNamespaceOperation(prefix));

		conditionalCommit(localTransaction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearNamespaces() throws RepositoryException {
		boolean localTransaction = startLocalTransaction();
		txn.add(new ClearNamespacesOperation());
		conditionalCommit(localTransaction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNamespace(String prefix, String name) throws RepositoryException {
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		if (name == null) {
			throw new NullPointerException("name must not be null");
		}

		boolean localTransaction = startLocalTransaction();
		txn.add(new SetNamespaceOperation(prefix, name));
		conditionalCommit(localTransaction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
		try {
			List<Namespace> namespaceList = new ArrayList<Namespace>();

			TupleQueryResult namespaces = getRepository().getHTTPClient().getNamespaces();
			try {
				while (namespaces.hasNext()) {
					BindingSet bindingSet = namespaces.next();
					Value prefix = bindingSet.getValue("prefix");
					Value namespace = bindingSet.getValue("namespace");

					if (prefix instanceof Literal && namespace instanceof Literal) {
						String prefixStr = ((Literal) prefix).getLabel();
						String namespaceStr = ((Literal) namespace).getLabel();
						namespaceList.add(new NamespaceImpl(prefixStr, namespaceStr));
					}
				}
			} finally {
				namespaces.close();
			}

			return createRepositoryResult(namespaceList);
		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespace(String prefix) throws RepositoryException {
		if (prefix == null) {
			throw new NullPointerException("prefix must not be null");
		}
		try {
			return getRepository().getHTTPClient().getNamespace(prefix);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Creates a RepositoryResult for the supplied element set.
	 *
	 * @param <E> the element type
	 * @param elements the elements
	 * @return the repository result
	 */
	protected <E> RepositoryResult<E> createRepositoryResult(Iterable<? extends E> elements) {
		return new RepositoryResult<E>(new CloseableIteratorIteration<E, RepositoryException>(
				elements.iterator()));
	}

	/**
	 * Schedule update.
	 *
	 * @param update the update
	 */
	protected void scheduleUpdate(HTTPUpdate update) {
		SPARQLUpdateOperation op = new SPARQLUpdateOperation();
		op.setUpdateString(update.getQueryString());
		op.setBaseURI(update.getBaseURI());
		op.setBindings(update.getBindingsArray());
		op.setIncludeInferred(update.getIncludeInferred());
		op.setDataset(update.getDataset());
		txn.add(op);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
			throws RepositoryException, MalformedQueryException {
		return new HTTPUpdate(this, ql, update, baseURI);
	}

	/**
	 * Verifies that the connection is open, throws a {@link StoreException} if it isn't.
	 *
	 * @throws RepositoryException the repository exception
	 */
	protected void verifyIsOpen() throws RepositoryException {
		if (!isOpen()) {
			throw new RepositoryException("Connection has been closed");
		}
	}

	/**
	 * Verifies that the connection has an active transaction, throws a {@link StoreException} if it
	 * hasn't.
	 *
	 * @throws RepositoryException the repository exception
	 */
	protected void verifyTxnActive() throws RepositoryException {
		if (!isActive()) {
			throw new RepositoryException("Connection does not have an active transaction");
		}
	}

	/**
	 * Verifies that the connection does not have an active transaction, throws a.
	 *
	 * @param msg the msg
	 * @throws RepositoryException the repository exception
	 * {@link RepositoryException} if it has.
	 */
	protected void verifyNotTxnActive(String msg) throws RepositoryException {
		if (isActive()) {
			throw new RepositoryException(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() throws UnknownTransactionStateException, RepositoryException {
		return active;
	}

	/**
	 * Log transaction operations.
	 *
	 * @return the map
	 */
	public Map<String, Long> logTransactionOperations() {
		HashMap<String, Long> operations = new HashMap<>();

		for (TransactionOperation operation : txn) {
			Long operationCount = operations.get(operation.getClass().getSimpleName());
			if (operationCount == null) {
				operationCount = 0l;
			}
			operationCount = operationCount + 1;
			operations.put(operation.getClass().getSimpleName(), operationCount);
		}

		LOGGER.trace(operations);

		return operations;
	}
}
