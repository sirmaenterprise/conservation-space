package com.sirmaenterprise.sep.jms.api;

import javax.jms.ConnectionFactory;

/**
 * Defines a resolver for {@link ConnectionFactory} instances;
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public interface JmsConnectionFactoryResolver {
	/**
	 * Resolve the default connection factory for the application. <br>If a connection factory cannot be resolved
	 * then {@link com.sirmaenterprise.sep.jms.exception.ConnectionFactoryNotFoundException} should be thrown.
	 *
	 * @param jndi the connection factory JNDI name
	 * @return connection factory instance.
	 */
	ConnectionFactory resolve(String jndi);
}
