package com.sirma.cmf.web.caseinstance;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.link.LinkService;

/**
 * Tests for LinkCaseAction.
 * 
 * @author svelikov
 */
@Test
public class LinkCaseActionTest extends CMFTest {

	private final LinkCaseAction action;

	private LinkService linkService;

	/**
	 * Instantiates a new link case action test.
	 */
	public LinkCaseActionTest() {
		action = new LinkCaseAction() {
			private static final long serialVersionUID = -6967461481595154928L;
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

		linkService = Mockito.mock(LinkService.class);

		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "linkService", linkService);
	}

	/**
	 * Link cases test.
	 */
	public void linkCasesTest() {

		String navigation = action.linkCases();
		Assert.assertEquals(navigation, NavigationConstants.RELOAD_PAGE);
		Mockito.verify(linkService, Mockito.never()).link(Mockito.any(CaseInstance.class),
				Mockito.any(CaseInstance.class), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyMap());

		//
		CaseInstance selectedCaseInstance = createCaseInstance(Long.valueOf(1L));
		CaseInstance currentCaseInstance = createCaseInstance(Long.valueOf(2L));
		DocumentContext documentContext = action.getDocumentContext();
		documentContext.addInstance(currentCaseInstance);

		navigation = action.linkCases();
		Assert.assertEquals(navigation, NavigationConstants.RELOAD_PAGE);
		Mockito.verify(linkService, Mockito.never()).link(Mockito.any(CaseInstance.class),
				Mockito.any(CaseInstance.class), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyMap());

		//
		action.setSelectedCaseInstance(selectedCaseInstance);
		navigation = action.linkCases();
		Assert.assertEquals(navigation, NavigationConstants.BACKWARD);
		Mockito.verify(linkService, Mockito.atLeastOnce()).link(Mockito.any(CaseInstance.class),
				Mockito.any(CaseInstance.class), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyMap());
	}
}
