package com.sirma.itt.objects.web.caseinstance.section;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.objects.ObjectsTest;

/**
 * The Class SectionObjectsActionTest.
 * 
 * @author svelikov
 */
@Test
public class SectionObjectsActionTest extends ObjectsTest {

	/** The Constant OBJECTS_SECTION. */
	private static final String OBJECTS_SECTION = "objectsSection";

	/** The action. */
	private SectionObjectsAction action;

	/**
	 * Instantiates a new section objects action test.
	 */
	public SectionObjectsActionTest() {
		action = new SectionObjectsAction() {
			private static final long serialVersionUID = 5965864447222339988L;
			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}
		};

		ReflectionUtils.setField(action, "log", log);
	}

	/**
	 * Inits the test.
	 */
	public void initTest() {
		CaseInstance caseInstance = createCaseInstance(1l);

		// check if no case instance exists in context
		action.init();
		Assert.assertNull(action.getCaseObjectSections());

		// check if no sections are found linked to the case: we should get an empty list
		action.getDocumentContext().addInstance(caseInstance);
		action.init();
		Assert.assertNotNull(action.getCaseObjectSections());
		Assert.assertTrue(action.getCaseObjectSections().isEmpty());

		// check if sections are found but with different purpose: we should get an empty list
		List<SectionInstance> sections = new ArrayList<SectionInstance>(2);
		sections.add(createSectionInstance(1l));
		sections.add(createSectionInstance(2l));
		caseInstance.setSections(sections);
		action.init();
		Assert.assertNotNull(action.getCaseObjectSections());
		Assert.assertTrue(action.getCaseObjectSections().isEmpty());

		// check if sections are found but with different purpose: we should get them extracted and
		// set in the class field
		SectionInstance sectionInstance = createSectionInstance(3l);
		sectionInstance.setPurpose(OBJECTS_SECTION);
		sections.add(sectionInstance);
		action.init();
		Assert.assertNotNull(action.getCaseObjectSections());
		Assert.assertTrue(action.getCaseObjectSections().size() == 1);
		Assert.assertEquals(action.getCaseObjectSections().get(0).getPurpose(), OBJECTS_SECTION);
	}

}
