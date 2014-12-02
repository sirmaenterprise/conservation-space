/*
 *
 */
package com.sirma.itt.cmf.services.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.UploadUnit;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.beans.model.WorkflowInfo;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.document.AfterDocumentCopyEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentCancelCheckOutEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentCheckInEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentCheckOutEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentCopyEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentDeleteEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentLockEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentMoveEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentPersistEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentRevertEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentUnlockEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentUpdateEvent;
import com.sirma.itt.cmf.event.document.BeforeDocumentUploadEvent;
import com.sirma.itt.cmf.event.document.DocumentChangeEvent;
import com.sirma.itt.cmf.event.document.DocumentCreateEvent;
import com.sirma.itt.cmf.event.document.DocumentPersistedEvent;
import com.sirma.itt.cmf.exceptions.DmsCaseException;
import com.sirma.itt.cmf.exceptions.DmsDocumentException;
import com.sirma.itt.cmf.instance.DocumentAllowedChildrenProvider;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.cmf.services.adapter.CMFContentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.DocumentInfoOperation;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.DocumentOperation;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.concurrent.WorkRecoveryLevel;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.CmfSecurityException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.exceptions.InstanceDeletedException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.DmsAware;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.io.ContentService;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default implementation of the {@link DocumentService}.
 * 
 * @author BBonev
 */
@Stateless
public class DocumentServiceImpl implements DocumentService {
	private static final Operation EDIT_DETAILS = new Operation(ActionTypeConstants.EDIT_DETAILS);

	/**
	 * The Enum AllowUpload to indicate allowed uploads modes. <br>
	 * TODO move in proper place
	 */
	enum AllowUpload {
		/** The create from definition. */
		CREATE_FROM_DEFINITION,
		/** The return first empty. */
		RETURN_FIRST_EMPTY,
		/** The return copy of first empty. */
		RETURN_COPY_OF_FIRST_EMPTY,
		/** The not allowed. */
		NOT_ALLOWED;
	}

	/** The Constant UPLOAD_OPERATION. */
	private static final Operation UPLOAD_OPERATION = new Operation(ActionTypeConstants.UPLOAD);
	private static final Operation MOVE_OTHER_CASE = new Operation(
			ActionTypeConstants.MOVE_OTHER_CASE);
	private static final Operation MOVE_SAME_CASE = new Operation(
			ActionTypeConstants.MOVE_SAME_CASE);
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceImpl.class);

	/** The adapter services. */
	@Inject
	private CMFDocumentAdapterService documentAdapterService;

	/** The generic adapter service. */
	@Inject
	private DMSInstanceAdapterService genericAdapterService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The case event. */
	@Inject
	private EventService eventService;

	/** The content service. */
	@Inject
	private ContentService contentService;

	/** The authentication service. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	/** The document instance dao instance. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.DOCUMENT)
	private InstanceDao<DocumentInstance> documentInstanceDao;

	/** The content adapter service. */
	@Inject
	private CMFContentAdapterService contentAdapterService;

	/** The db dao. */
	@Inject
	protected DbDao dbDao;

	/** The case instance service. */
	@Inject
	private javax.enterprise.inject.Instance<CaseService> caseInstanceService;

	/** The link service. */
	@Inject
	@RelationalDb
	private LinkService linkService;

	@Inject
	private TaskExecutor taskExecutor;
	/** The evaluator manager. */
	@Inject
	private ExpressionsManager evaluatorManager;

	@Inject
	private SectionService sectionService;

	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE)
	private String documentDefaultType;

	@Inject
	private DocumentAllowedChildrenProvider documentCalculator;

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	@Inject
	private RenditionService renditionService;

	@Inject
	private DraftService draftService;

	@Override
	@Secure
	public Map<String, Serializable> checkIn(DocumentInstance documentInstance) {
		return checkIn(documentInstance, getCurrentUser());
	}

	/**
	 * Gets the current user.
	 * 
	 * @return the current user
	 */
	private String getCurrentUser() {
		try {
			return authenticationService.get().getCurrentUserId();
		} catch (ContextNotActiveException e) {
			User authentication = SecurityContextManager.getFullAuthentication();
			return authentication == null ? null : authentication.getName();
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Map<String, Serializable> checkIn(DocumentInstance documentInstance, String userId) {
		if (!documentInstance.isWorkingCopy()) {
			throw new EmfRuntimeException("Document is not checked out!");
		}
		String lockedBy = (String) documentInstance.getProperties().get(
				DocumentProperties.LOCKED_BY);
		if (!EqualsHelper.nullSafeEquals(userId, lockedBy, true)) {
			throw new CmfSecurityException("Not same user that locked the document");
		}

		try {
			BeforeDocumentCheckInEvent event = new BeforeDocumentCheckInEvent(documentInstance,
					userId);
			eventService.fire(event);

			FileDescriptor descriptor = (FileDescriptor) documentInstance.getProperties()
					.get(DocumentProperties.FILE_LOCATOR);
			FileAndPropertiesDescriptor descriptorResult = documentAdapterService
					.uploadNewVersion(documentInstance, new UploadWrapperDescriptor(descriptor,
							null, null));
			Map<String, Serializable> map = descriptorResult.getProperties();
			// clear properties associated with checkIn operation
			documentInstance.getProperties().remove(DocumentProperties.FILE_LOCATOR);
			documentInstance.getProperties().remove(DocumentProperties.WORKING_COPY_LOCATION);
			documentInstance.getProperties().remove(DocumentProperties.LOCKED_BY);

			documentInstance.getProperties().put(DocumentProperties.VERSION,
					map.get(DocumentProperties.VERSION));

			eventService.fireNextPhase(event);
			saveChanges(documentInstance);
			return map;
		} catch (DMSException e) {
			throw new DmsDocumentException("Failed to check in document: "
					+ documentInstance.getProperties().get(DocumentProperties.NAME), e);
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void checkOut(DocumentInstance documentInstance) {
		checkOut(documentInstance, getCurrentUser());
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void checkOut(DocumentInstance documentInstance, String userId) {
		if (documentInstance.isWorkingCopy()) {
			throw new EmfRuntimeException("Document already locked for editing by "
					+ documentInstance.getProperties().get(DocumentProperties.LOCKED_BY));
		}
		if (documentInstance.isLocked()) {
			throw new EmfRuntimeException("Document already locked by "
					+ documentInstance.getProperties().get(DocumentProperties.LOCKED_BY));
		}
		try {
			BeforeDocumentCheckOutEvent event = new BeforeDocumentCheckOutEvent(documentInstance,
					userId);
			eventService.fire(event);
			FileAndPropertiesDescriptor checkOut = documentAdapterService.checkOut(
					documentInstance, userId);
			Map<String, Serializable> map = checkOut.getProperties();

			Serializable serializable = map.get(DocumentProperties.WORKING_COPY_LOCATION);
			if (serializable instanceof FileDescriptor) {
				FileDescriptor descriptor = (FileDescriptor) serializable;
				documentInstance.getProperties().put(DocumentProperties.WORKING_COPY_LOCATION,
						descriptor.getId());
			} else {
				// TODO: try to unlock the document if locked at all
				throw new DmsDocumentException("Failed to check out document: "
						+ documentInstance.getProperties().get(DocumentProperties.NAME));
			}

			if (map.containsKey(DocumentProperties.LOCKED_BY)) {
				documentInstance.getProperties().put(DocumentProperties.LOCKED_BY,
						map.get(DocumentProperties.LOCKED_BY));
			} else if (userId != null) {
				documentInstance.getProperties().put(DocumentProperties.LOCKED_BY, userId);
			} else {
				// TODO: try to unlock the document if locked at all
				throw new DmsDocumentException("Failed to aquare the lock owner for document "
						+ documentInstance.getProperties().get(DocumentProperties.NAME));
			}
			eventService.fireNextPhase(event);
			// update document instance and persist properties
			saveChanges(documentInstance);
		} catch (DMSException e) {
			throw new DmsDocumentException("Failed to check out document: "
					+ documentInstance.getProperties().get(DocumentProperties.NAME), e);
		}
	}

	@Secure
	@Override
	public void cancelCheckOut(DocumentInstance documentInstance) {
		if (!documentInstance.isWorkingCopy()) {
			return;
		}
		BeforeDocumentCancelCheckOutEvent event = new BeforeDocumentCancelCheckOutEvent(
				documentInstance);
		eventService.fire(event);

		try {
			FileAndPropertiesDescriptor descriptor = documentAdapterService
					.performDocumentOperation(documentInstance, DocumentOperation.CANCEL_CHECKOUT);
			Map<String, Serializable> performDocumentOperation = descriptor.getProperties();
			// update the dms id
			String documentDmsId = (String) performDocumentOperation
					.get(DocumentProperties.ATTACHMENT_LOCATION);
			if (documentDmsId == null) {
				throw new DMSException("Document model is violated!");
			}
			documentInstance.setDmsId(documentDmsId);
			documentInstance.getProperties().remove(DocumentProperties.WORKING_COPY_LOCATION);
			documentInstance.getProperties().remove(DocumentProperties.LOCKED_BY);

			eventService.fireNextPhase(event);
			saveChanges(documentInstance);
		} catch (DMSException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Faided to cancel checkout: ", e);
			} else {
				LOGGER.warn("Faided to cancel checkout: {}", e.getMessage());
			}
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateProperties(DocumentInstance documentInstance) {
		if (documentInstance.getProperties().containsKey(DocumentProperties.WORKING_COPY_LOCATION)) {
			throw new DmsDocumentException("Cannot update properties on working copy");
		}
		try {
			BeforeDocumentUpdateEvent event = new BeforeDocumentUpdateEvent(documentInstance);
			eventService.fire(event);
			Map<String, Serializable> updateNode = genericAdapterService
					.updateNode(documentInstance);
			// TODO may be only specific
			if (updateNode != null) {
				// remove modified on if returned because it breaks the stale data checks
				updateNode.remove(DefaultProperties.MODIFIED_ON);
				updateNode.remove(DefaultProperties.MODIFIED_BY);
				documentInstance.getProperties().putAll(updateNode);
			}
			eventService.fireNextPhase(event);
			// synch with local DB
			saveChanges(documentInstance);
		} catch (DMSException e) {
			throw new DmsDocumentException(e.getMessage(), e);
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FileDescriptor getDocumentPreview(DocumentInstance instance) {
		if (instance != null) {
			try {
				Serializable mimetypeCurrent = instance.getProperties().get(
						DocumentProperties.MIMETYPE);
				String mimetypeTarget = "application/pdf";
				if ((mimetypeCurrent != null) && mimetypeCurrent.toString().startsWith("image/")) {
					mimetypeTarget = mimetypeCurrent.toString();
				}
				return documentAdapterService.getDocumentPreview(instance, mimetypeTarget);
			} catch (Exception e) {
				LOGGER.warn("Failed to retrieve the document preview for "
						+ instance.getIdentifier() + " from DMS");
				LOGGER.trace("And the exception: ", e);
			}
		}
		return null;
	}

	@Override
	@Secure
	public DocumentInstance getDocumentVersion(DocumentInstance instance, String version)
			throws DmsDocumentException {
		try {
			DocumentInstance documentVersion = documentAdapterService.getDocumentVersion(instance,
					version);
			documentVersion.setOwningInstance(instance.getOwningInstance());
			// set link to original instance.
			documentVersion.getProperties().put(
					DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE, instance);
			// @Deprecated(add explicitly an id because the historical document instance is
			// created on the fly and there is no one we need it in order to allow
			// document header generation in the page)
			//
			//
			// FIX set the actual id because of semantic
			documentVersion.setId(instance.getId());
			eventService.fire(new DocumentChangeEvent(documentVersion));
			return documentVersion;
		} catch (Exception e) {
			throw new DmsDocumentException("Failed to retrieve version of document: "
					+ instance.getProperties().get(DocumentProperties.NAME), e);
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DocumentInstance revertVersion(DocumentInstance instance, String version)
			throws DmsDocumentException {
		return revertInternal(instance, version, true);
	}

	/**
	 * Internal method for revert operation.
	 * 
	 * @param instance
	 *            the instance
	 * @param version
	 *            the version
	 * @param saveChanges
	 *            the save changes
	 * @return the document instance
	 */
	private DocumentInstance revertInternal(DocumentInstance instance, String version,
			boolean saveChanges) {
		try {
			BeforeDocumentRevertEvent event = new BeforeDocumentRevertEvent(instance, version);
			eventService.fire(event);
			LOGGER.debug("Before revert: {}", instance.getProperties());

			// remove this and retrieve data from dms later
			instance.getProperties().remove(DocumentProperties.DOCUMENT_SIGNED_DATE);
			instance.getProperties().remove(DocumentProperties.THUMBNAIL_IMAGE);
			// get the dms copy
			DocumentInstance documentVersion = documentAdapterService.revertVersion(instance,
					version);

			LOGGER.debug("After revert: {}", documentVersion.getProperties());

			// remove the version data.
			documentVersion.getProperties().remove(DocumentProperties.FILE_LOCATOR);
			documentVersion.getProperties().remove(DocumentProperties.IS_MAJOR_VERSION);
			// override the date returned by alfresco to prevent stale data exceptions
			documentVersion.getProperties().put(DocumentProperties.MODIFIED_ON, new Date());

			eventService.fireNextPhase(event);

			if (saveChanges) {
				// mark the document and the case as modified and save the changes
				saveChanges(instance);
			}
			return documentVersion;
		} catch (Exception e) {
			throw new DmsDocumentException("Failed to retriev version of document: "
					+ instance.getProperties().get(DocumentProperties.NAME), e);
		}
	}

	/**
	 * Save document and case changes.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param operation
	 *            performed operation
	 */
	private void saveChanges(DocumentInstance documentInstance, Operation operation) {
		if (documentInstance != null) {
			Operation localOperation = operation;
			if (localOperation == null) {
				localOperation = EDIT_DETAILS;
			}
			// if document is part of a case then we update the document and
			// persist the changes via
			// case instance service
			CaseInstance caseInstance = InstanceUtil
					.getParent(CaseInstance.class, documentInstance);
			if (caseInstance != null) {
				saveInternal(documentInstance, localOperation);
				saveContainingCase(caseInstance, EDIT_DETAILS);
			} else {
				saveInternal(documentInstance, localOperation);
				LOGGER.warn("Given document instance is not attached to a case");
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(documentInstance.toString());
				}
			}
		}

	}

	/**
	 * Save document and case changes.
	 * 
	 * @param documentInstance
	 *            the document instance
	 */
	private void saveChanges(DocumentInstance documentInstance) {
		saveChanges(documentInstance, null);
	}

	/**
	 * Save containing case.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @param operation
	 *            the operation
	 */
	private void saveContainingCase(Instance caseInstance, Operation operation) {
		if (!(caseInstance instanceof CaseInstance)) {
			return;
		}
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		// disable DMS update
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		try {
			caseInstanceService.get().save((CaseInstance) caseInstance, operation);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
		}
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<DocumentInfoOperation, Serializable> getDocumentInfo(DocumentInstance instance,
			Set<DocumentInfoOperation> info) {
		if ((instance == null) || (instance.getDmsId() == null)) {
			// no info for not created document in DMS
			Map<DocumentInfoOperation, Serializable> infoResult = CollectionUtils.createHashMap(2);
			infoResult.put(DocumentInfoOperation.DOCUMENT_VERSIONS, new ArrayList<VersionInfo>());
			return infoResult;
		}
		try {
			Map<DocumentInfoOperation, Serializable> result = documentAdapterService
					.performDocumentInfoOperation(instance, info);
			for (DocumentInfoOperation documentInfoOperation : info) {
				switch (documentInfoOperation) {
					case DOCUMENT_VERSIONS:
						break;
					case DOCUMENT_INFO:
						// TODO - may be some filter
						result.put(documentInfoOperation, (Serializable) instance.getProperties());
						break;
					case DOCUMENT_WORKFLOWS:
						result.put(documentInfoOperation, new ArrayList<WorkflowInfo>());
						break;
					default:
						break;
				}

			}
			return result;
		} catch (Exception e) {
			LOGGER.warn("Failed to get document info for document "
					+ instance.getProperties().get(DocumentProperties.NAME) + " due to "
					+ e.getMessage());
			LOGGER.debug("And the exception: ", e);
		}
		return Collections.emptyMap();
	}

	@Override
	@Secure
	public boolean deleteDocument(DocumentInstance instance) {
		if (instance == null) {
			return false;
		}
		Instance context = InstanceUtil.getParentContext(instance, true);
		if ((context != null) && !SequenceEntityGenerator.isPersisted(context)) {
			return false;
		}

		BeforeDocumentDeleteEvent event = new BeforeDocumentDeleteEvent(instance);
		eventService.fire(event);

		// if the document instance is a link to a document, we
		// should not delete the original document from the DMS
		boolean success = deleteFromDms(instance, context);
		if (!success) {
			// failed to delete the document in DMS cannot continue
			return false;
		}
		unlinkFromOwningInstance(instance);
		draftService.delete(instance);
		// and then from the database
		documentInstanceDao.delete(instance);
		saveContainingCase(context, EDIT_DETAILS);
		eventService.fireNextPhase(event);
		return true;
	}

	/**
	 * Unlink from owning instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	private void unlinkFromOwningInstance(DocumentInstance instance) {
		// entity found so delete it from the list and from database
		Instance owningInstance = instance.getOwningInstance();
		// remove it from the section if any
		if (owningInstance instanceof SectionInstance) {
			SectionInstance section = (SectionInstance) owningInstance;
			section.getContent().remove(instance);
			linkService.unlink(section.toReference(), instance.toReference(),
					LinkConstantsCmf.SECTION_TO_CHILD, null);
		}
	}

	/**
	 * Delete from dms.
	 * 
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 * @return true, if successful
	 */
	private boolean deleteFromDms(DocumentInstance instance, Instance context) {
		if (!RuntimeConfiguration.isSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)
				&& instance.hasDocument()) {
			try {
				// imported documents are just delete nothing fancy to do with them
				if ((context == null)
						|| Boolean.TRUE.equals(instance.getProperties().get(
								DefaultProperties.IS_IMPORTED))) {
					return genericAdapterService.deleteNode(instance);
				} else if (!isAttachedInternal(null, instance) && (context instanceof DmsAware)) {
					instance.getProperties().put(DocumentProperties.CASE_DMS_ID,
							((DmsAware) context).getDmsId());
					return documentAdapterService.deleteAttachment(instance) != null;
				}
			} catch (DMSException e) {
				throw new DmsDocumentException("Failed to delete document from DMS", e);
			}
		}
		// nothing to do with the document continue with delete
		return true;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isAttached(Instance parent, DocumentInstance child) {
		return isAttachedInternal(parent, child);
	}

	/**
	 * Checks if is attached internal.
	 * 
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 * @return true, if is attached internal
	 */
	private boolean isAttachedInternal(Instance parent, DocumentInstance child) {
		InstanceReference local;
		if (parent != null) {
			local = parent.toReference();
		} else {
			local = child.getOwningReference();
		}
		if ((local == null) && (child.getOwningInstance() != null)) {
			local = child.getOwningInstance().toReference();
		}
		// if parent is unknown so we consider the instance for attached hence standalone
		if (local == null) {
			return true;
		}
		return !linkService.isLinked(child.toReference(), local, LinkConstants.PRIMARY_PARENT);
	}

	@Override
	public boolean upload(Instance targetContainer, boolean parallelUpload,
			Collection<UploadUnit> units, WorkRecoveryLevel recoveryLevel) {
		if ((units == null) || units.isEmpty()) {
			return true;
		}
		// is is not the actual implementation!!!! this is only for until decided what to do with it
		List<DocumentInstance> documentInstances = new ArrayList<DocumentInstance>(
				units.size() >> 1);
		for (UploadUnit uploadUnit : units) {
			documentInstances.addAll(uploadUnit.getUnitData());
		}

		return upload(targetContainer, parallelUpload,
				documentInstances.toArray(new DocumentInstance[documentInstances.size()]));
	}

	@Override
	@Secure
	public boolean upload(Instance targetContainer, boolean parallelUpload,
			DocumentInstance... instances) {
		if ((instances == null) || (instances.length == 0)) {
			// nothing to upload
			return true;
		}
		// if we does not require parallel upload or the document is only one then we will upload it
		// in the current thread
		if (!parallelUpload || (instances.length == 1)) {
			List<DocumentInstance> failedDocuments = new LinkedList<>();
			for (DocumentInstance documentInstance : instances) {
				if (uploadInternal(documentInstance)) {
					// if the file was successfully uploaded
					// we persist and update the instance if needed
					saveInternal(documentInstance, UPLOAD_OPERATION);
					// attach the uploaded instances to the target container
					instanceService.attach(targetContainer, UPLOAD_OPERATION, documentInstance);
				} else {
					failedDocuments.add(documentInstance);
				}
			}
			return failedDocuments.isEmpty();
		}

		// parallel loading
		List<DocumentUploadTask> tasks = new ArrayList<>(instances.length);
		for (DocumentInstance documentInstance : instances) {
			tasks.add(new DocumentUploadTask(documentInstance, targetContainer));
		}
		taskExecutor.execute(tasks);

		// on successful document upload persist changes for the documents
		for (DocumentInstance documentInstance : instances) {
			saveInternal(documentInstance, UPLOAD_OPERATION);
		}

		// attach the uploaded instances to the target container
		instanceService.attach(targetContainer, UPLOAD_OPERATION, instances);
		return true;
	}

	/**
	 * Upload internal.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @return true, if successful
	 */
	private boolean uploadInternal(DocumentInstance documentInstance) {
		Map<String, Serializable> properties = documentInstance.getProperties();
		if (properties == null) {
			if (documentInstance.getId() == null) {
				LOGGER.warn("No properties for non persisted document instance!");
				return true;
			}
			LOGGER.warn("Document instance properties were missing!! Fetching them from DB.");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The invalid document instance" + documentInstance);
			}
			Map<String, Serializable> map = propertiesService.getEntityProperties(documentInstance,
					documentInstance.getRevision(), documentInstance);
			documentInstance.setProperties(map);
			properties = map;
		}
		Serializable serializable = properties.get(DocumentProperties.FILE_LOCATOR);
		// if we have file locator then we need to upload the document to DMS
		// system after the successful upload we remove the locator
		if (serializable instanceof FileDescriptor) {
			FileDescriptor descriptor = (FileDescriptor) serializable;

			// call adapter to upload the given document and return the
			// document properties to update
			Map<String, Serializable> returnProperties = null;
			BeforeDocumentUploadEvent event = new BeforeDocumentUploadEvent(documentInstance);
			try {
				eventService.fire(event);

				// if we have not just signed the document then we need to
				// remove the signed date if
				// any
				if (!properties.containsKey(DocumentProperties.DOCUMENT_SIGNED)) {
					properties.remove(DocumentProperties.DOCUMENT_SIGNED_DATE);
				}

				properties.remove(DocumentProperties.THUMBNAIL_IMAGE);

				List<PropertyDefinition> list = new LinkedList<>();

				DefinitionModel model = dictionaryService.getInstanceDefinition(documentInstance);
				list.addAll(model.getFields());
				if (model instanceof RegionDefinitionModel) {
					for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model)
							.getRegions()) {
						list.addAll(regionDefinition.getFields());
					}
				}
				TimeTracker tracker = null;
				final boolean trace = LOGGER.isTraceEnabled();
				if (trace) {
					tracker = new TimeTracker().begin();
				}
				documentInstanceDao.preSaveInstance(documentInstance, list);
				FileAndPropertiesDescriptor uploadToDms = uploadToDms(documentInstance,
						descriptor);
				returnProperties = uploadToDms.getProperties();
				if (trace) {
					LOGGER.trace("Document uploaded to DMS in " + tracker.stopInSeconds() + " sec");
				}
			} catch (DMSException e) {
				throw new DmsDocumentException("Failed to upload document to DMS: "
						+ e.getMessage(), e);
			} finally {
				// this properties should be removed whatever the result from
				// the upload
				properties.remove(DocumentProperties.FILE_LOCATOR);
				properties.remove(DocumentProperties.IS_MAJOR_VERSION);
				properties.remove(DocumentProperties.DOCUMENT_SIGNED);
				properties.remove(DocumentProperties.VERSION_DESCRIPTION);
				properties.remove(DocumentProperties.DOCUMENT_THUMB_MODE);
			}
			if ((returnProperties != null) && !returnProperties.isEmpty()) {
				// on successful upload remove the locator
				// and update document properties
				properties.remove(DocumentProperties.LOCKED_BY);
				properties.remove(DocumentProperties.WORKING_COPY_LOCATION);
				Serializable location = returnProperties
						.remove(DocumentProperties.ATTACHMENT_LOCATION);
				if (location instanceof FileDescriptor) {
					FileDescriptor fileDescriptor = (FileDescriptor) location;
					// add the concrete attachment location
					properties.put(DocumentProperties.ATTACHMENT_LOCATION, fileDescriptor.getId());
					documentInstance.setDmsId(fileDescriptor.getId());
				} else if (location instanceof String) {
					properties.put(DocumentProperties.ATTACHMENT_LOCATION, location);
					documentInstance.setDmsId(location.toString());
				}
				Serializable documentVersion = returnProperties.remove(DocumentProperties.VERSION);
				if (documentVersion != null) {
					properties.put(DocumentProperties.VERSION, documentVersion);
				} else {
					LOGGER.warn("Document version was NOT returned on file upload!");
				}
				// optionally update the mimetype of the file
				Serializable mimeType = returnProperties.get(DocumentProperties.MIMETYPE);
				if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty((String) mimeType)) {
					// mimetype conversion to CL value have been done in the
					// adapter
					properties.put(DocumentProperties.MIMETYPE, mimeType);
				}

				properties.putAll(returnProperties);

				// extract and set content
				extractContent(documentInstance, descriptor);

				eventService.fireNextPhase(event);
				return true;
			}
		} else {
			// if we have nothing to upload we return success
			return true;
		}
		return false;
	}

	/**
	 * Upload to dms.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param descriptor
	 *            the descriptor
	 * @return the map
	 * @throws DMSException
	 *             the dMS exception
	 */
	protected FileAndPropertiesDescriptor uploadToDms(DocumentInstance documentInstance,
			FileDescriptor descriptor) throws DMSException {
		if ((documentInstance.getOwningReference() == null)
				|| (documentInstance.getOwningInstance() == null)) {
			return genericAdapterService.attachDocumenToLibrary(documentInstance, descriptor, null);
		}
		return genericAdapterService.attachDocumentToInstance(documentInstance, descriptor, null);
	}

	@Secure
	@Override
	public void lock(DocumentInstance documentInstance) {
		lock(documentInstance, getCurrentUser());
	}

	@Secure
	@Override
	public void lock(DocumentInstance documentInstance, String userId) {
		if (documentInstance == null) {
			return;
		}
		if (!documentInstance.hasDocument()) {
			return;
		}
		// should it throw an exception if the document is already locked by the
		// current user
		// (userId)?
		if (documentInstance.isLocked()) {
			Serializable currentLock = documentInstance.getLockedBy();
			if (!EqualsHelper.nullSafeEquals(userId, currentLock)) {
				throw new EmfRuntimeException("Document already locked by " + currentLock);
			}
			return;
		}
		if (documentInstance.isWorkingCopy()) {
			throw new EmfRuntimeException("Document already locked for editing by "
					+ documentInstance.getLockedBy());
		}
		if (userId == null) {
			throw new EmfRuntimeException("User id is required");
		}
		BeforeDocumentLockEvent event = new BeforeDocumentLockEvent(documentInstance, userId);
		eventService.fire(event);

		try {
			documentAdapterService.performDocumentOperation(documentInstance, userId,
					DocumentOperation.LOCK);

			documentInstance.setLockedBy(userId);

			eventService.fireNextPhase(event);
			saveChanges(documentInstance, new Operation(ActionTypeConstants.LOCK));
		} catch (DMSException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Faided to lock document: ", e);
			} else {
				LOGGER.warn("Faided to lock document: " + e.getMessage());
			}
		}
	}

	@Secure
	@Override
	public void unlock(DocumentInstance documentInstance) {
		unlockInternal(documentInstance, true);
	}

	@Override
	public void unlock(DocumentInstance documentInstance, boolean persistChanges) {
		unlockInternal(documentInstance, persistChanges);
	}

	/**
	 * Unlock internal.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param saveChanges
	 *            the save changes
	 */
	private void unlockInternal(DocumentInstance documentInstance, boolean saveChanges) {
		if (documentInstance.isWorkingCopy()) {
			throw new EmfRuntimeException("Document locked for editing by "
					+ documentInstance.getProperties().get(DocumentProperties.LOCKED_BY));
		}
		if (!documentInstance.isLocked()) {
			// document not locked, nothing to do more
			return;
		}
		BeforeDocumentUnlockEvent event = new BeforeDocumentUnlockEvent(documentInstance);
		eventService.fire(event);

		try {
			documentAdapterService.performDocumentOperation(documentInstance,
					DocumentOperation.UNLOCK);

			documentInstance.getProperties().remove(DocumentProperties.LOCKED_BY);

			eventService.fireNextPhase(event);
			if (saveChanges) {
				saveChanges(documentInstance, new Operation(ActionTypeConstants.UNLOCK));
			}
		} catch (DMSException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Faided to unlock document: ", e);
			} else {
				LOGGER.warn("Faided to unlock document: {}", e.getMessage());
			}
		}
	}

	@Override
	@Secure
	public File getContent(DocumentInstance doc) {
		try {
			return contentService.getContent(contentAdapterService.getContentDescriptor(doc));
		} catch (DMSException e) {
			LOGGER.warn("Failed to download the file from DMS", e);
		}
		return null;
	}

	@Override
	@Secure
	public long getContent(DocumentInstance doc, OutputStream out) {
		if (out == null) {
			return -1L;
		}
		try {
			return contentService.getContent(contentAdapterService.getContentDescriptor(doc), out);
		} catch (DMSException e) {
			LOGGER.warn("Failed to download the file from DMS", e);
		}
		return -1L;
	}

	@Override
	@Secure
	public InputStream getContentStream(DocumentInstance doc) {
		FileDescriptor contentStream;
		try {
			contentStream = contentAdapterService.getContentDescriptor(doc);
			if (contentStream != null) {
				return new BufferedInputStream(contentStream.getInputStream());
			}
		} catch (DMSException e) {
			LOGGER.warn("Failed to download the file from DMS", e);
		}
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance createInstance(DocumentDefinitionRef definition, Instance parent) {
		return createInstance(definition, parent, null);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance createInstance(DocumentDefinitionRef definition, Instance parent,
			Operation operation) {
		DocumentInstance instance = documentInstanceDao.createInstance(definition, true);
		if (parent != null) {
			instance.setOwningReference(parent.toReference());
			instance.setOwningInstance(parent);
		}
		return instance;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(DocumentInstance owner) {
		AllowedChildrenProvider<DocumentInstance> calculator = getCalculator();
		if (calculator == null) {
			return Collections.emptyMap();
		}
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	private AllowedChildrenProvider<DocumentInstance> getCalculator() {
		return documentCalculator;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(DocumentInstance owner, String type) {
		AllowedChildrenProvider<DocumentInstance> calculator = getCalculator();
		if (calculator == null) {
			return Collections.emptyList();
		}
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(DocumentInstance owner, String type) {
		AllowedChildrenProvider<DocumentInstance> calculator = getCalculator();
		if (calculator == null) {
			return false;
		}
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<DocumentDefinitionRef> getInstanceDefinitionClass() {
		return DocumentDefinitionRef.class;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<DocumentInstance> load(List<S> ids) {
		return load(ids, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<DocumentInstance> load(List<S> ids, boolean allProperties) {
		return documentInstanceDao.loadInstances(ids);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance load(Serializable instanceId) {
		DocumentInstance documentInstance = documentInstanceDao
				.loadInstance(null, instanceId, true);
		return fetchCaseDocument(documentInstance);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<DocumentInstance> loadByDbId(List<S> ids) {
		return loadByDbId(ids, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<DocumentInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return documentInstanceDao.loadInstancesByDbKey(ids, allProperties);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance loadByDbId(Serializable id) {
		DocumentInstance documentInstance = documentInstanceDao.loadInstance(id, null, true);
		return fetchCaseDocument(documentInstance);
	}

	/**
	 * Fetch case document.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @return the document instance
	 */
	private DocumentInstance fetchCaseDocument(DocumentInstance documentInstance) {
		if (documentInstance == null) {
			return null;
		}
		CaseInstance caseInstance = null;
		if (documentInstance.getOwningReference() == null) {
			// when we have multiple inheritance and we want the original document location we use
			// the primary parent link otherwise we use the old owning reference
			List<LinkReference> links = linkService.getLinks(documentInstance.toReference(),
					LinkConstants.PRIMARY_PARENT);
			if (links.isEmpty()) {
				// no primary parents - document uploaded to library
				return documentInstance;
			}
			LinkReference linkReference = links.get(0);
			// check if the primary parent is a case
			if ((linkReference.getTo() != null)
					&& linkReference.getTo().getReferenceType().getJavaClass()
							.equals(SectionInstance.class)) {
				Instance parentInstance = linkReference.getTo().toInstance();
				caseInstance = InstanceUtil.getParent(CaseInstance.class, parentInstance);

			}
		} else {
			caseInstance = InstanceUtil.getParent(CaseInstance.class, documentInstance);
		}
		if (caseInstance != null) {
			for (SectionInstance sectionInstance : caseInstance.getSections()) {
				for (Instance document : sectionInstance.getContent()) {
					if ((document instanceof DocumentInstance)
							&& document.getId().equals(documentInstance.getId())) {
						return (DocumentInstance) document;
					}
				}
			}
		}
		return renditionService.loadThumbnail(documentInstance);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DocumentInstance> loadInstances(Instance owner) {
		return documentInstanceDao.loadInstances(owner, true);
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DocumentInstance save(DocumentInstance instance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			if (!uploadInternal(instance)) {
				throw new EmfRuntimeException("Failed to upload document to DMS");
			}
			return saveInternal(instance, operation);
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
	}

	/**
	 * Save internal document instance. The save is initiated by the provided operation
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the document instance
	 */
	private DocumentInstance saveInternal(DocumentInstance instance, Operation operation) {
		try {
			BeforeDocumentPersistEvent persistEvent = null;
			if (!SequenceEntityGenerator.isPersisted(instance)) {
				persistEvent = new BeforeDocumentPersistEvent(instance);
				eventService.fire(persistEvent);
			}
			boolean operationChanging = isOperationChanging(operation);
			if (!operationChanging) {
				RuntimeConfiguration
						.enable(RuntimeConfigurationProperties.AUDIT_MODIFICATION_DISABLED);
			}
			// notify for state change
			eventService.fire(new OperationExecutedEvent(operation, instance));

			documentInstanceDao.instanceUpdated(instance, false);

			eventService.fire(new DocumentChangeEvent(instance));

			DocumentInstance old = null;
			if (operation != null) {
				old = documentInstanceDao.persistChanges(instance);
			}
			if (persistEvent != null) {
				eventService.fireNextPhase(persistEvent);
			}
			// if this is real changing opearation - delete it
			if (operationChanging) {
				draftService.delete(instance);
			}
			if (!RuntimeConfiguration
					.isSet(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT)) {
				eventService.fire(new DocumentPersistedEvent(instance, old,
						getOperationId(operation)));
			}
		} finally {
			RuntimeConfiguration
					.disable(RuntimeConfigurationProperties.AUDIT_MODIFICATION_DISABLED);
		}
		return instance;
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

	@Override
	public void refresh(DocumentInstance document) {
		if (document == null) {
			return;
		}
		if (InstanceUtil.isNotPersisted(document)) {
			LOGGER.warn("Trying to refresh non persisted document with id " + document.getId());
			return;
		}
		// refresh via case only if the document is opened via the case section, otherwise an
		// exception is thrown because the document owning instance is not found as an section
		if (document.getOwningInstance() instanceof SectionInstance) {
			CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class, document);
			if (caseInstance != null) {
				caseInstanceService.get().refresh(caseInstance);

				InstanceDeletedException exception = null;
				// check if the document still exists
				int sectionIndex = caseInstance.getSections().indexOf(document.getOwningInstance());
				if (sectionIndex < 0) {
					exception = new InstanceDeletedException("Section "
							+ document.getOwningInstance().getProperties()
									.get(DefaultProperties.TITLE) + " has been deleted from case "
							+ caseInstance.getProperties().get(DefaultProperties.TITLE));
				} else {
					SectionInstance section = caseInstance.getSections().get(sectionIndex);
					int documentIndex = section.getContent().indexOf(document);
					if (documentIndex < 0) {
						exception = new InstanceDeletedException("Document "
								+ document.getProperties().get(DefaultProperties.TITLE)
								+ " has been deleted from section "
								+ section.getProperties().get(DefaultProperties.TITLE));
					}
				}

				// the document or the section was not found so lets search for the document
				if (exception != null) {
					LOGGER.info("Not found the document {} in the expected section of the "
							+ " Will try to found it in the other sections.", document.getId());
					Instance foundDoc = null;
					for (SectionInstance sectionInstance : caseInstance.getSections()) {
						for (Instance instance : sectionInstance.getContent()) {
							if (instance.getId().equals(document.getId())) {
								foundDoc = instance;
								break;
							}
						}
						if (foundDoc != null) {
							break;
						}
					}
					if (foundDoc instanceof DocumentInstance) {
						LOGGER.info("Found the document {} in other section in the same case."
								+ " Will update the owning instance.", document.getId());
						// document has been moved

						DocumentInstance documentInstance = (DocumentInstance) foundDoc;
						if (documentInstance.getOwningInstance() instanceof SectionInstance) {
							SectionInstance newSection = (SectionInstance) documentInstance
									.getOwningInstance();
							int indexOf = newSection.getContent().indexOf(foundDoc);
							newSection.getContent().set(indexOf, document);
							newSection.initBidirection();
						}
					} else {
						// the document is deleted, removed from the case or moved to other case
						// but we cannot do anything else without change the context
						throw exception;
					}
				}
			}
		}
		documentInstanceDao.loadProperties(document);
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DocumentInstance cancel(DocumentInstance instance) {
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	@Override
	public DocumentInstance clone(DocumentInstance instanceToClone, Operation operation) {
		DocumentDefinitionRef definition = (DocumentDefinitionRef) dictionaryService
				.getInstanceDefinition(instanceToClone);

		// Should throw an event and what should be the target of the event?
		eventService.fire(new OperationExecutedEvent(operation, instanceToClone));

		DocumentInstance instance = createInstance(definition, null, operation);

		if (instance.getProperties() == null) {
			instance.setProperties(new HashMap<String, Serializable>());
		}

		for (Entry<String, Serializable> propertyEntry : instanceToClone.getProperties().entrySet()) {
			// Checks if property is not in the NOT_CLONABLE list and that it
			// exists in object definition (to avoid idoc custom properties)
			if (!DocumentProperties.NOT_CLONABLE_DOCUMENT_PROPERTIES.contains(propertyEntry
					.getKey())) {
				instance.getProperties().put(propertyEntry.getKey(), propertyEntry.getValue());
			}
		}
		instance.getProperties().put(DocumentProperties.CLONED_DMS_ID, instanceToClone.getDmsId());
		instance.setRevision(instanceToClone.getRevision());
		instance.setParentPath(instanceToClone.getParentPath());
		instance.setDocumentRefId(instanceToClone.getDocumentRefId());
		instance.getProperties().put(DocumentProperties.TYPE,
				instanceToClone.getProperties().get(DocumentProperties.TYPE));

		return instance;
	}

	@Override
	public void delete(DocumentInstance instance, Operation operation, boolean permanent) {
		deleteDocument(instance);
	}

	@Override
	public void attach(DocumentInstance targetInstance, Operation operation, Instance... children) {
		// does not support attach operation
	}

	@Override
	public void detach(DocumentInstance sourceInstance, Operation operation, Instance... instances) {
		// does not support detach operation
	}

	/**
	 * Asynchronous task for parallel document upload.
	 * 
	 * @author BBonev
	 */
	class DocumentUploadTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -880625664501954695L;

		/** The instance. */
		private final DocumentInstance instance;
		/** The owning instance. */
		private final Instance owningInstance;
		/** The delete document on rollback. */
		private boolean deleteDocumentOnRollback;
		/** The current version. */
		private Serializable currentVersion;

		/**
		 * Instantiates a new document upload task.
		 * 
		 * @param instance
		 *            the instance
		 * @param context
		 *            the context
		 */
		public DocumentUploadTask(DocumentInstance instance, Instance context) {
			super();
			if (instance == null) {
				throw new EmfRuntimeException("Cannot create upload task for invalid document!");
			}
			this.instance = instance;
			owningInstance = instance.getOwningInstance();
			if (context != null) {
				instance.setOwningInstance(context);
			}
			if (instance.getDmsId() == null) {
				deleteDocumentOnRollback = true;
			} else {
				currentVersion = instance.getProperties().get(DocumentProperties.VERSION);
			}
		}

		@Override
		protected boolean executeTask() throws Exception {
			// prevent asynch problems by generating thumbnail during upload - mode should be active
			// for multiple simultaneous uploads
			if (!instance.getProperties().containsKey(DocumentProperties.DOCUMENT_THUMB_MODE)) {
				instance.getProperties().put(DocumentProperties.DOCUMENT_THUMB_MODE,
						ThumbnailGenerationMode.ASYNCH);
			}
			return uploadInternal(instance);
		}

		@Override
		public void executeOnSuccess() {
			instance.setOwningInstance(owningInstance);
		}

		@Override
		public void onRollback() {
			// delete the uploaded file/revert version
			if (deleteDocumentOnRollback) {
				deleteFromDms(instance, InstanceUtil.getContext(owningInstance, true));
			} else if (currentVersion != null) {
				// revert the document without saving it
				revertInternal(instance, currentVersion.toString(), false);
			}
		}

	}

	/**
	 * Can upload document in section.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @return true, if successful
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean canUploadDocumentInSection(SectionInstance sectionInstance) {
		return canUploadDocumentInSection(sectionInstance, null);
	}

	/**
	 * Can upload document in section.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type
	 * @return true, if successful
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean canUploadDocumentInSection(SectionInstance sectionInstance, String attachmentType) {
		List<DocumentDefinitionRef> selectedDefinitions = selectDefinitions(sectionInstance,
				attachmentType);

		Pair<AllowUpload, Object> allowUpload = checkIfNewDocumentCanBeUploaded(
				selectedDefinitions, sectionInstance, attachmentType);
		LOGGER.trace("Can upload document {} in section {} => {}", attachmentType,
				sectionInstance.getIdentifier(), allowUpload.getFirst());
		return allowUpload.getFirst() != AllowUpload.NOT_ALLOWED;
	}

	/**
	 * Creates the document instance.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @return the document instance
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance createDocumentInstance(SectionInstance sectionInstance) {
		return createDocumentInstance(sectionInstance, null);
	}

	/**
	 * Creates the document instance.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type
	 * @return the document instance
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance createDocumentInstance(SectionInstance sectionInstance,
			String attachmentType) {

		List<DocumentDefinitionRef> selectedDefinitions = selectDefinitions(sectionInstance,
				attachmentType);

		Pair<AllowUpload, Object> allowUpload = checkIfNewDocumentCanBeUploaded(
				selectedDefinitions, sectionInstance, attachmentType);

		LOGGER.trace("Create document instance {} in section {} => {}", attachmentType,
				sectionInstance.getIdentifier(), allowUpload.getFirst());

		switch (allowUpload.getFirst()) {
			case CREATE_FROM_DEFINITION:
				return createInstanceFromDefinition(sectionInstance, allowUpload);
			case RETURN_COPY_OF_FIRST_EMPTY:
				return createCopyOfFirstEmpty(sectionInstance, allowUpload);
			case RETURN_FIRST_EMPTY:
				DocumentInstance instance = (DocumentInstance) allowUpload.getSecond();
				documentInstanceDao.instanceUpdated(instance, false);
				return instance;
			default:
				return null;
		}
	}

	/**
	 * Creates the copy of first empty.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param allowUpload
	 *            the allow upload
	 * @return the document instance
	 */
	@SuppressWarnings("unchecked")
	private DocumentInstance createCopyOfFirstEmpty(SectionInstance sectionInstance,
			Pair<AllowUpload, Object> allowUpload) {
		Pair<DocumentInstance, DocumentDefinitionRef> pair = (Pair<DocumentInstance, DocumentDefinitionRef>) allowUpload
				.getSecond();
		DocumentInstance clone = pair.getFirst().clone();
		SequenceEntityGenerator.generateStringId(clone, true);
		// fill default values from the definition
		documentInstanceDao.populateProperties(clone, pair.getSecond());
		clone.setOwningInstance(sectionInstance);
		sectionInstance.getContent().add(clone);

		documentInstanceDao.instanceUpdated(clone, false);
		eventService.fire(new DocumentCreateEvent(clone));
		return clone;
	}

	/**
	 * Creates the instance from definition.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param allowUpload
	 *            the allow upload
	 * @return the document instance
	 */
	private DocumentInstance createInstanceFromDefinition(SectionInstance sectionInstance,
			Pair<AllowUpload, Object> allowUpload) {
		DocumentInstance documentInstance = createDocumentInstance((DocumentDefinitionRef) allowUpload
				.getSecond());
		documentInstance.setRevision(sectionInstance.getRevision());
		documentInstance.setOwningInstance(sectionInstance);
		sectionInstance.getContent().add(documentInstance);

		// initialize the path and revision
		documentInstance.setParentPath(PathHelper.getPath(documentInstance));
		documentInstance.setRevision(sectionInstance.getRevision());

		documentInstance.setOwningReference(sectionInstance.toReference());
		documentInstance.setOwningInstance(sectionInstance);

		documentInstanceDao.instanceUpdated(documentInstance, false);
		return documentInstance;
	}

	@Override
	public Map<String, Pair<DocumentDefinitionRef, Integer>> getAllowedDocuments(
			SectionInstance sectionInstance) {

		SectionDefinition sectionDefinition = (SectionDefinition) dictionaryService
				.getInstanceDefinition(sectionInstance);
		if (sectionDefinition == null) {
			return CollectionUtils.emptyMap();
		}
		Map<String, Pair<DocumentDefinitionRef, Integer>> result = new LinkedHashMap<>();
		for (DocumentDefinitionRef definitionRef : sectionDefinition.getDocumentDefinitions()) {
			// CMF-1109: structured document are not allowed to be uploaded
			// if the definition is set to be of count 0 then we ignore
			// the definition
			Integer maxInstances = definitionRef.getMaxInstances();
			if (Boolean.TRUE.equals(definitionRef.getStructured()) || (maxInstances == null)
					|| (maxInstances == 0)) {
				continue;
			}
			PropertyDefinition propertyDefinition = dictionaryService.getProperty(
					DocumentProperties.TYPE, sectionInstance.getRevision(), definitionRef);
			String attachmentType = (String) evaluatorManager.evaluate(propertyDefinition);

			String returnAttachmentType = attachmentType;
			if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(attachmentType)) {
				returnAttachmentType = documentDefaultType;
			}
			if (maxInstances < 0) {
				// no limit
				result.put(returnAttachmentType, new Pair<>(definitionRef, -1));
			} else {
				int currentAttached = countInstances(sectionInstance, attachmentType,
						definitionRef, true);

				int allowed = maxInstances - currentAttached;
				if (allowed > 0) {
					result.put(returnAttachmentType, new Pair<>(definitionRef, allowed));
				}
			}
		}

		return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<SectionInstance, Map<String, Pair<DocumentDefinitionRef, Integer>>> getAllowedDocuments(
			CaseInstance caseInstance) {
		if (caseInstance == null) {
			return CollectionUtils.emptyMap();
		}
		Map<SectionInstance, Map<String, Pair<DocumentDefinitionRef, Integer>>> result = CollectionUtils
				.createLinkedHashMap(caseInstance.getSections().size());
		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			result.put(sectionInstance, getAllowedDocuments(sectionInstance));
		}
		return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Map<String, Integer>> getDefinedDocuments(CaseInstance caseInstance) {
		if (caseInstance == null) {
			return CollectionUtils.emptyMap();
		}
		CaseDefinition definition = (CaseDefinition) dictionaryService
				.getInstanceDefinition(caseInstance);

		Map<String, Map<String, Integer>> result = CollectionUtils.createLinkedHashMap(definition
				.getSectionDefinitions().size());
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			Map<String, Integer> map = CollectionUtils.createLinkedHashMap(sectionDefinition
					.getDocumentDefinitions().size());
			result.put(sectionDefinition.getIdentifier(), map);
			collectSectionDocuments(sectionDefinition, map);
		}
		return result;
	}

	/**
	 * Collect section documents.
	 * 
	 * @param sectionDefinition
	 *            the section definition
	 * @param map
	 *            the map
	 */
	private void collectSectionDocuments(SectionDefinition sectionDefinition,
			Map<String, Integer> map) {
		for (DocumentDefinitionRef ref : sectionDefinition.getDocumentDefinitions()) {
			Integer integer = map.get(ref.getIdentifier());
			if ((integer == null) && (ref.getMaxInstances() > 0)) {
				map.put(ref.getIdentifier(), ref.getMaxInstances());
			} else {
				if (integer == null) {
					integer = 0;
				}
				if (ref.getMaxInstances() < 0) {
					map.put(ref.getIdentifier(), -1);
				} else {
					map.put(ref.getIdentifier(), integer + ref.getMaxInstances());
				}
			}
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DocumentInstance selectDocumentInstance(String caseDmsId, String documentDmsId) {
		if ((caseDmsId == null) || (documentDmsId == null)) {
			return null;
		}
		// load case and search in if for the document
		// Eventually this can be changed to search into the DB and check if
		// there is a document associated with the given case
		CaseInstance instance = caseInstanceService.get().load(caseDmsId);
		if (instance == null) {
			return null;
		}
		for (SectionInstance sectionInstance : instance.getSections()) {
			for (Instance documentInstance : sectionInstance.getContent()) {
				if ((documentInstance instanceof DocumentInstance)
						&& EqualsHelper.nullSafeEquals(
								((DocumentInstance) documentInstance).getDmsId(), documentDmsId)) {
					return (DocumentInstance) documentInstance;
				}
			}
		}
		return null;
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean moveDocument(DocumentInstance src, SectionInstance targetSection) {
		CaseInstance srcCase = InstanceUtil.getParent(CaseInstance.class, src);
		if ((srcCase == null) || (!SequenceEntityGenerator.isPersisted(src.getOwningInstance()))) {
			return false;
		}
		Serializable srcCaseId = srcCase.getId();
		if ((srcCaseId == null) || (targetSection == null)) {
			return false;
		}
		// cannot move to the same section
		if (EqualsHelper.entityEquals(src.getOwningInstance(), targetSection)) {
			return false;
		}
		Serializable location = targetSection.getProperties().get(
				DocumentProperties.SECTION_LOCATION);
		if (location == null) {
			return false;
		}
		if ((src.getOwningInstance() != null)
				&& location.equals(src.getOwningInstance().getProperties()
						.get(DocumentProperties.SECTION_LOCATION))) {
			// same dest and src
			return false;
		}
		// check if the target case is the same as the original
		Operation operation = MOVE_SAME_CASE;
		if (!srcCaseId.toString().equals(targetSection.getOwningReference().getIdentifier())) {
			operation = MOVE_OTHER_CASE;
		}
		try {
			if (!isAttachedInternal(src.getOwningInstance(), src)) {
				// update with the new data- name could be changed
				FileAndPropertiesDescriptor descriptor = documentAdapterService.moveDocument(
						src, targetSection);
				src.getProperties().putAll(descriptor.getProperties());
			}
		} catch (DMSException e) {
			throw new DmsCaseException(e);
		}
		// notify that the file is going to be moved
		BeforeDocumentMoveEvent event = new BeforeDocumentMoveEvent(src, src.getOwningInstance(),
				targetSection);
		eventService.fire(event);

		// remove from the old section
		if (src.getOwningInstance() instanceof SectionInstance) {
			SectionInstance sectionInstance = (SectionInstance) src.getOwningInstance();
			sectionService.detach(sectionInstance, operation, src);
		}
		sectionService.attach(targetSection, operation, src);

		// notify that the file has been moved.
		// NOTE: that the new event is with the new document
		// instance and is attached the the new section/case
		eventService.fireNextPhase(event);
		// save the target case and create the
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		try {
			CaseService caseService = caseInstanceService.get();
			if (MOVE_OTHER_CASE.equals(operation)) {
				// update the other case if not moved to the same CMF-3492
				caseService.save(srcCase, operation);
			}
			caseService.save((CaseInstance) targetSection.getOwningInstance(), operation);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
		return true;
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean copyDocumentAsNew(DocumentInstance src, SectionInstance targetSection,
			String newDocumentName) {
		CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class, src);
		if (caseInstance == null) {
			return false;
		}
		Serializable entityId = caseInstance.getId();
		if (entityId == null) {
			return false;
		}

		BeforeDocumentCopyEvent event = new BeforeDocumentCopyEvent(src, false, false);
		eventService.fire(event);

		// clone the instance (we need a new copy)
		DocumentInstance clone = src.clone();
		try {
			// duplicate properties
			clone.setProperties(PropertiesUtil.cloneProperties(src.getProperties()));

			// update with the new data- name could be changed
			FileAndPropertiesDescriptor descriptor = documentAdapterService.copyDocument(src,
					targetSection, newDocumentName);
			Map<String, Serializable> updatedProps = descriptor.getProperties();
			clone.getProperties().putAll(updatedProps);
			// get the new DMS ID
			Serializable location = clone.getProperties().get(
					DocumentProperties.ATTACHMENT_LOCATION);
			if (location instanceof FileDescriptor) {
				FileDescriptor dmsFileDescriptor = (FileDescriptor) location;
				clone.setDmsId(dmsFileDescriptor.getId());
				clone.getProperties().put(DocumentProperties.ATTACHMENT_LOCATION,
						dmsFileDescriptor.getId());
			} else if (location instanceof String) {
				clone.setDmsId((String) location);
			}
		} catch (DMSException e) {
			throw new DmsCaseException(e);
		}
		// and add the new document
		targetSection.getContent().add(clone);
		clone.setOwningInstance(targetSection);
		// save the document instance
		documentInstanceDao.persistChanges(clone);

		AfterDocumentCopyEvent nextPhaseEvent = event.getNextPhaseEvent();
		if (nextPhaseEvent != null) {
			nextPhaseEvent.setNewInstance(clone);
		}
		eventService.fireNextPhase(event);
		// and save the target case
		caseInstanceService.get().save((CaseInstance) targetSection.getOwningInstance(),
				EDIT_DETAILS);
		return true;
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean copyDocumentAsLink(DocumentInstance src, SectionInstance targetSection,
			boolean isHardLink) {
		BeforeDocumentCopyEvent event = new BeforeDocumentCopyEvent(src, true, isHardLink);
		eventService.fire(event);
		DocumentInstance clone = null;
		if (isHardLink) {
			// for fixed version we copy the current instance and create new one
			// that will be added to the new section
			clone = src.clone();
			clone.setProperties(PropertiesUtil.cloneProperties(src.getProperties()));
			clone.setDmsId(src.getDmsId());

			// add the clone to the target section
			targetSection.getContent().add(clone);
			clone.setOwningInstance(targetSection);
			// save the document instance
			documentInstanceDao.persistChanges(clone);
		} else {
			// for soft link we create new instance that will point the the same
			// documentEntity into the DB, we just create new relation entry for
			// the given document and the new owning case
			clone = src.clone();
			clone.setId(src.getId());
			clone.setProperties(PropertiesUtil.cloneProperties(src.getProperties()));
			clone.setDmsId(src.getDmsId());

			// add the clone to the target section
			targetSection.getContent().add(clone);
			clone.setOwningInstance(targetSection);
		}
		AfterDocumentCopyEvent nextPhaseEvent = event.getNextPhaseEvent();
		if (nextPhaseEvent != null) {
			nextPhaseEvent.setNewInstance(clone);
		}
		eventService.fireNextPhase(event);

		// and save the target case
		caseInstanceService.get().save((CaseInstance) targetSection.getOwningInstance(),
				EDIT_DETAILS);
		return true;
	}

	/**
	 * Check if new document can be uploaded. REVIEW attachmentType da se preimenuva na type. V
	 * momenta e obyrkvashto. Tova da se napravi za celiq proekt :)
	 * 
	 * @param definitionRefs
	 *            the definition refs
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type
	 * @return the pair
	 */
	private Pair<AllowUpload, Object> checkIfNewDocumentCanBeUploaded(
			List<DocumentDefinitionRef> definitionRefs, SectionInstance sectionInstance,
			String attachmentType) {
		for (DocumentDefinitionRef definitionRef : definitionRefs) {
			Integer integer = definitionRef.getMaxInstances();
			int maxAllowed = ((integer == null) || (integer < 0)) ? Integer.MAX_VALUE : integer;

			int currentlyAttached = countInstances(sectionInstance, attachmentType, definitionRef,
					true);
			int totalInstances = countInstances(sectionInstance, attachmentType, definitionRef,
					false);

			if (totalInstances == 0) {
				// we have removed all documents of the given type
				// from the given section so we initialize the
				// section again
				return new Pair<AllowUpload, Object>(AllowUpload.CREATE_FROM_DEFINITION,
						definitionRef);
			}

			// no attached documents of this type
			if (currentlyAttached == 0) {
				// find the first empty instance
				// if can have only one return it
				// if multiple return copy
				DocumentInstance documentInstance = getFirstEmptyInstance(sectionInstance,
						attachmentType, definitionRef);
				if (documentInstance != null) {
					if (maxAllowed == 1) {
						return new Pair<AllowUpload, Object>(AllowUpload.RETURN_FIRST_EMPTY,
								documentInstance);
					}
					return new Pair<AllowUpload, Object>(AllowUpload.RETURN_COPY_OF_FIRST_EMPTY,
							new Pair<>(documentInstance, definitionRef));
				}
			}
			if (((maxAllowed - totalInstances) == 0) && ((totalInstances - currentlyAttached) == 1)) {
				// we have reached to the last empty instance - find
				// it and return it
				return new Pair<AllowUpload, Object>(AllowUpload.RETURN_FIRST_EMPTY,
						getFirstEmptyInstance(sectionInstance, attachmentType, definitionRef));
			}
			if ((maxAllowed - totalInstances) > 0) {
				// we can have more instance - find the empty one
				// and return a copy of it
				DocumentInstance documentInstance = getFirstEmptyInstance(sectionInstance,
						attachmentType, definitionRef);
				if (documentInstance != null) {
					return new Pair<AllowUpload, Object>(AllowUpload.RETURN_COPY_OF_FIRST_EMPTY,
							new Pair<>(documentInstance, definitionRef));
				}
				// all instances where deleted so we create new one
				return new Pair<AllowUpload, Object>(AllowUpload.CREATE_FROM_DEFINITION,
						definitionRef);
			}
		}
		return new Pair<>(AllowUpload.NOT_ALLOWED, null);
	}

	/**
	 * Select document definitions for the given attachment type for the given section.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type to search for
	 * @return the list of definitions
	 */
	private List<DocumentDefinitionRef> selectDefinitions(SectionInstance sectionInstance,
			String attachmentType) {
		List<DocumentDefinitionRef> list = new LinkedList<>();

		CaseDefinition definition = (CaseDefinition) dictionaryService
				.getInstanceDefinition(sectionInstance.getOwningInstance());

		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			if (PathHelper.equalsPaths(sectionInstance, sectionDefinition)) {
				for (DocumentDefinitionRef definitionRef : sectionDefinition
						.getDocumentDefinitions()) {
					// if the definition is set to be of count 0 then we ignore
					// the definition
					Integer maxInstances = definitionRef.getMaxInstances();
					if ((maxInstances != null) && (maxInstances == 0)) {
						continue;
					}
					PropertyDefinition propertyDefinition = dictionaryService.getProperty(
							DocumentProperties.TYPE, sectionInstance.getRevision(), definitionRef);
					String value = (String) evaluatorManager.evaluate(propertyDefinition);
					if (EqualsHelper.nullSafeEquals(attachmentType, value, true)) {
						list.add(definitionRef);
					}
				}
			}
		}
		return list;
	}

	/**
	 * Gets the first empty document instance of the given type.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type
	 * @param definitionRef
	 *            the definition ref
	 * @return the first empty instance or <code>null</code> if not found
	 */
	private DocumentInstance getFirstEmptyInstance(SectionInstance sectionInstance,
			String attachmentType, DocumentDefinitionRef definitionRef) {

		for (Instance instance : sectionInstance.getContent()) {
			if (PathHelper.equalsPaths(definitionRef, instance)
					&& EqualsHelper.nullSafeEquals(
							(String) instance.getProperties().get(DocumentProperties.TYPE),
							attachmentType, true) && (instance instanceof DocumentInstance)
					&& !((DocumentInstance) instance).hasDocument()) {
				return (DocumentInstance) instance;
			}
		}
		return null;
	}

	/**
	 * Count the document instances from the given attachment type. If the flag to present
	 * attachment is set to <code>true</code> then the method will count only the document instances
	 * that has an uploaded document
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param attachmentType
	 *            the attachment type
	 * @param definitionRef
	 *            is the concrete definition to check
	 * @param hasAttachment
	 *            the has attachment
	 * @return the int
	 */
	private int countInstances(SectionInstance sectionInstance, String attachmentType,
			DocumentDefinitionRef definitionRef, boolean hasAttachment) {
		int count = 0;
		for (Instance instance : sectionInstance.getContent()) {
			if (PathHelper.equalsPaths(definitionRef, instance)
					&& EqualsHelper.nullSafeEquals(
							(String) instance.getProperties().get(DocumentProperties.TYPE),
							attachmentType, true)) {

				if (hasAttachment) {
					if ((instance instanceof DocumentInstance)
							&& ((DocumentInstance) instance).hasDocument()) {
						count++;
					}
				} else {
					count++;
				}
			}
		}
		return count;
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

	@Override
	public String getContentURI(DocumentInstance instance) {
		try {
			return documentAdapterService.getDocumentDirectAccessURI(instance);
		} catch (DMSException e) {
			LOGGER.error("URI not retrieved!", e);
		}
		return "";
	}

	/**
	 * Extract content.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param descriptor
	 *            the descriptor
	 */
	protected void extractContent(DocumentInstance documentInstance, FileDescriptor descriptor) {
		String extract = contentService.extractContent(documentInstance, descriptor);
		if (StringUtils.isNotNullOrEmpty(extract)) {
			documentInstance.getProperties().put(DefaultProperties.CONTENT, extract);
		}
	}

	/**
	 * Checks if is operation auditable and changes data.
	 * 
	 * @param operation
	 *            the operation to check
	 * @return true, if is operation auditable
	 */
	private boolean isOperationChanging(Operation operation) {
		if (operation == null) {
			return true;
		}
		return !(ActionTypeConstants.LOCK.equals(operation.getOperation()) || ActionTypeConstants.UNLOCK
				.equals(operation.getOperation()));
	}
}
