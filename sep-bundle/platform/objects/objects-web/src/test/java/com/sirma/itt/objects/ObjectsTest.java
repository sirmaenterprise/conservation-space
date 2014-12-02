package com.sirma.itt.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Common functionality for unit tests in objects module.
 * 
 * @author svelikov
 */
public class ObjectsTest {

	protected static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(ObjectsTest.class);

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
	public EMFActionEvent createEventObject(String navigation, Instance instance, String actionId,
			Action action) {

		return new EMFActionEvent(instance, navigation, actionId, action);
	}

	/**
	 * Creates the case instance.
	 * 
	 * @param id
	 *            the id
	 * @return the case instance
	 */
	public CaseInstance createCaseInstance(Long id) {
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId(id);
		addReference(caseInstance, id, CaseInstance.class);
		return caseInstance;
	}

	/**
	 * Creates the section instance.
	 * 
	 * @param id
	 *            the id
	 * @return the section instance
	 */
	public SectionInstance createSectionInstance(Long id) {
		SectionInstance sectionInstance = new SectionInstance();
		sectionInstance.setId(id);
		sectionInstance.setIdentifier(id.toString());
		addReference(sectionInstance, id, SectionInstance.class);
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
	public DocumentInstance createDocumentInstance(Long id) {
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(id);
		addReference(documentInstance, id, DocumentInstance.class);
		return documentInstance;
	}

	/**
	 * Creates the object definition.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the object definition
	 */
	public ObjectDefinition createObjectDefinition(String dmsId) {
		ObjectDefinitionImpl objectDefinition = new ObjectDefinitionImpl();
		objectDefinition.setDmsId(dmsId);
		return objectDefinition;
	}

	/**
	 * Creates the case definition.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the case definition
	 */
	public CaseDefinition createCaseDefinition(String dmsId) {
		CaseDefinition caseDefinition = new CaseDefinitionImpl();
		caseDefinition.setDmsId(dmsId);
		return caseDefinition;
	}

	/**
	 * Creates the link reference list.
	 * 
	 * @return the list
	 */
	public List<LinkReference> createLinkReferenceList() {
		List<LinkReference> list = new ArrayList<LinkReference>();
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
