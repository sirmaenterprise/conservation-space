package com.sirma.cmf.web.caseinstance;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.tab.CaseDashboardTabAction;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * CaseDashboardTabAction test class.
 * 
 * @author svelikov
 */
@Test
public class CaseDashboardTabActionTest extends CMFTest {

	private static final String TAB_1 = "tab_1";

	/**
	 * Class under test.
	 */
	private final CaseDashboardTabAction action;

	/**
	 * Initializes the test.
	 */
	public CaseDashboardTabActionTest() {

		action = new CaseDashboardTabAction() {

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
			protected void fireTabSelectedEvent(String selectedTab) {
				// do nothing here
			}
		};

		ReflectionUtils.setField(action, "log", LOG);
	}

	/**
	 * Test for switchTab method.
	 */
	public void switchTabTest() {
		String navigation = action.switchTab(TAB_1);

		assertEquals(navigation, TAB_1);

		String selectedTab = action.getDocumentContext().getSelectedTab();
		assertEquals(selectedTab, TAB_1);
	}

}
