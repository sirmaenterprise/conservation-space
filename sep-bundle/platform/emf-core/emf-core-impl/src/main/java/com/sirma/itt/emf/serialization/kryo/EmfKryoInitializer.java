package com.sirma.itt.emf.serialization.kryo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.emf.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.emf.definition.model.BaseDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlParamImpl;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.StateTransitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Default extension point for EMf module to add kryo initialization configuration.
 * 
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 0)
public class EmfKryoInitializer implements KryoInitializer {

	/**
	 * List of registered Kryo classes
	 * <p>
	 * <b>WARNING: DO NOT CHANGE THE ELEMENTS ORDER. IF NEW ELEMENT NEED TO BE ADDED IT SHOULD GO AT
	 * THE END OF THE ARRAY!!!</b>
	 */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<>(
			Arrays.asList(new Pair<Class<?>, Integer>(BaseDefinition.class, 1),
					new Pair<Class<?>, Integer>(FieldDefinitionImpl.class, 6),
					new Pair<Class<?>, Integer>(PropertyDefinitionProxy.class, 7),
					new Pair<Class<?>, Integer>(DataType.class, 8), new Pair<Class<?>, Integer>(
							RegionDefinitionImpl.class, 9), new Pair<Class<?>, Integer>(
							BaseRegionDefinition.class, 10), new Pair<Class<?>, Integer>(
							MergeableBase.class, 11), new Pair<Class<?>, Integer>(
							ControlDefinitionImpl.class, 12), new Pair<Class<?>, Integer>(
							ControlParamImpl.class, 13), new Pair<Class<?>, Integer>(
							AllowedChildDefinitionImpl.class, 14), new Pair<Class<?>, Integer>(
							AllowedChildConfigurationImpl.class, 15), new Pair<Class<?>, Integer>(
							TransitionDefinitionImpl.class, 19), new Pair<Class<?>, Integer>(
							ConditionDefinitionImpl.class, 20), new Pair<Class<?>, Integer>(
							ArrayList.class, 21),
					new Pair<Class<?>, Integer>(LinkedList.class, 22), new Pair<Class<?>, Integer>(
							LinkedHashSet.class, 23), new Pair<Class<?>, Integer>(
							DisplayType.class, 24), new Pair<Class<?>, Integer>(Date.class, 25),
					new Pair<Class<?>, Integer>(StateTransitionImpl.class, 202),
					new Pair<Class<?>, Integer>(LinkReference.class, 26),
					new Pair<Class<?>, Integer>(LinkSourceId.class, 27),
					new Pair<Class<?>, Integer>(LinkedHashMap.class, 28),
					new Pair<Class<?>, Integer>(EmfUser.class, 29), new Pair<Class<?>, Integer>(
							EmfGroup.class, 30),
					new Pair<Class<?>, Integer>(ResourceType.class, 31)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<>(CLASS_REGISTER));
	}

}
