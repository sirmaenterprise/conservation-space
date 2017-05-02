package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static com.sirma.itt.seip.domain.util.VersionUtil.combine;
import static com.sirma.itt.seip.domain.util.VersionUtil.split;
import static java.lang.Math.incrementExact;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles versions incrementing for instances, before their save/update. This step is executed before the actual
 * instance save, so that the version is incremented correctly, when the save is executed. If the mode is not set to
 * {@link VersionMode#NONE}, the instance version will be incremented according to the mode that is set. The mode is
 * stored and retrieved from the save context.<br>
 * If the instance is new, the version should be initial and should not be incremented. If the property is missing, it
 * will be set automatically to initial.
 * <p>
 * If the mode is: <br>
 * <b>MINOR</b> - the minor part of the instance version will be increment by 1. <br />
 * <b>MAJOR</b> - the major part of the instance version will be increment by 1 and the minor part will be reset to 0.
 * <br>
 * <b>NONE</b> - instance version will be unchanged.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 30)
public class HandleVersionPropertyStep implements InstanceSaveStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int MINOR_VERSION_RESET_NUMBER = 0;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		Serializable instanceId = instance.getId();
		if (!instanceTypeResolver.resolveReference(instanceId).isPresent()) {
			LOGGER.trace("Instance with id - [{}] is new, the version should be initial."
					+ "It will be set, if the property is missing.", instanceId);
			instanceVersionService.setInitialVersion(instance);
			return;
		}

		switch (saveContext.getVersionContext().getVersionMode()) {
			case MINOR:
				incremntMinorVersion(instance);
				break;
			case MAJOR:
				incrementMajorVersion(instance);
				break;
			case NONE:
				LOGGER.trace("The version mode is set to [NONE] for instance - {},"
						+ " instance version will stay unchanged.", instanceId);
				break;
			default:
				break;
		}
	}

	private static void incremntMinorVersion(Instance instance) {
		IntegerPair version = split(instance.getString(VERSION));
		setVersion(instance, version.getFirst(), incrementExact(version.getSecond()));
	}

	private static void incrementMajorVersion(Instance instance) {
		IntegerPair version = split(instance.getString(VERSION));
		setVersion(instance, incrementExact(version.getFirst()), MINOR_VERSION_RESET_NUMBER);
	}

	private static void setVersion(Instance instance, int major, int minor) {
		String incrementedVersion = combine(major, minor);
		instance.add(VERSION, incrementedVersion);
		LOGGER.trace("Version for instance - [{}], was changed to - [{}].", instance.getId(), incrementedVersion);
	}

	@Override
	public String getName() {
		return "incrementInstanceVersion";
	}

}
