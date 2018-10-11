package com.sirma.sep.content.preview.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;

/**
 * Factory for producing {@link JmsListenerContainerFactory} for receiving and {@link JmsTemplate} for sending {@link
 * javax.jms.Message} in {@link com.sirma.sep.content.preview.ContentPreviewApplication} application context.
 *
 * @author Mihail Radkov
 */
@Component
public class JMSContentFactory {

	public static final String CONTENT_PREVIEW_FACTORY = "remoteContentPreviewQueueFactory";

	private JMSContentFactoryConfiguration contentFactoryConfiguration;

	/**
	 * Instantiates the factory with the given configurations.
	 *
	 * @param contentFactoryConfiguration
	 * 		- the factory configurations to be used by the factory
	 */
	@Autowired
	public JMSContentFactory(JMSContentFactoryConfiguration contentFactoryConfiguration) {
		this.contentFactoryConfiguration = contentFactoryConfiguration;
	}

	/**
	 * Produces a {@link JmsListenerContainerFactory} for receiving {@link javax.jms.Message}s in queue mode.
	 *
	 * @param connectionFactory
	 * 		- the provided {@link ConnectionFactory} for configuring the container factory
	 * @param configurer
	 * 		- configures the produced {@link JmsListenerContainerFactory} with the provided connection factory
	 * @return configured {@link JmsListenerContainerFactory}
	 */
	@Bean
	public JmsListenerContainerFactory<DefaultMessageListenerContainer> remoteContentPreviewQueueFactory(
			ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setPubSubDomain(false);
		factory.setConcurrency(this.contentFactoryConfiguration.getConcurrency());
		configurer.configure(factory, connectionFactory);
		return factory;
	}

	/**
	 * Produces {@link JmsTemplate}s for sending {@link javax.jms.Message} to remote JMS queues or topics.
	 *
	 * @param connectionFactory
	 * 		- the connection factory to instantiate the {@link JmsTemplate} with
	 * @return ready to use template
	 */
	@Bean
	public JmsTemplate template(ConnectionFactory connectionFactory) {
		return new JmsTemplate(connectionFactory);
	}
}
