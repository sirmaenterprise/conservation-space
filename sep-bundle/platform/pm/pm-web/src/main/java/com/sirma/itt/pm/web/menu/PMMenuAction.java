package com.sirma.itt.pm.web.menu;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;

/**
 * The Class PMMenuAction.
 * 
 * @author svelikov
 */
@Named
public class PMMenuAction extends Action {

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/**
	 * Render members menu.
	 * 
	 * @param projectInstance
	 *            the project instance
	 * @return true, if successful
	 */
	public boolean renderMembersMenu(ProjectInstance projectInstance) {
		boolean renderMenu = false;

		if (projectInstance != null) {
			renderMenu = authorityService.isActionAllowed(projectInstance,
					PmActionTypeConstants.MANAGE_RESOURCES, "");
		}

		return renderMenu;
	}
}
