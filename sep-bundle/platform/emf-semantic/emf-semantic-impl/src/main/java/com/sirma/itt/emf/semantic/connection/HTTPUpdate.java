/*
 * Licensed to Aduna under one or more contributor license agreements.
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD
 * License (the "License"); you may not use this file except in compliance
 * with the License. See the LICENSE.txt file distributed with this work
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.sirma.itt.emf.semantic.connection;

import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.client.query.AbstractHTTPUpdate;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

// TODO: Auto-generated Javadoc
/**
 * Update specific to the HTTP protocol. Methods in this class may throw the
 * specific RepositoryException subclass UnautorizedException, the semantics of
 * which is defined by the HTTP protocol.
 *
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Jeen Broekstra
 */
public class HTTPUpdate extends AbstractHTTPUpdate {

	/** The http con. */
	protected final HTTPRepositoryConnection httpCon;

	/**
	 * Instantiates a new hTTP update.
	 *
	 * @param con the con
	 * @param ql the ql
	 * @param queryString the query string
	 * @param baseURI the base uri
	 */
	public HTTPUpdate(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		super(con.getRepository().getHTTPClient(), ql, queryString, baseURI);
		this.httpCon = con;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute()
		throws UpdateExecutionException
	{
		try {
			// TODO have a look at this
			if (httpCon.isAutoCommit()) {
				// execute update immediately
				HTTPClient client = httpCon.getRepository().getHTTPClient();
				try {
					client.sendUpdate(getQueryLanguage(), getQueryString(), getBaseURI(), dataset, includeInferred,
							getBindingsArray());
				}
				catch (UnauthorizedException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (QueryInterruptedException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (MalformedQueryException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (IOException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
			}
			else {
				// defer execution as part of transaction.
				httpCon.scheduleUpdate(this);
			}
		}
		catch (RepositoryException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}

	}
}
