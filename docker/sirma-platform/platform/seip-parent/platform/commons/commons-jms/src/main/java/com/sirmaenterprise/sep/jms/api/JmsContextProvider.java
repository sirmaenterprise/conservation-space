package com.sirmaenterprise.sep.jms.api;

import javax.jms.JMSContext;

/**
 * {@link JMSContext} provider that will produce context from the configured {@link javax.jms.ConnectionFactory}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/05/2017
 */
public interface JmsContextProvider {
	/**
	 * Create new {@link JMSContext} instance from the default {@link javax.jms.ConnectionFactory}. The method may
	 * throw {@link javax.jms.JMSRuntimeException} if the connection throws such.
	 *
	 * @return created context instance, never null.
	 */
	JMSContext provide();
}
