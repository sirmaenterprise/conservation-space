package com.sirma.itt.objects.web.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.Serializable;

import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;

/**
 * The Class ObjectActionTest.
 * 
 * @author svelikov
 */
@Test
public class ObjectActionTest extends ObjectsTest {

	/** The action. */
	private ObjectAction action;
	private ObjectInstance objectInstance;
	private CaseInstance caseInstance;
	private SectionInstance sectionInstance;

	/**
	 * Instantiates a new object action test.
	 */
	public ObjectActionTest() {
		action = new ObjectAction() {
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
			public Instance fetchInstance(Serializable instanceId, String instanceType) {
				if (instanceId == null) {
					return null;
				}
				if ("sectioninstance".equals(instanceType)) {
					sectionInstance.setId(instanceId);
					return sectionInstance;
				} else if ("caseinstance".equals(instanceType)) {
					caseInstance.setId(instanceId);
					return caseInstance;
				}

				return null;
			}
		};

		objectInstance = createObjectInstance(1l);
		caseInstance = createCaseInstance(1L);
		sectionInstance = createSectionInstance(1L);

		ReflectionUtils.setField(action, "log", log);
	}

	/**
	 * Creates the object test.
	 */
	public void createObjectActionTest() {
		DocumentContext documentContext = action.getDocumentContext();

		// no data is passed
		String navigation = action.createObjectAction();
		assertNull(navigation);
		assertNull(documentContext.getInstance(SectionInstance.class));
		assertNull(documentContext.getInstance(CaseInstance.class));

		// passed an empty string as argument
		action.setCreateObjectData("");
		navigation = action.createObjectAction();
		assertNull(navigation);
		assertNull(documentContext.getInstance(SectionInstance.class));
		assertNull(documentContext.getInstance(CaseInstance.class));

		// // passed data but missing section instance id and context instance data
		// String stringData =
		// "{\"contextType\":\"\",\"sourceType\":\"\",\"contextId\":\"\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertNull(navigation);
		// assertNull(documentContext.getInstance(SectionInstance.class));
		// assertNull(documentContext.getInstance(CaseInstance.class));
		//
		// // passed actual context but no section instance id
		// stringData =
		// "{\"contextType\":\"\",\"instanceType\":\"caseinstance\",\"sourceType\":\"\",\"contextId\":\"\",\"instanceId\":\"emf:e15a3ff9-42e7-4fe3-b142-71db7164da9c\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertNull(navigation);
		// assertNull(documentContext.getInstance(SectionInstance.class));
		// assertNull(documentContext.getInstance(CaseInstance.class));
		//
		// // passed section instance id but no context instance type
		// stringData =
		// "{\"contextType\":\"\",\"sourceType\":\"\",\"contextId\":\"\",\"selectedSectionId\":\"emf:198dfb4e-ebd0-48f9-a003-6a48d1b7445a\",\"instanceId\":\"emf:e15a3ff9-42e7-4fe3-b142-71db7164da9c\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertNull(navigation);
		// assertNull(documentContext.getInstance(SectionInstance.class));
		// assertNull(documentContext.getInstance(CaseInstance.class));
		//
		// // passed section instance id but no context instance id
		// stringData =
		// "{\"contextType\":\"\",\"instanceType\":\"caseinstance\",\"sourceType\":\"\",\"contextId\":\"\",\"selectedSectionId\":\"emf:198dfb4e-ebd0-48f9-a003-6a48d1b7445a\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertNull(navigation);
		// assertNull(documentContext.getInstance(SectionInstance.class));
		// assertNull(documentContext.getInstance(CaseInstance.class));
		//
		// // passed section instance id but no context instance id and type
		// stringData =
		// "{\"contextType\":\"\",\"sourceType\":\"\",\"contextId\":\"\",\"selectedSectionId\":\"emf:198dfb4e-ebd0-48f9-a003-6a48d1b7445a\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertNull(navigation);
		// assertNull(documentContext.getInstance(SectionInstance.class));
		// assertNull(documentContext.getInstance(CaseInstance.class));
		//
		// // passed all required data
		// stringData =
		// "{\"contextType\":\"\",\"instanceType\":\"caseinstance\",\"sourceType\":\"\",\"contextId\":\"\",\"selectedSectionId\":\"emf:198dfb4e-ebd0-48f9-a003-6a48d1b7445a\",\"instanceId\":\"emf:e15a3ff9-42e7-4fe3-b142-71db7164da9c\",\"sourceId\":\"\"}";
		// action.setCreateObjectData(stringData);
		// navigation = action.createObjectAction();
		// assertEquals(navigation, "object");
		// assertEquals(documentContext.getInstance(SectionInstance.class), sectionInstance);
		// assertEquals(documentContext.getInstance(CaseInstance.class), caseInstance);

		// clear context
		documentContext.clear();
	}

	/**
	 * Attach object test.
	 */
	public void attachObjectTest() {
		EMFActionEvent event = createEventObject(null, null,
				ObjectActionTypeConstants.ATTACH_OBJECT, null);

		// if there is no instance in the event: the context should not be populated and navigation
		// should be null
		action.attachObject(event);
		DocumentContext documentContext = action.getDocumentContext();
		ObjectInstance actualObjectInstance = documentContext.getInstance(ObjectInstance.class);
		assertNull(actualObjectInstance);
		String navigation = event.getNavigation();
		assertNull(navigation);

		// if there is an instance in the event: the context should be populated and navigation
		// should null because we should stay on the same page
		event = createEventObject(null, objectInstance, ObjectActionTypeConstants.ATTACH_OBJECT,
				null);
		action.attachObject(event);
		actualObjectInstance = documentContext.getInstance(ObjectInstance.class);
		assertEquals(actualObjectInstance, objectInstance);
		navigation = event.getNavigation();
		assertNull(navigation);

		// clear context
		documentContext.clear();
	}

}
