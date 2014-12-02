package com.sirma.itt.pm.web.project.dashboard;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Test class for {@link ProjectsDocumentPanel}.
 * 
 * @author cdimitrov
 */
@Test
public class ProjectsDocumentPanelTest extends PMTest {

	/** The reference for tested panel. */
	private ProjectsDocumentPanel projectDocumentPanel;

	/** The project instance, that will be used for panel context. */
	private ProjectInstance projectInstnace;

	/**
	 * Default test constructor, used for initializing test components.
	 */
	public ProjectsDocumentPanelTest() {
		projectDocumentPanel = new ProjectsDocumentPanel() {

			private static final long serialVersionUID = 1L;

			/** The document context, provide additional panel component. */
			private final DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}
		};
	}

	/**
	 * Method for testing panel context data.
	 */
	public void getPanelContextInstanceTest() {
		Assert.assertNull(projectDocumentPanel.getDocumentContext().get(ProjectInstance.class));
		projectInstnace = createProjectInstance(1L, "dmsId");
		projectDocumentPanel.getDocumentContext().addInstance(projectInstnace);
		Assert.assertEquals(
				projectDocumentPanel.getDocumentContext().getInstance(ProjectInstance.class),
				projectInstnace);
	}

}
