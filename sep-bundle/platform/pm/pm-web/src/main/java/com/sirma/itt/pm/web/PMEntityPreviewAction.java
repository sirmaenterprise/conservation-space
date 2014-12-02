package com.sirma.itt.pm.web;

import javax.enterprise.inject.Specializes;

import com.sirma.cmf.web.EntityPreviewAction;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.SecurityModel;

/**
 * The Class PMEntityPreviewAction.
 *
 * @author svelikov
 */
@Specializes
public class PMEntityPreviewAction extends EntityPreviewAction {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -245938531574201076L;

	/**
	 * Can open project.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	public boolean canOpenProject(Instance instance) {
		boolean canOpen = false;
		if (instance != null) {
			Boolean permission = authorityService.hasPermission(SecurityModel.PERMISSION_READ,
					instance, currentUser);
			if (Boolean.TRUE.equals(permission)) {
				canOpen = true;
			}
		}

		return canOpen;
	}

}
