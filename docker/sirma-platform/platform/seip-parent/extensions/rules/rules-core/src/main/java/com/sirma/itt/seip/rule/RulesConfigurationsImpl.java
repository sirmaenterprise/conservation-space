package com.sirma.itt.seip.rule;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.rule.RulesConfigurations;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Configuration class for Rules Module.
 *
 * @author Hristo Lungov
 */
@Singleton
@Documentation("Rules Configurations properties")
public class RulesConfigurationsImpl implements RulesConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "rules.activate", defaultValue = "false", sensitive = true, type = Boolean.class, label = "Controls rules module")
	private ConfigurationProperty<Boolean> isRulesActivate;

	@Override
	public boolean getIsRulesActivate() {
		return isRulesActivate.get();
	}

}
