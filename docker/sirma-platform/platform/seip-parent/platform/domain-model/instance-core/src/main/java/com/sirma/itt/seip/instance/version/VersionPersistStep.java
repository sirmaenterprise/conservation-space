package com.sirma.itt.seip.instance.version;

import javax.inject.Inject;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles version instances persist. This step should be executed after all changes that should be performed over the
 * version are done, like versioning of properties, etc.
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
		versionDao.persistVersion(context.getVersionInstance().orElseThrow(
				() -> new EmfRuntimeException("There is not version instance that could be persisted.")));
	}
}