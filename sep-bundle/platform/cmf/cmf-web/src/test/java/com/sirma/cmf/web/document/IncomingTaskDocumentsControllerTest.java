package com.sirma.cmf.web.document;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.workflow.task.TaskDocument;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Test for IncomingTaskDocumentsController.
 * 
 * @author svelikov
 */
public class IncomingTaskDocumentsControllerTest extends CMFTest {

	private final IncomingTaskDocumentsController controller;
	private CaseDocumentsTableAction caseDocumentsTableAction;
	private CaseInstance caseInstance;
	private InstanceService instanceService;

	/**
	 * Instantiates a new incoming task documents controller test.
	 */
	public IncomingTaskDocumentsControllerTest() {
		controller = new IncomingTaskDocumentsController() {

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
			public List<TaskDocument> getIncomingDocuments() {
				return buildIncomingDocumentsList();
			}

			@Override
			protected boolean hasVisibleDocuments(SectionInstance sectionInstance) {
				return true;
			}

			@Override
			protected DocumentInstance findDocumentInCase(String dmsId) {
				if (dmsId == null) {
					return null;
				}
				return super.findDocumentInCase(dmsId);
			}
		};

		caseInstance = createCaseInstance(Long.valueOf(1L));

		caseDocumentsTableAction = Mockito.mock(CaseDocumentsTableAction.class);
		instanceService = Mockito.mock(InstanceService.class);

		ReflectionUtils.setField(controller, "instanceService", instanceService);
		ReflectionUtils.setField(controller, "caseDocumentsTableAction", caseDocumentsTableAction);
		ReflectionUtils.setField(controller, "log", LOG);
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		controller.getDocumentContext().clear();
	}

	/**
	 * Test for initBean method.
	 */
	@Test
	public void initBeanTest() {
		// if there is no case instance in context, we should not have any allowed sections
		// populated
		controller.initBean();
		List<SectionInstance> allowedSections = controller.getAllowedSections();
		assertNotNull(allowedSections);
		Assert.assertTrue(allowedSections.size() == 0);
		Mockito.verify(instanceService, Mockito.never()).refresh(Mockito.any(Instance.class));

		// if there is caseinstance in context but there is no sections, we should not have any
		// allowed sections populated
		List<SectionInstance> sections = new ArrayList<>();
		caseInstance.setSections(sections);
		controller.getDocumentContext().addInstance(caseInstance);
		controller.initBean();
		allowedSections = controller.getAllowedSections();
		assertNotNull(allowedSections);
		assertTrue(allowedSections.size() == 0);
		Mockito.verify(instanceService, Mockito.atLeastOnce()).refresh(Mockito.any(Instance.class));

		// if there are only objects sections, we should not have any allowed sections populated
		SectionInstance sectionInstance1 = createSectionInstance(Long.valueOf(1L));
		sectionInstance1.setPurpose("objectsSection");
		sections.add(sectionInstance1);
		SectionInstance sectionInstance2 = createSectionInstance(Long.valueOf(2L));
		sectionInstance2.setPurpose("objectsSection");
		sections.add(sectionInstance2);
		controller.initBean();
		allowedSections = controller.getAllowedSections();
		assertNotNull(allowedSections);
		assertTrue(allowedSections.size() == 0);

		// if there are sections and all sections are documents sections
		SectionInstance sectionInstance3 = createSectionInstance(Long.valueOf(3L));
		sections.add(sectionInstance3);
		SectionInstance sectionInstance4 = createSectionInstance(Long.valueOf(4L));
		sections.add(sectionInstance4);
		controller.initBean();
		allowedSections = controller.getAllowedSections();
		assertNotNull(allowedSections);
		assertTrue(allowedSections.size() == 2);
	}

	/**
	 * Test for open method.
	 */
	@Test
	public void openTest() {
		String navigationString = controller.open(null);
		assertNull(navigationString);
		assertNull(controller.getDocumentContext().getDocumentInstance());

		// there is no current caseinstance in context
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		documentInstance.setDmsId("dmsId");
		documentInstance.setId("emf:id");
		navigationString = controller.open(documentInstance);
		assertNull(navigationString);
		assertNull(controller.getDocumentContext().getDocumentInstance());

		// there is case instance in context but the document is not inside
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		controller.getDocumentContext().addInstance(caseInstance);
		navigationString = controller.open(documentInstance);
		assertNull(navigationString);
		assertNull(controller.getDocumentContext().getDocumentInstance());

		// document is found in case
		Mockito.when(caseDocumentsTableAction.open(documentInstance))
				.thenReturn("navigationstring");
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		sectionInstance.getContent().add(documentInstance);
		caseInstance.getSections().add(sectionInstance);
		navigationString = controller.open(documentInstance);
		assertEquals(controller.getDocumentContext().getDocumentInstance(), documentInstance);
		assertEquals(navigationString, "navigationstring");
	}

	/**
	 * Test for getRowClasses method.
	 */
	@Test
	public void getRowClassesTest() {
		// no case instance in context
		String rowClasses = controller.getRowClasses();
		assertTrue(rowClasses.isEmpty());

		// there is case instance but no documents
		controller.getDocumentContext().addInstance(caseInstance);
		rowClasses = controller.getRowClasses();
		assertTrue(rowClasses.isEmpty());

		// there is case instance and documents in one section and no documents in other
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		sectionInstance.getContent().add(documentInstance);
		caseInstance.getSections().add(sectionInstance);
		SectionInstance sectionInstance2 = createSectionInstance(Long.valueOf(2L));
		caseInstance.getSections().add(sectionInstance2);
		rowClasses = controller.getRowClasses();
		assertEquals(rowClasses, ",pad-0,pad-0 empty-row");
	}

	/**
	 * Test for isLinked method.
	 */
	@Test
	public void isLinkedTest() {
		boolean linked = controller.isLinked("emf:id");
		assertTrue(linked);
		linked = controller.isLinked("dmsid-notlinked");
		assertFalse(linked);
	}

	/**
	 * Test for isLinked method.
	 */
	@Test
	public void isLinked2Test() {
		boolean linked = controller.isLinked(buildIncomingDocumentsList(), "emf:id");
		assertTrue(linked);
		linked = controller.isLinked(buildIncomingDocumentsList(), "dmsid-notlinked");
		assertFalse(linked);
	}

	/**
	 * Update selection test.
	 */
	@Test
	public void updateSelectionTest() {
		controller.updateSelection(null);
		List<DocumentInstance> selectedDocuments = controller.getSelectedDocuments();
		assertTrue(selectedDocuments.isEmpty());

		// try to add document instance to selection
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		controller.updateSelection(documentInstance);
		selectedDocuments = controller.getSelectedDocuments();
		assertTrue(selectedDocuments.size() == 1);

		// next try with same instance should remove the instance from selection
		controller.updateSelection(documentInstance);
		selectedDocuments = controller.getSelectedDocuments();
		assertTrue(selectedDocuments.size() == 0);

		// trying with different instances should lead to adding them all to selection
		controller.updateSelection(documentInstance);
		DocumentInstance documentInstance2 = createDocumentInstance(Long.valueOf(2L));
		controller.updateSelection(documentInstance2);
		selectedDocuments = controller.getSelectedDocuments();
		assertTrue(selectedDocuments.size() == 2);

		//
		controller.updateSelection(documentInstance);
		selectedDocuments = controller.getSelectedDocuments();
		assertTrue(selectedDocuments.size() == 1);
	}

	/**
	 * Checks for uploaded documents test.
	 */
	@Test
	public void hasUploadedDocumentsTest() {
		boolean hasUploadedDocuments = controller.hasUploadedDocuments();
		assertFalse(hasUploadedDocuments);

		// there is a caseinstance but no documents
		controller.getDocumentContext().addInstance(caseInstance);
		hasUploadedDocuments = controller.hasUploadedDocuments();
		assertFalse(hasUploadedDocuments);

		// there are documents but not linked once
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		sectionInstance.getContent().add(documentInstance);
		caseInstance.getSections().add(sectionInstance);
		hasUploadedDocuments = controller.hasUploadedDocuments();
		assertFalse(hasUploadedDocuments);
	}

	/**
	 * Adds the incoming documents test.
	 */
	// @Test
	// TODO: implement
	public void addIncomingDocumentsTest() {
		controller.addIncomingDocuments();
	}

	/**
	 * Gets the incoming documents test.
	 */
	// TODO: implement
	// @Test
	public void getIncomingDocumentsTest() {
		controller.getIncomingDocuments();
	}

	/**
	 * Removes the incoming document test.
	 */
	// TODO: implement
	// @Test
	public void removeIncomingDocumentTest() {
		controller.removeIncomingDocument(null);
	}

	/**
	 * Test for showAvailableDocuments.
	 */
	@Test
	public void showAvailableDocumentsTest() {
		controller.showAvailableDocuments();
		List<DocumentInstance> selectedDocuments = controller.getSelectedDocuments();
		assertNotNull(selectedDocuments);
		assertTrue(selectedDocuments.isEmpty());
	}

	/**
	 * Builds the incoming documents list.
	 * 
	 * @return the list
	 */
	private List<TaskDocument> buildIncomingDocumentsList() {
		List<TaskDocument> list = new ArrayList<TaskDocument>();
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		documentInstance.setDmsId("dmsid");
		documentInstance.setId("emf:id");
		TaskDocument taskDocument = new TaskDocument();
		taskDocument.setDocumentInstance(documentInstance);
		list.add(taskDocument);
		return list;
	}

}
