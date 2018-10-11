package com.sirma.sep.instance.template;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;
import com.sirma.itt.seip.template.TemplateDefinitionImpl;

/**
 * Extension that registers {@link TemplateDefinitionImpl} to Kryo serialization engine.
 *
 * @author A. Kunchev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 100.1)
public class TemplateDefinitionKryoInitializer implements KryoInitializer {

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(Collections.singletonList(new Pair<>(TemplateDefinitionImpl.class, 52)));
	}
}