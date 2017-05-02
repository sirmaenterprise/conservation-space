package com.sirma.itt.seip.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.MockPlugins.PluginWithName;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Tests for {@link Plugins}
 *
 * @author BBonev
 */
public class PluginsTest {

	@Test
	public void pluginResolving() throws Exception {
		Plugins<PluginWithName> plugins = buildInstance();
		Optional<PluginWithName> pluginWithName = plugins.get("PluginWithName1");
		assertTrue(pluginWithName.get() instanceof MockPlugins.PluginWithName1);

		plugins.setIdentityResolver(Named::getName);

		pluginWithName = plugins.get("PluginWithName2");
		assertTrue(pluginWithName.get() instanceof MockPlugins.PluginWithName2);

		plugins.setIdentityComparator(null);
		pluginWithName = plugins.get("PluginWithName2");
		assertTrue(pluginWithName.get() instanceof MockPlugins.PluginWithName2);

		plugins.setIdentityComparator((k1, k2) -> EqualsHelper.nullSafeEquals((String) k1, (String) k2, true));

		pluginWithName = plugins.get("pluginwithname2");
		assertTrue(pluginWithName.get() instanceof MockPlugins.PluginWithName2);

		// non existing
		assertFalse(plugins.get("PluginWithName3").isPresent());

		// invalid key
		assertFalse(plugins.get(null).isPresent());
	}

	@Test
	public void select() throws Exception {
		Optional<PluginWithName> plugin = buildInstance().select(p -> p instanceof MockPlugins.PluginWithName2);
		assertNotNull(plugin);
		assertTrue(plugin.isPresent());

		plugin = buildInstance().select(p -> false);
		assertNotNull(plugin);
		assertFalse(plugin.isPresent());
	}

	private static Plugins<PluginWithName> buildInstance() {
		List<PluginWithName> list = Arrays.asList(new MockPlugins.PluginWithName1(), new MockPlugins.PluginWithName2());
		Plugins<PluginWithName> plugins = new Plugins<>("", list);
		return plugins;
	}
}
