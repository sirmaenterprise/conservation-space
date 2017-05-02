package com.sirma.itt.cmf.util.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;
import com.sirma.itt.seip.template.TemplateDefinitionImpl;

/**
 * Default extension point for CMF module to add kryo initialization configuration.
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 100)
public class CmfKryoInitializer implements KryoInitializer {
	/**
	 * List of registered Kryo classes
	 * <p>
	 * <b>WARNING: DO NOT CHANGE THE ELEMENTS ORDER. IF NEW ELEMENT NEED TO BE ADDED IT SHOULD GO AT THE END OF THE
	 * ARRAY!!!</b>
	 */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<>(
			Arrays.asList(
					new Pair<Class<?>, Integer>(GenericDefinitionImpl.class, 51),
					new Pair<>(TemplateDefinitionImpl.class, 52)));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<>(CLASS_REGISTER));
	}

}
