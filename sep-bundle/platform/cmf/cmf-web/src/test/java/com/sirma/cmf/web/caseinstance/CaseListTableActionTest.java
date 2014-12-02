package com.sirma.cmf.web.caseinstance;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;

/**
 * The Class CaseListTableActionTest.
 * 
 * @author svelikov
 */
@Test
public class CaseListTableActionTest extends CMFTest {

	/** The action. */
	private CaseListTableAction action;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The case instance. */
	private CaseInstance caseInstance;

	/** The case definition. */
	private CaseDefinition caseDefinition;

	/**
	 * Instantiates a new case list table action test.
	 */
	public CaseListTableActionTest() {
		action = new CaseListTableAction() {

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

		dictionaryService = Mockito.mock(DictionaryService.class);
		caseInstance = createCaseInstance(Long.valueOf(1));
		caseInstance.setIdentifier("defid");
		caseDefinition = createCaseDefinition("dmsid");

		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "dictionaryService", dictionaryService);
	}

	/**
	 * Open test.
	 */
	public void openTest() {
		DocumentContext documentContext = action.getDocumentContext();
		Mockito.when(dictionaryService.getDefinition(CaseDefinition.class, "defid")).thenReturn(
				caseDefinition);
		String navigation = action.open(caseInstance);

		// test if selected case instance is properly set in document context
		CaseInstance appliedCaseInstance = documentContext.getInstance(CaseInstance.class);
		Assert.assertEquals(appliedCaseInstance, caseInstance);

		// test if selected case definition is properly set in document context
		Assert.assertEquals(documentContext.getDefinition(CaseDefinition.class), caseDefinition);

		// test navigation
		Assert.assertEquals(CasesConstants.NAVIGATE_CASE_DETAILS, navigation);
	}
}
