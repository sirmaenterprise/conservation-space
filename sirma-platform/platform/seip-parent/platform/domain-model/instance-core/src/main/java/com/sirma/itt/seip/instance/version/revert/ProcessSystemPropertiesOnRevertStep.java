package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;

import java.util.Date;

import javax.inject.Inject;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles the transfer of the few system properties that should be with the same values as they are in the current
 * instance. The main properties that should be transfered are the version number, so that when the result instance is
 * saved, it can trigger the version creation with correct version. The other property that should be transfered to the
 * result instance is the id of the current instance, because the result instance is cloned from the version it has
 * version id, which is not correct. <br>
 * All system properties that should be transfered to the result instance should be done in this step.
 *
 * @author A. Kunchev
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 30)
public class ProcessSystemPropertiesOnRevertStep implements RevertStep {

	private static final String LAST_PUBLISHED_REVISION = "lastPublishedRevision";
	private static final String LAST_REVISION = "lastRevision";

	@Inject
	private DatabaseIdManager databaseIdManager;
	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public String getName() {
		return "processSystemProperties";
	}

	@Override
	public void invoke(RevertContext context) {
		Instance current = context.getCurrentInstance();
		Instance revertResultInstance = context.getRevertResultInstance();
		// unregister generated id, when the version was cloned
		databaseIdManager.unregisterId(revertResultInstance.getId());
		revertResultInstance.setId(current.getId());
		revertResultInstance.add(VERSION, current.get(VERSION, fieldConverter), fieldConverter);
		revertResultInstance.add(STATUS, current.get(STATUS));
		// When reverting a version we have to copy the revision properties from the current version. if we don't we
		// might overwrite an already existing revision.
		revertResultInstance.add(LAST_PUBLISHED_REVISION, current.get(LAST_PUBLISHED_REVISION));
		revertResultInstance.add(LAST_REVISION, current.get(LAST_REVISION));
		revertResultInstance.add(DefaultProperties.REVISION_NUMBER, current.get(DefaultProperties.REVISION_NUMBER, fieldConverter), fieldConverter);

		// prevents stale modification error, because it contains the one cloned from the version
		revertResultInstance.add(MODIFIED_ON, new Date());
	}

}
