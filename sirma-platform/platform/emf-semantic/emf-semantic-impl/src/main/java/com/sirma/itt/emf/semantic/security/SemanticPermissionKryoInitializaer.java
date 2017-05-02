package com.sirma.itt.emf.semantic.security;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * Kryo register for semantic classes
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 4343)
public class SemanticPermissionKryoInitializaer implements KryoInitializer {

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Arrays.asList(new Pair<>(AutoPermissionAssignmentChangeRequest.class, Integer.valueOf(4545)),
				new Pair<>(ParentChangeRequest.class, Integer.valueOf(4546)));
	}
}
