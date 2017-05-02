package com.sirma.itt.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Common functionality for unit tests in objects module.
 *
 * @author svelikov
 */
public class ObjectsTest {

	protected static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectsTest.class);

	protected static final org.slf4j.Logger SLF4J_LOG = LoggerFactory.getLogger(ObjectsTest.class);

	/**
	 * Creates the event object.
	 *
	 * @param navigation
	 *            the navigation
	 * @param instance
	 *            the instance
	 * @param actionId
	 *            the action id
	 * @param action
	 *            the action
	 * @return the cMF action event
	 */
	public EMFActionEvent createEventObject(String navigation, Instance instance, String actionId, Action action) {

		return new EMFActionEvent(instance, navigation, actionId, action);
	}

	/**
	 * Creates the case instance.
	 *
	 * @param id
	 *            the id
	 * @return the case instance
	 */
	public ObjectInstance createCaseInstance(Long id) {
		ObjectInstance caseInstance = new ObjectInstance();
		caseInstance.setId(id);
		addReference(caseInstance, id, ObjectInstance.class);
		return caseInstance;
	}

	/**
	 * Creates the section instance.
	 *
	 * @param id
	 *            the id
	 * @return the section instance
	 */
	public ObjectInstance createSectionInstance(Long id) {
		ObjectInstance sectionInstance = new ObjectInstance();
		sectionInstance.setId(id);
		sectionInstance.setIdentifier(id.toString());
		addReference(sectionInstance, id, ObjectInstance.class);
		return sectionInstance;
	}

	/**
	 * Creates the object instance.
	 *
	 * @param id
	 *            the id
	 * @return the object instance
	 */
	public ObjectInstance createObjectInstance(Long id) {
		ObjectInstance objectInstance = new ObjectInstance();
		objectInstance.setId(id);
		addReference(objectInstance, id, ObjectInstance.class);
		return objectInstance;
	}

	/**
	 * Creates the instance reference.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @return the instance reference
	 */
	public InstanceReference createInstanceReference(String id, Class<?> type) {
		DataType sourceType = new DataType();
		sourceType.setName(type.getSimpleName().toLowerCase());
		sourceType.setJavaClassName(type.getCanonicalName());
		sourceType.setJavaClass(type);
		InstanceReference ref = new LinkSourceId(id.toString(), sourceType);
		return ref;
	}

	/**
	 * Creates the document instance.
	 *
	 * @param id
	 *            the id
	 * @return the document instance
	 */
	public ObjectInstance createDocumentInstance(Long id) {
		ObjectInstance documentInstance = new ObjectInstance();
		documentInstance.setId(id);
		addReference(documentInstance, id, ObjectInstance.class);
		return documentInstance;
	}

	/**
	 * Creates the object definition.
	 *
	 * @param dmsId
	 *            the dms id
	 * @return the object definition
	 */
	public GenericDefinition createObjectDefinition(String dmsId) {
		GenericDefinitionImpl objectDefinition = new GenericDefinitionImpl();
		objectDefinition.setType("object");
		objectDefinition.setDmsId(dmsId);
		return objectDefinition;
	}

	/**
	 * Creates the link reference list.
	 *
	 * @return the list
	 */
	public List<LinkReference> createLinkReferenceList() {
		List<LinkReference> list = new ArrayList<>();
		list.add(createLinkReference());
		return list;
	}

	/**
	 * Creates the link reference.
	 *
	 * @return the link reference
	 */
	public LinkReference createLinkReference() {
		LinkReference linkReference = new LinkReference();
		return linkReference;
	}

	/**
	 * Adds the reference.
	 *
	 * @param instance
	 *            the instance
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 */
	private void addReference(Instance instance, Serializable id, Class<?> type) {
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(instance, "reference",
				createInstanceReference(id.toString(), type));
	}
}
