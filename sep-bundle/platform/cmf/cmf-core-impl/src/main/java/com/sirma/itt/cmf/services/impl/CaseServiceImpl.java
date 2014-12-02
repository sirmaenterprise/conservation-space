/*
 *
 */
package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.cases.BeforeCaseCancelEvent;
import com.sirma.itt.cmf.event.cases.BeforeCaseDeleteEvent;
import com.sirma.itt.cmf.event.cases.CaseChangeEvent;
import com.sirma.itt.cmf.event.cases.CasePersistedEvent;
import com.sirma.itt.cmf.exceptions.DmsCaseException;
import com.sirma.itt.cmf.instance.CaseAllowedChildrenProvider;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.exceptions.InstanceDeletedException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.BaseInstanceService;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.Chaining;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * Implementation for case instances services. All logic for cases should be placed here.
 *
 * @author BBonev
 */
@Stateless
public class CaseServiceImpl extends BaseInstanceService<CaseInstance, CaseDefinition> implements
		CaseService, Serializable {

	private static final long serialVersionUID = 8339234916288763604L;

	private static final SortableComparator SORTABLE_COMPARATOR = new SortableComparator();

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseServiceImpl.class);

	@Inject
	private CMFCaseInstanceAdapterService caseInstanceAdapterService;

	@Inject
	@InstanceType(type = ObjectTypesCmf.CASE)
	private InstanceDao<CaseInstance> instanceDao;

	@Inject
	private javax.enterprise.inject.Instance<WorkflowService> workflowServiceInstance;

	@Inject
	private TaskService taskService;

	@Inject
	@Chaining
	private LinkService chainigLinkService;

	@Inject
	private LinkService linkService;

	@Inject
	private CaseAllowedChildrenProvider calculator;

	@Inject
	private DocumentService documentService;
	@Inject
	private SectionService sectionService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CaseInstance save(CaseInstance instance, Operation operation) {
		// the method is overridden due to the fact that the interceptor is not notified for method
		// invocation
		return saveInternal(instance, operation);
	}

	@Override
	protected void deleteInstanceFromDms(CaseInstance instance, boolean permanent)
			throws DMSException {
		caseInstanceAdapterService.deleteCaseInstance(instance, permanent);
	}

	/**
	 * Creates the case instance in the DMS and update the fields related to creating a case.
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	@Override
	protected String createInstanceInDms(CaseInstance instance) {
		try {
			return caseInstanceAdapterService.createCaseInstance(instance);
		} catch (DMSException e) {
			throw new DmsCaseException("Error creating case instance in DMS", e);
		}
	}

	/**
	 * Called on update case instance in the DMS sub system.
	 *
	 * @param instance
	 *            the instance
	 */
	@Override
	protected void updateInstanceInDms(CaseInstance instance) {
		try {
			caseInstanceAdapterService.updateCaseInstance(instance);
		} catch (DMSException e) {
			throw new DmsCaseException("Error updating case instance in DMS", e);
		}
	}

	/**
	 * Close case instance.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @param operation
	 *            the operation
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void closeCaseInstance(CaseInstance caseInstance, Operation operation) {
		instanceDao.setCurrentUserTo(caseInstance, CaseProperties.CLOSED_BY);
		Map<String, Serializable> properties = caseInstance.getProperties();
		properties.put(CaseProperties.CLOSED_ON, new Date());
		cancelInternal(caseInstance, operation, false, true);

	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(CaseInstance caseInstance, Operation operation, boolean permanent) {
		refresh(caseInstance);
		cancelInternal(caseInstance, operation, permanent, false);
	}

	/**
	 * Internal cancel/delete method for the given operation and persist the changes. REVIEW:BB:
	 * this should separated in 2 methods or refactored other way.
	 *
	 * @param instance
	 *            is the case
	 * @param operation
	 *            is the delete/stop operation
	 * @param permanent
	 *            whether this operation is not revertable - is this delete
	 * @param onCancel
	 *            if the method is called for cancel or not
	 */
	private void cancelInternal(CaseInstance instance, Operation operation, boolean permanent,
			boolean onCancel) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			AbstractInstanceTwoPhaseEvent<?, ?> event = null;
			if (onCancel) {
				event = new BeforeCaseCancelEvent(instance);
				eventService.fire(event);

				notifyForStateChange(operation, instance);
				caseInstanceAdapterService.closeCaseInstance(instance);
			} else {
				event = new BeforeCaseDeleteEvent(instance);
				eventService.fire(event);

				// unlock documents and remove links
				unlockAllDocuments(instance, true);
				// first notify to update statutes
				notifyForStateChange(operation, instance);
			}
			cancelCaseActvities(instance, operation, permanent);
			if (!onCancel) {
				// delete case after cancellation
				deleteInstanceFromDms(instance, permanent);
			}

			// fire next part of the event
			eventService.fireNextPhase(event);

			// persist the changes
			instanceDao.instanceUpdated(instance, false);

			eventService.fire(new CaseChangeEvent(instance));

			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			CaseInstance old;
			try {
				old = instanceDao.persistChanges(instance);
			} finally {
				RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
			eventService.fire(new CasePersistedEvent(instance, old, getOperationId(operation)));
		} catch (DMSException e1) {
			throw new DmsCaseException("Failed to delete case instance from DMS on rollback", e1);
		}
	}

	/**
	 * Cancel all activities for case instance only in cmf. In Dms activities are cancelled as batch
	 * process for delete/close
	 *
	 * @param caseInstance
	 *            the instance to update
	 * @param operation
	 *            is the current operation to execute (cancel, delete)
	 * @param permanent
	 *            is whether this is permanent operation
	 */
	private void cancelCaseActvities(CaseInstance caseInstance, Operation operation,
			boolean permanent) {
		boolean activeProcesses = false;
		try {

			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);

			List<String> owningInstanceTasks = taskService.getOwnedTaskInstances(caseInstance,
					TaskState.IN_PROGRESS, TaskType.STANDALONE_TASK);
			List<LinkReference> links = linkService.getLinks(caseInstance.toReference(),
					LinkConstants.PARENT_TO_CHILD);
			List<LinkInstance> linkInstances = linkService.convertToLinkInstance(links, false);

			for (LinkInstance link : linkInstances) {
				Instance instance = link.getTo();
				// filter the active tasks only
				if ((instance instanceof StandaloneTaskInstance)
						&& owningInstanceTasks.contains(((StandaloneTaskInstance) instance)
								.getTaskInstanceId())) {
					if (operation != null) {
						if (ActionTypeConstants.DELETE.equals(operation.getOperation())) {
							taskService.delete((StandaloneTaskInstance) instance, operation,
									permanent);
						} else {
							taskService.cancel((StandaloneTaskInstance) instance);
						}
					} else {
						taskService.cancel((StandaloneTaskInstance) instance);
					}
				}
			}
			// cancel the active workflow - the case will be updated also
			// from there
			WorkflowService workflowService = workflowServiceInstance.get();
			List<WorkflowInstanceContext> list = workflowService.getCurrentWorkflow(caseInstance);
			activeProcesses = !list.isEmpty();
			for (WorkflowInstanceContext workflowInstanceContext : list) {
				if (operation != null) {
					if (ActionTypeConstants.DELETE.equals(operation.getOperation())) {
						workflowService.delete(workflowInstanceContext, operation, permanent);
					} else {
						workflowService.cancel(workflowInstanceContext);
					}
				} else {
					workflowService.cancel(workflowInstanceContext);
				}
			}

		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		}
		// handled by wf service
		// TODO needed optimization
		if (!activeProcesses) {
			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			try {
				eventService.fire(new CaseChangeEvent(caseInstance));
				// persist changes to DB
				instanceDao.instanceUpdated(caseInstance, false);
				CaseInstance old = instanceDao.persistChanges(caseInstance);

				String operationId = getOperationId(operation);
				eventService.fire(new CasePersistedEvent(caseInstance, old, operationId));
			} finally {
				RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}
	}

	/**
	 * Unlocks all documents.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @param removeLinks
	 *            the remove links
	 */
	private void unlockAllDocuments(CaseInstance caseInstance, boolean removeLinks) {
		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			for (Instance instance : sectionInstance.getContent()) {
				instance.getProperties().remove(DocumentProperties.LOCKED_BY);
				instance.getProperties().remove(DocumentProperties.WORKING_COPY_LOCATION);

				// remove all links of the documents
				if (removeLinks) {
					// we should remove all links for documents that are created into the containing
					// case/section, because they will be deleted on case deletion
					if ((instance instanceof DocumentInstance)
							&& !documentService.isAttached(sectionInstance,
									(DocumentInstance) instance)) {
						chainigLinkService.removeLinksFor(instance.toReference());
						InstanceEventProvider<Instance> provider = serviceRegister
								.getEventProvider(instance);
						if (provider != null) {
							BeforeInstanceDeleteEvent<Instance, ? extends AfterInstanceDeleteEvent<Instance, TwoPhaseEvent>> event = provider
									.createBeforeInstanceDeleteEvent(instance);
							eventService.fire(event);
							eventService.fireNextPhase(event);
						}
					} else {
						// for non document attached instances we should only remove links that are
						// between the instance and the case and not all links of these objects
						linkService.unlink(sectionInstance.toReference(), instance.toReference());

						InstanceEventProvider<Instance> eventProvider = serviceRegister
								.getEventProvider(sectionInstance);
						InstanceDetachedEvent<Instance> event = eventProvider.createDetachEvent(
								sectionInstance, instance);
						eventService.fire(event);
					}
				}
			}
		}
	}

	@Override
	public void refresh(CaseInstance instance) {
		// if not persisted we cannot refresh it or will throw an exception
		if (instance == null) {
			return;
		}
		if (InstanceUtil.isNotPersisted(instance)) {
			LOGGER.warn("Tried to refresh a case with id {} that is not persisted",
					instance.getId());
			return;
		}
		// we does not fetch the properties they will be fetched at the end of the method
		// here we only needs the base structure
		// NOTE: if needed to check for modifications of the document/case properties should be
		// fetched - the last argument should be true
		CaseInstance newInstance = instanceDao.loadInstance(instance.getId(), null, false);
		// if not found and is persisted (the check above) then the instance is deleted or invalid
		if (newInstance == null) {
			throw new InstanceDeletedException(instance.getClass().getSimpleName() + " with id "
					+ instance.getId() + " has been deleted or not found!");
		}

		// we will try to merge sections and its contents from the cache to the current instance
		Map<Serializable, SectionInstance> sectionsMap = CollectionUtils.toEntityMap(newInstance
				.getSections());
		for (Iterator<SectionInstance> it = instance.getSections().iterator(); it.hasNext();) {
			SectionInstance sectionInstance = it.next();
			SectionInstance currentSection = sectionsMap.remove(sectionInstance.getId());
			if (currentSection == null) {
				// section has been deleted
				it.remove();
				continue;
			}
			Map<Serializable, Instance> contentMap = CollectionUtils.toEntityMap(currentSection
					.getContent());
			for (Iterator<Instance> cIt = sectionInstance.getContent().iterator(); cIt.hasNext();) {
				Instance content = cIt.next();
				Instance element = contentMap.remove(content.getId());
				if (element == null) {
					// element has been deleted
					cIt.remove();
					continue;
				}
				// the element has the same id, there is nothing to update to the instance at this
				// point only the properties which will be updated at the end of the method
			}
			// add all remaining elements of the section - these are the one added and not present
			// in the current section
			sectionInstance.getContent().addAll(contentMap.values());
		}
		// add all remaining sections - these are the one added and not present in the current case
		instance.getSections().addAll(sectionsMap.values());
		// update the owning references/instances of the added children
		instance.initBidirection();

		// sort the sections to keep it consistent
		Collections.sort(instance.getSections(), SORTABLE_COMPARATOR);

		// reloads all properties, including the properties that came from the new documents
		instanceDao.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<CaseInstance> batchLoadCaseInstance(List<String> dmsIds, boolean loadAllProperties) {
		if ((dmsIds == null) || dmsIds.isEmpty()) {
			return Collections.emptyList();
		}
		return instanceDao.loadInstances(dmsIds, loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<VerificationMessage> verifyCaseInstance(CaseInstance caseInstance,
			CaseDefinition caseDefinition) {
		return Collections.emptyList();
	}

	/**
	 * Populates the given {@link CaseInstance} from the given definition. Populates all properties
	 * of the instance and also populates the defined sections and their properties and documents
	 *
	 * @param instance
	 *            is the target instance to populate
	 * @param definition
	 *            is the source definition
	 */
	@Override
	protected void populateNewInstance(CaseInstance instance, CaseDefinition definition) {

		List<SectionDefinition> sectionDefs = definition.getSectionDefinitions();
		for (SectionDefinition sectionDefinition : sectionDefs) {
			SectionInstance sectionInstance = sectionService.createInstance(sectionDefinition,
					instance);
			instance.getSections().add(sectionInstance);
		}

		for (SectionInstance sectionInstance : instance.getSections()) {
			for (Instance documentInstance : sectionInstance.getContent()) {
				// calculate and set the parent path and revision.This fields
				// should not be changed until the document instance is deleted
				((DocumentInstance) documentInstance).setParentPath(PathHelper
						.getPath(documentInstance));
				if (sectionInstance.getId() != null) {
					((DocumentInstance) documentInstance).setOwningReference(sectionInstance
							.toReference());
					((DocumentInstance) documentInstance).setOwningInstance(sectionInstance);
				}
				documentInstance.setRevision(instance.getRevision());
			}
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CaseInstance getPrimaryCaseForDocument(String dmsId) {
		DocumentInstance documentInstance = documentService.load(dmsId);
		CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class, documentInstance);
		return caseInstance;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CaseInstance createInstance(CaseDefinition definition, Instance parent) {
		return createInstance(definition, parent, new Operation(ActionTypeConstants.CREATE_CASE));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<CaseDefinition> getInstanceDefinitionClass() {
		return CaseDefinition.class;
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CaseInstance cancel(CaseInstance instance) {
		refresh(instance);
		cancelInternal(instance, new Operation(ActionTypeConstants.STOP), false, true);
		return instance;
	}

	@Override
	public CaseInstance clone(CaseInstance instance, Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attach(CaseInstance targetInstance, Operation operation, Instance... children) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detach(CaseInstance sourceInstance, Operation operation, Instance... instances) {
		// TODO Auto-generated method stub

	}

	@Override
	protected InstanceDao<CaseInstance> getInstanceDao() {
		return instanceDao;
	}

	@Override
	protected Class<CaseInstance> getInstanceClass() {
		return CaseInstance.class;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected AllowedChildrenProvider<CaseInstance> getAllowChildrenProvider() {
		return calculator;
	}

}
