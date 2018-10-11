package com.sirmaenterprise.sep.jms.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException;

/**
 * Default implementation of {@link JmsDestinationResolver} that uses {@link InitialContext} to lookup the JMS
 * {@link Destination}s
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
@ApplicationScoped
public class InitialContextDestinationResolver implements JmsDestinationResolver {

	@Override
	public <D extends Destination> D resolve(String jndi) {
		try {
			return InitialContext.doLookup(jndi);
		} catch (NamingException e) {
			DestinationNotFoundException exception = new DestinationNotFoundException(jndi);
			exception.addSuppressed(e);
			throw exception;
		}
	}
}
