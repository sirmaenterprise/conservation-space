package com.sirma.itt.seip.adapters.remote;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * Extension for {@link KryoInitializer} to register classes to Kryo from adapters modules.
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 120)
public class AdaptersKryoRegister implements KryoInitializer {

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Arrays.asList(new Pair<>(FTPConfiguration.class, 332));
	}
}
