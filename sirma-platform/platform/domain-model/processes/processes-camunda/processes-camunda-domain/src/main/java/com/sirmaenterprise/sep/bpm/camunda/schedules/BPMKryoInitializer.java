package com.sirmaenterprise.sep.bpm.camunda.schedules;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;
import com.sirmaenterprise.sep.bpm.camunda.schedule.BPMScheduleWrapperEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds Kryo configuration for the BPM module. Registers {@link com.sirma.itt.seip.event.EmfEvent} for serialization.
 *
 * @author hlungov
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 10000)
public class BPMKryoInitializer implements KryoInitializer {

	/**
	 * Contains the classes to be registered.
	 */
	private static final Set<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedHashSet<Pair<Class<?>, Integer>>(
			Arrays.asList(new Pair<Class<?>, Integer>(AfterOperationExecutedEvent.class, 10000), new Pair<Class<?>, Integer>(BPMScheduleWrapperEvent.class, 10001)));

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Collections.unmodifiableList(new ArrayList<Pair<Class<?>, Integer>>(CLASS_REGISTER));
	}

}
