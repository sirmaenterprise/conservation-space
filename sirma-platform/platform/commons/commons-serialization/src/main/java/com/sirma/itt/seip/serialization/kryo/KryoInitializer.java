package com.sirma.itt.seip.serialization.kryo;

import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines an extension point to register additional classes to Kryo serialization engine.
 *
 * @author BBonev
 */
public interface KryoInitializer extends Plugin {

	/** The plugin name. */
	String TARGET_NAME = "KryoInitializer";

	/**
	 * Gets the classes to register.
	 *
	 * @return the classes to register
	 */
	List<Pair<Class<?>, Integer>> getClassesToRegister();
}