package com.sirma.itt.pm.web.project.dashboard;

import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceServiceImpl;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * ProjectColleaguesPanel test class.
 * 
 * @author cdimitrov
 */
@Test
public class ProjectColleaguesPanelTest extends PMTest {

	/** The project colleagues panel. */
	private ProjectColleaguesPanel projectColleaguesPanel;

	/** The resource service mock. */
	private ResourceServiceImpl resourceService;

	/** The project instance. */
	private ProjectInstance projectInstance;

	/**
	 * Constructor.
	 */
	public ProjectColleaguesPanelTest() {

		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");

		projectColleaguesPanel = new ProjectColleaguesPanel() {

			private static final long serialVersionUID = -5145927948558783761L;

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

		ReflectionUtils.setField(projectColleaguesPanel, "log", log);

		resourceService = Mockito.mock(ResourceServiceImpl.class);

		ReflectionUtils.setField(projectColleaguesPanel, "resourceService", resourceService);
	}

	/**
	 * Test for empty project instance in the context.
	 */
	public void emptyProjectInstanceTest() {

		projectColleaguesPanel.getDocumentContext()
				.put(ProjectInstance.class.getSimpleName(), null);

		Assert.assertNull(
				projectColleaguesPanel.getDocumentContext().getInstance(ProjectInstance.class),
				"Sould passed, no instance into the context !");
	}

	/**
	 * Test for available project instance into the context.
	 */
	public void availableProjectInstanceTest() {

		projectColleaguesPanel.getDocumentContext().addInstance(projectInstance);

		Assert.assertNotNull(
				projectColleaguesPanel.getDocumentContext().getInstance(ProjectInstance.class),
				"Sould passed, available instance into the context !");
	}

	/**
	 * Test for not null-able resource from project.
	 */
	public void extractProjectColleaguesNotNullTest() {
		Assert.assertNotNull(resourceService.getResources(projectInstance),
				"Should passed, retrieving resource from project instance ! ");
	}

	/**
	 * Test for empty project resources.
	 */
	public void extractProjectColleaguesEmptyTest() {
		Mockito.when(resourceService.getResources(Mockito.any(Instance.class))).thenReturn(
				Mockito.anyList());
		List<Resource> roleList = resourceService.getResources(projectInstance);
		Assert.assertTrue(roleList.isEmpty());
	}

	/**
	 * Test for available resources from project.
	 */
	public void extractProjectColleaguesWithElementsTest() {
		List<Resource> roleList = resourceService.getResources(projectInstance);
		roleList.add(new EmfGroup());
		roleList.add(new EmfUser());
		Assert.assertFalse(roleList.isEmpty());
	}

}
