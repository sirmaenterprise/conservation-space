package com.sirma.itt.seip.eai.dam.util.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.dam.service.communication.DAMEAIServices;
import com.sirma.itt.seip.eai.dam.service.communication.response.ContentDownloadServiceRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * The initialization extension to register dam classes for kryo serialization
 *
 * @author siliev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 610)
public class DAMKryoInitializer implements KryoInitializer {

	/** The registered classes. */
	private static final List<Pair<Class<?>, Integer>> CLASS_REGISTER = Collections
			.unmodifiableList(Arrays.asList(new Pair<Class<?>, Integer>(ContentDownloadServiceRequest.class, 610),
					new Pair<Class<?>, Integer>(CSExternalInstanceId.class, 611),
					new Pair<Class<?>, Integer>(DAMEAIServices.class, 612)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return CLASS_REGISTER;
	}

}
