package com.sirma.cmf.web.userdashboard.panel;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Test class for @link {@link MyDocumentsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class MyDocumentsPanelTest extends CMFTest {

	/** The reference for tested panel. */
	private final MyDocumentsPanel myDocumentPanel;

	/** The label provider that will be used for retrieving filter labels. */
	private final LabelProvider labelProvider;

	/** The default supported filter size. */
	private final int documentDefailtSize = 2;

	/**
	 * Default test constructor, used for initializing test components.
	 */
	public MyDocumentsPanelTest() {
		myDocumentPanel = new MyDocumentsPanel() {

			private static final long serialVersionUID = 1L;

		};

		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(myDocumentPanel, "labelProvider", labelProvider);
	}

}
