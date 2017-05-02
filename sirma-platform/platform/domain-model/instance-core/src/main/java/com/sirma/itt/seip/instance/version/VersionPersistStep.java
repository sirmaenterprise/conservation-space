package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles the instances persist. This step should be executed after all the information needed before the save is
 * available in the context. This step should transfer all the information needed in the instance from the context and
 * then save the updated instance.
 *
 * @author A. Kunchev
 */
@Extension(target = VersionStep.TARGET_NAME, enabled = true, order = 15)
public class VersionPersistStep implements VersionStep {

	@Inject
	private VersionDao versionDao;

	@Override
	public String getName() {
		return "versionPersist";
	}

	@Override
	public void execute(VersionContext context) {
		Instance instance = context.getTargetInstance();
		instance.add(VERSION_CREATED_ON, context.getCreationDate());
		ArchivedInstance persistedVersion = versionDao.persistVersion(instance);
		// this instance is needed for contents mapping and thumbnail copy
		context.setVersionInstance(persistedVersion);
	}

}
