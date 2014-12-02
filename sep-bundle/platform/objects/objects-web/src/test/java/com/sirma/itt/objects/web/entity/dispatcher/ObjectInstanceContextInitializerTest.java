package com.sirma.itt.objects.web.entity.dispatcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityPreviewAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryServiceImpl;
import com.sirma.itt.emf.event.EventServiceImpl;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkServiceImpl;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.web.constants.ObjectNavigationConstants;

/**
 * The Class ObjectInstanceContextInitializerTest.
 * 
 * @author svelikov
 */
@Test
public class ObjectInstanceContextInitializerTest extends ObjectsTest {

	private ObjectInstanceContextInitializer contextInitializer;

	private EventServiceImpl eventServiceImpl;

	private LinkServiceImpl linkServiceImpl;

	private DictionaryServiceImpl dictionaryServiceImpl;

	private ObjectInstance objectInstance;

	private ObjectDefinition objectDefinition;

	private SectionInstance sectionInstance;

	private CaseInstance caseInstance;

	private EntityPreviewAction entityPreviewAction;

	/**
	 * Instantiates a new object instance context initializer test.
	 */
	public ObjectInstanceContextInitializerTest() {
		contextInitializer = new ObjectInstanceContextInitializer() {
			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

			@Override
			protected Instance getLinkedInstance(LinkReference linkReference) {
				sectionInstance = createSectionInstance(1l);
				caseInstance = createCaseInstance(1l);
				sectionInstance.setOwningInstance(caseInstance);
				return sectionInstance;
			}
		};

		ReflectionUtils.setField(contextInitializer, "log", log);

		objectInstance = createObjectInstance(1l);
		objectDefinition = createObjectDefinition("dmsid");

		eventServiceImpl = Mockito.mock(EventServiceImpl.class);
		ReflectionUtils.setField(contextInitializer, "eventService", eventServiceImpl);

		linkServiceImpl = Mockito.mock(LinkServiceImpl.class);
		Mockito.when(
				linkServiceImpl.getLinksTo(objectInstance.toReference(),
						LinkConstantsCmf.SECTION_TO_CHILD)).thenReturn(createLinkReferenceList());
		ReflectionUtils.setField(contextInitializer, "linkService", linkServiceImpl);

		dictionaryServiceImpl = Mockito.mock(DictionaryServiceImpl.class);
		Mockito.when(dictionaryServiceImpl.getInstanceDefinition(objectInstance)).thenReturn(
				objectDefinition);
		ReflectionUtils.setField(contextInitializer, "dictionaryService", dictionaryServiceImpl);

		entityPreviewAction = Mockito.mock(EntityPreviewAction.class);
		ReflectionUtils.setField(contextInitializer, "entityPreviewAction", entityPreviewAction);
	}

	/**
	 * Test for the method.
	 */
	public void initContextForTest() {
		DocumentContext documentContext = contextInitializer.getDocumentContext();

		// check if null is passed for object instance
		String navigation = contextInitializer.initContextFor(null);
		ObjectInstance instance = documentContext.getInstance(ObjectInstance.class);
		assertNull(instance);
		ObjectDefinition definition = documentContext.getDefinition(ObjectDefinitionImpl.class);
		assertNull(definition);
		Instance contextInstance = documentContext.getContextInstance();
		assertNull(contextInstance);
		assertEquals(navigation, NavigationConstants.NAVIGATE_HOME);

		// if actual object instance is passed then:
		// object instance and definition should be populated in context
		Mockito.when(entityPreviewAction.canOpenInstance(objectInstance)).thenReturn(Boolean.TRUE);
		navigation = contextInitializer.initContextFor(objectInstance);
		instance = documentContext.getInstance(ObjectInstance.class);
		assertNotNull(instance);
		assertEquals(instance, objectInstance);
		definition = documentContext.getDefinition(ObjectDefinition.class);
		assertNotNull(definition);
		assertEquals(definition, objectDefinition);
		// the object instance should in set as context instance too
		contextInstance = documentContext.getContextInstance();
		assertNotNull(contextInstance);
		assertEquals(contextInstance, objectInstance);
		assertEquals(navigation, ObjectNavigationConstants.OBJECT);
	}
}
