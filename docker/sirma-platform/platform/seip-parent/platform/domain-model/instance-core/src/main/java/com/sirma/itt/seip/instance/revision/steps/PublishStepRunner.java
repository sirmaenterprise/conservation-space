package com.sirma.itt.seip.instance.revision.steps;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Invoker class that provides means to run {@link PublishStep}
 *
 * @author BBonev
 */
@Singleton
public class PublishStepRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	@ExtensionPoint(PublishStep.EXTENSION_NAME)
	private Plugins<PublishStep> steps;

	/**
	 * Builds a {@link StepRunner} that will execute the given steps
	 *
	 * @param stepNames
	 *            to call in order if found
	 * @return a runner instance that can execute the given names
	 */
	public StepRunner getRunner(String[] stepNames) {
		return context -> {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Executing steps: {}", Arrays.asList(stepNames));
			}
			for (String name : stepNames) {
				steps.get(name).ifPresent(step -> {
					LOGGER.trace("Executing step: ", step.getName());
					step.execute(context);
				});
			}
			LOGGER.trace("Completed step execution");
		};
	}

	/**
	 * Publish step executor. Runs steps using the provided context
	 *
	 * @author BBonev
	 */
	public interface StepRunner {
		/**
		 * Run configured steps using the context
		 *
		 * @param context
		 *            to use when calling the steps
		 */
		void run(PublishContext context);
	}
}
