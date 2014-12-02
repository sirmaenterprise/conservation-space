package com.sirma.itt.emf.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerServiceImpl.SchedulerData;
import com.sirma.itt.emf.serialization.kryo.KryoInitializer;

/**
 * Kryo initializer extension implementation to add the default specific scheduler entities to kryo
 * engine.
 *
 * @author BBonev
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 10)
public class SchedulerKryoInitializer implements KryoInitializer {

	/** The Constant CLASS_REGISTER. */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<Pair<Class<?>, Integer>>(
			Arrays.asList(new Pair<Class<?>, Integer>(DefaultSchedulerConfiguration.class, 223),
					new Pair<Class<?>, Integer>(EventTrigger.class, 224),
					new Pair<Class<?>, Integer>(SchedulerData.class, SchedulerData.CLASS_INDEX),
					new Pair<Class<?>, Integer>(SchedulerContext.class, 225),
					new Pair<Class<?>, Integer>(SchedulerEntryType.class, 226),
					new Pair<Class<?>, Integer>(SchedulerEntryStatus.class, 227)
					));
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<Pair<Class<?>, Integer>>(CLASS_REGISTER));
	}
}
