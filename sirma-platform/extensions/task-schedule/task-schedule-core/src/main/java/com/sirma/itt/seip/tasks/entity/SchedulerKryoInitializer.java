package com.sirma.itt.seip.tasks.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.EventTrigger;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity.SchedulerData;

/**
 * Kryo initializer extension implementation to add the default specific scheduler entities to kryo engine.
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 10)
public class SchedulerKryoInitializer implements KryoInitializer {

	@SuppressWarnings("boxing")
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<>(Arrays.asList(
			new Pair<Class<?>, Integer>(DefaultSchedulerConfiguration.class, 223), new Pair<>(EventTrigger.class, 224),
			new Pair<>(SchedulerData.class, SchedulerData.CLASS_INDEX), new Pair<>(SchedulerContext.class, 225),
			new Pair<>(SchedulerEntryType.class, 226), new Pair<>(SchedulerEntryStatus.class, 227),
			new Pair<>(TransactionMode.class, 228), new Pair<>(RunAs.class, 229)));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<>(CLASS_REGISTER));
	}
}
