package com.sirma.itt.objects.web.object;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryServiceImpl;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.services.ObjectService;

/**
 * The Class ObjectLandingPageTest.
 * 
 * @author svelikov
 */
@Test
public class ObjectLandingPageTest extends ObjectsTest {

	/** The Constant DMSID. */
	private static final String DMSID = "dmsid";

	/** The landing page. */
	private ObjectLandingPage landingPage;

	/** The dictionary service impl. */
	private DictionaryServiceImpl dictionaryServiceImpl;

	/** The object instance. */
	private ObjectInstance objectInstance;

	/** The object definition. */
	private ObjectDefinition objectDefinition;

	/** The object service. */
	private ObjectService objectService;

	/** The section instnace. */
	private SectionInstance sectionInstnace;

	/**
	 * Instantiates a new object landing page test.
	 */
	public ObjectLandingPageTest() {
		landingPage = new ObjectLandingPage() {
			private static final long serialVersionUID = 4527660509905297671L;
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

		sectionInstnace = createSectionInstance(1l);
		objectInstance = createObjectInstance(1l);
		objectDefinition = createObjectDefinition(DMSID);

		ReflectionUtils.setField(landingPage, "log", log);

		dictionaryServiceImpl = Mockito.mock(DictionaryServiceImpl.class);
		Mockito.when(dictionaryServiceImpl.getDefinition(ObjectDefinition.class, DMSID))
				.thenReturn(objectDefinition);
		ReflectionUtils.setField(landingPage, "dictionaryService", dictionaryServiceImpl);

		objectService = Mockito.mock(ObjectService.class);
		Mockito.when(objectService.createInstance(objectDefinition, null)).thenReturn(
				objectInstance);
		ReflectionUtils.setField(landingPage, "objectService", objectService);
	}

	/**
	 * Item selected action test.
	 */
	// TODO: update test
	// public void itemSelectedActionTest() {
	// // we should not have populated context
	// landingPage.itemSelectedAction(null, null);
	// DocumentContext documentContext = landingPage.getDocumentContext();
	// ObjectInstance actualObjectInstance = documentContext.getInstance(ObjectInstance.class);
	// assertNull(actualObjectInstance);
	// ObjectDefinition actualObjectDefinition = documentContext
	// .getDefinition(ObjectDefinition.class);
	// assertNull(actualObjectDefinition);
	// assertNull(landingPage.getSelectedType());
	//
	// // if we pass selected definition id we should get context populated and selected type
	// // should be set
	// landingPage.itemSelectedAction(DMSID, null);
	// actualObjectInstance = documentContext.getInstance(ObjectInstance.class);
	// assertEquals(actualObjectInstance, objectInstance);
	// actualObjectDefinition = documentContext.getDefinition(ObjectDefinition.class);
	// assertEquals(actualObjectDefinition, objectDefinition);
	// assertEquals(landingPage.getSelectedType(), null);
	//
	// // if context instance exists in context, then it should be set to object instance
	// documentContext.addContextInstance(sectionInstnace);
	// landingPage.itemSelectedAction(DMSID, null);
	// actualObjectInstance = documentContext.getInstance(ObjectInstance.class);
	// Instance owningInstance = actualObjectInstance.getOwningInstance();
	// assertEquals(owningInstance, sectionInstnace);
	// }

	/**
	 * Gets the instance definition class test.
	 */
	public void getInstanceDefinitionClassTest() {
		Class<ObjectDefinition> instanceDefinitionClass = landingPage.getInstanceDefinitionClass();
		assertEquals(instanceDefinitionClass, ObjectDefinition.class);
	}

	/**
	 * Gets the new instance test.
	 */
	public void getNewInstanceTest() {
		ObjectInstance newInstance = landingPage.getNewInstance(objectDefinition, null);
		assertEquals(newInstance, objectInstance);
	}

	/**
	 * Gets the instance class test.
	 */
	public void getInstanceClassTest() {
		Class<ObjectInstance> instanceClass = landingPage.getInstanceClass();
		assertEquals(instanceClass, ObjectInstance.class);
	}

	/**
	 * Gets the definition filter type test.
	 */
	public void getDefinitionFilterTypeTest() {
		String definitionFilterType = landingPage.getDefinitionFilterType();
		assertEquals(definitionFilterType, ObjectTypesObject.OBJECT);
	}

}
