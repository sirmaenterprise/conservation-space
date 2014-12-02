package com.sirma.cmf;

import java.io.Serializable;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * CMFTest.
 * 
 * @author svelikov
 */
public class CMFTest extends CmfTest {

	/** The Constant LOG. */
	protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CMFTest.class);

	/** The Constant slf4j logger. */
	protected static final Logger SLF4J_LOG = LoggerFactory.getLogger(CMFTest.class);

	/**
	 * If id is not null, then use it or return default value instead.
	 * 
	 * @param id
	 *            the id
	 * @return the id
	 */
	private Long getId(Long id) {
		return (id != null) ? id : Long.valueOf(1L);
	}

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
	 * Creates the navigation menu event.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @return the navigation menu event
	 */
	public NavigationMenuEvent createNavigationMenuEvent(String menu, String action) {
		NavigationMenuEvent event = new NavigationMenuEvent(menu, action);
		return event;
	}

	/**
	 * Creates the navigation history event.
	 * 
	 * @return the navigation history event
	 */
	public NavigationHistoryEvent createNavigationHistoryEvent() {
		NavigationHistoryEvent event = new NavigationHistoryEvent();
		return event;
	}

	/**
	 * Creates the document instance.
	 * 
	 * @param id
	 *            the id
	 * @return the document instance
	 */
	public DocumentInstance createDocumentInstance(Long id) {
		Long id2 = getId(id);
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setId(id2);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(documentInstance,
				"reference", createInstanceReference(id2.toString(), DocumentInstance.class));
		documentInstance.setProperties(new HashMap<String, Serializable>());
		return documentInstance;
	}

	/**
	 * Creates the section instance.
	 * 
	 * @param id
	 *            the id
	 * @return the section instance
	 */
	public SectionInstance createSectionInstance(Long id) {
		Long id2 = getId(id);
		SectionInstance sectionInstance = new SectionInstance();
		sectionInstance.setId(id2);
		sectionInstance.setIdentifier(id2.toString());
		return sectionInstance;
	}

	/**
	 * Creates a section instance with id and purpose.
	 * 
	 * @param id
	 *            the id
	 * @param purpose
	 *            the purpose
	 * @return the section instance
	 */
	public SectionInstance createSectionInstance(Long id, String purpose) {
		SectionInstance sectionInstance = createSectionInstance(id);
		sectionInstance.setPurpose(purpose);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(sectionInstance,
				"reference", createInstanceReference(id.toString(), SectionInstance.class));
		return sectionInstance;
	}

	/**
	 * Creates the case instance.
	 * 
	 * @param id
	 *            the id
	 * @return the case instance
	 */
	public CaseInstance createCaseInstance(Long id) {
		Long id2 = getId(id);
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId(id2);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(caseInstance, "reference",
				createInstanceReference(id2.toString(), CaseInstance.class));
		return caseInstance;
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
	 * Creates the document definition.
	 * 
	 * @param identifier
	 *            the identifier
	 * @return the document definition ref
	 */
	public DocumentDefinitionRef createDocumentDefinition(String identifier) {
		DocumentDefinitionRef documentDefinition = new DocumentDefinitionRefImpl();
		documentDefinition.setIdentifier(identifier);
		return documentDefinition;
	}

	/**
	 * Creates the workflow task instance.
	 * 
	 * @param id
	 *            the id
	 * @return the task instance
	 */
	public TaskInstance createWorkflowTaskInstance(Long id) {
		Long id2 = getId(id);
		TaskInstance taskInstance = new TaskInstance();
		taskInstance.setId(id2);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(taskInstance, "reference",
				createInstanceReference(id2.toString(), TaskInstance.class));
		return taskInstance;
	}

	/**
	 * Creates the standalone task instance.
	 * 
	 * @param id
	 *            the id
	 * @return the standalone task instance
	 */
	public StandaloneTaskInstance createStandaloneTaskInstance(Long id) {
		Long id2 = getId(id);
		StandaloneTaskInstance taskInstance = new StandaloneTaskInstance();
		taskInstance.setId(id2);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils.setField(taskInstance, "reference",
				createInstanceReference(id2.toString(), StandaloneTaskInstance.class));
		return taskInstance;
	}

	/**
	 * Creates the workflow instance.
	 * 
	 * @param id
	 *            the id
	 * @return the workflow instance context
	 */
	public WorkflowInstanceContext createWorkflowInstance(Long id) {
		Long id2 = getId(id);
		WorkflowInstanceContext workflowInstanceContext = new WorkflowInstanceContext();
		workflowInstanceContext.setId(id2);
		com.sirma.itt.commons.utils.reflection.ReflectionUtils
				.setField(workflowInstanceContext, "reference",
						createInstanceReference(id2.toString(), WorkflowInstanceContext.class));
		return workflowInstanceContext;
	}

	/**
	 * Creates the workflow definition.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the workflow definition
	 */
	public WorkflowDefinition createWorkflowDefinition(String dmsId) {
		WorkflowDefinitionImpl workflowDefinitionImpl = new WorkflowDefinitionImpl();
		workflowDefinitionImpl.setDmsId(dmsId);
		return workflowDefinitionImpl;
	}

	/**
	 * Creates the task definition.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the task definition
	 */
	public TaskDefinitionRef createTaskDefinition(String dmsId) {
		TaskDefinitionRef taskDefinition = new TaskDefinitionRefImpl();
		taskDefinition.setIdentifier(dmsId);
		return taskDefinition;
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
}
