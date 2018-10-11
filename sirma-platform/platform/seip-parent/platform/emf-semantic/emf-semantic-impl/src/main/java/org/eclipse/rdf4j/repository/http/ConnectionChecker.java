package org.eclipse.rdf4j.repository.http;

import java.lang.invoke.MethodHandles;

import org.eclipse.rdf4j.http.client.RDF4JProtocolSession;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporary checker class that will validate the connection timeout for managed connections and will ensure that they
 * have proper socket timeout value.<br>
 * This is here mainly because the accessed classes are package protected
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/04/2018
 */
public class ConnectionChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ConnectionChecker() {
		// utility class
	}

	/**
	 * Checks and resets the connection timeout of the given connection if it's different than the default expected
	 * value of zero. The method only works for {@link HTTPRepositoryConnection} instances. Anything else is ignored.
	 *
	 * @param connection the connection to check
	 */
	public static void ensureProperRequestTimeout(RepositoryConnection connection) {
		if (connection instanceof HTTPRepositoryConnection) {
			RDF4JProtocolSession protocolSession = ((HTTPRepositoryConnection) connection).getSesameSession();
			long connectionTimeout = protocolSession.getConnectionTimeout();
			if (connectionTimeout > 0) {
				LOGGER.warn(
						"Detected non valid connection timeout value of {} for managed connection {}. Resetting it to the correct value of 0",
						connectionTimeout, System.identityHashCode(connection));
				// only reset it when the value is not correct otherwise this will override any default request
				// parameters set in the http client when the repository is build
				protocolSession.setConnectionTimeout(0L);
			}
		}
	}
}
