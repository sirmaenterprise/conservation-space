/**
 *
 */
package com.sirmaenterprise.sep.ui.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Idoc specific configurations loaded using idoc-js-config.xhtml
 *
 * @author BBonev
 */
@Named
@ApplicationScoped
public class IDocConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "js.time.format", defaultValue = "hh:mm TT", label = "Date format used in iDoc JS")
	private ConfigurationProperty<String> jsTimeFormat;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "idoc.draftAutosaveIntervalMillis", type = Integer.class, defaultValue = "60000", label = "The time for draft auto save in millisecods")
	private ConfigurationProperty<Integer> draftAutosaveInterval;
	
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "idoc.print.timeout", type = Integer.class, defaultValue = "120", label = "Specify the print timeout in seconds.")
	private ConfigurationProperty<Integer> printTimeout;

	/**
	 * Gets max time to complete the operation.
	 * 
	 * @return max print time
	 */
	public Integer getPrintTimeout() {
		return printTimeout.get();
	}

	/**
	 * Gets the js time format.
	 *
	 * @return the js time format
	 */
	public String getJsTimeFormat() {
		return jsTimeFormat.get();
	}

	/**
	 * Gets the draft autosave interval.
	 *
	 * @return the draft autosave interval
	 */
	public Integer getDraftAutosaveInterval() {
		return draftAutosaveInterval.get();
	}
}
