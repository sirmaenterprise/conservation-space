package com.sirma.cmf.web.caseinstance.section;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * Test for SectionDocumentsAction class.
 * 
 * @author svelikov
 */
public class SectionDocumentsActionTest extends CMFTest {

	private final SectionDocumentsAction action;

	/**
	 * Instantiates a new section documents action test.
	 */
	public SectionDocumentsActionTest() {
		action = new SectionDocumentsAction() {

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

			@Override
			protected void refreshInstance(Instance instance) {
				// do nothing
			}
		};

		ReflectionUtils.setField(action, "log", LOG);
	}

	/**
	 * Inits the test class.
	 */
	@BeforeMethod
	public void initTestClass() {
		action.getDocumentContext().clear();
		List<SectionInstance> caseDocumentSections = action.getCaseDocumentSections();
		if (caseDocumentSections != null) {
			caseDocumentSections.clear();
		}
	}

	/**
	 * Test for init method.
	 */
	@Test
	public void initTest() {
		action.init();
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 0);

		//
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		SectionInstance sectionInstance1 = createSectionInstance(Long.valueOf(1L));
		SectionInstance sectionInstance2 = createSectionInstance(Long.valueOf(1L));
		List<SectionInstance> sections = new ArrayList<>();
		sections.add(sectionInstance1);
		sections.add(sectionInstance2);
		caseInstance.setSections(sections);
		action.getDocumentContext().setCurrentInstance(caseInstance);
		action.init();
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 2);

		// sections with any purpose different from null should be ignored
		SectionInstance sectionInstance3 = createSectionInstance(Long.valueOf(1L));
		sectionInstance3.setPurpose("objectsSection");
		action.init();
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 2);

		// any not persisted documents in sections should be omitted
		List<Instance> documents = new ArrayList<>();
		DocumentInstance documentInstance1 = createDocumentInstance(Long.valueOf(1L));
		documents.add(documentInstance1);
		DocumentInstance documentInstance2 = createDocumentInstance(null);
		documentInstance2.setId(null);
		documents.add(documentInstance2);
		sectionInstance1.setContent(documents);
		action.init();
		SectionInstance firstsection = action.getCaseDocumentSections().get(0);
		List<Instance> content = firstsection.getContent();
		assertNotNull(content);
		assertTrue(content.size() == 1);
		assertEquals(content.get(0), documentInstance1);
	}

	/**
	 * Test for historyOpenCaseDocumentsTabObserver method.
	 */
	@Test
	public void historyOpenCaseDocumentsTabObserverTest() {
		NavigationHistoryEvent historyEvent = createNavigationHistoryEvent();
		action.historyOpenCaseDocumentsTabObserver(historyEvent);
		assertNull(action.getCaseDocumentSections());

		//
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		List<SectionInstance> sections = new ArrayList<>();
		caseInstance.setSections(sections);
		action.getDocumentContext().setCurrentInstance(caseInstance);
		action.historyOpenCaseDocumentsTabObserver(historyEvent);
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 0);

		//
		SectionInstance sectionInstance1 = createSectionInstance(Long.valueOf(1L));
		SectionInstance sectionInstance2 = createSectionInstance(Long.valueOf(1L));
		sections.add(sectionInstance1);
		sections.add(sectionInstance2);
		action.historyOpenCaseDocumentsTabObserver(historyEvent);
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 2);

		// sections with any purpose different from null should be ignored
		SectionInstance sectionInstance3 = createSectionInstance(Long.valueOf(1L));
		sectionInstance3.setPurpose("objectsSection");
		action.historyOpenCaseDocumentsTabObserver(historyEvent);
		assertNotNull(action.getCaseDocumentSections());
		assertTrue(action.getCaseDocumentSections().size() == 2);

		// any not persisted documents in sections should be omitted
		List<Instance> documents = new ArrayList<>();
		DocumentInstance documentInstance1 = createDocumentInstance(Long.valueOf(1L));
		documents.add(documentInstance1);
		DocumentInstance documentInstance2 = createDocumentInstance(null);
		documentInstance2.setId(null);
		documents.add(documentInstance2);
		sectionInstance1.setContent(documents);
		action.historyOpenCaseDocumentsTabObserver(historyEvent);
		SectionInstance firstsection = action.getCaseDocumentSections().get(0);
		List<Instance> content = firstsection.getContent();
		assertNotNull(content);
		assertTrue(content.size() == 1);
		assertEquals(content.get(0), documentInstance1);
	}

	/**
	 * Test for attachDocument method.
	 */
	@Test
	public void attachDocumentTest() {
		Instance caseInstance = createCaseInstance(Long.valueOf(1L));
		EMFActionEvent event = createEventObject(null, caseInstance, null, null);
		action.attachDocument(event);
		assertEquals(action.getDocumentContext().getInstance(CaseInstance.class), caseInstance);
	}
}
