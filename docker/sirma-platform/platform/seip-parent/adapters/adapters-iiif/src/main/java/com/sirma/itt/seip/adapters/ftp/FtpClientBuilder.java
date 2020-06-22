package com.sirma.itt.seip.adapters.ftp;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;

/**
 * Builder class for {@link FTPClient}. Enables easier testing.
 *
 * @author BBonev
 */
public class FtpClientBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Builds a client using the given configuration. The returned client is connected and authenticated to the remote
	 * server so it could be used directly. Note that it should be disconnected after use.
	 *
	 * @param configuration
	 *            the configuration to use for client building.
	 * @return non <code>null</code> client that is ready for use. Note the method will build new instance each call.
	 * @throws DMSException
	 *             if could not connect to remote FTP server or communication exception occur.
	 */
	@SuppressWarnings("static-method")
	public FTPClient buildClient(FTPConfiguration configuration) throws DMSException {
		FTPClient ftpClient;

		if (configuration.isSecured()) {
			ftpClient = new FTPSClient("SSL", true);
		} else {
			ftpClient = new FTPClient();
		}

		try {
			ftpClient.setRemoteVerificationEnabled(configuration.isRemoteVerificationEnabled());
			ftpClient.connect(configuration.getHostname(), configuration.getPort());
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new DMSException("Could not connect to ftp server. Received a reply code: " + reply);
			}

			if (configuration.getUsername() != null && configuration.getPassword() != null) {
				if (!ftpClient.login(configuration.getUsername(), configuration.getPassword())) {
					throw new DMSException("Error while trying to log in.");
				}
				LOGGER.debug("Successfully connected to the FTP server.");
			}

			// set the transfer buffer size to improve the file transfer performance
			// https://stackoverflow.com/questions/11572588/speed-up-apache-commons-ftpclient-transfer
			ftpClient.setBufferSize(configuration.getBufferSize());
		} catch (IOException e) {
			throw new DMSException("An error occurred while trying to connect to the server.", e);
		}
		return ftpClient;
	}

	/**
	 * Logout and disconnect the given client.
	 *
	 * @param client
	 *            the client
	 */
	@SuppressWarnings("static-method")
	public void closeClient(FTPClient client) {
		if (client == null) {
			return;
		}
		try {
			disconnect(client);
		} catch (IOException e) {
			LOGGER.error("Error occurred while trying to disconnect from the FTP server.", e);
		}
	}

	private static void disconnect(FTPClient ftpClient) throws IOException {
		try {
			ftpClient.logout();
		} finally {
			ftpClient.disconnect();
		}
	}

}
