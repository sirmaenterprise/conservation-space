package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.tasks.SchedulerService;

/**
 * Class responsible for scheduling the automatic scheduled operations
 *
 * @author BBonev
 */
class AutomaticSchedulerStartup {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AutomaticSchedulerCache schedulerCache;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private Instance<ConfigurationProvider> configurationProvider;

	/**
	 * Start schedule.
	 */
	// should be after the timed scheduler is initialized (order 10)
	@Startup(async = true, order = 1000)
	void startSchedule() {
		ConfigurationProvider provider = configurationProvider.get();
		for (SchedulerMethodCaller methodCaller : schedulerCache.getAll()) {
			try {
				methodCaller.schedule(schedulerService, provider);
			} catch (Exception e) {
				LOGGER.warn("Failed to schedule for automatic exection {}", methodCaller, e);
			}
		}
	}

}
