package com.sirma.itt.cmf.util.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.serialization.kryo.KryoInitializer;

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
	 * <b>WARNING: DO NOT CHANGE THE ELEMENTS ORDER. IF NEW ELEMENT NEED TO BE ADDED IT SHOULD GO AT
	 * THE END OF THE ARRAY!!!</b>
	 */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<Pair<Class<?>, Integer>>(
			Arrays.asList(new Pair<Class<?>, Integer>(CaseDefinitionImpl.class, 2),
					new Pair<Class<?>, Integer>(SectionDefinitionImpl.class, 3),
					new Pair<Class<?>, Integer>(DocumentDefinitionRefImpl.class, 4),
					new Pair<Class<?>, Integer>(DocumentDefinitionImpl.class, 5),
					new Pair<Class<?>, Integer>(WorkflowDefinitionImpl.class, 16),
					new Pair<Class<?>, Integer>(TaskDefinitionRefImpl.class, 17),
					new Pair<Class<?>, Integer>(TaskDefinitionTemplateImpl.class, 18),
					new Pair<Class<?>, Integer>(TaskDefinitionImpl.class, 50),
					new Pair<Class<?>, Integer>(GenericDefinitionImpl.class, 51)));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<Pair<Class<?>, Integer>>(CLASS_REGISTER));
	}

}
