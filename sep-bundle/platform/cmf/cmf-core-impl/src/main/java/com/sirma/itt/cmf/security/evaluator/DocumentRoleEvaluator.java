package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.ADMINISTRATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.constants.allowed_action.AllowedActionType;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Role evaluator for document instances.
 * 
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.DOCUMENT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 5)
public class DocumentRoleEvaluator extends BaseRoleEvaluator<DocumentInstance> implements
		RoleEvaluator<DocumentInstance> {
	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { DocumentInstance.class });
	/** The Constant HTML_MIMETYPE. */
	public static final String HTML_MIMETYPE = "text/html";
	/** The Constant XHTML_MIMETYPE. */
	public static final String XHTML_MIMETYPE = "text/xhtml";

	/** The download. */
	static final Action DOWNLOAD = new EmfAction(AllowedActionType.DOWNLOAD.getType());
	/** The upload. */
	static final Action UPLOAD = new EmfAction(AllowedActionType.UPLOAD.getType());
	/** The Constant PRINT. */
	static final Action PRINT = new EmfAction(AllowedActionType.PRINT.getType());
	/** The edit structured document. */
	static final Action EDIT_STRUCTURED_DOCUMENT = new EmfAction(
			AllowedActionType.EDIT_STRUCTURED_DOCUMENT.getType());
	/** The lock. */
	static final Action LOCK = new EmfAction(AllowedActionType.LOCK.getType());
	/** The unlock. */
	static final Action UNLOCK = new EmfAction(AllowedActionType.UNLOCK.getType());
	/** The cancel edit offline. */
	static final Action CANCEL_EDIT_OFFLINE = new EmfAction(
			AllowedActionType.CANCEL_EDIT_OFFLINE.getType());
	/** The document edit offline. */
	static final Action DOCUMENT_EDIT_OFFLINE = new EmfAction(
			AllowedActionType.DOCUMENT_EDIT_OFFLINE.getType());
	/** The Constant DOCUMENT_EDIT_ONLINE. */
	static final Action DOCUMENT_EDIT_ONLINE = new EmfAction(
			AllowedActionType.DOCUMENT_EDIT_ONLINE.getType());
	/** The Constant UPLOAD_NEW_VERSION. */
	static final Action UPLOAD_NEW_VERSION = new EmfAction(
			AllowedActionType.DOCUMENT_UPLOAD_NEW_VERSION.getType());
	/** The Constant COPY_CONTENT. */
	static final Action COPY_CONTENT = new EmfAction(AllowedActionType.COPY_CONTENT.getType());
	/** The Constant LINK_DOCUMENT. */
	static final Action LINK_DOCUMENT = new EmfAction(AllowedActionType.LINK.getType());
	/** The Constant DOCUMENT_MOVE_SAME_CASE. */
	static final Action DOCUMENT_MOVE_SAME_CASE = new EmfAction(
			AllowedActionType.DOCUMENT_MOVE_SAME_CASE.getType());
	/** The Constant DOCUMENT_MOVE_OTHER_CASE. */
	static final Action DOCUMENT_MOVE_OTHER_CASE = new EmfAction(
			AllowedActionType.DOCUMENT_MOVE_OTHER_CASE.getType());
	/** The Constant DELETE. */
	static final Action DELETE = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());
	/** The Constant Document sign. */
	static final Action DOCUMENT_SIGN = new EmfAction(AllowedActionType.SIGN_DOCUMENT.getType());
	/** The Constant EDIT_PROPERTIES. */
	static final Action EDIT_PROPERTIES = new EmfAction(
			AllowedActionType.DOCUMENT_EDIT_PROPERTIES.getType());
	static final Action REVERT = new EmfAction(ActionTypeConstants.REVERT);
	/** The Constant START_WORKFLOW. */
	static final Action START_WORKFLOW = new EmfAction(ActionTypeConstants.START_WORKFLOW);
	static final Action HISTORY_PREVIEW = new EmfAction(ActionTypeConstants.HISTORY_PREVIEW);
	static final Action DETACH_DOCUMENT = new EmfAction(ActionTypeConstants.DETACH_DOCUMENT);

	static final Action CREATE_SUB_DOCUMENT = new EmfAction(
			AllowedActionType.CREATE_SUB_DOCUMENT.getType());
	static final Action CLONE = new EmfAction(AllowedActionType.CLONE.getType());
	static final Action SAVE_AS_PUBLIC_TEMPLATE = new EmfAction(
			AllowedActionType.SAVE_AS_PUBLIC_TEMPLATE.getType());
	static final Action EXPORT = new EmfAction(ActionTypeConstants.EXPORT);

	/** The close case actions. */
	private Set<Action> closeCaseActions;
	/** The history actions. */
	private Set<Action> historyActions;
	private Set<Action> lockedHistoryActions;
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRoleEvaluator.class);

	@Inject
	private DocumentService documentService;

	/**
	 * Initialize the evaluator.
	 */
	@PostConstruct
	public void initialize() {
		closeCaseActions = CollectionUtils.createLinkedHashSet(5);
		closeCaseActions.add(DOWNLOAD);
		closeCaseActions.add(PRINT);
		closeCaseActions.add(COPY_CONTENT);
		closeCaseActions.add(LINK_DOCUMENT);

		historyActions = CollectionUtils.createLinkedHashSet(5);
		historyActions.add(DOWNLOAD);
		historyActions.add(REVERT);
		historyActions.add(PRINT);
		historyActions.add(HISTORY_PREVIEW);

		lockedHistoryActions = CollectionUtils.createLinkedHashSet(2);
		lockedHistoryActions.add(DOWNLOAD);
	}

	/**
	 * Evaluate internal.
	 * 
	 * @param target
	 *            the target
	 * @param resource
	 *            the resource
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role
	 */
	@Override
	protected Pair<Role, RoleEvaluator<DocumentInstance>> evaluateInternal(DocumentInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if (resource == null) {
			LOGGER.warn("Null resource is not valid");
			return null;
		}
		if (resourceService.areEqual(resource,
				target.getProperties().get(DefaultProperties.CREATED_BY))) {
			return constructRoleModel(CREATOR);
		}
		Instance primaryContainer = InstanceUtil.getParentContext(target, true);
		if (primaryContainer == null) {
			LOGGER.warn("No parent found for document: {}", target.getIdentifier());
			// TODO: add additional checks for documents from document library
			return constructRoleModel(null, resource, BaseRoles.CONSUMER, chainRuntimeSettings);
		}
		return constructRoleModel(primaryContainer, resource, BaseRoles.VIEWER,
				chainRuntimeSettings);
	}

	@Override
	protected Boolean filterInternal(DocumentInstance target, Resource resource, Role role,
			Set<Action> actions) {

		// if deleted we does not have any documents
		Instance primaryContainer = InstanceUtil.getParentContext(target, true);
		if (isInstanceInStates(primaryContainer, PrimaryStates.DELETED)) {
			// no actions for deleted documents
			actions.clear();
			return Boolean.TRUE;
		}

		// 4.1.3 CSUC10: Създаване на връзка към съдържание на документа
		// Документ може да се копира само в преписка със статус "Отворена".
		// (Статусът на изходната преписка е без значение.)

		// Ако преписката се състои само от един раздел, то бутона
		// "Премести в друг раздел" е неактивен.

		// TODO: this should be removed at the actions by state are controlled externally
		// CMF-276: if case is closed then we can have only read operations
		if (isInstanceInStates(primaryContainer, PrimaryStates.CANCELED)) {
			actions.retainAll(closeCaseActions);
			// bbanchev optimization
			return Boolean.TRUE;
		}

		// if history version then we can only download or print
		if (target.isHistoryInstance()) {
			actions.retainAll(historyActions);
			// if the documents is locked then only download is allowed
			if (target.isLocked()) {
				actions.retainAll(lockedHistoryActions);
			}
			return Boolean.TRUE;
		}

		// forbid moving of documents that are not part of a section
		if (!isAttachedInSection(target)) {
			actions.remove(DOCUMENT_MOVE_SAME_CASE);
			actions.remove(DOCUMENT_MOVE_OTHER_CASE);
		}

		if (target.getOwningReference() == null) {
			if ((target.getOwningInstance() == null)
					|| !documentService.isAttached(target.getOwningInstance(), target)) {
				// 1. we does not have a reference and instance we have attached document but the
				// context is unknown so we can't have the operation visible
				// 2. for non attached documents we should remove the action
				actions.remove(DETACH_DOCUMENT);
			}
		} else {
			// if we have reference the document has not been attached for now
			actions.remove(DETACH_DOCUMENT);
		}

		boolean isIdoc = DocumentProperties.PURPOSE_IDOC.equals(target.getPurpose());
		if (isIdoc) {
			// remove actions that should not appear for iDoc
			actions.remove(DOWNLOAD);
			actions.remove(UPLOAD_NEW_VERSION);
			actions.remove(EDIT_STRUCTURED_DOCUMENT);
			actions.remove(DOCUMENT_EDIT_OFFLINE);
		} else {
			// remove
			actions.remove(EXPORT);
			actions.remove(SAVE_AS_PUBLIC_TEMPLATE);
			actions.remove(CREATE_SUB_DOCUMENT);
			actions.remove(CLONE);
			actions.remove(PRINT);
			actions.remove(UPLOAD);
		}

		// else
		actions.remove(REVERT);
		actions.remove(HISTORY_PREVIEW);

		Map<String, Serializable> documentProperties = target.getProperties();
		// if document is locked nothing else can be done except operations for
		// removing the lock
		if (target.isLocked()) {
			actions.remove(EDIT_PROPERTIES);
			actions.remove(COPY_CONTENT);
			actions.remove(LINK_DOCUMENT);
			actions.remove(DOCUMENT_MOVE_SAME_CASE);
			actions.remove(DOCUMENT_MOVE_OTHER_CASE);
			actions.remove(EDIT_STRUCTURED_DOCUMENT);
			actions.remove(DELETE);
			actions.remove(DOCUMENT_SIGN);
			if (!isIdoc || !resourceService.areEqual(target.getLockedBy(), resource)) {
				actions.remove(UPLOAD);
			}
		}

		boolean creator = resourceService.areEqual(resource,
				documentProperties.get(DocumentProperties.CREATED_BY));

		if (!creator) {
			// TODO - tricky check
			boolean manager = SecurityModel.BaseRoles.MANAGER.getGlobalPriority() <= role
					.getRoleId().getGlobalPriority();
			if (!manager) {
				actions.remove(DELETE);
			}
		}

		// TODO: this should be removed at the actions by type could be controlled externally
		if (Boolean.TRUE.equals(target.getStructured())) {
			// upload new version is obsolete for structured documents
			actions.remove(UPLOAD_NEW_VERSION);
			actions.remove(DOCUMENT_EDIT_OFFLINE);
			actions.remove(DOCUMENT_EDIT_ONLINE);
		} else {
			actions.remove(EDIT_STRUCTURED_DOCUMENT);
		}

		// remove signing if this version is signed
		if (shouldRemoveSign(target)) {
			actions.remove(DOCUMENT_SIGN);
		}

		// do we have a role with lock operations at all
		if (role.getPermissions().containsKey(SecurityModel.PERMISSION_LOCK)) {
			Map<String, Serializable> properties = target.getProperties();
			Serializable lockedBy = properties.get(DocumentProperties.LOCKED_BY);

			// if not locked we remove unlock actions
			if (lockedBy == null) {
				if (!isAllowedForOnlineEdit(target)) {
					actions.remove(DOCUMENT_EDIT_ONLINE);
				}
				actions.remove(UNLOCK);
				actions.remove(CANCEL_EDIT_OFFLINE);

			} else {
				// if locked but not from the given user we remove release
				// operations
				if (!resourceService.areEqual(resource, lockedBy)
						&& !role.getRoleId().equals(ADMINISTRATOR)) {
					// only administrators can unlock
					actions.remove(UNLOCK);
					actions.remove(CANCEL_EDIT_OFFLINE);
					actions.remove(UPLOAD_NEW_VERSION);
					actions.remove(DOCUMENT_SIGN);
				}
				// if locked we remove the lock operations
				actions.remove(LOCK);
				actions.remove(DOCUMENT_SIGN);
				actions.remove(DOCUMENT_EDIT_OFFLINE);
				actions.remove(DOCUMENT_EDIT_ONLINE);
				// if document is locked for offline edit then we remove the
				// unlock option
				if (target.isWorkingCopy()) {
					actions.remove(UNLOCK);
				} else {
					// otherwise remove the cancel and upload new version
					actions.remove(CANCEL_EDIT_OFFLINE);
					actions.remove(UPLOAD_NEW_VERSION);
					actions.remove(DOCUMENT_SIGN);
				}
			}
		}

		// if the document is in the case or in project only then we can start a workflow
		if ((primaryContainer instanceof CaseInstance)
				|| (primaryContainer instanceof RootInstanceContext)) {
			if (actions.contains(START_WORKFLOW)
					&& !instanceService.isChildAllowed(target, ObjectTypesCmf.WORKFLOW)) {
				actions.remove(START_WORKFLOW);
			}
		} else {
			actions.remove(START_WORKFLOW);
		}

		return Boolean.FALSE;
	}

	/**
	 * Should remove sign.We should remove the sign action if the document is already signed or the
	 * mimetype is not PDF or XML
	 * 
	 * @param target
	 *            the target
	 * @return true, if the sign action should be removed
	 */
	private boolean shouldRemoveSign(DocumentInstance target) {
		Serializable mimetype = target.getProperties().get(DocumentProperties.MIMETYPE);
		// we should remove the sign action if the document is already signed or the mimetype is not
		// pdf or xml
		return target.getProperties().containsKey(DocumentProperties.DOCUMENT_SIGNED_DATE)
				|| (mimetype == null)
				|| !(mimetype.toString().endsWith("/pdf") || mimetype.toString().endsWith("/xml"));
	}

	/**
	 * Checks if is attached in section/folder.
	 * 
	 * @param target
	 *            the target
	 * @return true, if is attached in section
	 */
	private boolean isAttachedInSection(DocumentInstance target) {
		Class<?> parent = null;
		if (target.getOwningReference() != null) {
			parent = target.getOwningReference().getReferenceType().getJavaClass();
		} else if (target.getOwningInstance() != null) {
			parent = target.getOwningInstance().getClass();
		}
		return parent != null && SectionInstance.class.isAssignableFrom(parent);
	}

	/**
	 * Checks if the document is allowed for online edit.
	 * 
	 * @param target
	 *            the target
	 * @return true, if is allowed for online edit
	 */
	private boolean isAllowedForOnlineEdit(DocumentInstance target) {
		String mimeType = (String) target.getProperties().get(DocumentProperties.MIMETYPE);
		return StringUtils.isNotNull(mimeType)
				&& (HTML_MIMETYPE.equals(mimeType) || XHTML_MIMETYPE.equals(mimeType));
	}

	/**
	 * {@inheritDoc}
	 */
	protected Class<DocumentInstance> allowedClass() {
		return DocumentInstance.class;
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

}
