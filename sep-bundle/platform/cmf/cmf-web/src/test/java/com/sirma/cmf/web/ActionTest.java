package com.sirma.cmf.web;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Test for Action class.
 * 
 * @author svelikov
 */
@Test
public class ActionTest extends CMFTest {

	private final Action action;

	/**
	 * Instantiates a new action test.
	 */
	public ActionTest() {
		action = new Action() {

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
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
	}

	/**
	 * Test for initializeContextInstance method.
	 */
	public void initializeContextInstanceTest() {
		action.initializeContextInstance(null);
		assertNull(action.getDocumentContext().getContextInstance());

		//
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		action.initializeContextInstance(caseInstance);
		assertEquals(action.getDocumentContext().getContextInstance(), caseInstance);
		action.getDocumentContext().clear();

		//
		DocumentInstance documentInstance = createDocumentInstance(Long.valueOf(1L));
		documentInstance.setOwningInstance(caseInstance);
		action.initializeContextInstance(documentInstance);
		assertEquals(action.getDocumentContext().getContextInstance(), caseInstance);
		assertEquals(action.getDocumentContext().getInstance(CaseInstance.class), caseInstance);
	}
}
