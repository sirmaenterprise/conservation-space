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
 * Test method for {@link ProjectCasePanel}
 * 
 * @author cdimitrov
 */
@Test
public class ProjectCasePanelTest extends PMTest {

	/** The project panel reference. */
	private ProjectCasePanel projectCasePanel;

	/** The label provider that will be used for retrieving filter labels. */
	private LabelProvider labelProvider;

	/** The default supported case filters. */
	private final int supportedCaseFilters = 2;

	/** The default supported date filters. */
	private final int supportedDateFilters = 4;

	/** The project instance that will be used in the tests. */
	private ProjectInstance projectInstance;

	/**
	 * Default constructor for the test class.
	 */
	public ProjectCasePanelTest() {

		projectCasePanel = new ProjectCasePanel() {

			private static final long serialVersionUID = 1L;

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

		};

		// mock the label builder
		labelProvider = mock(LabelProvider.class);

		ReflectionUtils.setField(projectCasePanel, "labelProvider", labelProvider);

		projectInstance = createProjectInstance(Long.valueOf(3), "dmsId");

	}

	/**
	 * Test method for empty context instance(project).
	 */
	public void getProjectInstanceFromContextEmptyTest() {
		projectCasePanel.getDocumentContext().addContextInstance(null);
		Assert.assertNull(projectCasePanel.getDocumentContext().getContextInstance());
	}

	/**
	 * Test method for supported context instance(project).
	 */
	public void getProjectInstanceFromContextSupportedTest() {
		projectCasePanel.getDocumentContext().addContextInstance(projectInstance);
		Assert.assertNotNull(projectCasePanel.getDocumentContext().getContextInstance());
	}

}
