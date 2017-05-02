package com.sirma.itt.seip.configuration.build;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Raw provider that uses all found {@link ConfigurationSource}s that are available to fetch a raw value. It iterates
 * over the sources and tries to fetch the value for each one of them. The returned value is the first non
 * <code>null</code> value. The bean scope (Singleton) is provided by a custom bean class.
 *
 * @author BBonev
 */
@Vetoed
public class DefaultRawConfigurationAccessor implements RawConfigurationAccessor {

	@Inject
	@ExtensionPoint(ConfigurationSource.NAME)
	private Iterable<ConfigurationSource> sources;

	@Override
	public String getRawConfigurationValue(String name) {
		for (ConfigurationSource source : sources) {
			String value = source.getConfigurationValue(name);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public Set<String> getAvailableConfigurations() {
		Set<String> configurations = new HashSet<>(64);
		for (ConfigurationSource source : sources) {
			for (Object key : source.getConfigurations().keySet()) {
				configurations.add(key.toString());
			}
		}
		return Collections.unmodifiableSet(configurations);
	}

}
