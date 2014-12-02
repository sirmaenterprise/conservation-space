package com.sirma.itt.objects.web.entity.dispatcher;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityPreviewAction;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryServiceImpl;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventServiceImpl;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.label.LabelProviderImpl;
import com.sirma.itt.emf.web.notification.NotificationSupport;
import com.sirma.itt.emf.web.notification.NotificationSupportImpl;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.web.caseinstance.tab.ObjectsCaseTabConstants;
import com.sirma.itt.objects.web.constants.ObjectNavigationConstants;

/**
 * The Class SectionInstanceContextInitializerTest.
 * 
 * @author svelikov
 */
@Test
public class SectionInstanceContextInitializerTest extends ObjectsTest {

	/** The context initializer. */
	private SectionInstanceContextInitializer contextInitializer;

	private NotificationSupport notificationSupport;

	private LabelProvider labelProvider;

	private EntityPreviewAction entityPreviewAction;

	private SectionInstance sectionInstance;
	private CaseInstance caseInstance;

	private DictionaryServiceImpl dictionaryServiceImpl;

	private DefinitionModel caseDefinition;

	private EventServiceImpl eventServiceImpl;

	/**
	 * Instantiates a new section instance context initializer test.
	 */
	public SectionInstanceContextInitializerTest() {
		contextInitializer = new SectionInstanceContextInitializer() {
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

		ReflectionUtils.setField(contextInitializer, "log", log);

		sectionInstance = createSectionInstance(Long.valueOf(1));
		caseInstance = createCaseInstance(Long.valueOf(1));
		caseDefinition = createCaseDefinition("dmsid");

		notificationSupport = Mockito.mock(NotificationSupportImpl.class);
		ReflectionUtils.setField(contextInitializer, "notificationSupport", notificationSupport);

		labelProvider = Mockito.mock(LabelProviderImpl.class);
		Mockito.when(labelProvider.getValue(Mockito.anyString())).thenReturn("message");
		ReflectionUtils.setField(contextInitializer, "labelProvider", labelProvider);

		notificationSupport = Mockito.mock(NotificationSupportImpl.class);
		ReflectionUtils.setField(contextInitializer, "notificationSupport", notificationSupport);

		eventServiceImpl = Mockito.mock(EventServiceImpl.class);
		ReflectionUtils.setField(contextInitializer, "eventService", eventServiceImpl);

		entityPreviewAction = Mockito.mock(EntityPreviewAction.class);
		ReflectionUtils.setField(contextInitializer, "entityPreviewAction", entityPreviewAction);

		dictionaryServiceImpl = Mockito.mock(DictionaryServiceImpl.class);
		Mockito.when(dictionaryServiceImpl.getInstanceDefinition(caseInstance)).thenReturn(
				caseDefinition);
		ReflectionUtils.setField(contextInitializer, "dictionaryService", dictionaryServiceImpl);
	}

	/**
	 * Test for the method.
	 */
	public void initContextForTest() {
		DocumentContext documentContext = contextInitializer.getDocumentContext();

		// check if null is passed for object instance
		String navigation = contextInitializer.initContextFor(null);
		CaseInstance actualCaseInstance = documentContext.getInstance(CaseInstance.class);
		Assert.assertNull(actualCaseInstance);
		CaseDefinition actualCaseDefinition = documentContext.getDefinition(CaseDefinition.class);
		Assert.assertNull(actualCaseDefinition);
		Assert.assertEquals(navigation, ObjectNavigationConstants.NAVIGATE_HOME);

		// check if actual instance is passed to method but no owning instance is provided
		navigation = contextInitializer.initContextFor(sectionInstance);
		actualCaseInstance = documentContext.getInstance(CaseInstance.class);
		Assert.assertNull(actualCaseInstance);
		actualCaseDefinition = documentContext.getDefinition(CaseDefinition.class);
		Assert.assertNull(actualCaseDefinition);
		Assert.assertEquals(navigation, ObjectNavigationConstants.NAVIGATE_HOME);

		// check if actual instance is passed to method but the user doesn't have privileges to open
		// the case
		sectionInstance.setOwningInstance(caseInstance);
		Mockito.when(entityPreviewAction.canOpenInstance(caseInstance)).thenReturn(Boolean.FALSE);
		navigation = contextInitializer.initContextFor(sectionInstance);
		Assert.assertEquals(navigation, ObjectNavigationConstants.NAVIGATE_HOME);

		// check if actual instance is passed to method
		sectionInstance.setOwningInstance(caseInstance);
		Mockito.when(entityPreviewAction.canOpenInstance(caseInstance)).thenReturn(Boolean.TRUE);
		navigation = contextInitializer.initContextFor(sectionInstance);
		Assert.assertEquals(navigation, ObjectNavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS);
		// context should be populated with the case instance and definition
		actualCaseInstance = documentContext.getInstance(CaseInstance.class);
		Assert.assertEquals(actualCaseInstance, caseInstance);
		actualCaseDefinition = documentContext.getDefinition(CaseDefinition.class);
		Assert.assertEquals(actualCaseDefinition, caseDefinition);
		Assert.assertEquals(documentContext.getSelectedTab(), ObjectsCaseTabConstants.DOCUMENTS);

		// if a section have purpose set, then we get navigation to other tab
		sectionInstance.setPurpose("objectsSection");
		navigation = contextInitializer.initContextFor(sectionInstance);
		Assert.assertEquals(documentContext.getSelectedTab(), ObjectsCaseTabConstants.CASE_OBJECTS);
		Assert.assertEquals(navigation, ObjectNavigationConstants.CASE_OBJECTS_TAB);
		// TODO: test if root instance (ProjectInstance is properly set in context)

	}
}
