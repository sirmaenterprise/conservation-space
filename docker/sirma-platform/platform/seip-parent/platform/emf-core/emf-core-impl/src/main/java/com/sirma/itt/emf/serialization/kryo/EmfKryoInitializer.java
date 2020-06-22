package com.sirma.itt.emf.serialization.kryo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.FilterMode;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.permissions.RestorePermissionTrigger;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * Default extension point for EMf module to add kryo initialization configuration.
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 0)
public class EmfKryoInitializer implements KryoInitializer {

	/**
	 * List of registered Kryo classes
	 */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<>(Arrays.asList(
			new Pair<>(DataType.class, 8),
			new Pair<>(MergeableBase.class, 11), 
			new Pair<>(ArrayList.class, 21),
			new Pair<>(LinkedList.class, 22),
			new Pair<>(LinkedHashSet.class, 23),
			new Pair<>(DisplayType.class, 24),
			new Pair<>(Date.class, 25), 
			new Pair<>(LinkReference.class, 26),
			new Pair<>(LinkSourceId.class, 27),
			new Pair<>(LinkedHashMap.class, 28),
			new Pair<>(EmfUser.class, 29),
			new Pair<>(EmfGroup.class, 30),
			new Pair<>(ResourceType.class, 31), 
			new Pair<>(Pair.class, 32),
			new Pair<>(FilterMode.class, 33), 
			new Pair<>(HashMap.class, 34),
			new Pair<>(IntegerPair.class, 35),
			new Pair<>(StringPair.class, 36),
			// events
			new Pair<>(RestorePermissionTrigger.class, 400)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<>(CLASS_REGISTER));
	}
}
