package com.sirma.cmf.web;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.SecurityModel;

/**
 * The Class EntityPreviewAction.
 * 
 * @author svelikov
 */
// REVIEW: For optimization
@Named
@ViewAccessScoped
public class EntityPreviewAction extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8897545774899810272L;

	/** The authority service. */
	@Inject
	protected AuthorityService authorityService;

	/**
	 * Can open instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	public boolean canOpenInstance(Instance instance) {
		if (instance != null) {
			return authorityService.hasPermission(SecurityModel.PERMISSION_READ, instance,
					currentUser);
		}

		return false;
	}

	/**
	 * Check operation edit has permission for current task.
	 * 
	 * @param standaloneTask
	 *            standalone task
	 * @return true if has permission
	 */
	public boolean canEditStandaloneTask(AbstractTaskInstance standaloneTask) {
		if (standaloneTask != null) {
			return authorityService.hasPermission(SecurityModel.PERMISSION_EDIT, standaloneTask,
					currentUser);
		}
		return false;
	}

	/**
	 * If current user can open an item from the search result.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @return true, if successful
	 */
	public boolean canOpenCase(Instance caseInstance) {
		return canOpenInstance(caseInstance);
	}

	/**
	 * If current user can open an item from the search result.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @return true, if successful
	 */
	public boolean canOpenDocument(DocumentInstance documentInstance) {
		return false;
	}

	/**
	 * If current user can open selected task.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return true, if successful
	 */
	public boolean canOpenTask(AbstractTaskInstance taskInstance) {
		return canOpenInstance(taskInstance);
	}

	/**
	 * Can edit case is when current user doesn't have a role or is with role VIEWER or COLABORATOR.
	 * 
	 * @param instance
	 *            the case instance
	 * @return true, if successful
	 */
	public boolean canEditCase(CaseInstance instance) {
		return authorityService.hasPermission(SecurityModel.PERMISSION_EDIT, instance, currentUser);
	}

}
