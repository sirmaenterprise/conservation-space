package com.sirmaenterprise.sep.jms.provision;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Configurations for JMS subsystem.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/01/2019
 */
public class JmsSubsystemConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.jms.persistenceLocation", type = File.class,
			converter = "directory", subSystem = "jms", system = true, label = "Base location for JMS persistence")
	private ConfigurationProperty<File> persistenceLocation;

	/**
	 * Specifies the base location for all JMS persistence files
	 *
	 * @return the JMS persistence location folder
	 */
	public ConfigurationProperty<File> getPersistenceLocation() {
		return persistenceLocation;
	}
}
