package com.sirma.itt.seip.instance.version;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles version saving for the current instance. Before phase of the step handles the transferring of the search
 * queries in to context. The version is created in the after phase of this step, if the mode is not set to
 * {@link VersionMode#NONE} and the current instance is versionable (handled by {@link InstanceVersionService}).
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 45)
public class SaveInstanceVersionStep implements InstanceSaveStep {

	public static final String VERSION_INSTANCE_ID = "versionInstanceId";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceVersionService instanceVersionService;

	@Override
	public void afterSave(InstanceSaveContext saveContext) {
		VersionContext versionContext = saveContext.getVersionContext();
		if (VersionMode.NONE.equals(versionContext.getVersionMode())) {
			LOGGER.trace("Version mode is [NONE] for instance - {}. Skipping version creation.",
					saveContext.getInstanceId());
			return;
		}

		instanceVersionService.saveVersion(versionContext);
		saveContext.setPropertyIfNotNull(VERSION_INSTANCE_ID, versionContext.getVersionInstanceId());
	}

	@Override
	public String getName() {
		return "saveInstanceVersion";
	}
}