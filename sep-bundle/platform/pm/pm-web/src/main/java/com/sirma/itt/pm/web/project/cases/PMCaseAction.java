package com.sirma.itt.pm.web.project.cases;

import java.io.Serializable;

import javax.enterprise.event.Observes;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Action bean for actions performed on case under project context.
 * 
 * @author BBonev
 */
@ViewAccessScoped
public class PMCaseAction extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5758472782356822587L;

	/**
	 * Observer for create case action in project context.
	 * 
	 * @param event
	 *            The event payload object.
	 */
	public void caseCreateInProject(
			@Observes @EMFAction(value = ActionTypeConstants.CREATE_CASE, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer PMCaseAction.caseCreateInProject");

		// initialize context with the root (project instance)
		getDocumentContext().addInstance(event.getInstance());
		getDocumentContext().addContextInstance(event.getInstance());
		getDocumentContext().setRootInstance(event.getInstance());

		navigationMenuAction.setSelectedMenu(NavigationConstants.NAVIGATE_MENU_CASE_LIST);
		getDocumentContext().setCurrentOperation(CaseInstance.class.getSimpleName(),
				event.getActionId());
		event.setNavigation(NavigationConstants.NAVIGATE_NEW_CASE);
	}

}
