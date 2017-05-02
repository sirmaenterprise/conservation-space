package com.sirma.itt.seip.instance.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.actions.AllowedActionType;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;

/**
 * The ObjectRoleEvaluator provides role evaluation and actions filtering .
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 90)
public class ObjectRoleEvaluator extends DomainObjectsBaseRoleEvaluator<ObjectInstance> {

	/** The Constant SUPPORTED. */
	private static final List<Class> SUPPORTED = Arrays.asList(new Class<?>[] { ObjectInstance.class });

	/**
	 * Some predefined action constants to be used when filtering actions
	 */
	protected static final Action EDIT_DETAILS = new EmfAction(ActionTypeConstants.EDIT_DETAILS);
	protected static final Action DELETE = new EmfAction(ActionTypeConstants.DELETE);
	protected static final Action UNLOCK = new EmfAction(ActionTypeConstants.UNLOCK);
	protected static final Action CLONE = new EmfAction(ActionTypeConstants.CLONE);
	protected static final Action PRINT = new EmfAction(ActionTypeConstants.PRINT);
	protected static final Action EXPORT = new EmfAction(ActionTypeConstants.EXPORT);
	private static final Action PUBLISH = new EmfAction(ActionTypeConstants.PUBLISH);
	private static final Action PUBLISH_AS_PDF = new EmfAction(ActionTypeConstants.PUBLISH_AS_PDF);
	private static final Action DETACH = new EmfAction(ActionTypeConstants.DETACH);
	private static final Action DOWNLOAD = new EmfAction(AllowedActionType.DOWNLOAD.getType());
	private static final Action UPLOAD_NEW_VERSION = new EmfAction(
			AllowedActionType.DOCUMENT_UPLOAD_NEW_VERSION.getType());
	private static final Action REVERT_VERSION = new EmfAction(
			InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
	private static final Action HISTORY_PREVIEW = new EmfAction(ActionTypeConstants.HISTORY_PREVIEW);
	private static final Action MANAGE_PERMISSIONS = new EmfAction(ActionTypeConstants.MANAGE_PERMISSIONS);
	private static final Action VIEW_DETAILS = new EmfAction(ActionTypeConstants.VIEW_DETAILS);
	private static final Action PRINT_TAB = new EmfAction(ActionTypeConstants.PRINT_TAB);
	private static final Action EXPORT_PDF = new EmfAction(ActionTypeConstants.EXPORT_PDF);
	private static final Action EXPORT_TAB_PDF = new EmfAction(ActionTypeConstants.EXPORT_TAB_PDF);

	private static final Action EXPORT_WORD = new EmfAction(ActionTypeConstants.EXPORT_WORD);
	private static final Action EXPORT_TAB_WORD = new EmfAction(ActionTypeConstants.EXPORT_TAB_WORD);
	private static final Action CREATE_IN_CONTEXT = new EmfAction(ActionTypeConstants.CREATE_IN_CONTEXT);

	private static final Set<Action> ALLOWED_ACTIONS_WHEN_LOCKED = new HashSet<>(
			Arrays.asList(EXPORT, MANAGE_PERMISSIONS, PRINT, UNLOCK, DOWNLOAD, EDIT_DETAILS, CLONE, VIEW_DETAILS,
					PRINT_TAB, EXPORT_PDF, EXPORT_TAB_PDF, EXPORT_WORD, EXPORT_TAB_WORD, CREATE_IN_CONTEXT));

	private static final Set<Action> ALLOWED_ACTIONS_FOR_VERSION = new HashSet<>(Arrays.asList(EXPORT, PRINT, DOWNLOAD,
			VIEW_DETAILS, PRINT_TAB, EXPORT_PDF, EXPORT_TAB_PDF, EXPORT_WORD, EXPORT_TAB_WORD, REVERT_VERSION));

	@Inject
	private RevisionService revisionService;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Boolean filterInternal(ObjectInstance target, Resource resource, Role role, Set<Action> actions) {
		if (InstanceVersionService.isVersion(target.getId())) {
			return filterForVersion(target, actions);
		}

		if (target.isLocked()) {
			return filterForLocked(target, resource, actions);
		}

		actions.remove(UNLOCK);
		actions.remove(REVERT_VERSION);

		Instance owningInstance = target.getOwningInstance();

		if (owningInstance == null) {
			actions.remove(DETACH);
		}

		if (!target.isUploaded()) {
			actions.remove(UPLOAD_NEW_VERSION);
			actions.remove(DOWNLOAD);
		}

		boolean revision = revisionService.isRevision(target);
		if (revision) {
			actions.remove(PUBLISH);
			actions.remove(PUBLISH_AS_PDF);
			actions.remove(HISTORY_PREVIEW);
			actions.remove(DELETE);
		}
		filterRestorePermissions(target, resource, role, actions);
		return Boolean.FALSE;
	}

	private Boolean filterForVersion(ObjectInstance target, Set<Action> actions) {
		actions.retainAll(ALLOWED_ACTIONS_FOR_VERSION);

		if (!target.isUploaded()) {
			actions.remove(DOWNLOAD);
		}

		// At the moment this is determined by the date when the version is created and the date when the revert
		// functionality is enabled. There are few issues with older versions, that is why the revert operation
		// should be filtered for them
		if (!instanceVersionService.isRevertOperationAllowed(target)) {
			actions.remove(REVERT_VERSION);
		}

		return Boolean.FALSE;
	}

	private Boolean filterForLocked(ObjectInstance target, Resource resource, Set<Action> actions) {
		actions.retainAll(ALLOWED_ACTIONS_WHEN_LOCKED);
		if (!target.isUploaded()) {
			actions.remove(DOWNLOAD);
		}

		if (!hasUnlockPermissions(target, resource)) {
			actions.remove(UNLOCK);
		}

		if (!resourceService.areEqual(target.getLockedBy(), resource)) {
			actions.remove(EDIT_DETAILS);
		}
		return Boolean.FALSE;
	}

}
