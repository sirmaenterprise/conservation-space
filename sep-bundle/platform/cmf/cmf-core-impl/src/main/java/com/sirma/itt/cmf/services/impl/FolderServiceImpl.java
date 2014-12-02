package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.section.BeforeSectionDeleteEvent;
import com.sirma.itt.cmf.event.section.BeforeSectionPersistEvent;
import com.sirma.itt.cmf.event.section.SectionChangeEvent;
import com.sirma.itt.cmf.event.section.SectionPersistedEvent;
import com.sirma.itt.cmf.services.FolderService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.Lockable;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default section implementation
 *
 * @author BBonev
 */
@Stateless
public class FolderServiceImpl implements FolderService {

	private static final Logger LOGGER = Logger.getLogger(FolderServiceImpl.class);

	@Inject
	private DMSInstanceAdapterService genericAdapterService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The allowed children type provider. */
	@Inject
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	/** The calculator. */
	private BaseAllowedChildrenProvider<FolderInstance> calculator;

	@Inject
	@InstanceType(type = ObjectTypesCmf.FOLDER)
	private InstanceDao<FolderInstance> instanceDao;

	@Inject
	private EventService eventService;

	@Inject
	private ServiceRegister serviceRegister;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		calculator = new BaseAllowedChildrenProvider<FolderInstance>(dictionaryService,
				allowedChildrenTypeProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<GenericDefinition> getInstanceDefinitionClass() {
		return GenericDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FolderInstance createInstance(GenericDefinition definition, Instance parent) {
		return createInstance(definition, parent, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FolderInstance createInstance(GenericDefinition definition, Instance parent,
			Operation operation) {
		FolderInstance instance = instanceDao.createInstance(definition, true);

		String definitionPath = definition.getIdentifier();
		if (parent != null) {
			instance.setOwningInstance(parent);
			if (parent.getId() != null) {
				instance.setOwningReference(parent.toReference());
			}
			// this is valid only for non standalone definitions
			if (!instance.isStandalone()) {
				definitionPath = parent.getIdentifier() + PathElement.PATH_SEPARATOR
						+ definition.getIdentifier();
			}
		}
		instance.setDefinitionPath(definitionPath);
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void refresh(FolderInstance instance) {
		instanceDao.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<FolderInstance> loadInstances(Instance owner) {
		return instanceDao.loadInstances(owner, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FolderInstance loadByDbId(Serializable id) {
		return instanceDao.loadInstance(id, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FolderInstance load(Serializable instanceId) {
		return instanceDao.loadInstance(null, instanceId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<FolderInstance> load(List<S> ids) {
		return instanceDao.loadInstances(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<FolderInstance> loadByDbId(List<S> ids) {
		return instanceDao.loadInstancesByDbKey(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<FolderInstance> load(List<S> ids, boolean allProperties) {
		return instanceDao.loadInstances(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<FolderInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstancesByDbKey(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(FolderInstance owner) {
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(FolderInstance owner, String type) {
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(FolderInstance owner, String type) {
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FolderInstance save(FolderInstance instance, Operation operation) {
		// 1. call the adapter 2. Convert to caseEntity 3.persist data, 4.
		// persist properties
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			eventService.fire(new SectionChangeEvent(instance));

			BeforeSectionPersistEvent event = null;
			if (!SequenceEntityGenerator.isPersisted(instance)) {
				event = new BeforeSectionPersistEvent(instance);
				eventService.fire(event);
			}

			instanceDao.instanceUpdated(instance, false);

			FolderInstance old = instanceDao.persistChanges(instance);
			if (event != null) {
				eventService.fireNextPhase(event);
			}
			if (!RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT)) {
				eventService.fire(new SectionPersistedEvent(instance, old,
						getOperationId(operation)));
			}
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
		return instance;
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FolderInstance cancel(FolderInstance instance) {
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	@Override
	public FolderInstance clone(FolderInstance instance, Operation operation) {
		return null;
	}

	@Override
	public void delete(FolderInstance instance, Operation operation,
			boolean permanent) {
		BeforeSectionDeleteEvent event = new BeforeSectionDeleteEvent(instance);
		eventService.fire(event);

		boolean success = deleteFromDms(instance);

		if (success) {
			// we should add here deletion of all children in that folder because they are now all
			// gone in the DMS

			instanceDao.delete(instance);

			eventService.fireNextPhase(event);
		}
	}

	/**
	 * Delete from dms.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	private boolean deleteFromDms(FolderInstance instance) {
		if (RuntimeConfiguration.isSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)) {
			return true;
		}
		try {
			return genericAdapterService.deleteNode(instance);
		} catch (DMSException e) {
			throw new EmfRuntimeException("Failed to delete folder from DMS due to error: ", e);
		}
	}

	@Override
	public void attach(FolderInstance targetInstance, Operation operation, Instance... children) {
		if (targetInstance == null) {
			return;
		}
		String operationId = null;
		if (operation != null) {
			operationId = operation.getOperation();
		} else {
			LOGGER.warn("NO operation passed when attaching instances to folder " + targetInstance.getIdentifier() + "("
					+ targetInstance.getId() + ")");
		}

		List<Instance> list = instanceDao.attach(targetInstance, operation, children);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(targetInstance);
		for (Instance instance : list) {
			if (!(instance instanceof Lockable) || !(instance instanceof FolderInstance)) {
				LOGGER.warn("Trying to attach not lockable instance to a folder "
						+ targetInstance.getIdentifier());
				continue;
			}
			InstanceAttachedEvent<Instance> event = eventProvider.createAttachEvent(targetInstance,
					instance);
			if (!targetInstance.getContent().contains(instance)) {
				targetInstance.getContent().add(instance);
				if (instance instanceof OwnedModel) {
					OwnedModel ownedModel = (OwnedModel) instance;
					// we set the owning instance to the partOf semantic relations to be created
					ownedModel.setOwningInstance(targetInstance);
					// if the instance has owning reference that we are attaching it more than once
					// so we should not override the original reference
					// if the reference is empty means that it's probably the first time the
					// instance is attached to anything

					if (EqualsHelper.nullSafeEquals(operationId, ActionTypeConstants.UPLOAD)
							|| EqualsHelper.nullSafeEquals(operationId,
									ActionTypeConstants.MOVE_OTHER_CASE)
							|| EqualsHelper.nullSafeEquals(operationId,
									ActionTypeConstants.MOVE_SAME_CASE)) {
						// for upload we set the parent reference - it's the first
						ownedModel.setOwningReference(targetInstance.toReference());
					} else if (EqualsHelper.nullSafeEquals(operationId,
							ActionTypeConstants.ATTACH_DOCUMENT)
							|| EqualsHelper.nullSafeEquals(operationId,
									ActionTypeConstants.ATTACH_OBJECT)) {
						// we clear the owning reference because is very possible the owning
						// reference to be invalid: if was already null (from library) or if not
						ownedModel.setOwningReference(null);
					}
				}
			}
			eventService.fire(event);
		}
	}

	@Override
	public void detach(FolderInstance sourceInstance, Operation operation, Instance... instances) {
		if (sourceInstance == null) {
			return;
		}
		List<Instance> list = instanceDao.detach(sourceInstance, operation, instances);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(sourceInstance);
		for (Instance instance : list) {
			InstanceDetachedEvent<Instance> event = eventProvider.createDetachEvent(sourceInstance, instance);
			sourceInstance.getContent().remove(instance);
			eventService.fire(event);
		}
	}

	/**
	 * Gets the operation id.
	 *
	 * @param operation
	 *            the operation
	 * @return the operation id
	 */
	protected String getOperationId(Operation operation) {
		String operationId = null;
		if (operation != null) {
			operationId = operation.getOperation();
		}
		return operationId;
	}
}
