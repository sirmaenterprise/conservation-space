package com.sirma.itt.seip.permissions;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.exception.NoPermissionsException;

/**
 * Handles permission validation for the target instance. If the instance is not new and current user does not have
 * permissions to make changes this step will cancel the save by throwing permission exception.
 * <p>
 * Note that this step should be executed first before any other that may change the target instance in some way.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 1)
public class ValidatePermissionsStep implements InstanceSaveStep {

	@Inject
	private DatabaseIdManager idManager;

	@Inject
	private InstanceAccessEvaluator accessEvaluator;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		Serializable id = instance.getId();
		if (idManager.isIdPersisted(id) && !accessEvaluator.canWrite(instance)) {
			throw new NoPermissionsException(id, "No write permissions!");
		}
	}

	@Override
	public String getName() {
		return "validatePermissions";
	}

}
