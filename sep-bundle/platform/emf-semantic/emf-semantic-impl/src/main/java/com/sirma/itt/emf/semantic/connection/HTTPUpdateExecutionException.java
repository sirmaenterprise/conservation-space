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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;


// TODO: Auto-generated Javadoc
/**
 * The Class HTTPUpdateExecutionException.
 *
 * @author Jeen Broekstra
 */
public class HTTPUpdateExecutionException extends UpdateExecutionException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8315025167877093273L;

	/**
	 * Instantiates a new hTTP update execution exception.
	 *
	 * @param msg the msg
	 */
	public HTTPUpdateExecutionException(String msg) {
		super(msg);
	}
	
	/**
	 * Instantiates a new hTTP update execution exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public HTTPUpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new hTTP update execution exception.
	 *
	 * @param cause the cause
	 */
	public HTTPUpdateExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Checks if is caused by io exception.
	 *
	 * @return true, if is caused by io exception
	 */
	public boolean isCausedByIOException() {
		return getCause() instanceof IOException;
	}

	/**
	 * Checks if is caused by repository exception.
	 *
	 * @return true, if is caused by repository exception
	 */
	public boolean isCausedByRepositoryException() {
		return getCause() instanceof RepositoryException;
	}

	/**
	 * Checks if is caused by malformed query exception.
	 *
	 * @return true, if is caused by malformed query exception
	 */
	public boolean isCausedByMalformedQueryException() {
		return getCause() instanceof MalformedQueryException;
	}

	/**
	 * Gets the cause as io exception.
	 *
	 * @return the cause as io exception
	 */
	public IOException getCauseAsIOException() {
		return (IOException)getCause();
	}

	/**
	 * Gets the cause as repository exception.
	 *
	 * @return the cause as repository exception
	 */
	public RepositoryException getCauseAsRepositoryException() {
		return (RepositoryException)getCause();
	}

	/**
	 * Gets the cause as malformed query exception.
	 *
	 * @return the cause as malformed query exception
	 */
	public MalformedQueryException getCauseAsMalformedQueryException() {
		return (MalformedQueryException)getCause();
	}
}
