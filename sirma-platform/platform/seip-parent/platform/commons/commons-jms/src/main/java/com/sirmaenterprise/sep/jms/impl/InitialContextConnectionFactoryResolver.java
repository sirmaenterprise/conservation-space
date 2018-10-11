package com.sirmaenterprise.sep.jms.impl;

import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirmaenterprise.sep.jms.api.JmsConnectionFactoryResolver;

/**
 * Default connection factory resolver that uses {@link InitialContext} to resolve the factory instance
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
@Singleton
public class InitialContextConnectionFactoryResolver implements JmsConnectionFactoryResolver {

	@Override
	public ConnectionFactory resolve(String jndi) {
		try {
			return InitialContext.doLookup(jndi);
		} catch (NamingException e) {
			throw new ConfigurationException("Connection factory configurations is not valid", e);
		}
	}
}
