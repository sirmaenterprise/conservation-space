package com.sirma.cmf.web.caseinstance;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.mock.instance.RootInstanceContextMock;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.menu.NavigationMenuAction;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * CaseAction test class.
 * 
 * @author svelikov
 */
@Test
public class CaseActionTest extends CMFTest {

	/** The Constant TAB_1. */
	private static final String TAB_1 = "tab_1";

	/** Class under test. */
	private final CaseAction caseAction;

	/** The case service mock. */
	private CaseService caseService;

	/** The current operation that should be passed to backend service for given calls. */
	private Operation operation;

	/** The case search action. */
	private CaseSearchAction caseSearchAction;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The case instance. */
	private CaseInstance caseInstance;

	/** The case definition. */
	private CaseDefinition caseDefinition;

	/** The event service. */
	private EventService eventService;

	private InstanceService instanceService;

	/**
	 * Initializes the test.
	 */
	public CaseActionTest() {

		caseAction = new CaseAction() {

			private static final long serialVersionUID = 1297202454708628873L;

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}

			@Override
			public Operation createOperation() {
				if (operation == null) {
					operation = super.createOperation();
				}
				return operation;
			}

		};

		caseInstance = createCaseInstance(Long.valueOf(1));
		caseDefinition = createCaseDefinition("dmsid");
		caseService = Mockito.mock(CaseService.class);
		caseSearchAction = Mockito.mock(CaseSearchAction.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		eventService = Mockito.mock(EventService.class);
		instanceService = Mockito.mock(InstanceService.class);

		ReflectionUtils.setField(caseAction, "log", LOG);
		ReflectionUtils.setField(caseAction, "navigationMenuAction", new NavigationMenuAction());
		ReflectionUtils.setField(caseAction, "caseInstanceService", caseService);
		ReflectionUtils.setField(caseAction, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(caseAction, "caseSearchAction", caseSearchAction);
		ReflectionUtils.setField(caseAction, "eventService", eventService);
		ReflectionUtils.setField(caseAction, "instanceService", instanceService);

		Mockito.when(dictionaryService.getInstanceDefinition(caseInstance)).thenReturn(
				caseDefinition);
	}

	/**
	 * Clear context.
	 */
	@BeforeMethod
	public void clearContext() {
		caseAction.getDocumentContext().clear();
	}

	/**
	 * Creates the case test.
	 */
	public void createCaseTest() {
		EMFActionEvent event = createEventObject(null, null, null, null);
		caseAction.caseCreateNoContext(event);

		// test navigation
		assertEquals(event.getNavigation(), NavigationConstants.NAVIGATE_NEW_CASE);

		// test if navigation menu item is properly selected
		NavigationMenuAction navigationMenuAction = (NavigationMenuAction) ReflectionUtils
				.getField(caseAction, "navigationMenuAction");
		String selectedMenu = navigationMenuAction.getSelectedMenu();
		assertEquals(selectedMenu, NavigationConstants.NAVIGATE_MENU_CASE_LIST);
	}

	/**
	 * Save case test.
	 */
	public void saveCaseTest() {
		DocumentContext documentContext = caseAction.getDocumentContext();
		// test navigation
		String navigation = caseAction.saveInstance(caseInstance);
		assertEquals(navigation, NavigationConstants.NAVIGATE_TAB_CASE_DETAILS);

		// test if caseinstance is properly set in document context
		CaseInstance actualCaseInstance = documentContext.getInstance(CaseInstance.class);
		assertEquals(actualCaseInstance, caseInstance);

		Instance currentInstance = documentContext.getCurrentInstance();
		assertEquals(currentInstance, caseInstance);
	}

	/**
	 * Case edit test.
	 */
	public void caseEditTest() {
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));
		Map<String, Serializable> propertiesMap = new HashMap<String, Serializable>(1);
		propertiesMap.put(CaseProperties.TYPE, "CASE_TYPE");
		caseInstance.setProperties(propertiesMap);
		DocumentContext documentContext = caseAction.getDocumentContext();
		documentContext.clear();

		// if no instance is provided with the event, then the operation should not be executed and
		// the page should be reloaded
		EMFActionEvent event = createEventObject(null, null, null, null);
		caseAction.caseEdit(event);
		// if service can't find definition for given case, we should not have context populated
		assertTrue(documentContext.isEmpty());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		// if we provide instance but we don't find any definition we should not have context
		// populated
		event = createEventObject(null, caseInstance, null, null);
		caseAction.caseEdit(event);
		assertTrue(documentContext.isEmpty());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		caseInstance.setIdentifier("dmsid");
		Mockito.when(dictionaryService.getDefinition(CaseDefinition.class, "dmsid")).thenReturn(
				caseDefinition);
		caseAction.caseEdit(event);
		assertEquals(documentContext.getInstance(CaseInstance.class), caseInstance);
		assertEquals(documentContext.getDefinition(CaseDefinition.class), caseDefinition);
		assertEquals(documentContext.getCurrentInstance(), caseInstance);
		assertEquals(event.getNavigation(), NavigationConstants.NAVIGATE_TAB_CASE_DETAILS);
		assertEquals(caseAction.getSelectedType(), "CASE_TYPE");
	}

	/**
	 * Test cancel method.
	 */
	public void cancelSaveTest() {
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));

		String navigation = null;
		navigation = caseAction.cancelEdit(caseInstance);
		assertEquals(navigation, NavigationConstants.BACKWARD);

		//
		assertNull(caseAction.getSelectedType());
	}

	/**
	 * Test close method.
	 */
	public void closeCaseTest() {
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));
		Map<String, Serializable> propertiesMap = new HashMap<String, Serializable>();
		caseInstance.setProperties(propertiesMap);
		EMFActionEvent event = createEventObject(null, caseInstance, null, null);
		DocumentContext documentContext = caseAction.getDocumentContext();
		documentContext.addInstance(caseInstance);
		caseAction.caseClose();

		// if closing reason was not set, then we should not perform any action and page should be
		// reloaded
		Assert.assertEquals(NavigationConstants.RELOAD_PAGE, event.getNavigation());

		// if closing reason field is set, then service is called and at the end the closing reason
		// is cleared
		caseAction.setCaseClosingReason("closing reason");
		documentContext.setCurrentOperation(caseInstance.getClass().getSimpleName(),
				ActionTypeConstants.STOP);
		caseAction.caseClose();
		// check if service is actually invoked once
		Mockito.verify(caseService, Mockito.times(1)).closeCaseInstance(caseInstance, operation);
		// check if closing reason field is cleared
		assertNull(caseAction.getCaseClosingReason());
	}

	/**
	 * Test delete method.
	 */
	public void deleteCaseTest() {
		DocumentContext documentContext = caseAction.getDocumentContext();

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));

		Instance projectInstance = new RootInstanceContextMock();
		projectInstance.setId(1L);

		EMFActionEvent event = createEventObject(null, caseInstance, null, null);

		// if current instance is not set in document context: expect navigation=null and
		caseAction.caseDelete(event);
		String navigation = event.getNavigation();
		assertEquals(navigation, NavigationConstants.RELOAD_PAGE);
		Mockito.verify(caseService, Mockito.atLeastOnce()).delete(caseInstance, operation, false);

		// if current instance is set in document context and is case instance and context is
		// project instance: expect after case delete to be returned to project dashboard and the
		// project to be populated in document context
		documentContext.setCurrentInstance(caseInstance);
		documentContext.addContextInstance(projectInstance);
		caseAction.caseDelete(event);
		navigation = event.getNavigation();
		assertEquals(navigation, NavigationConstants.PROJECT_DASHBOARD);
		Mockito.verify(caseService, Mockito.atLeastOnce()).delete(caseInstance, operation, false);
		assertEquals(documentContext.getCurrentInstance(), projectInstance);
		assertEquals(documentContext.getInstance(projectInstance.getClass()), projectInstance);

		// if current instance is set in document context and is project instance: expect after case
		// delete to stay on project dashboard
		documentContext.clear();
		documentContext.setCurrentInstance(projectInstance);
		caseAction.caseDelete(event);
		navigation = event.getNavigation();
		assertEquals(navigation, NavigationConstants.PROJECT_DASHBOARD);
		Mockito.verify(caseService, Mockito.atLeastOnce()).delete(caseInstance, operation, false);
		assertEquals(documentContext.getCurrentInstance(), projectInstance);
		assertEquals(documentContext.getInstance(projectInstance.getClass()), projectInstance);
	}

	/**
	 * Link case test.
	 */
	public void linkCaseTest() {
		EMFActionEvent event = createEventObject(null, null, null, null);
		caseAction.linkCase(event);
		DocumentContext documentContext = caseAction.getDocumentContext();
		assertNull(documentContext.getRootInstance());
		assertNull(documentContext.getInstance(CaseInstance.class));

		//
		event = createEventObject(null, caseInstance, null, null);
		caseAction.linkCase(event);
		assertEquals(documentContext.getInstance(CaseInstance.class), caseInstance);
		assertNull(documentContext.getRootInstance());
	}
}
