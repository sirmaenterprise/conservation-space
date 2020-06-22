package com.sirma.sep.definition.serialization.kryo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.BaseRegionDefinition;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.StateTransitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

/**
 * Extension that registers definitions to Kryo serialization engine.
 *
 * @author A. Kunchev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 100)
public class DefinitionsKryoInitializer implements KryoInitializer {

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(Arrays.asList(
				new Pair<>(BaseDefinition.class, 1),
				new Pair<>(FieldDefinitionImpl.class, 6), 
				new Pair<>(PropertyDefinitionProxy.class, 7),
				new Pair<>(RegionDefinitionImpl.class, 9), 
				new Pair<>(BaseRegionDefinition.class, 10),
				new Pair<>(ControlDefinitionImpl.class, 12),
				new Pair<>(ControlParamImpl.class, 13),
				new Pair<>(AllowedChildDefinitionImpl.class, 14), 
				new Pair<>(AllowedChildConfigurationImpl.class, 15),
				new Pair<>(TransitionDefinitionImpl.class, 19), 
				new Pair<>(ConditionDefinitionImpl.class, 20),
				new Pair<>(TransitionGroupDefinitionImpl.class, 37), 
				new Pair<>(GenericDefinitionImpl.class, 51),
				new Pair<>(StateTransitionImpl.class, 202)));
	}
}