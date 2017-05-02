package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * Scheduler action that runs the automatic scheduler methods.
 *
 * @author BBonev
 */
@Singleton
@Named(AutomaticSchedulerAction.NAME)
class AutomaticSchedulerAction extends SchedulerActionAdapter {
	public static final String NAME = "automaticSchedulerAction";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AutomaticSchedulerCache schedulerCache;

	/**
	 * Configuration property that holds the executable name of type {@link String}
	 */
	public static final String EXECUTABLE = "executable";

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String name = context.getIfSameType(EXECUTABLE, String.class);
		SchedulerMethodCaller methodCaller = schedulerCache.getExecutable(name);
		if (methodCaller != null) {
			methodCaller.invoke(context);
		} else {
			LOGGER.warn("Tried to call non existent automatic scheduler operation with name " + name);
		}
	}
}
