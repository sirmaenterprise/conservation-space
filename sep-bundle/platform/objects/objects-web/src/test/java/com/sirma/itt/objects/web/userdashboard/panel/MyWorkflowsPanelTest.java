package com.sirma.itt.objects.web.userdashboard.panel;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.objects.ObjectsTest;

/**
 * Test class for {@link MyWorkflowsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class MyWorkflowsPanelTest extends ObjectsTest {

	/** The size of supported filters. */
	public int supportedWorkflowFilters = 4;

	/** Reference to the panel that will be tested. */
	private final MyWorkflowsPanel myWorkflowPanel;

	/** The label provider, for retrieving panel filter labels. */
	private final LabelProvider labelProvider;

	/**
	 * Default test constructor.
	 */
	public MyWorkflowsPanelTest() {

		myWorkflowPanel = new MyWorkflowsPanel() {

			private static final long serialVersionUID = 1L;

		};

		// mock the label builder
		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(myWorkflowPanel, "labelProvider", labelProvider);
	}

}
