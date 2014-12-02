package com.sirma.cmf.web.actionsmanager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.NullInstance;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * Allowed action test.
 * 
 * @author svelikov
 */
@Test
public class ActionsManagerTest extends CMFTest {

	private static final String DASHBOARD = "dashboard";

	private static final String CREATE_CASE = "createCase";

	/** The manager. */
	private final ActionsManager manager;

	private final List<EMFActionEvent> firedEvents = new ArrayList<EMFActionEvent>();

	private AuthorityService authorityService;

	private Instance caseInstance;

	/**
	 * Constructor initializes the class under test.
	 */
	public ActionsManagerTest() {

		manager = new ActionsManager() {

			private DocumentContext docContext = new DocumentContext();

			@Override
			protected EMFActionEvent fireActionEvent(Action action, String actionId,
					Instance instance, String navigation) {
				firedEvents.clear();
				EMFActionEvent event = new EMFActionEvent(instance, navigation, actionId, action);
				firedEvents.add(event);
				return event;
			}

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}

			@Override
			public Instance fetchInstance(Serializable instanceId, String instanceType) {
				caseInstance.setId(2l);
				return caseInstance;
			}
		};

		ReflectionUtils.setField(manager, "log", LOG);
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		manager.getDocumentContext().clear();
	}

	/**
	 * Execute context action test.
	 */
	public void executeContextActionTest() {
		String navigation = manager.executeContextAction(null, null, null);
		assertNull(navigation);
		assertNull(manager.getDocumentContext().getContextInstance());
		assertNull(manager.getDocumentContext().getSelectedAction());

		//
		navigation = manager.executeContextAction(CREATE_CASE, DASHBOARD, null);
		assertEquals(navigation, DASHBOARD);
		assertNull(manager.getDocumentContext().getContextInstance());
		assertNull(manager.getDocumentContext().getSelectedAction());
		String currentOperation = manager.getDocumentContext().getCurrentOperation(
				NullInstance.class.getSimpleName());
		assertEquals(currentOperation, CREATE_CASE);
	}

	/**
	 * Execute allowed action test.
	 */
	public void executeAllowedActionTest() {
		DocumentContext documentContext = manager.getDocumentContext();

		// for no 'delete' operation
		String operation = "approve";
		caseInstance = createCaseInstance(Long.valueOf(1L));
		EmfAction action = new EmfAction(operation);
		String navigation = manager.executeAllowedAction(action, caseInstance);
		// test navigation - should be null
		assertNull(navigation);
		// check if an event is fired as result of this method call
		assertFalse(firedEvents.isEmpty());
		// check fired event attributes
		EMFActionEvent event = firedEvents.get(0);
		assertEquals(event.getActionId(), operation);
		assertEquals(event.getInstance(), caseInstance);
		assertEquals(event.getNavigation(), null);
		assertEquals(event.getAction(), action);
		// check if executed action is stored in the context
		assertEquals(manager.getDocumentContext().getSelectedAction(), action);
		// check if in context is set current operation
		assertEquals(
				manager.getDocumentContext()
						.getCurrentOperation(CaseInstance.class.getSimpleName()), operation);
		// test if context is not updated because there is no current and context instance in
		// document context
		assertNull(documentContext.getCurrentInstance());
		assertNull(documentContext.getContextInstance());

		// 'delete' operation
		documentContext.clear();
		documentContext.addContextInstance(caseInstance);
		documentContext.setCurrentInstance(caseInstance);
		operation = "delete";
		caseInstance = createCaseInstance(Long.valueOf(1L));
		action = new EmfAction(operation);
		navigation = manager.executeAllowedAction(action, caseInstance);
		// test navigation - should be null
		assertNull(navigation);
		// check if an event is fired as result of this method call
		assertFalse(firedEvents.isEmpty());
		// check fired event attributes
		event = firedEvents.get(0);
		assertEquals(event.getActionId(), operation);
		assertEquals(event.getInstance(), caseInstance);
		assertEquals(event.getNavigation(), null);
		assertEquals(event.getAction(), action);
		// check if executed action is stored in the context
		assertEquals(manager.getDocumentContext().getSelectedAction(), action);
		// check if in context is set current operation
		assertEquals(
				manager.getDocumentContext()
						.getCurrentOperation(CaseInstance.class.getSimpleName()), operation);
		// test if context is updated because there is current and context instance in
		// document context
		assertNotNull(documentContext.getCurrentInstance());
		assertNotNull(documentContext.getContextInstance());
		assertEquals(documentContext.getCurrentInstance().getId(), 2l);
	}

	/**
	 * Test for getActionStyleClass method.
	 */
	public void getActionStyleClassTest() {
		String actual = manager.getActionStyleClass(null, null, null, null);
		assertEquals(actual, "allowed-action-button");

		actual = manager.getActionStyleClass("", null, null, "");
		assertEquals(actual, "allowed-action-button");

		actual = manager.getActionStyleClass("case-delete-btn", null, null, null);
		assertEquals(actual, "case-delete-btn");

		CaseInstance caseInstance = createCaseInstance(null);
		actual = manager.getActionStyleClass("case-delete-btn", caseInstance, null, null);
		assertEquals(actual, "caseinstance case-delete-btn");

		Action action = new EmfAction("deleteCase");
		actual = manager.getActionStyleClass("case-delete-btn", caseInstance, action, null);
		assertEquals(actual, "caseinstance case-delete-btn deleteCase");

		actual = manager.getActionStyleClass("case-delete-btn", caseInstance, action, "compact");
		assertEquals(actual, "caseinstance case-delete-btn deleteCase has-tooltip");
	}
}
