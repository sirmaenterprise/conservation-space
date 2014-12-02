package com.sirma.itt.pm.web.entity.dispatcher;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectOpenEvent;
import com.sirma.itt.pm.web.PMEntityPreviewAction;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * The Class ProjectInstanceContextInitializerTest.
 * 
 * @author svelikov
 */
@Test
public class ProjectInstanceContextInitializerTest extends PMTest {

	/** The initializer. */
	private final ProjectInstanceContextInitializer initializer;

	/** The pm entity preview action. */
	private final PMEntityPreviewAction pmEntityPreviewAction;

	/** The project instance. */
	private final ProjectInstance projectInstance;

	/** The dictionary service. */
	private final DictionaryService dictionaryService;

	/** The project definition. */
	private final ProjectDefinition projectDefinition;

	/** The event service. */
	private EventService eventService;

	/**
	 * Instantiates a new project instance context initializer test.
	 */
	public ProjectInstanceContextInitializerTest() {
		initializer = new ProjectInstanceContextInitializer() {
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

		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");
		projectDefinition = createProjectDefinition("pr1");

		pmEntityPreviewAction = Mockito.mock(PMEntityPreviewAction.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		eventService = Mockito.mock(EventService.class);

		ReflectionUtils.setField(initializer, "pmEntityPreviewAction", pmEntityPreviewAction);
		ReflectionUtils.setField(initializer, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(initializer, "eventService", eventService);
	}

	/**
	 * Inits the context for test.
	 */
	@SuppressWarnings("boxing")
	public void initContextForTest() {
		// if no instance is passed, then we should navigate to home page
		String navigation = initializer.initContextFor(null);
		Assert.assertEquals(navigation, PmNavigationConstants.NAVIGATE_HOME);

		// if instance is passed but user doesn't have privileges to open it, then it should be
		// navigated to home page
		Mockito.when(pmEntityPreviewAction.canOpenProject(projectInstance)).thenReturn(
				Boolean.FALSE);
		navigation = initializer.initContextFor(projectInstance);
		Assert.assertEquals(navigation, PmNavigationConstants.NAVIGATE_HOME);

		//
		Mockito.when(pmEntityPreviewAction.canOpenProject(projectInstance))
				.thenReturn(Boolean.TRUE);
		Mockito.when(dictionaryService.getInstanceDefinition(projectInstance)).thenReturn(
				projectDefinition);
		navigation = initializer.initContextFor(projectInstance);
		Assert.assertEquals(navigation, PmNavigationConstants.PROJECT_DASHBOARD);
		// check that context is initialized
		DocumentContext documentContext = initializer.getDocumentContext();
		Assert.assertEquals(documentContext.getInstance(ProjectInstance.class), projectInstance);
		Assert.assertEquals(documentContext.getDefinition(ProjectDefinition.class),
				projectDefinition);
		Assert.assertEquals(documentContext.getContextInstance(), projectInstance);
		// check if open event is fired once
		Mockito.verify(eventService, Mockito.atLeastOnce()).fire(
				Mockito.any(ProjectOpenEvent.class));
	}
}
