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
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.BaseRegionDefinition;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.StateTransitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.FilterMode;
import com.sirma.itt.seip.domain.definition.MergeableBase;
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
			new Pair<Class<?>, Integer>(BaseDefinition.class, 1),
			new Pair<Class<?>, Integer>(FieldDefinitionImpl.class, 6),
			new Pair<Class<?>, Integer>(PropertyDefinitionProxy.class, 7),
			new Pair<Class<?>, Integer>(DataType.class, 8), new Pair<Class<?>, Integer>(RegionDefinitionImpl.class, 9),
			new Pair<Class<?>, Integer>(BaseRegionDefinition.class, 10),
			new Pair<Class<?>, Integer>(MergeableBase.class, 11),
			new Pair<Class<?>, Integer>(ControlDefinitionImpl.class, 12),
			new Pair<Class<?>, Integer>(ControlParamImpl.class, 13),
			new Pair<Class<?>, Integer>(AllowedChildDefinitionImpl.class, 14),
			new Pair<Class<?>, Integer>(AllowedChildConfigurationImpl.class, 15),
			new Pair<Class<?>, Integer>(TransitionDefinitionImpl.class, 19),
			new Pair<Class<?>, Integer>(ConditionDefinitionImpl.class, 20),
			new Pair<Class<?>, Integer>(ArrayList.class, 21), new Pair<Class<?>, Integer>(LinkedList.class, 22),
			new Pair<Class<?>, Integer>(LinkedHashSet.class, 23), new Pair<Class<?>, Integer>(DisplayType.class, 24),
			new Pair<Class<?>, Integer>(Date.class, 25), new Pair<Class<?>, Integer>(StateTransitionImpl.class, 202),
			new Pair<Class<?>, Integer>(LinkReference.class, 26), new Pair<Class<?>, Integer>(LinkSourceId.class, 27),
			new Pair<Class<?>, Integer>(LinkedHashMap.class, 28), new Pair<Class<?>, Integer>(EmfUser.class, 29),
			new Pair<Class<?>, Integer>(EmfGroup.class, 30), new Pair<Class<?>, Integer>(ResourceType.class, 31),
			new Pair<Class<?>, Integer>(Pair.class, 32), new Pair<Class<?>, Integer>(FilterMode.class, 33),
			new Pair<Class<?>, Integer>(HashMap.class, 34), new Pair<Class<?>, Integer>(IntegerPair.class, 35),
			new Pair<Class<?>, Integer>(StringPair.class, 36),
			// events
			new Pair<Class<?>, Integer>(RestorePermissionTrigger.class, 400)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<>(CLASS_REGISTER));
	}

}
