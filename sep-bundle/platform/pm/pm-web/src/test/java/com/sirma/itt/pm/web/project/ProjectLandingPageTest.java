package com.sirma.itt.pm.web.project;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * The Class ProjectLandingPageTest.
 * 
 * @author svelikov
 */
@Test
public class ProjectLandingPageTest extends PMTest {

	/** The landing page. */
	private final ProjectLandingPage landingPage;

	/** The project service. */
	private ProjectService projectService;

	/** The project definition. */
	private ProjectDefinition projectDefinition;

	/** The project instance. */
	private ProjectInstance projectInstance;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The event service. */
	private EventService eventService;

	/**
	 * Instantiates a new project landing page test.
	 */
	public ProjectLandingPageTest() {
		landingPage = new ProjectLandingPage() {

			private static final long serialVersionUID = 1L;
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

		projectDefinition = createProjectDefinition("dmsId");
		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");

		projectService = Mockito.mock(ProjectService.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		eventService = Mockito.mock(EventService.class);

		ReflectionUtils.setField(landingPage, "projectService", projectService);
		ReflectionUtils.setField(landingPage, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(landingPage, "eventService", eventService);
	}

	/**
	 * Clear context.
	 */
	@BeforeMethod
	public void clearContext() {
		landingPage.getDocumentContext().clear();
	}

	/**
	 * Gets the instance definition class test.
	 */
	public void getInstanceDefinitionClassTest() {
		assertEquals(landingPage.getInstanceDefinitionClass(), ProjectDefinition.class);
	}

	/**
	 * Gets the new instance test.
	 */
	public void getNewInstanceTest() {
		Mockito.when(projectService.createInstance(projectDefinition, null)).thenReturn(
				projectInstance);
		ProjectInstance newInstance = landingPage.getNewInstance(projectDefinition, null);
		assertNotNull(newInstance);
	}

	/**
	 * Gets the instance class.
	 */
	public void getInstanceClassTest() {
		assertEquals(landingPage.getInstanceClass(), ProjectInstance.class);
	}

	/**
	 * Save instance.
	 */
	public void saveInstance() {
		String navigation = landingPage.save(null);

		assertEquals(navigation, PmNavigationConstants.RELOAD_PAGE);

		//
		Mockito.when(dictionaryService.getInstanceDefinition(projectInstance)).thenReturn(
				projectDefinition);
		navigation = landingPage.save(projectInstance);
		Mockito.verify(projectService, Mockito.times(1)).save(Mockito.any(ProjectInstance.class),
				Mockito.any(Operation.class));
		assertEquals(navigation, PmNavigationConstants.PROJECT);
		DocumentContext documentContext = landingPage.getDocumentContext();
		assertEquals(documentContext.getInstance(ProjectInstance.class), projectInstance);
		assertEquals(documentContext.getDefinition(ProjectDefinition.class), projectDefinition);
		assertEquals(documentContext.getContextInstance(), projectInstance);
		assertEquals(documentContext.getCurrentInstance(), projectInstance);
	}

	/**
	 * Cancel edit instance.
	 */
	public void cancelEditInstanceTest() {
		String navigation = landingPage.cancelEditInstance(null);
		assertEquals(navigation, PmNavigationConstants.BACKWARD);

		//
		navigation = landingPage.cancelEditInstance(projectInstance);
		assertEquals(navigation, PmNavigationConstants.BACKWARD);

		// if instance is not persisted, then the context should be cleared
		projectInstance.setId(null);
		navigation = landingPage.cancelEditInstance(projectInstance);
		assertEquals(navigation, PmNavigationConstants.BACKWARD);
		DocumentContext documentContext = landingPage.getDocumentContext();
		assertNull(documentContext.getInstance(ProjectInstance.class));
		assertNull(documentContext.getDefinition(ProjectDefinition.class));
	}

	/**
	 * Gets the definition filter type.
	 */
	protected void getDefinitionFilterTypeTest() {
		assertEquals(landingPage.getDefinitionFilterType(), ObjectTypesPm.PROJECT);
	}

	/**
	 * Gets the instance service.
	 */
	protected void getInstanceServiceTest() {
		assertEquals(landingPage.getInstanceService(), projectService);
	}
}
