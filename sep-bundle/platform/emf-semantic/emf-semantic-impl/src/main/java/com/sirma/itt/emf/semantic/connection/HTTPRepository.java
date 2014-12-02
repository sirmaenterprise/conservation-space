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

import java.io.File;
import java.io.IOException;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.client.SesameHTTPClient;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Literals;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;
import org.openrdf.rio.RDFFormat;

// TODO: Auto-generated Javadoc
/**
 * A repository that serves as a proxy for a remote repository on a Sesame
 * server. Methods in this class may throw the specific RepositoryException
 * subclass UnautorizedException, the semantics of which is defined by the HTTP
 * protocol.
 *
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Arjohn Kampman
 * @author jeen
 * @author Herko ter Horst
 */
public class HTTPRepository extends RepositoryBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private final HTTPClient httpClient;

	/** The data dir. */
	private File dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Instantiates a new hTTP repository.
	 */
	private HTTPRepository() {
		super();
		httpClient = new SesameHTTPClient();
		httpClient.setValueFactory(new ValueFactoryImpl());
	}

	/**
	 * Instantiates a new hTTP repository.
	 *
	 * @param serverURL the server url
	 * @param repositoryID the repository id
	 */
	public HTTPRepository(final String serverURL, final String repositoryID) {
		this();
		getHTTPClient().setRepository(serverURL, repositoryID);
	}

	/**
	 * Instantiates a new hTTP repository.
	 *
	 * @param repositoryURL the repository url
	 */
	public HTTPRepository(final String repositoryURL) {
		this();
		getHTTPClient().setRepository(repositoryURL);
	}

	/* ---------------*
	 * public methods *
	 * ---------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataDir(final File dataDir) {
		this.dataDir = dataDir;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getDataDir() {
		return dataDir;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueFactory getValueFactory() {
		return httpClient.getValueFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new HTTPRepositoryConnection(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
		throws RepositoryException
	{
		if (!isInitialized()) {
			throw new IllegalStateException("HTTPRepository not initialized.");
		}

		boolean isWritable = false;
		final String repositoryURL = getHTTPClient().getRepositoryURL();

		try {
			final TupleQueryResult repositoryList = getHTTPClient().getRepositoryList();
			try {
				while (repositoryList.hasNext()) {
					final BindingSet bindingSet = repositoryList.next();
					final Value uri = bindingSet.getValue("uri");

					if (uri != null && uri.stringValue().equals(repositoryURL)) {
						isWritable = Literals.getBooleanValue(bindingSet.getValue("writable"), false);
						break;
					}
				}
			}
			finally {
				repositoryList.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
		catch (IOException e) {
			throw new RepositoryException(e);
		}

		return isWritable;
	}

	/**
	 * Sets the preferred serialization format for tuple query results to the
	 * supplied {@link TupleQueryResultFormat}, overriding the {@link HTTPClient}
	 * 's default preference. Setting this parameter is not necessary in most
	 * cases as the {@link HTTPClient} by default indicates a preference for the
	 * most compact and efficient format available.
	 *
	 * @param format
	 *        the preferred {@link TupleQueryResultFormat}. If set to 'null' no
	 *        explicit preference will be stated.
	 */
	public void setPreferredTupleQueryResultFormat(final TupleQueryResultFormat format) {
		httpClient.setPreferredTupleQueryResultFormat(format);
	}

	/**
	 * Indicates the current preferred {@link TupleQueryResultFormat}.
	 *
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public TupleQueryResultFormat getPreferredTupleQueryResultFormat() {
		return httpClient.getPreferredTupleQueryResultFormat();
	}

	/**
	 * Sets the preferred serialization format for RDF to the supplied.
	 *
	 * @param format the preferred {@link RDFFormat}. If set to 'null' no explicit
	 * preference will be stated.
	 * {@link RDFFormat}, overriding the {@link HTTPClient}'s default preference.
	 * Setting this parameter is not necessary in most cases as the
	 * {@link HTTPClient} by default indicates a preference for the most compact
	 * and efficient format available.
	 * <p>
	 * Use with caution: if set to a format that does not support context
	 * serialization any context info contained in the query result will be lost.
	 */
	public void setPreferredRDFFormat(final RDFFormat format) {
		httpClient.setPreferredRDFFormat(format);
	}

	/**
	 * Indicates the current preferred {@link RDFFormat}.
	 *
	 * @return The preferred format, of 'null' if no explicit preference is
	 *         defined.
	 */
	public RDFFormat getPreferredRDFFormat() {
		return httpClient.getPreferredRDFFormat();
	}

	/**
	 * Set the username and password to use for authenticating with the remote
	 * repository.
	 *
	 * @param username
	 *        the username. Setting this to null will disable authentication.
	 * @param password
	 *        the password. Setting this to null will disable authentication.
	 */
	public void setUsernameAndPassword(final String username, final String password) {
		httpClient.setUsernameAndPassword(username, password);
	}

	/**
	 * Gets the repository url.
	 *
	 * @return the repository url
	 */
	public String getRepositoryURL() {
		return getHTTPClient().getRepositoryURL();
	}

	/* -------------------*
	 * non-public methods *
	 * -------------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		httpClient.initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		httpClient.shutDown();
	}

	// httpClient is shared with HTTPConnection
	/**
	 * Gets the hTTP client.
	 *
	 * @return the hTTP client
	 */
	SesameHTTPClient getHTTPClient() {
		return (SesameHTTPClient)httpClient;
	}
}
