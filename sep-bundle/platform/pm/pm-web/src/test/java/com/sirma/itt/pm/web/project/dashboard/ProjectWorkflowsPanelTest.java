package com.sirma.itt.pm.web.project.dashboard;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.pm.PMTest;

/**
 * Test class for {@link ProjectWorkflowsPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class ProjectWorkflowsPanelTest extends PMTest {

	/** The size of supported filters. */
	public int supportedWorkflowFilters = 4;

	/** Reference to the project panel that will be tested. */
	private final ProjectWorkflowsPanel projectWorkflowPanel;

	/** The label provider, for retrieving panel filter labels. */
	private final LabelProvider labelProvider;

	/**
	 * Default test constructor.
	 */
	public ProjectWorkflowsPanelTest() {

		projectWorkflowPanel = new ProjectWorkflowsPanel() {

			private static final long serialVersionUID = 1L;

		};

		// mock the label builder
		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(projectWorkflowPanel, "labelProvider", labelProvider);
	}

}
