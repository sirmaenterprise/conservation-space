package com.sirma.itt.pm.web.project.dashboard;

import static org.mockito.Mockito.mock;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Test class for {@link ProjectTasksPanel}
 * 
 * @author cdimitrov
 */
@Test
public class ProjectTasksPanelTest extends PMTest {

	/** The reference based on tested panel. */
	private ProjectTasksPanel projectTasksPanel;

	/** The label provider, needed for retrieving panel filter labels. */
	private LabelProvider labelProvider;

	/** The supported panel filter number. */
	private final int projectTaskFiltersNumber = 6;

	/**
	 * Default test panel constructor for initializing test componenets.
	 */
	public ProjectTasksPanelTest() {

		projectTasksPanel = new ProjectTasksPanel() {

			private static final long serialVersionUID = 1L;

			/** Simulate document context for the tests. Holds additional data for testing. */
			private final DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}
		};

		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(projectTasksPanel, "labelProvider", labelProvider);
	}

	/**
	 * Method for testing the context data.
	 */
	public void getProjectInstanceFromContextTest() {
		Assert.assertNull(projectTasksPanel.getDocumentContext().get(ProjectInstance.class));
		ProjectInstance projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");
		projectTasksPanel.getDocumentContext().addInstance(projectInstance);
		Assert.assertEquals(
				projectTasksPanel.getDocumentContext().getInstance(ProjectInstance.class),
				projectInstance);
	}

}
