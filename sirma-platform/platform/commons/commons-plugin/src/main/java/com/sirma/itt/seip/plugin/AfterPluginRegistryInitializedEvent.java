package com.sirma.itt.seip.plugin;

import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;

/**
 * Fired after the plugin registry is initialized. Provides the currently fetched plugins so they could be modified
 * based on custom logic.
 *
 * @author Adrian Mitev
 */
public class AfterPluginRegistryInitializedEvent {

	private final Map<String, List<Bean<?>>> plugins;

	/**
	 * Initializes the plugins map.
	 *
	 * @param plugins
	 *            list of the plugins that are currently found.
	 */
	public AfterPluginRegistryInitializedEvent(Map<String, List<Bean<?>>> plugins) {
		this.plugins = plugins;
	}

	/**
	 * Getter method for plugins.
	 *
	 * @return the plugins
	 */
	public Map<String, List<Bean<?>>> getPlugins() {
		return plugins;
	}

}
