package com.sirmaenterprise.sep.jms.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Enumeration;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirmaenterprise.sep.jms.api.JmsConnectionFactoryResolver;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.exception.ConnectionFactoryNotFoundException;
import com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link DefaultJmsContextProvider}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2017
 */
public class DefaultJmsContextProviderTest {

	@Mock
	private GroupConverterContext context;
	@Mock
	private JmsConnectionFactoryResolver factoryResolver;
	@Mock
	private JmsDestinationResolver destinationResolver;
	@Mock
	private ConnectionFactory connectionFactory;
	@Mock
	private JMSContext ctx;
	@Mock
	private QueueBrowser browser;
	@Mock
	private Queue queue;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(factoryResolver.resolve(anyString())).thenReturn(connectionFactory);
		when(connectionFactory.createContext(Session.SESSION_TRANSACTED)).thenReturn(ctx);
		when(connectionFactory.createContext(anyString(), anyString(), eq(Session.SESSION_TRANSACTED))).thenReturn(ctx);
		when(ctx.createBrowser(queue)).thenReturn(browser);
		when(browser.getEnumeration()).thenReturn(mock(Enumeration.class));
		when(context.get("system.jms.testQueue")).thenReturn("java:/jms/queue/DLQ");
		when(destinationResolver.resolveQueue("java:/jms/queue/DLQ")).thenThrow(DestinationNotFoundException.class);
		when(destinationResolver.resolveQueue("java:/jms.queue.DLQ")).thenReturn(queue);
	}

	@Test
	public void buildJmsContextWithOutUserAndPass() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		JmsContextProvider contextProvider = DefaultJmsContextProvider.buildJmsContext(context, factoryResolver,
				destinationResolver);
		assertNotNull(contextProvider);

		verify(connectionFactory).createContext(Session.SESSION_TRANSACTED);
	}

	@Test
	public void buildJmsContextWithUserAndPass() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");
		when(context.get("system.jms.connectionFactory.username")).thenReturn("user");
		when(context.get("system.jms.connectionFactory.password")).thenReturn("pass");

		JmsContextProvider contextProvider = DefaultJmsContextProvider.buildJmsContext(context, factoryResolver,
				destinationResolver);
		assertNotNull(contextProvider);

		verify(connectionFactory).createContext("user", "pass", Session.SESSION_TRANSACTED);
	}

	@Test(expected = ConfigurationException.class)
	public void buildJmsContext_shouldFail_OnMissingConnectionFactory() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		reset(factoryResolver);
		when(factoryResolver.resolve(anyString())).thenThrow(ConnectionFactoryNotFoundException.class);

		DefaultJmsContextProvider.buildJmsContext(context, factoryResolver, destinationResolver);
	}

	@Test(expected = ConfigurationException.class)
	public void buildJmsContext_shouldFail_IfCannotCreateContext() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		reset(connectionFactory);
		when(connectionFactory.createContext(Session.SESSION_TRANSACTED)).thenThrow(JMSRuntimeException.class);

		DefaultJmsContextProvider.buildJmsContext(context, factoryResolver, destinationResolver);
	}

	@Test(expected = ConfigurationException.class)
	public void buildJmsContext_shouldFail_IfCannotFindDLQ() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		reset(destinationResolver);
		when(destinationResolver.resolveQueue(anyString())).thenThrow(DestinationNotFoundException.class);

		DefaultJmsContextProvider.buildJmsContext(context, factoryResolver, destinationResolver);
	}

	@Test(expected = ConfigurationException.class)
	public void buildJmsContext_shouldFail_IfCannotBrowseDLQ_JMS2() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		reset(browser);
		when(browser.getEnumeration()).thenThrow(JMSRuntimeException.class);

		DefaultJmsContextProvider.buildJmsContext(context, factoryResolver, destinationResolver);
	}

	@Test(expected = ConfigurationException.class)
	public void buildJmsContext_shouldFail_IfCannotBrowseDLQ_JMS1() throws Exception {
		when(context.get("system.jms.connectionFactory.jndi")).thenReturn("ConnectionFactory");

		reset(browser);
		when(browser.getEnumeration()).thenThrow(JMSException.class);

		DefaultJmsContextProvider.buildJmsContext(context, factoryResolver, destinationResolver);
	}
}
