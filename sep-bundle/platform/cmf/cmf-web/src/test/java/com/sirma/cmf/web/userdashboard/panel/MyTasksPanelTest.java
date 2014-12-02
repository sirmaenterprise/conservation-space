package com.sirma.cmf.web.userdashboard.panel;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Test class for {@link MyTasksPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class MyTasksPanelTest extends CMFTest {

	/** The panel reference that will be tested. */
	private final MyTasksPanel myTasksPanel;

	/** The label builder that will be used for retrieving panel filters. */
	private final LabelProvider labelProvider;

	/** The number for panel filters. */
	private final int taskFiltersNumber = 6;

	/**
	 * Default test constructor, for initializing test components.
	 */
	public MyTasksPanelTest() {
		myTasksPanel = new MyTasksPanel() {

			private static final long serialVersionUID = 1L;

		};

		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(myTasksPanel, "labelProvider", labelProvider);
	}

}
