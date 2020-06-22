package com.sirma.itt.seip.wildfly;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Controller service that provides means to execute requests to JBoss Management console.
 *
 * @author bbanchev
 * @author BBonev
 */
@ApplicationScoped
public class WildflyControllerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String SERVER_MGMT_PORT = "server.mgmt.port";
	public static final String SERVER_MGMT_HOST = "server.mgmt.host";
	public static final String SERVER_MGMT_USER = "server.mgmt.user";
	public static final String SERVER_MGMT_PASS = "server.mgmt.pass";

	private InetAddress host = null;
	private int port = 9990;
	private CallbackHandler callbackHandler;

	@PostConstruct
	protected void init() {
		String hostProperty = System.getProperty(SERVER_MGMT_HOST);
		try {
			host = InetAddress.getByName(hostProperty);
		} catch (UnknownHostException e) {
			LOGGER.warn("Invalid server management host {}, will try localhost", hostProperty, e);
			try {
				host = InetAddress.getByName((String) null);
			} catch (UnknownHostException e1) {
				throw new IllegalArgumentException(e1);
			}
		}
		String portProperty = System.getProperty(SERVER_MGMT_PORT);
		try {
			if (portProperty != null) {
				port = Integer.parseInt(portProperty);
			}
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid server management port {}, using default 9990", portProperty, e);
		}

		LOGGER.info("Expecting server administration to be accessible at {}:{}", host, port);

		callbackHandler = callbacks -> {
			for (Callback current : callbacks) {
				if (current instanceof NameCallback) {
					NameCallback ncb = (NameCallback) current;
					ncb.setName(System.getProperty(SERVER_MGMT_USER, "mgmt"));
				} else if (current instanceof PasswordCallback) {
					PasswordCallback pcb = (PasswordCallback) current;
					pcb.setPassword(System.getProperty(SERVER_MGMT_PASS, "ciuser").toCharArray());
				} else if (current instanceof RealmCallback) {
					RealmCallback rcb = (RealmCallback) current;
					rcb.setText(rcb.getDefaultText());
				} else {
					throw new UnsupportedCallbackException(current);
				}
			}
		};
	}

	/**
	 * Executes remote CLI request
	 *
	 * @param request
	 *            is the request model
	 * @return the result model
	 * @throws EmfException
	 *             on any error
	 */
	public ModelNode execute(ModelNode request) throws RollbackedException {
		try (ModelControllerClient client = createClient(); Operation build = new OperationBuilder(request).build()) {
			return client.execute(build);
		} catch (Exception e) {
			throw new RollbackedException("Error during remote operation!", e);
		}
	}

	private ModelControllerClient createClient() {
		return ModelControllerClient.Factory.create(host, port, callbackHandler);
	}
}
