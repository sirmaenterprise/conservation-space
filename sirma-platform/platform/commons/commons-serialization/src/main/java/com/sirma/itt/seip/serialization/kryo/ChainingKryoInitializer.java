/**
 *
 */
package com.sirma.itt.seip.serialization.kryo;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Wrapper class to provide single injection point for the {@link KryoInitializer} plugin.
 *
 * @author BBonev
 */
@Singleton
public class ChainingKryoInitializer implements KryoInitializer {

	@Inject
	@ExtensionPoint(KryoInitializer.TARGET_NAME)
	private Iterable<KryoInitializer> kryoInitializers;

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		List<Pair<Class<?>, Integer>> classes = new LinkedList<>();
		for (KryoInitializer initializer : kryoInitializers) {
			classes.addAll(initializer.getClassesToRegister());
		}
		return classes;
	}

}
