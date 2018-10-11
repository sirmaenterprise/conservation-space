package com.sirma.sep.ocr.communication.hornetq;

import javax.jms.ConnectionFactory;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Bean provider for JMS services - message factories, listener factories, etc.
 *
 * @author bbanchev
 */
@Component
@Profile("service")
public class JMSServiceProviders {

	/**
	 * Remote OCR queue factory based on {@link DefaultJmsListenerContainerFactory}. The used mode is 'queue'.
	 *
	 * @param connectionFactory the connection factory
	 * @param configurer the configurer to used for factory configure
	 * @return the JMS listener container factory
	 */
	@SuppressWarnings("static-method")
	@Bean
	public JmsListenerContainerFactory<DefaultMessageListenerContainer> remoteOcrQueueFactory(
			ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setPubSubDomain(Boolean.FALSE);
		configurer.configure(factory, connectionFactory);
		return factory;
	}

	/**
	 * Template is the sending message default template
	 *
	 * @param connectionFactory the connection factory
	 * @return the JMS template
	 */
	@SuppressWarnings("static-method")
	@Bean
	public JmsTemplate template(ConnectionFactory connectionFactory) {
		return new JmsTemplate(connectionFactory);
	}
}
