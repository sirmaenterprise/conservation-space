package com.sirma.itt.emf.audit.kryo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * Adds Kryo configuration for the Audit module. Registers {@link AuditActivity} for serialization.
 *
 * @author Mihail Radkov
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 9000)
public class AuditKryoInitializer implements KryoInitializer {

	/** Contains the classes to be registered. */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<Pair<Class<?>, Integer>>(
			Arrays.asList(new Pair<Class<?>, Integer>(AuditActivity.class, 9000)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<Pair<Class<?>, Integer>>(CLASS_REGISTER));
	}

}
