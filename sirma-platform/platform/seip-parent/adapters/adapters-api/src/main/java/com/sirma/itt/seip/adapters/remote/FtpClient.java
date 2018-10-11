package com.sirma.itt.seip.adapters.remote;

import java.io.InputStream;
import java.io.Serializable;

import com.sirma.itt.emf.adapter.DMSException;

/**
 * The FtpClient is a base interface for sending data to a ftp server.
 *
 * @author Nikolay Ch
 */
public interface FtpClient {
	/**
	 * Sends the data by its input stream to the server.
	 *
	 * @param stream
	 *            the input stream of the file
	 * @param name
	 *            the new name of the file
	 * @param config
	 *            the configuration for the connection
	 * @throws DMSException
	 *             when an error with the connection occurs
	 */
	void transfer(InputStream stream, String name, FTPConfiguration config) throws DMSException;

	/**
	 * Sends the data to remote FTP server asynchronously.
	 *
	 * @param stream
	 *            the input stream of the file
	 * @param name
	 *            the new name of the file
	 * @param config
	 *            the configuration for the connection
	 * @return the async request id. The id could be used for requesting the status of the async operation by calling
	 *         the method.
	 * @throws DMSException
	 *             when an error with the connection occurs
	 */
	Serializable transferAsync(InputStream stream, String name, FTPConfiguration config) throws DMSException;

	/**
	 * Deletes a file identified by he given name/path relative to the {@link FTPConfiguration#getRemoteDir()}.
	 *
	 * @param name
	 *            the name/path to the remote file to delete
	 * @param config
	 *            the configuration to use for connecting to the server.
	 * @return true, if successfully deleted the file or <code>false</code> if not found or cannot be deleted
	 * @throws DMSException
	 *             the DMS exception if could not connect to server
	 */
	boolean delete(String name, FTPConfiguration config) throws DMSException;
}
