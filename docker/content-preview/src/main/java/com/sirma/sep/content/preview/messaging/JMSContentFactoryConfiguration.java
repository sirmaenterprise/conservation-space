package com.sirma.sep.content.preview.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.stereotype.Component;

/**
 * Configurations related to constructing {@link JmsListenerContainerFactory} in {@link JMSContentFactory}.
 *
 * @author Mihail Radkov
 */
@Component
@ConfigurationProperties(prefix = "content.preview.jms")
public class JMSContentFactoryConfiguration {

	private String concurrency = "1";

	public String getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(String concurrency) {
		this.concurrency = concurrency;
	}

}
