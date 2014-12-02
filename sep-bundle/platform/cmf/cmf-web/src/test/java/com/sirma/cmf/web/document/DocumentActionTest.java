package com.sirma.cmf.web.document;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * The Class DocumentActionTest.
 * 
 * @author svelikov
 */
@Test
public class DocumentActionTest extends CMFTest {

	/** The document location path, represent as string. */
	private static final String DOCUMENT_LOCATION_PATH = "document_location_path";

	/** The document version, represent as string. */
	private static final String DOCUMENT_VERSION = "1.1";

	private DocumentAction action;

	private DictionaryService dictionaryService;

	private DocumentInstance documentInstance;

	private DefinitionModel documentDefinition;

	private DocumentService documentService;

	private boolean readerInvoked;

	private CaseDocumentsTableAction caseDocumentsTableAction;

	protected InstanceService instanceService;

	/**
	 * Instantiates a new document action test.
	 */
	public DocumentActionTest() {

		action = new DocumentAction() {

			/** Comment for serialVersionUID. */
			private static final long serialVersionUID = -699653712730148032L;

			/**
			 * Comment for docContext.
			 */
			private DocumentContext docContext = new DocumentContext();

			@Override
			protected void invokeReader(RegionDefinitionModel definition, Instance instance,
					UIComponent panel, FormViewMode formViewMode, String rootInstanceName) {
				readerInvoked = true;
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
			protected UIComponent getFormPanel() {
				return new HtmlPanelGroup();
			}
		};

		documentInstance = createDocumentInstance(Long.valueOf(1));
		documentDefinition = createDocumentDefinition("identifier");
		dictionaryService = Mockito.mock(DictionaryService.class);
		documentService = Mockito.mock(DocumentService.class);
		caseDocumentsTableAction = Mockito.mock(CaseDocumentsTableAction.class);
		instanceService = Mockito.mock(InstanceService.class);

		Mockito.when(documentService.getContentURI(documentInstance)).thenReturn(
				DOCUMENT_LOCATION_PATH);

		// set fields
		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "documentService", documentService);
		ReflectionUtils.setField(action, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(action, "caseDocumentsTableAction", caseDocumentsTableAction);
		ReflectionUtils.setField(action, "instanceService", instanceService);
	}

	/**
	 * Inits the test.
	 */
	@BeforeMethod
	public void initTest() {
		action.getDocumentContext().clear();
		readerInvoked = false;
	}

	/**
	 * Test method for download historical version of document.
	 */
	public void downloadHistoricalDocVersionTest() {

		DocumentInstance docInstance = createDocumentInstance(Long.valueOf(3));
		documentInstance.getProperties().put(DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE,
				docInstance);

		Mockito.when(documentService.getDocumentVersion(docInstance, DOCUMENT_VERSION)).thenReturn(
				documentInstance);

		String string = action.downloadHistoricalDocVersion(docInstance, DOCUMENT_VERSION);
		Assert.assertTrue(StringUtils.isNullOrEmpty(string));
	}

	/**
	 * Test method for supported download path to document.
	 */
	public void downloadDocumentFailTest() {
		Assert.assertNull(action.getDocumentDownloadUrl(documentInstance));
	}

	/**
	 * Download test.
	 */
	public void downloadTest() {
		// test navigation
		EMFActionEvent event = createEventObject(null, createDocumentInstance(Long.valueOf(1)),
				null, null);
		action.editOffline(event);

		assertEquals(NavigationConstants.RELOAD_PAGE, event.getNavigation());
	}

	/**
	 * Test for method for edit document properties.
	 */
	public void editDocumentPropertiesTest() {
		DocumentInstance selectedDocumentInstance = createDocumentInstance(Long.valueOf(1));

		EMFActionEvent event = createEventObject(null, selectedDocumentInstance, null, null);
		Mockito.when(dictionaryService.getInstanceDefinition(documentInstance)).thenReturn(
				documentDefinition);
		action.editDocumentProperties(event);

		// test navigation
		assertEquals(NavigationConstants.NAVIGATE_DOCUMENT_EDIT_PROPERTIES, event.getNavigation());

		// test if document instance is properly set in document context
		DocumentInstance appliedDocumentInstance = action.getDocumentContext()
				.getDocumentInstance();
		assertEquals(appliedDocumentInstance, documentInstance);

		// test if definition is properly set in document context
		DocumentDefinitionRefImpl appliedDocumentDefinition = (DocumentDefinitionRefImpl) action
				.getDocumentContext().get(DocumentContext.DOCUMENT_DEFINITION);
		assertEquals(appliedDocumentDefinition, documentDefinition);
	}

	/**
	 * Save document properties test.
	 */
	public void saveDocumentPropertiesTest() {
		// if no instance is provided, we expect services to not be invoked
		action.saveDocumentProperties(null);
		Mockito.verify(documentService, Mockito.never()).updateProperties(
				Mockito.any(DocumentInstance.class));
	}

	/**
	 * test for cancel edit method.
	 */
	public void cancelEditTest() {
		// test navigation
		EMFActionEvent event = createEventObject(null, null, null, null);
		action.cancelDocumentEdit(event);

		assertEquals(NavigationConstants.RELOAD_PAGE, event.getNavigation());
	}

	/**
	 * Test for method that initializes a form for the selected document instance.
	 */
	public void initDocumentPropertiesFormTest() {
		// if no instance and definition exists in context, we expect definition reader to not be
		// invoked
		action.initDocumentPropertiesForm();
		assertFalse(readerInvoked);

		// if we have instance but not definition in context, we expect definition reader to not be
		// invoked
		initTest();
		action.getDocumentContext().addInstance(documentInstance);
		action.initDocumentPropertiesForm();
		assertFalse(readerInvoked);

		// if we have definition but not instance in context, we expect definition reader to not be
		// invoked
		initTest();
		action.getDocumentContext().addDefinition(DefinitionModel.class, documentDefinition);
		action.initDocumentPropertiesForm();
		assertFalse(readerInvoked);

		// we have instance and definition in context
		initTest();
		action.getDocumentContext().setDocumentInstance(documentInstance);
		action.getDocumentContext().setDocumentDefinition(
				(DocumentDefinitionRef) documentDefinition);
		action.initDocumentPropertiesForm();
		assertTrue(readerInvoked);
	}

	/**
	 * Test for observer method for edit offline document operation.
	 */
	public void editOfflineTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "editOffline",
				new EmfAction("editOffline"));
		action.editOffline(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).checkOut(documentInstance);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Edits the online test.
	 */
	public void editOnlineTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "editOnline",
				new EmfAction("editOnline"));
		action.editOnline(event);
		Mockito.verify(caseDocumentsTableAction, Mockito.atLeastOnce()).openForEdit(
				documentInstance);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Delete test.
	 */
	public void deleteTest() {
		CaseInstance caseInstance = createCaseInstance(1l);
		DocumentContext documentContext = action.getDocumentContext();
		documentContext.clear();
		EMFActionEvent event = createEventObject(null, documentInstance, "delete", new EmfAction(
				"delete"));

		// - there is no document instance in context that means we are not on the document landing
		// page
		// - there is no context instance in document context: suppose we are on the user dashboard
		// - the service succeeds with delete operation
		// - expecting: no context and current instance to be set in context and navigation to be
		// null(reload)
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(true);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNull(documentContext.getCurrentInstance());
		assertNull(documentContext.getContextInstance());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		// same as above but the service respond that instance was not deleted
		documentContext.clear();
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(false);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNull(documentContext.getCurrentInstance());
		assertNull(documentContext.getContextInstance());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		// - there is no document instance in context that means we are not on the document landing
		// page
		// - there is a context instance in document context: suppose we are on the case dashboard
		// - the service succeeds with delete operation
		// - expecting: the case to be set as context and current instance and navigation to be
		// null(reload)
		documentContext.clear();
		documentContext.addContextInstance(caseInstance);
		documentContext.setCurrentInstance(caseInstance);
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(true);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNotNull(documentContext.getCurrentInstance());
		assertNotNull(documentContext.getContextInstance());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		// same as above but the service respond that instance was not deleted
		documentContext.clear();
		documentContext.addContextInstance(caseInstance);
		documentContext.setCurrentInstance(caseInstance);
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(false);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNotNull(documentContext.getCurrentInstance());
		assertNotNull(documentContext.getContextInstance());
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);

		// - there is document instance in context that means we are on the document landing page
		// - there is a context instance case in document context
		// - the service succeeds with delete operation
		// - expecting: the case to be set as context and current instance and navigation to be
		// BACKWARD
		documentContext.clear();
		documentContext.setDocumentInstance(documentInstance);
		documentContext.setDocumentDefinition((DocumentDefinitionRef) documentDefinition);
		documentContext.addContextInstance(caseInstance);
		documentContext.setCurrentInstance(caseInstance);
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(true);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNull(documentContext.getDocumentDefinition());
		assertNull(documentContext.getDocumentInstance());
		assertEquals(documentContext.getInstance(CaseInstance.class), caseInstance);
		assertEquals(documentContext.getCurrentInstance(), caseInstance);
		assertEquals(event.getNavigation(), NavigationConstants.BACKWARD);

		// same as above but the service respond that instance was not deleted
		documentContext.clear();
		documentContext.setDocumentInstance(documentInstance);
		documentContext.setDocumentDefinition((DocumentDefinitionRef) documentDefinition);
		documentContext.addContextInstance(caseInstance);
		documentContext.setCurrentInstance(caseInstance);
		Mockito.when(documentService.deleteDocument(documentInstance)).thenReturn(false);
		action.delete(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).deleteDocument(documentInstance);
		assertNotNull(documentContext.getDocumentDefinition());
		assertNotNull(documentContext.getDocumentInstance());
		assertEquals(documentContext.getInstance(CaseInstance.class), caseInstance);
		assertEquals(documentContext.getCurrentInstance(), caseInstance);
		assertEquals(event.getNavigation(), NavigationConstants.BACKWARD);
	}

	/**
	 * Cancel document edit test.
	 */
	public void cancelDocumentEditTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "cancelEdit",
				new EmfAction("cancelEdit"));
		action.cancelDocumentEdit(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).cancelCheckOut(documentInstance);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Document lock test.
	 */
	public void documentLockTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "lock", new EmfAction(
				"lock"));
		action.documentLock(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).lock(documentInstance);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Document unlock test.
	 */
	public void documentUnlockTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "unlock", new EmfAction(
				"unlock"));
		action.documentUnlock(event);
		Mockito.verify(documentService, Mockito.atLeastOnce()).unlock(documentInstance);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	// TODO: implement tests
	//
	// /**
	// * Render document fields list test.
	// */
	// public void renderDocumentFieldsListTest() {
	//
	// }
	//
	// /**
	// * Document preview url test.
	// */
	// public void getDocumentPreviewURLTest() {
	//
	// }
	//
	// /**
	// * Document preview path test.
	// */
	// public void getDocumentPreviewPathTest() {
	//
	// }
	//
	// /**
	// * Share link test.
	// */
	// public void getShareLinkTest() {
	//
	// }
	//
	// /**
	// * Checks if is allowed operation test.
	// */
	// public void isAllowedOperationTest() {
	//
	// }
	//
	//
	// /**
	// * Retrieve document type test.
	// */
	// public void retrieveDocumentTypeTest() {
	//
	// }

	/**
	 * Upload new version test.
	 */
	public void uploadNewVersionTest() {
		EMFActionEvent event = createEventObject(null, documentInstance, "uploadNewVersion",
				new EmfAction("uploadNewVersion"));
		action.uploadNewVersion(event);
		assertEquals(event.getNavigation(), NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * getHistoricalDocVersion test.
	 */
	public void getHistoricalDocVersionTest() {
		String version = "2.0";
		DocumentInstance docInstance = createDocumentInstance(Long.valueOf(3));
		documentInstance.getProperties().put(DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE,
				docInstance);

		Mockito.when(documentService.getDocumentVersion(docInstance, version)).thenReturn(
				documentInstance);

		String navigationString = action.getHistoricalDocVersion(docInstance, version);
		assertEquals(action.getDocumentContext().get(DocumentConstants.DOCUMENT_LATEST_VERSION),
				docInstance);
		Mockito.verify(caseDocumentsTableAction, Mockito.atLeastOnce()).open(
				Mockito.eq(documentInstance));
		// assertEquals(docInstance.isHistoryInstance(), true);
		assertEquals(NavigationConstants.RELOAD_PAGE, navigationString);

	}

	/**
	 * getLatestDocVersion test.
	 */
	public void getLatestDocVersionTest() {
		String navigationString = action.getLatestDocVersion();
		assertEquals(navigationString, NavigationConstants.RELOAD_PAGE);
		assertEquals(action.getDocumentContext().get(DocumentConstants.DOCUMENT_LATEST_VERSION),
				null);
	}

	/**
	 * Test for method that checks if a document instance is image.
	 */
	public void isDocumentImageTest() {
		// we don't have instance in context so we should get false as result
		boolean isImage = action.isDocumentImage();
		assertFalse(isImage);

		// we have instance in context but there is no mimetype set and we expect false as result
		action.getDocumentContext().addInstance(documentInstance);
		isImage = action.isDocumentImage();
		assertFalse(isImage);

		// we have instance with mimetype different than image and we expect false as result
		documentInstance.getProperties().put(DocumentProperties.MIMETYPE, "text/xml");
		isImage = action.isDocumentImage();
		assertFalse(isImage);

		// we have instance with image mimetype
		documentInstance.getProperties().put(DocumentProperties.MIMETYPE, "image/png");
		isImage = action.isDocumentImage();
		assertTrue(isImage);
	}

	/**
	 * Test for method that checks if a document instance is a scene.
	 */
	public void isDocumentSceneTest() {
		// we don't have instance in context so we should get false as result
		boolean isScene = action.isDocumentScene();
		assertFalse(isScene);

		// we have instance in context but there is no mimetype set and we expect false as result
		action.getDocumentContext().addInstance(documentInstance);
		isScene = action.isDocumentScene();
		assertFalse(isScene);

		// we have instance with mimetype different than application and we expect false as result
		documentInstance.getProperties().put(DocumentProperties.MIMETYPE, "text/xml");
		isScene = action.isDocumentScene();
		assertFalse(isScene);

		// we have instance with application mimetype
		documentInstance.getProperties().put(DocumentProperties.MIMETYPE,
				"application/octet-stream");
		isScene = action.isDocumentScene();
		assertTrue(isScene);
	}
}
