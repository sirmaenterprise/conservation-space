package com.sirma.itt.pm.web.project.cases;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.menu.NavigationMenuAction;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.pm.PMTest;

/**
 * The Class PMCaseActionTest.
 * 
 * @author svelikov
 */
@Test
public class PMCaseActionTest extends PMTest {

	/** The action. */
	private PMCaseAction action;

	/** The navigation menu action. */
	private NavigationMenuAction navigationMenuAction;

	/**
	 * Instantiates a new pM case action test.
	 */
	public PMCaseActionTest() {
		action = new PMCaseAction() {

			private static final long serialVersionUID = 7562345486279973163L;

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}
		};

		navigationMenuAction = Mockito.mock(NavigationMenuAction.class);

		ReflectionUtils.setField(action, "log", log);
		ReflectionUtils.setField(action, "navigationMenuAction", navigationMenuAction);
	}

	/**
	 * Case create in project test.
	 */
	public void caseCreateInProjectTest() {
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.CREATE_CASE, null);
		action.caseCreateInProject(event);

		Mockito.verify(navigationMenuAction, Mockito.atLeastOnce()).setSelectedMenu(
				Mockito.anyString());
		Assert.assertEquals(
				action.getDocumentContext().getCurrentOperation(CaseInstance.class.getSimpleName()),
				ActionTypeConstants.CREATE_CASE);
		Assert.assertEquals(event.getNavigation(), NavigationConstants.NAVIGATE_NEW_CASE);
	}

}
