package com.sirma.itt.seip.instance.version;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * {@link VersionStepsExecutor} implementation that just processes the given {@link VersionContext} via the defined
 * {@link VersionStep}s.
 *
 * @author A. Kunchev
 * @author BBonev
 */
@ApplicationScoped
public class VersionStepsExecutorImpl implements VersionStepsExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(VersionStep.TARGET_NAME)
	private Plugins<VersionStep> versionSteps;

	@Override
	public void execute(VersionContext context) {
		Objects.requireNonNull(context, "VersionContext arument cannot be null");
		String targetInstanceId = context.getTargetInstanceId();
		TimeTracker tracker = TimeTracker.createAndStart();
		for (VersionStep step : versionSteps) {
			tracker.begin();
			step.execute(context);
			LOGGER.trace("Version step [{}] for instance - [{}], was executed for - {} ms.", step.getName(),
					targetInstanceId, tracker.stop());
		}
		LOGGER.debug("New version for instance - {} was created for - {} ms.", targetInstanceId, tracker.stop());
	}

}
