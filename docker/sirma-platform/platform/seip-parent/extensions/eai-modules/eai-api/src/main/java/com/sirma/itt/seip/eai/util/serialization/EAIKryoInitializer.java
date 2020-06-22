package com.sirma.itt.seip.eai.util.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * The initialization extension to register EAI classes for kryo serialization
 *
 * @author siliev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 600)
public class EAIKryoInitializer implements KryoInitializer {

	/** The Constant CLASS_REGISTER. */
	private static final List<Pair<Class<?>, Integer>> CLASS_REGISTER = Collections
			.unmodifiableList(Arrays.asList(new Pair<Class<?>, Integer>(RequestInfo.class, 600),
					new Pair<Class<?>, Integer>(ExternalInstanceIdentifier.class, 601)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return CLASS_REGISTER;
	}

}
