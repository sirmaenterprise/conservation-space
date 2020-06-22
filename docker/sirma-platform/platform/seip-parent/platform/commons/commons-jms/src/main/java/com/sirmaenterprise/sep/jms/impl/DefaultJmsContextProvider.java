package com.sirmaenterprise.sep.jms.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirmaenterprise.sep.jms.api.JmsConnectionFactoryResolver;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.exception.ConnectionFactoryNotFoundException;
import com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException;

/**
 * Default {@link JMSContext} provider. The JMS context is produced from a connection factory defined by
 * the configurations:<ul>
 * <li>system.jms.connectionFactory.jndi</li>
 * <li>system.jms.connectionFactory.username</li>
 * <li>system.jms.connectionFactory.password</li>
 * <li>system.jms.testQueue</li>
 * </ul>
 * The user name and password are used when connecting to a remote connection factory. When connecting to local in-vm
 * connection factory they should not be set. <br>When the configurations are changed the connection factory will be
 * looked up and tested by accessing the specified queue. If not configured the {@code
 * java:/jms/queue/DLQ} or {@code java:/jms.queue.DLQ} will be accessed. If this test fails the configurations will
 * be considered invalid and rejected.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/05/2017
 */
@ApplicationScoped
public class DefaultJmsContextProvider implements JmsContextProvider {
	@ConfigurationPropertyDefinition(defaultValue = "java:/jms/queue/DLQ", system = true, shared = false,
			subSystem = "jms", sensitive = true, label = "Specifies a queue to be used for testing the connection "
			+ "factory. Suggested value is the default Dead Letter Queue (DLQ)")
	private static final String TEST_QUEUE = "system.jms.testQueue";

	@ConfigurationPropertyDefinition(system = true, shared = false,
			subSystem = "jms", sensitive = true, label = "Specifies the user name to be used when creating remote JMS"
			+ " connections and connecting the Remote JMS servers and connection factories. For local (in-vm) "
			+ "connection factories this configuration should not be set")
	private static final String CONNECTION_USER = "system.jms.connectionFactory.username";

	@ConfigurationPropertyDefinition(system = true, shared = false,
			subSystem = "jms", sensitive = true, password = true, label = "Specifies the password to be used when "
			+ "creating remote JMS connections and connecting the Remote JMS servers and connection factories. For local (in-vm) "
			+ "connection factories this configuration should not be set")
	private static final String CONNECTION_PASS = "system.jms.connectionFactory.password";

	@ConfigurationPropertyDefinition(defaultValue = "java:jboss/DefaultJMSConnectionFactory", system = true,
			shared = false, subSystem = "jms", sensitive = true,
			label = "Specifies the default Connection factory JNDI name that should be used for accepting JMS messages")
	private static final String CONNECTION_FACTORY_JNDI = "system.jms.connectionFactory.jndi";

	@ConfigurationGroupDefinition(system = true, subSystem = "jms", type = JmsContextProvider.class, properties = {
			CONNECTION_USER,
			CONNECTION_PASS,
			CONNECTION_FACTORY_JNDI,
			TEST_QUEUE })
	private static final String CONNECTION_FACTORY = "system.jms.connectionFactory";

	@Inject
	@Configuration(CONNECTION_FACTORY)
	private ConfigurationProperty<JmsContextProvider> instance;

	@ConfigurationConverter(CONNECTION_FACTORY)
	static JmsContextProvider buildJmsContext(GroupConverterContext context,
			JmsConnectionFactoryResolver factoryResolver, JmsDestinationResolver destinationResolver) {

		String user = context.get(CONNECTION_USER);
		String pass = context.get(CONNECTION_PASS);
		String jndi = context.get(CONNECTION_FACTORY_JNDI);
		String testQueue = context.get(TEST_QUEUE);

		ConnectionFactory connectionFactory;
		try {
			connectionFactory = factoryResolver.resolve(jndi);
		} catch (ConnectionFactoryNotFoundException e) {
			throw new ConfigurationException("Connection factory configurations is not valid", e);
		}

		final ConnectionFactory factory = connectionFactory;
		JmsContextProvider provider;
		if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) {
			provider = () -> factory.createContext(user, pass, Session.SESSION_TRANSACTED);
		} else {
			provider = () -> factory.createContext(Session.SESSION_TRANSACTED);
		}

		// we have a connection factory now check if the factory is valid or not
		// test if the provider is valid or not after configuration changes by connecting to the default DLQ
		validateProvider(testQueue, destinationResolver, provider);
		return provider;
	}

	private static void validateProvider(String testQueue, JmsDestinationResolver destinationResolver,
			JmsContextProvider provider) {
		try (JMSContext ctx = provider.provide();
				QueueBrowser browser = ctx.createBrowser(getTestQueue(testQueue, destinationResolver))) {
			browser.getEnumeration().hasMoreElements(); // NOSONAR
		} catch (JMSException | JMSRuntimeException e) {
			throw new ConfigurationException("Connection factory configurations is not valid", e);
		}
	}

	private static Queue getTestQueue(String testQueue, JmsDestinationResolver destinationResolver) {
		try {
			return destinationResolver.resolveQueue(testQueue);
		} catch (DestinationNotFoundException e) {
			try {
				// convert the JNDI name from the format java:/jms/queue/DLQ to java:/jms.queue.DLQ
				// as some of the provides uses different format for lookup or the queue can be defined differently
				String changedName = testQueue.replaceAll("(?<!:)/", ".");
				return destinationResolver.resolveQueue(changedName);
			} catch (DestinationNotFoundException e1) {
				e1.addSuppressed(e);
				throw new ConfigurationException(e1);
			}
		}
	}

	@Override
	public JMSContext provide() {
		return instance.getOrFail().provide();
	}
}
