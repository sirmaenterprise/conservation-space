package com.sirma.cmf.web.document;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.EntityOpenDispatcher;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Test for DocumentMoveAction.
 * 
 * @author svelikov
 */
@Test
public class DocumentMoveActionTest extends CMFTest {

	/** The action. */
	private final DocumentMoveAction action;

	private boolean invokedMoveToOtherCase;
	private boolean invokedMoveToOtherSection;

	private SectionService sectionService;

	private DocumentService documentService;

	private EntityOpenDispatcher entityOpenDispatcher;

	/**
	 * Instantiates a new document move action test.
	 */
	public DocumentMoveActionTest() {

		action = new DocumentMoveAction() {
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
			protected void reloadDocumentInstance() {
				// do nothing
			}

			@Override
			protected void reloadCaseInstance() {
				// do nothing
			}
			
			@Override
			protected String getParameterByName(String name) {
				return name;
			}

			@Override
			protected String moveToOtherCase() {
				invokedMoveToOtherCase = true;
				return super.moveToOtherCase();
			}
		};

		sectionService = Mockito.mock(SectionService.class);
		documentService = Mockito.mock(DocumentService.class);
		entityOpenDispatcher = Mockito.mock(EntityOpenDispatcher.class);

		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "sectionService", sectionService);
		ReflectionUtils.setField(action, "documentService", documentService);
		ReflectionUtils.setField(action, "entityOpenDispatcher", entityOpenDispatcher);
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		invokedMoveToOtherCase = false;
		invokedMoveToOtherSection = false;
		action.setSelectedDocumentForMove(null);
		action.setSelectedSectionInstance(null);
		action.setSelectedTargetSectionId(null);
		action.setAction(null);
	}

	/**
	 * Toggle target test.
	 */
	public void toggleTargetTest() {
		action.toggleTarget(null);
		assertNull(action.getSelectedSectionInstance());
		assertNull(action.getSelectedTargetSectionId());

		// if passed section is same as one previously selected
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		action.setSelectedSectionInstance(sectionInstance);
		action.toggleTarget(sectionInstance);
		assertNull(action.getSelectedSectionInstance());
		assertNull(action.getSelectedTargetSectionId());

		// if passed section is different from the stored one
		SectionInstance sectionInstance2 = createSectionInstance(Long.valueOf(2L));
		action.toggleTarget(sectionInstance2);
		assertEquals(action.getSelectedSectionInstance(), sectionInstance2);
		assertEquals(action.getSelectedTargetSectionId(), sectionInstance2.getIdentifier());
	}

	/**
	 * Move to other case test.
	 */
	public void moveToOtherCaseTest() {
		//String navigation = action.moveToOtherCase();
		//assertEquals(navigation, NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS);

		// document not moved
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		sectionInstance.setOwningInstance(caseInstance);
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		Mockito.when(sectionService.loadByDbId(sectionInstance.getId()))
				.thenReturn(sectionInstance);
		
		Mockito.when(documentService.loadByDbId(documentInstance.getId()))
		.thenReturn(documentInstance);
		System.out.println(" 1 " + documentInstance);
		Mockito.when(documentService.moveDocument(documentInstance, sectionInstance)).thenReturn(
				Boolean.FALSE);
		action.setSelectedSectionInstance(sectionInstance);
		action.setSelectedDocumentForMove(documentInstance);
		String navigation = action.moveToOtherCase();
		assertEquals(navigation, NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS);
		Mockito.verify(documentService, Mockito.atLeastOnce()).moveDocument(documentInstance,
				sectionInstance);

		// document moved successfully
		Mockito.when(documentService.moveDocument(documentInstance, sectionInstance)).thenReturn(
				Boolean.TRUE);
		action.moveToOtherCase();
		assertEquals(navigation, NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS);
		Mockito.verify(documentService, Mockito.atLeastOnce()).moveDocument(documentInstance,
				sectionInstance);
		Mockito.verify(entityOpenDispatcher, Mockito.atLeastOnce()).openInternal(sectionInstance,
				null);
	}

	/**
	 * Initialize test.
	 */
	// TODO: implement
	// public void initializeTest() {
	// }

	/**
	 * test for isCurrent method.
	 */
	public void isCurrentTest() {
		boolean current = action.isCurrent(null);
		assertFalse(current);

		//
		SectionInstance sectionInstance = createSectionInstance(Long.valueOf(1L));
		current = action.isCurrent(sectionInstance);
		assertFalse(current);

		//
		action.setSelectedTargetSectionId(sectionInstance.getIdentifier());
		current = action.isCurrent(sectionInstance);
		assertTrue(current);
	}

	/**
	 * Test for canHandle method.
	 */
	public void canHandleTest() {
		boolean canHandle = action.canHandle(null);
		assertFalse(canHandle);
		assertNull(action.getAction());

		// pass action that can not be handled by this controller
		Action someAction = new EmfAction(ActionTypeConstants.ADD_THUMBNAIL);
		canHandle = action.canHandle(someAction);
		assertFalse(canHandle);
		assertNull(action.getAction());

		// action that can be handled
		Action moveOtherCaseAction = new EmfAction(ActionTypeConstants.MOVE_OTHER_CASE);
		canHandle = action.canHandle(moveOtherCaseAction);
		assertTrue(canHandle);
		assertEquals(action.getAction(), moveOtherCaseAction);

		// action that can be handled
		Action moveSameCaseAction = new EmfAction(ActionTypeConstants.MOVE_SAME_CASE);
		canHandle = action.canHandle(moveSameCaseAction);
		assertTrue(canHandle);
		assertEquals(action.getAction(), moveSameCaseAction);
	}

	/**
	 * Filter sections test.
	 */
	public void filterSectionsTest() {
		List<SectionInstance> filtered = action.filterSections(null);
		assertNotNull(filtered);
		assertTrue(filtered.size() == 0);

		// passed case instance without sections
		CaseInstance caseInstance = createCaseInstance(null);
		filtered = action.filterSections(caseInstance);
		assertNotNull(filtered);
		assertTrue(filtered.size() == 0);

		// there is case sections list but its empty
		List<SectionInstance> sections = new ArrayList<>();
		caseInstance.setSections(sections);
		filtered = action.filterSections(caseInstance);
		assertNotNull(filtered);
		assertTrue(filtered.size() == 0);

		// we have only sections without purpose (document sections)
		SectionInstance documentSection1 = createSectionInstance(Long.valueOf(1L));
		sections.add(documentSection1);
		SectionInstance documentSection2 = createSectionInstance(Long.valueOf(2L));
		sections.add(documentSection2);
		SectionInstance documentSection3 = createSectionInstance(Long.valueOf(3L));
		sections.add(documentSection3);
		filtered = action.filterSections(caseInstance);
		assertNotNull(filtered);
		assertTrue(filtered.size() == 3);

		// pass some other type of sections
		SectionInstance documentSection4 = createSectionInstance(Long.valueOf(4L));
		documentSection4.setPurpose("objects");
		sections.add(documentSection4);
		SectionInstance documentSection5 = createSectionInstance(Long.valueOf(5L));
		documentSection5.setPurpose("objects");
		sections.add(documentSection5);
		filtered = action.filterSections(caseInstance);
		assertNotNull(filtered);
		assertTrue(filtered.size() == 3);
	}

	/**
	 * Cancel selection test.
	 */
	public void cancelSelectionTest() {
		String cancelSelection = action.cancelSelection();
		assertNull(cancelSelection);
		assertNull(action.getSelectedSectionInstance());
		assertNull(action.getSelectedDocumentForMove());
	}
}
