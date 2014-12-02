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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.client.query.AbstractHTTPQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;

// TODO: Auto-generated Javadoc
/**
 * TupleQuery specific to the HTTP protocol. Methods in this class may throw the specific
 * RepositoryException subclass UnautorizedException, the semantics of which is defined by the HTTP
 * protocol.
 *
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 * @author Herko ter Horst
 */
public class HTTPTupleQuery extends AbstractHTTPQuery implements TupleQuery {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(HTTPTupleQuery.class);

	/**
	 * Instantiates a new hTTP tuple query.
	 *
	 * @param con
	 *            the con
	 * @param ql
	 *            the ql
	 * @param queryString
	 *            the query string
	 * @param baseURI
	 *            the base uri
	 */
	public HTTPTupleQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString,
			String baseURI) {
		super(con.getRepository().getHTTPClient(), ql, queryString, baseURI);
	}

	// TODO maybe even make this shared code for SPARQL and REMOTE repository
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		HTTPClient client = getHttpClient();
		try {
			LOGGER.trace("Executing query: " + queryString.replace("\n", " ") + " Dataset: "
					+ dataset + " Include inferred: " + includeInferred + " Bindings: "
					+ getBindings());

			if (SemanticOperationLogger.getIsEnabled()) {
				SemanticOperationLogger.addLogOperation("Q", queryString, getBindings());
			}

			return client.sendTupleQuery(queryLanguage, queryString, baseURI, dataset,
					includeInferred, maxQueryTime, getBindingsArray());
		} catch (IOException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		} catch (RepositoryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		} catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void evaluate(TupleQueryResultHandler handler) throws QueryEvaluationException,
			TupleQueryResultHandlerException {
		HTTPClient client = getHttpClient();
		try {
			client.sendTupleQuery(queryLanguage, queryString, baseURI, dataset, includeInferred,
					maxQueryTime, handler, getBindingsArray());
		} catch (IOException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		} catch (RepositoryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		} catch (MalformedQueryException e) {
			throw new HTTPQueryEvaluationException(e.getMessage(), e);
		}
	}
}
