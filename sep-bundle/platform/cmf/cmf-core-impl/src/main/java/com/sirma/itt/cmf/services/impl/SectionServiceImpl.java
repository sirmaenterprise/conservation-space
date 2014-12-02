package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.document.DocumentCreateEvent;
import com.sirma.itt.cmf.event.document.structured.StructuredDocumentCreateNewEmptyEvent;
import com.sirma.itt.cmf.event.document.structured.StructuredDocumentOperationEvent;
import com.sirma.itt.cmf.event.section.BeforeSectionDeleteEvent;
import com.sirma.itt.cmf.event.section.BeforeSectionPersistEvent;
import com.sirma.itt.cmf.event.section.SectionChangeEvent;
import com.sirma.itt.cmf.event.section.SectionPersistedEvent;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
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
public class SectionServiceImpl implements SectionService {

	private static final Logger LOGGER = Logger.getLogger(SectionServiceImpl.class);

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The allowed children type provider. */
	@Inject
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	/** The calculator. */
	private BaseAllowedChildrenProvider<SectionInstance> calculator;

	@Inject
	@InstanceType(type = ObjectTypesCmf.SECTION)
	private InstanceDao<SectionInstance> instanceDao;

	@Inject
	@InstanceType(type = ObjectTypesCmf.DOCUMENT)
	private InstanceDao<DocumentInstance> documentInstanceDao;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private EventService eventService;

	@Inject
	private ServiceRegister serviceRegister;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		calculator = new BaseAllowedChildrenProvider<SectionInstance>(dictionaryService,
				allowedChildrenTypeProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<SectionDefinition> getInstanceDefinitionClass() {
		return SectionDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SectionInstance createInstance(SectionDefinition definition, Instance parent) {
		return createInstance(definition, parent, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SectionInstance createInstance(SectionDefinition definition, Instance parent,
			Operation operation) {
		SectionInstance instance = instanceDao.createInstance(definition, true);

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

		List<DocumentDefinitionRef> documentDefinitions = definition.getDocumentDefinitions();
		for (DocumentDefinitionRef documentDefinition : documentDefinitions) {
			// if the definition is set to be of count 0 then we ignore the
			// definition
			Integer maxInstances = documentDefinition.getMaxInstances();
			if ((maxInstances != null) && (maxInstances == 0)) {
				continue;
			}
			DocumentInstance documentInstance = createDocumentInstance(documentDefinition);

			documentInstance.setOwningInstance(instance);
			instance.getContent().add(documentInstance);

			// no actual document is created then we just remove it from the
			// section
			if (!createStructuredDocumentIfAny(documentInstance, documentDefinition)) {
				if (!"INIT".equals(documentInstance.getPurpose())) {
					documentInstance.setOwningInstance(null);
					instance.getContent().remove(instance.getContent().size() - 1);
				}
			}
		}
		return instance;
	}

	/**
	 * Creates the document instance from the given definition.
	 *
	 * @param documentDefinition
	 *            the document definition
	 * @return the document instance
	 */
	private DocumentInstance createDocumentInstance(DocumentDefinitionRef documentDefinition) {
		DocumentInstance instance = documentInstanceDao.createInstance(documentDefinition, true);
		eventService.fire(new DocumentCreateEvent(instance));
		return instance;
	}

	/**
	 * Creates the structured document if such, not initializing document and is mandatory.
	 *
	 * @param instance
	 *            the instance
	 * @param documentDefinition
	 *            the document definition
	 * @return true, if handled successfully
	 */
	private boolean createStructuredDocumentIfAny(DocumentInstance instance,
			DocumentDefinitionRef documentDefinition) {
		if (!instance.hasDocument() && Boolean.TRUE.equals(documentDefinition.getStructured())
				&& !"INIT".equals(documentDefinition.getPurpose())
				&& Boolean.TRUE.equals(documentDefinition.getMandatory())) {
			String attachmentType = (String) instance.getProperties().get(DocumentProperties.TYPE);
			if (StringUtils.isNotEmpty(attachmentType)) {
				StructuredDocumentOperationEvent event = new StructuredDocumentCreateNewEmptyEvent(
						instance, attachmentType);
				eventService.fire(event);
				FileDescriptor response = event.getDocumentResponse();
				if (event.isHandled()) {
					if (response != null) {
						// on the first save the document will be uploaded to
						// DMS
						instance.getProperties().put(DocumentProperties.FILE_LOCATOR, response);
						instance.getProperties().put(DocumentProperties.NAME, response.getId());
						return true;
					}
					LOGGER.error("Structured Document Operation Event was handled but not filled correctly: no response!");
				} else {
					LOGGER.warn("No one handled the StructuredDocumentCreateNewEmptyEvent event");
				}
			} else {
				LOGGER.error("Document definition was marked as structured but the attachment type is empty!");
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void refresh(SectionInstance instance) {
		instanceDao.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<SectionInstance> loadInstances(Instance owner) {
		return instanceDao.loadInstances(owner, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SectionInstance loadByDbId(Serializable id) {
		return fetchCaseDocument(instanceDao.loadInstance(id, null, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SectionInstance load(Serializable instanceId) {
		return fetchCaseDocument(instanceDao.loadInstance(null, instanceId, true));
	}

	/**
	 * Fetch case document.
	 *
	 * @param sectionInstance
	 *            the document instance
	 * @return the document instance
	 */
	private SectionInstance fetchCaseDocument(SectionInstance sectionInstance) {
		if (sectionInstance == null) {
			return null;
		}
		// for standalone folders there is no need to fetch anything from the upper chain
		if (sectionInstance.isStandalone()
				&& SectionProperties.PURPOSE_FOLDER.equals(sectionInstance.getPurpose())) {
			return sectionInstance;
		}
		CaseInstance instance = InstanceUtil.getParent(CaseInstance.class, sectionInstance);
		if (instance != null) {
			for (SectionInstance section : instance.getSections()) {
				if (section.getId().equals(sectionInstance.getId())) {
					return section;
				}
			}
		}
		return sectionInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<SectionInstance> load(List<S> ids) {
		return instanceDao.loadInstances(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<SectionInstance> loadByDbId(List<S> ids) {
		return instanceDao.loadInstancesByDbKey(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<SectionInstance> load(List<S> ids, boolean allProperties) {
		return instanceDao.loadInstances(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<SectionInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstancesByDbKey(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(SectionInstance owner) {
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(SectionInstance owner, String type) {
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(SectionInstance owner, String type) {
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SectionInstance save(SectionInstance instance, Operation operation) {
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

			SectionInstance old = instanceDao.persistChanges(instance);
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
	public SectionInstance cancel(SectionInstance instance) {
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	@Override
	public SectionInstance clone(SectionInstance instance, Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(SectionInstance instance, Operation operation,
			boolean permanent) {
		BeforeSectionDeleteEvent event = new BeforeSectionDeleteEvent(instance);
		eventService.fire(event);

		instanceDao.delete(instance);

		eventService.fireNextPhase(event);
	}

	@Override
	public void attach(SectionInstance targetInstance, Operation operation, Instance... children) {
		if (targetInstance == null) {
			return;
		}
		List<Instance> list = instanceDao.attach(targetInstance, operation, children);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(targetInstance);
		for (Instance instance : list) {
			if (!(instance instanceof Lockable)) {
				LOGGER.warn("Trying to attach not lockable instance to a section " + targetInstance.getIdentifier());
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
					if (operation != null) {
						if (EqualsHelper.nullSafeEquals(operation.getOperation(),
								ActionTypeConstants.UPLOAD)
								|| EqualsHelper.nullSafeEquals(operation.getOperation(),
										ActionTypeConstants.MOVE_OTHER_CASE)
								|| EqualsHelper.nullSafeEquals(operation.getOperation(),
										ActionTypeConstants.MOVE_SAME_CASE)) {
							// for upload we set the parent reference - it's the first
							ownedModel.setOwningReference(targetInstance.toReference());
						} else if (EqualsHelper.nullSafeEquals(operation.getOperation(),
								ActionTypeConstants.ATTACH_DOCUMENT)
								|| EqualsHelper.nullSafeEquals(operation.getOperation(),
										ActionTypeConstants.ATTACH_OBJECT)) {
							// we clear the owning reference because is very possible the owning
							// reference to be invalid: if was already null (from library) or if not
							ownedModel.setOwningReference(null);
						}
					} else {
						LOGGER.warn("NO operation passed when attaching " + instance.getId()
								+ " to section " + targetInstance.getIdentifier() + "("
								+ targetInstance.getId() + ")");
					}
				}
			}
			eventService.fire(event);
		}
	}

	@Override
	public void detach(SectionInstance sourceInstance, Operation operation, Instance... instances) {
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
