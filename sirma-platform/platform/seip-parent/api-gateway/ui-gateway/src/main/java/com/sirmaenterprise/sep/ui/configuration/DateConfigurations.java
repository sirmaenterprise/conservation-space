package com.sirmaenterprise.sep.ui.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Date configurations used by the UI applications to construct dates in widgets for example.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 06/11/2017
 */
@Named
@ApplicationScoped
public class DateConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.date.format", defaultValue = "DD.MM.YY", subSystem = "ui",
			label = "Date format pattern that is used in ui. Used for example in widgets.")
	private ConfigurationProperty<String> dateFormat;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.time.format", defaultValue = "HH.mm", subSystem = "ui",
			label = "Time format pattern that is used in ui. Used for example in widgets.")
	private ConfigurationProperty<String> timeFormat;

	public ConfigurationProperty<String> getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(ConfigurationProperty<String> dateFormat) {
		this.dateFormat = dateFormat;
	}

	public ConfigurationProperty<String> getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(ConfigurationProperty<String> timeFormat) {
		this.timeFormat = timeFormat;
	}
}
