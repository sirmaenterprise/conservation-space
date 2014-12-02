package com.sirma.cmf.web.caseinstance.dashboard;

import static org.mockito.Mockito.mock;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Test class for {@link CaseTasksPanel}
 * 
 * @author cdimitrov
 */
@Test
public class CaseTasksPanelTest extends CMFTest {

	/** The reference for tested panel. */
	private CaseTasksPanel caseTasksPanel;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The size of the supported filters. */
	private final int taskFiltersNumber = 6;

	/**
	 * The default test constructor.
	 */
	public CaseTasksPanelTest() {
		caseTasksPanel = new CaseTasksPanel() {

			private static final long serialVersionUID = 1L;

			/** The document context. */
			private final DocumentContext docContext = new DocumentContext();

			/**
			 * Getter method for document context
			 * 
			 * @return current document context
			 */
			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

		};

		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(caseTasksPanel, "labelProvider", labelProvider);
	}

	/**
	 * Test method for retrieving context data.
	 */
	public void retrievePanelContextTest() {
		Assert.assertNull(caseTasksPanel.getDocumentContext().getCurrentInstance());
		Assert.assertNull(caseTasksPanel.getDocumentContext().getContextInstance());

		CaseInstance caseInstance = createCaseInstance(6L);
		caseTasksPanel.getDocumentContext().setCurrentInstance(caseInstance);
		Assert.assertEquals(caseTasksPanel.getDocumentContext().getCurrentInstance(), caseInstance);
	}

}
